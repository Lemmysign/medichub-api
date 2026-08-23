# MedicHub Academy — Pre-Launch Checklist

Status of the backend: MVP feature-complete, unit-tested, end-to-end smoke-tested locally.
This file tracks everything that must be done (or verified) before going to production.
Nothing here blocks the current **test phase** — these are go-live gates.

---

## 1. Secrets & configuration (production)
Set these as environment variables on Railway (never commit real values):

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (Railway Postgres)
- [ ] `JWT_SECRET` — a fresh, random value **≥ 32 bytes** (app fails fast if too short)
- [ ] `ADMIN_EMAIL` — Dr Samuel's real email (no default; without it nobody is admin)
- [ ] `FRONTEND_URL` — the real HTTPS SPA origin (drives CORS + Paystack callback)
- [ ] `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- [ ] `BUNNY_LIBRARY_ID`, `BUNNY_API_KEY`, `BUNNY_CDN_HOSTNAME`, `BUNNY_TOKEN_AUTH_KEY`
- [ ] `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_BUCKET`, `R2_PUBLIC_BASE_URL`
- [ ] `PAYSTACK_SECRET_KEY`, `PAYSTACK_PUBLIC_KEY`, `PAYSTACK_CALLBACK_URL`
- [ ] `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
- [ ] `SUBSCRIPTION_PLAN_PRICE_KOBO` (e.g. `1000000` = ₦10,000) — or set the price via the admin endpoint after boot

## 2. Third-party dashboards
- [ ] **Register the admin account first** (admin is granted by matching `ADMIN_EMAIL` — claim it before anyone else can).
- [ ] **Paystack:** set webhook URL to `https://api.medichubacademy.com/api/webhooks/paystack`; go live keys.
- [ ] **Bunny:** enable token authentication, disable direct MP4 download, lock allowed referrers to the domain.
- [ ] **Cloudflare R2:** bucket + S3 API token created; public base URL set for thumbnails.
- [ ] **Google OAuth:** redirect URI `https://api.medichubacademy.com/login/oauth2/code/google`.
- [ ] **Email:** verify sending domain (SPF/DKIM).

## 3. Database & migrations
- [ ] Switch off `ddl-auto` (`spring.jpa.hibernate.ddl-auto=validate`).
- [ ] Generate a Flyway **baseline migration** from the current schema; set `spring.flyway.enabled=true`.
- [ ] Confirm the subscription plan row exists (seed via env or `PUT /api/admin/subscription-plan`).
- [ ] Backfill denormalized names on existing rows if any pre-date the name columns (see chat notes).

## 4. Security hardening (remaining Low items)
- [ ] **Password policy** — currently min 8 chars; consider complexity / breached-password check.
- [ ] **PII in logs** — `EmailServiceImpl` logs recipient email at INFO; reduce if needed.
- [ ] **Actuator** — confirm only `health`/`info` exposed in prod.
- [ ] **Devtools** — confirm `spring-boot-devtools` is excluded from the prod image (it's `optional` scope; verify the built jar).
- [ ] **Rate limit** — tune `AUTH_RATE_LIMIT_PER_MINUTE` for real traffic.
- [ ] **Google OAuth2** — enable `http.oauth2Login(...)` in `SecurityConfig` (currently a wired TODO stub) once Google creds exist; it funnels into the same JWT issuance.

## 5. Scaling note (only if running more than one instance)
Two controls are **per-instance in-memory** and correct for a single Railway instance:
- `AuthRateLimitFilter` (auth rate limiting)
- `DisabledUserRegistry` (instant disabled-account rejection)

If you scale to multiple instances, move both to a shared store (e.g. Redis) so limits and
disabled-account state are consistent across instances.

## 6. Deploy
- [ ] Multi-stage `Dockerfile` → Railway service (auto-deploy on push).
- [ ] Custom domains: `api.medichubacademy.com` → backend, `medichubacademy.com` → frontend (HTTPS auto).
- [ ] Smoke-test the deployed API (auth, catalog, subscribe → Paystack → webhook → playback).

---

### Already done (security fixes applied during the test phase)
- Secrets removed from committed config → `application-local.properties` (gitignored) / env vars.
- No default admin email or JWT secret (fail-fast).
- Auth rate limiting on `/api/auth/**`.
- Disabled accounts rejected immediately (registry seeded from DB on startup).
- Upload size limits; webhook HMAC-SHA512 verified before processing; parameterized queries.
