<p align="center">
  <img src="https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/Kafka-Event_Driven-blue?style=for-the-badge&logo=apache-kafka&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-Distributed_Lock-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/Keycloak-OAuth2_IAM-4D4D4D?style=for-the-badge&logo=keycloak&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</p>

# Distributed Ticketing Platform

> A production-grade, event-driven microservices platform for high-concurrency ticket booking — engineered to **eliminate double-booking** through Redis distributed locking and Kafka-based choreography.

---

## Architecture Diagram

```
                         ┌──────────────────────────────────────┐
                         │         OBSERVABILITY STACK          │
                         │       Prometheus  · Grafana          │
                         └──────────────┬───────────────────────┘
                                        │
  ┌──────────┐     ┌────────────────────┼────────────────────────────┐
  │  Client   │────▶│          API GATEWAY  :8083                   │
  └──────────┘     │   Spring Cloud Gateway + Resilience4j + JWT    │
                   └──────┬──────────────┬───────────────┬──────────┘
                          │              │               │
                  ┌───────▼──────┐ ┌─────▼────────┐ ┌───▼──────────┐
                  │    EVENT     │ │  TICKETLOCK  │ │    ORDER     │
                  │   SERVICE   │ │   SERVICE    │ │   SERVICE    │
                  │    :8080    │ │    :8081     │ │    :8082     │
                  │  MongoDB    │ │ Redis+MySQL  │ │    MySQL     │
                  └──────┬──────┘ └──────┬───────┘ └──────┬───────┘
                         │               │                │
                  ┌──────▼───────────────▼────────────────▼───────┐
                  │              APACHE KAFKA  :9092              │
                  └──────────────────────────────────────────────┘
                  ┌──────────────────────────────────────────────┐
                  │         EUREKA DISCOVERY SERVICE :8761       │
                  └──────────────────────────────────────────────┘
                  ┌──────────────────────────────────────────────┐
                  │           KEYCLOAK IAM  :8181                │
                  └──────────────────────────────────────────────┘
```

---

## Key Features

- **Distributed Locking** — Redis `setIfAbsent()` with TTL ensures zero double-booking
- **Event-Driven** — 4 Kafka topics for async communication between services
- **Circuit Breakers** — Resilience4j on gateway routes with fallback URIs
- **OAuth2/JWT** — Keycloak for authentication & authorization
- **Service Discovery** — Eureka for dynamic service registration
- **Distributed Tracing** — Zipkin with correlated traceId/spanId in logs
- **Metrics & Dashboards** — Prometheus + Grafana scraping all services
- **Containerized** — Full Docker Compose with 13+ containers

---

## Performance Benchmarks (k6 Load Test)

> 200 concurrent users · 50 seats · 50-second ramp profile

| Metric | Value | Threshold | Status |
|--------|-------|-----------|--------|
| **Total HTTP Requests** | **40,000+** | — | ✅ |
| **Message Loss** | **0%** | — | ✅ |
| **System Error Rate** | **< 1%** | `< 1%` | ✅ Pass |
| **P95 Response Time** | **< 200ms** | `< 200ms` | ✅ Pass |
| **Lock Response Time** | **< 150ms** | `< 150ms` | ✅ Pass |
| **Peak Concurrent Users** | **200 VUs** | — | ✅ |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.x, Spring Cloud |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Service Discovery | Netflix Eureka |
| Message Broker | Apache Kafka (Confluent 7.5) |
| Distributed Lock | Redis |
| Databases | MySQL 8.0, MongoDB |
| Auth/IAM | Keycloak 24 (OAuth2/OIDC) |
| Resilience | Resilience4j |
| Tracing | Zipkin + Micrometer |
| Metrics | Prometheus + Grafana |
| Load Testing | k6 |
| Containerization | Docker + Docker Compose |

---

## Getting Started

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### 1. Start Infrastructure

```bash
docker-compose up -d
```

### 2. Build & Run Services

```bash
mvn clean package -DskipTests

cd discovery-service && mvn spring-boot:run
cd event-service && mvn spring-boot:run
cd ticketlock-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### 3. Configure Keycloak
1. Navigate to `http://localhost:8181`
2. Create realm: `ticketing-realm`
3. Create client: `spring-cloud-client`
4. Create a test user

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
