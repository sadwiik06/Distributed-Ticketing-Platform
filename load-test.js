import http from 'k6/http';
import { check, sleep } from 'k6';

// Treat 200 (Success) and 409 (Seat Already Locked) as valid responses
http.setResponseCallback(http.expectedStatuses(200, 409));

export const options = {
    stages: [
        { duration: '10s', target: 50 },   // Ramp up to 50 virtual users
        { duration: '30s', target: 200 },  // Spike to 200 virtual users (high concurrency)
        { duration: '10s', target: 0 },    // Ramp down to 0 users
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],   // Error rate under 1%
        http_req_duration: ['p(95)<100'], // 95% of requests complete under 100ms
    },
};

// 1. FETCH JWT TOKEN FROM KEYCLOAK
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

// 2. MAIN VIRTUAL USER LOAD LOOP
export default function (data) {
    const eventId = '6a93f2ec90262125bac74cf0';
    const seatCode = `A-${Math.floor(Math.random() * 50) + 1}`; // Random seat A-1 to A-50
    const userId = `USER_${__VU}`;                              // Dynamic VU ID

    // Construct URL with @RequestParam values
    const url = `http://localhost:8081/api/lock?eventId=${eventId}&seatCode=${seatCode}&userId=${userId}`;

    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
        },
    };

    // POST request with empty body since params are in the URL
    const res = http.post(url, null, params);

    check(res, {
        'status is 200 or 409': (r) => r.status === 200 || r.status === 409,
        'response time < 50ms': (r) => r.timings.duration < 50,
    });

    sleep(0.1);
}