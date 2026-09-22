import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom Business Metrics
export const systemErrors = new Rate('system_errors'); 
export const successfulLocks = new Rate('successful_locks'); 
export const conflictLocks = new Rate('conflict_locks'); 

http.setResponseCallback(http.expectedStatuses(200, 201, 409));

export const options = {
    stages: [
        { duration: '10s', target: 50 },   
        { duration: '30s', target: 200 },  
        { duration: '10s', target: 0 },    
    ],
    thresholds: {
        system_errors: ['rate<0.01'],     
        http_req_duration: ['p(95)<200'], 
    },
};


export function setup() {
    const tokenUrl = 'http://localhost:8181/realms/ticketing-realm/protocol/openid-connect/token';

    const payload = {
        grant_type: 'password',
        client_id: 'spring-cloud-client',
        client_secret: 'xzqSFai1RWi2Yyf89v7NpUoabKm55uGz',
        username: 'user1',
        password: 'password',
    };

    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };

    const response = http.post(tokenUrl, payload, params);

    if (response.status !== 200) {
        console.error(`Keycloak Token Fetch Failed [${response.status}]: ${response.body}`);
    }

    const jsonBody = response.json();
    return { token: jsonBody.access_token };
}

export default function (data) {
    const eventId = __ENV.EVENT_ID || '6ab26d5bb7522507df7ade25';
    const seatCode = `A-${Math.floor(Math.random() * 50) + 1}`;

    const authHeaders = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
            'Content-Type': 'application/json',
        },
    };

    const lockUrl = `http://localhost:8083/api/lock?eventId=${eventId}&seatCode=${seatCode}`;
    const lockRes = http.post(lockUrl, null, authHeaders);

    const isLockWon = lockRes.status === 200;
    const isConflict = lockRes.status === 409;
    const isError = !isLockWon && !isConflict;

    successfulLocks.add(isLockWon);
    conflictLocks.add(isConflict);
    systemErrors.add(isError);

    check(lockRes, {
        'lock status is 200 (Won) or 409 (Prevented Double-Booking)': () => isLockWon || isConflict,
        'lock response time < 150ms': (r) => r.timings.duration < 150,
    });

    if (isLockWon) {
        const orderPayload = JSON.stringify({
            eventId: eventId,
            quantity: 1,
            pricePerTicket: 150.00,
            seatCode: seatCode,
        });

        const orderRes = http.post('http://localhost:8083/api/orders', orderPayload, authHeaders);

        if (orderRes.status === 201) {
            const orderId = orderRes.body.trim();

            const checkoutUrl = `http://localhost:8083/api/orders/checkout?orderId=${orderId}`;
            const checkoutRes = http.post(checkoutUrl, null, authHeaders);

            const isCheckoutSuccess = checkoutRes.status === 200;
            systemErrors.add(!isCheckoutSuccess);

            check(checkoutRes, {
                'checkout status is 200 (Confirmed)': () => isCheckoutSuccess,
                'checkout response time < 200ms': (r) => r.timings.duration < 200,
            });
        }
    }

    sleep(0.1);
}