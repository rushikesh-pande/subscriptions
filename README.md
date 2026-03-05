# Subscriptions & Recurring Orders Microservice

## Overview
Spring Boot microservice for managing product subscriptions and recurring deliveries.

## Features
- Subscribe to regular product deliveries
- Flexible frequency: Weekly / Bi-weekly / Monthly / Quarterly
- Pause & Resume subscriptions
- Auto-payment integration (via `paymentMethodId`)
- Subscription discounts (configurable, default 10%)
- Skip delivery option (up to 3 times)
- Scheduled renewal job (daily @ 8 AM)

## Kafka Topics
| Topic                   | Direction | Description                      |
|-------------------------|-----------|----------------------------------|
| `subscription.created`  | PRODUCE   | New subscription created         |
| `subscription.renewed`  | PRODUCE   | Subscription renewal processed   |
| `subscription.paused`   | PRODUCE   | Subscription paused by customer  |

## API Endpoints
| Method | Path                                            | Description                     |
|--------|--------------------------------------------------|---------------------------------|
| POST   | `/api/subscriptions`                            | Create subscription             |
| GET    | `/api/subscriptions/customer/{customerId}`      | Get customer subscriptions      |
| GET    | `/api/subscriptions/customer/{customerId}/active`| Get active subscriptions       |
| GET    | `/api/subscriptions/{id}`                       | Get subscription by ID          |
| PUT    | `/api/subscriptions/{id}/pause`                 | Pause subscription              |
| PUT    | `/api/subscriptions/{id}/resume`                | Resume subscription             |
| DELETE | `/api/subscriptions/{id}/cancel`                | Cancel subscription             |
| POST   | `/api/subscriptions/{id}/skip`                  | Skip next delivery              |
| PUT    | `/api/subscriptions/{id}/frequency`             | Update delivery frequency       |
| POST   | `/api/subscriptions/{id}/renew`                 | Manual renewal trigger          |

## Tech Stack
- Java 17
- Spring Boot 3.2.2
- Spring Data JPA / H2 (dev)
- Spring Kafka
- Spring Scheduling
- Lombok
- Maven

## Running Locally
```bash
mvn spring-boot:run
```
Server starts on port **8088**.

## Configuration
```properties
subscription.discount.percentage=10
subscription.max-skip-count=3
```
