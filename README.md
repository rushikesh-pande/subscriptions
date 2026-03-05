# Subscriptions Microservice

## Enhancement 15 — Subscription & Recurring Orders

Production-ready Spring Boot 3.2.2 microservice for managing recurring deliveries.

### Features
- ✅ Create subscriptions (weekly/biweekly/monthly/quarterly)
- ✅ Pause & resume subscriptions
- ✅ Skip delivery option (3 skips included)
- ✅ Auto-payment via stored payment method token
- ✅ 10% subscription discount applied automatically
- ✅ Scheduled renewal processing (daily at 08:00)
- ✅ Kafka event publishing

### Kafka Topics
| Topic | Description |
|-------|-------------|
| `subscription.created` | New subscription created |
| `subscription.renewed` | Subscription auto-renewed |
| `subscription.paused` | Subscription paused by customer |

### API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/subscriptions` | Create subscription |
| GET | `/api/subscriptions/customer/{customerId}` | List customer subscriptions |
| GET | `/api/subscriptions/{id}` | Get subscription |
| PUT | `/api/subscriptions/{id}/pause` | Pause |
| PUT | `/api/subscriptions/{id}/resume` | Resume |
| PUT | `/api/subscriptions/{id}/skip` | Skip next delivery |
| DELETE | `/api/subscriptions/{id}` | Cancel |

### Tech Stack
- Java 17, Spring Boot 3.2.2, Maven
- Spring Data JPA + H2
- Spring Kafka
- Lombok, Bean Validation, Spring Scheduling

### Run
```bash
mvn spring-boot:run
```
