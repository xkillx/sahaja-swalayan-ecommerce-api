# Sahaja Backend (API) — Roadmap & Alignment

This roadmap outlines pragmatic, incremental backend work to support Sahaja Admin and Shop. It focuses on API contracts, data model evolution, integrations (Xendit, Biteship), security, and observability. Keep changes additive and versioned to avoid breaking clients.

## 1) API Contract, Versioning, and Consistency
- Keep v1 stable; introduce v1.1/v2 via additive fields or new endpoints. Ensure compatibility with Admin and Shop. 
- Standardize response envelopes and error format across controllers. 
- Enforce request/response DTOs with explicit JSON properties (snake_case for external contracts), MapStruct for entity mapping. 
- Expand OpenAPI (springdoc) with examples, tags, and schemas; publish at /api/swagger-ui.

Deliverables:
- API style guide (conventions on naming, pagination, sorting, filtering, errors). 
- Contract tests to guard regressions on critical endpoints (products, orders, payments, shipping).

## 2) Orders — Listing, Detail, and Actions (Admin alignment)
- GET /v1/orders (paginated) with filters: status, date range, user, total amount range, courier, payment status.
- GET /v1/orders/{id} with embedded: items, payment(s), shipping snapshot, tracking.
- POST /v1/orders/{id}/actions:
  - create-shipping (idempotent guard if already created)
  - cancel-shipping (requires reason; checks courier capability)
  - update-status (with reason and audit trail)
- Background job to re-sync shipping statuses (polling) if webhook missed.

Data/Infra:
- New table order_admin_actions for audit (adminId, action, payload, created_at).
- Indexes: orders(status, created_at), orders(user_id, created_at).

## 3) Catalog — Flat Product Model, Inventory, and Bulk Ops (Admin alignment)
- Flat product model only: one SKU per product with single price, weight, dimensions, and stock. No variants.
- Stock management endpoints: reserve/release on order create/cancel; low-stock thresholds and notifications.
- Bulk import/export: async job with CSV/Excel template; store job status.
- Image management API: multiple images per product, ordering, alt text. Static file serving remains under /api/uploads/products.

Migrations:
- Flyway scripts to ensure flat product table has required columns; product_images, stock thresholds.

## 4) Checkout & Shipping — Rates and Orders (Shop alignment)
- POST /v1/shipping/rates: ensure request accepts origin/destination (area_id, postal_code, coordinates), items (weight/qty); implement stubbed pricing when shipping.stub.enabled = true.
- Require destination_coordinate for instant/same-day; provide geocoding hook (service abstraction) to enrich address if lat/lng missing.
- POST /v1/shipping/orders: keep idempotency via reference_id (orderId) and defensive guards; enrich with origin config; normalize courier company/service names.
- Webhook handler hardening: signature validation (configurable open mode for dev), persistence of webhook logs for troubleshooting.

Enhancements:
- Cache couriers and rate quotes briefly (Caffeine) keyed by route+items to reduce external calls.

## 5) Payments — Robustness and Idempotency
- Xendit: ensure createPayment is idempotent per order (externalId reuse when pending). 
- Webhook verification: strictly validate callback token/signature; protect against replay. 
- Reconciliation job: nightly job to reconcile payment statuses via Xendit API.

## 6) Security, Auth, and RBAC (Admin alignment)
- Role matrix: ADMIN, OPERATOR, SUPPORT. Fine-grained method security via @PreAuthorize on modules (Orders, Catalog, Users, Finance). 
- Token management: prefer HttpOnly cookie + CSRF protection for state-changing endpoints (configurable for SPA).
- Audit logging: who did what and when on admin actions.

## 7) Observability & Ops
- Structured logging with correlation-id propagation (from request header X-Correlation-Id; generate if absent). 
- Central error handler that maps domain exceptions to stable error codes/messages. 
- Health endpoints and readiness checks for DB, Xendit, Biteship, SMTP.
- Feature flags (env/properties) for risky paths (real vs stub shipping, webhook open mode).

