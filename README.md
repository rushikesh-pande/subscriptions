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

## 🔒 Security Enhancements

This service implements all 7 security enhancements:

| # | Enhancement | Implementation |
|---|-------------|----------------|
| 1 | **OAuth 2.0 / JWT** | `SecurityConfig.java` — stateless JWT auth, Bearer token validation |
| 2 | **API Rate Limiting** | `RateLimitingFilter.java` — 100 req/min per IP using Bucket4j |
| 3 | **Input Validation** | `InputSanitizer.java` — SQL injection, XSS, command injection prevention |
| 4 | **Data Encryption** | `EncryptionService.java` — AES-256-GCM for sensitive data at rest |
| 5 | **PCI DSS** | `PciDssAuditAspect.java` — Full audit trail for payment operations |
| 6 | **GDPR Compliance** | `GdprDataService.java` — Right to erasure, consent management, data export |
| 7 | **Audit Logging** | `AuditLogService.java` — All transactions logged with user, IP, timestamp |

### Security Endpoints
- `GET /api/v1/audit/recent?limit=100` — Recent audit events (ADMIN only)
- `GET /api/v1/audit/user/{userId}` — User's audit trail (ADMIN or self)
- `GET /api/v1/audit/violations` — Security violations (ADMIN only)

### JWT Authentication
```bash
# Include Bearer token in all requests:
curl -H "Authorization: Bearer <JWT_TOKEN>" http://localhost:8088/api/v1/...
```

### Security Headers Added
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `X-RateLimit-Remaining: <n>` (on every response)
