# Subscriptions Microservice — Enhancement 15

**Subscription & Recurring Orders** — Spring Boot 3.2.2, Java 17, Kafka

## Features
- Weekly / Biweekly / Monthly / Quarterly deliveries
- Pause & Resume subscriptions
- Skip delivery (3 skips included per subscription)
- Auto-payment via stored token
- 10% subscription discount applied automatically
- Daily scheduler processes due renewals (08:00)

## Kafka Topics
| Topic | Trigger |
|-------|---------|
| `subscription.created` | New subscription |
| `subscription.renewed` | Auto-renewal processed |
| `subscription.paused`  | Customer pauses |

## API
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/subscriptions` | Create subscription |
| GET | `/api/subscriptions/customer/{id}` | Customer subscriptions |
| GET | `/api/subscriptions/{id}` | Get subscription |
| PUT | `/api/subscriptions/{id}/pause` | Pause |
| PUT | `/api/subscriptions/{id}/resume` | Resume |
| PUT | `/api/subscriptions/{id}/skip` | Skip next delivery |
| DELETE | `/api/subscriptions/{id}` | Cancel |

## Run
```bash
mvn spring-boot:run
```