## 8) Performance & Caching
- Caching layers: 
  - Reference data (couriers, cancel reasons) via Caffeine. 
  - Rate quotes short TTL. 
- Pagination defaults and guarded limits. 
- N+1 query checks and JPA fetch plans for order detail endpoints.

## 9) Data Model & Migrations
- Flyway migrations for all schema changes; include rollback notes and backfill scripts. 
- Soft-delete vs hard-delete policies; add deleted_at where needed.

## 10) Documentation & Testing
- Expand OpenAPI with examples for all new endpoints. 
- Postman collections (or Hoppscotch) checked into /docs for QA.
- Integration tests:
  - Orders CRUD + actions (with auth). 
  - Payments flow (create → webhook). 
  - Shipping: rates + create order (stub and real). 
  - Catalog CRUD + search + image upload.

---

Alignment with Admin Roadmap:
- Orders list/detail/actions → Sections 2 and 6 (RBAC), 7 (observability), 10 (tests).
- Webhooks dashboard → Section 7 (webhook log persistence, correlation IDs).
- Bulk product ops & QoL → Section 3.

Alignment with Shop Roadmap:
- Shipping rates at checkout and coordinate handling → Section 4. 
- Payment flows and retry handling → Section 5. 
- SEO/Images support (multiple images, alt text) → Section 3 and 10.

Priorities to implement first (Milestone M1):
1) Orders listing/detail + actions API with audit table. 
2) Shipping rates endpoint hardening + coordinate requirements for instant; stub pricing complete. 
3) Webhook handler logs + signature verification toggle; correlation IDs across BE.

Milestone M2:
4) RBAC enforcement across admin endpoints. 
5) Product variants + low-stock alerting. 
6) Payment idempotency and reconciliation job.

Milestone M3:
7) Bulk import/export jobs. 
8) Image management v2. 
9) OpenAPI examples and Postman collections.


## 11) Notifications & Messaging (Push + Web Push)
- Goals
  - Deliver order status updates and promos via push to mobile devices; optional web push for desktop users.
- Data Model
  - device_tokens table: id, user_id, token, platform (ios/android/web), app_version, last_seen_at, created_at, revoked_at.
- Endpoints (proposed)
  - POST /v1/notifications/device-tokens: register token (auth required).
  - DELETE /v1/notifications/device-tokens/{token}: unregister.
  - POST /v1/notifications/send: admin-only; payload { user_id(s) | topic | all, title, body, data }.
- Providers
  - Firebase Cloud Messaging (FCM) server key; optional OneSignal as abstraction later.
- Implementation notes
  - Use official FCM HTTP v1 (OAuth2 service account) or legacy server key (dev only).
  - Batch sends with retries; exponential backoff; prune invalid tokens on error codes.

## 12) Auth Federation (Google) & Phone/WhatsApp OTP
- Google Login
  - Spring Security: spring-boot-starter-oauth2-client to validate Google ID tokens; map to internal User; mint Sahaja JWT.
  - FE (Shop/Mobile): use Google Identity or native SDK, exchange token with BE.
- WhatsApp OTP
  - Use WhatsApp Cloud API (Meta Graph API) to send OTP. Endpoints:
    - POST /v1/auth/otp/request { phone } → send OTP via WhatsApp; rate-limited.
    - POST /v1/auth/otp/verify { phone, code } → mint JWT if valid.
  - Fallback: SMS OTP via Twilio/Vonage.
- Security
  - Brute-force protection, short TTL, device fingerprint/captcha after N attempts.
- Libraries (recommended)
  - spring-boot-starter-oauth2-client, jjwt (already used), an HTTP client (WebClient/Feign) for WhatsApp Cloud API.

## 13) Developer Experience for Notifications/Auth
- Configuration
  - application.yaml keys: fcm.project-id, fcm.client-email, fcm.private-key (env), whatsapp.token, google.client-id(s).
- Observability
  - Store send logs (success/error code) with correlation-id.
- Tests
  - Integration tests for token register/unregister; mock FCM/WhatsApp in dev profile.
