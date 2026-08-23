# MedicHub Academy — Project Brief & Engineering Guide

> This file is the single source of truth for the MedicHub Academy build. It documents the
> product, architecture, data model, integrations, and conventions. Keep it in the repo root.
> Claude Code reads this automatically — treat it as the standing context for all work here.

---

## 1. What we're building

**MedicHub Academy** is a **subscription-based medical e-learning platform** — think Udemy, but
subscription-based instead of per-course. A medical consultant runs the platform to train medical
professionals and students preparing for exams. Anyone who subscribes gets access to **all** courses
on the platform for as long as their subscription is active.

- **Owner / lead instructor:** Dr Samuel Eseile
- **Developer:** Omorogiuwa Inneh
- **Domain:** `medichubacademy.com` (web app), `api.medichubacademy.com` (backend API)
- **Delivery:** **Web first**, then a mobile app later that reuses the same API. Nothing about the
  backend changes when mobile arrives — the API is stateless (JWT).

### Three audiences, one platform
- **Student** — subscribes, watches course videos, takes mock tests, tracks progress, asks questions.
- **Instructor** — creates courses, uploads videos & materials, authors tests, answers questions.
  The platform supports **multiple instructors** (open instructor signup).
- **Admin (back office)** — the platform owner; monitors everything, manages accounts, sees revenue.

---

## 2. Tech stack

| Layer | Choice |
|---|---|
| Language | **Java 21** |
| Framework | **Spring Boot 4.1.1** (Spring Framework 7) |
| Build | **Maven** |
| Config format | **`application.properties`** |
| Database | **PostgreSQL** (Railway-managed in prod) |
| Auth | Spring Security + **JWT** (access + refresh) and **Google OAuth2** |
| ORM | Spring Data JPA / Hibernate |
| Mapping | **MapStruct** (entity ↔ DTO) |
| Boilerplate | **Lombok** |
| Migrations | **Flyway** (versioned schema) |
| Frontend | **React** SPA (built separately) |
| Video | **Bunny Stream** |
| File storage | **Cloudflare R2** (S3-compatible; accessed via the AWS S3 SDK for Java) |
| Payments | **Paystack** (subscriptions) |
| Email | **Resend or Brevo** (SMTP) |
| Hosting | **Railway** (Docker), frontend on **Cloudflare Pages / Vercel** |

### Maven dependencies
From Spring Initializr: Spring Web, Spring Data JPA, Spring Security, OAuth2 Client, Validation,
PostgreSQL Driver, Lombok, Java Mail Sender, Spring Boot Actuator, Flyway Migration (+ optional DevTools).

Added manually to `pom.xml` (not on Initializr):
- **JWT** — `io.jsonwebtoken:jjwt-api`, `jjwt-impl` (runtime), `jjwt-jackson` (runtime) — `0.12.6`
- **MapStruct** — `org.mapstruct:mapstruct` `1.6.3` + `mapstruct-processor` + `lombok-mapstruct-binding` `0.2.0` in the compiler annotation processors
- **Cloudflare R2** — `software.amazon.awssdk:s3` `2.28.x`

---

## 3. Architecture — layered monolith

One Spring Boot application, one React SPA, one PostgreSQL database. Deliberately a monolith: one
thing to build, deploy, and reason about at this scale. Split later only if ever needed.

### Package structure (base package `com.medichub`)
```
com.medichub
├── model            JPA entities
│   └── enums        Role, AuthProvider, SubscriptionStatus, PaymentStatus, QuestionType
├── repository       Spring Data JPA repositories
├── dto              request/response objects (never expose entities over the wire)
│   ├── request
│   └── response
├── mapper           MapStruct entity <-> DTO mappers
├── service          service interfaces
│   └── impl         service implementations (business logic + @Transactional)
├── controller       thin REST controllers (HTTP + validation only)
├── config           Spring config: security, CORS, beans, R2/Bunny/Paystack clients
├── exception        custom exceptions + @RestControllerAdvice global handler
└── security         JWT filter, token provider, OAuth2 success handler, user details
```

### Layer rules
- **Controllers are thin** — validate input (`@Valid`), delegate to a service, return a DTO. No business logic.
- **Services hold all business logic** and own transactions (`@Transactional`). Interface + `impl`.
- **Entities never leave the service layer.** Controllers speak DTOs only; MapStruct converts.
- **One global exception handler** (`@RestControllerAdvice`) turns every exception into a consistent
  JSON error shape: `{ "timestamp", "status", "error", "message", "path" }`.
- **Authorization is enforced at the API**, not the UI, via method/endpoint security.

### API path convention
```
/api/auth/**          public: register, login, refresh, google, forgot/reset password
/api/public/**        public catalog browsing (course list/preview)
/api/student/**       ROLE_STUDENT
/api/instructor/**    ROLE_INSTRUCTOR
/api/admin/**         ROLE_ADMIN
/api/webhooks/**      Paystack webhook (verified by signature, not JWT)
```

---

## 4. Data model

IDs are `Long` (auto-increment) for MVP simplicity. All entities carry `createdAt` / `updatedAt`
(via a `BaseEntity` `@MappedSuperclass` using `@CreationTimestamp` / `@UpdateTimestamp`).
Money is stored in **kobo** (`Long`, the smallest Naira unit) to avoid floating-point issues; display in Naira.

### Enums
- `Role` — `STUDENT`, `INSTRUCTOR`, `ADMIN`
- `AuthProvider` — `LOCAL`, `GOOGLE`
- `SubscriptionStatus` — `PENDING`, `ACTIVE`, `EXPIRED`, `CANCELLED`
- `PaymentStatus` — `PENDING`, `SUCCESS`, `FAILED`
- `QuestionType` — `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`

### Entities & relationships

**User** — one table for all roles.
- `fullName`, `email` (unique), `phone`, `passwordHash` (nullable for Google-only users),
  `role` (Role), `authProvider` (AuthProvider), `providerId` (nullable), `enabled` (boolean, default true).
- Admin is granted automatically when `email == app.admin.email`.

**Course**
- `title`, `description`, `thumbnailUrl` (R2), `published` (boolean).
- `instructor` → ManyToOne `User`.
- OneToMany: `topics`, `materials`, `tests`, `comments`, `enrollments`.

**Topic** — a course is an ordered list of topics; **one video per topic**.
- `course` → ManyToOne, `title`, `orderIndex` (int), `bunnyVideoId` (String, the Bunny GUID; null until uploaded),
  `videoDurationSeconds` (int, nullable).

**CourseMaterial** — downloadable PDFs/docs.
- `course` → ManyToOne, `topic` → ManyToOne (nullable), `fileName`, `contentType`, `r2Key`, `url`, `sizeBytes`.

**Enrollment** — created when a student opens/starts a course. Powers "courses enrolled" + progress.
- `student` → ManyToOne `User`, `course` → ManyToOne, `enrolledAt`, `lastAccessedAt`.
- Unique constraint on `(student, course)`.

**TopicProgress** — per student, per topic.
- `student` → ManyToOne `User`, `topic` → ManyToOne, `completed` (boolean), `secondsWatched` (int), `completedAt`.
- Unique constraint on `(student, topic)`.
- **Course progress % = completed topics ÷ total topics** for that course.

**Test** — a mock test attached to a course.
- `course` → ManyToOne, `title`, `passMarkPercent` (int).

**Question**
- `test` → ManyToOne, `text`, `type` (QuestionType, defaults to multiple choice), `orderIndex`.

**QuestionOption**
- `question` → ManyToOne, `text`, `correct` (boolean), `orderIndex`.

**TestAttempt** — kept forever so students can reference past attempts.
- `test` → ManyToOne, `student` → ManyToOne `User`, `scorePercent`, `passed` (boolean),
  `startedAt`, `submittedAt`.

**AttemptAnswer**
- `attempt` → ManyToOne, `question` → ManyToOne, `selectedOption` → ManyToOne `QuestionOption`, `correct` (boolean).

**CourseComment** — the Q&A / discussion. Self-referential for replies.
- `course` → ManyToOne, `topic` → ManyToOne (nullable), `author` → ManyToOne `User`,
  `parent` → ManyToOne `CourseComment` (nullable — a student's question is a root comment, the
  instructor's reply is a child), `text`.

**SubscriptionPlan**
- `name`, `priceKobo` (Long), `currency` (default `NGN`), `intervalDays`, `paystackPlanCode`, `active`.

**Subscription**
- `student` → ManyToOne `User`, `plan` → ManyToOne, `status` (SubscriptionStatus),
  `startDate`, `endDate`, `paystackSubscriptionCode`, `paystackCustomerCode`.
- A student has **platform access while status = ACTIVE and `endDate` is in the future**.

**Payment** — every charge, for revenue analytics.
- `student` → ManyToOne `User`, `subscription` → ManyToOne (nullable), `amountKobo` (Long),
  `currency`, `status` (PaymentStatus), `paystackReference`, `paidAt`.

**PlatformSettings** — singleton row (`id = 1`), admin-editable.
- `videoDownloadEnabled` (boolean, default **false**) — the admin toggle for whether students can download videos.

**PasswordResetToken** — `user`, `token`, `expiresAt`, `used`.

**RefreshToken** — `user`, `token`, `expiresAt`, `revoked`.

---

## 5. Feature specification

### 5.1 Authentication (all roles)
- **Sign up:** full name, email, phone, password — OR Google. Role chosen by signup context
  (student vs instructor). Passwords hashed with BCrypt.
- **Log in:** email + password, OR Google.
- **Admin:** no self-signup for admin. A user is treated as ADMIN when their email equals the
  preconfigured `app.admin.email`. The code validates the admin by email.
- **JWT:** short-lived access token (~30 min) + longer refresh token (stored in DB, revocable).
  Google login funnels into the same JWT so the rest of the app treats all users identically.
- **Account settings (all roles):** change name, change email, change password, forgot/reset password.

### 5.2 Instructor
- **Dashboard metrics:** total courses, total students enrolled across their courses, total tests
  created, total students who have taken their tests.
- **Create course:** title, description, thumbnail, configuration (Udemy-style). A course has
  **multiple topics**; each topic has **one video**.
- **Topics:** add/reorder topics, upload one video per topic (→ Bunny), upload course materials
  (PDF, DOC → R2).
- **Tests:** create a test per course; author questions with answers; **multiple choice by default**
  (type depends on how the question is set). Set the pass mark.
- **Edit content:** change a topic's video, edit test questions & answers, edit course details.
- **Q&A:** receive student questions on their courses and reply.

### 5.3 Student
- Same auth flow. **Subscribe to the platform → access to ALL courses** while subscribed.
- **Watch videos** (Bunny). **No download option** by default — the admin can enable/disable
  video download globally via `PlatformSettings.videoDownloadEnabled`.
- **Progress tracking:** a course with N topics has N videos. Clicking a topic card plays its video.
  A **client-side timer** watches playback; once a topic has been watched for **≥ 30 seconds**, the
  card is marked **complete (green)** and the course completion % increases.
  (Backend endpoint records `TopicProgress`; % = completed ÷ total topics.)
- **Tests:** take the mock test for any course; auto-graded; **past attempts are always
  referenceable** (score history).
- **Metrics:** total courses enrolled, total tests taken, and other smart metrics.
- **Q&A:** ask questions on a course/topic; the instructor replies; student sees the reply.
- **Account settings** as above.

### 5.4 Admin (back office)
- **Manage accounts:** view and **disable/enable** students and instructors.
- **Metrics dashboard:** total instructors, total students, total tests on the platform,
  **revenue** filterable by **day / week / month**, and subscription metrics (who is subscribed,
  total active subscriptions).
- **Platform settings:** toggle video download on/off.
- **Account settings** for the admin account.

---

## 6. Key mechanics

### Subscription gating
Content endpoints (video playback token, materials download) require an **ACTIVE, unexpired
subscription**. Browsing the catalog is open; consuming content is gated. Enforce centrally
(a service check / method security), not per-controller-ad-hoc.

### Video (Bunny Stream)
- Videos live in a **Bunny Stream Video Library** (not in our DB, not on Railway, not in R2).
  Each video has a **GUID**, stored on `Topic.bunnyVideoId`. Optionally use one Bunny **Collection**
  per course for organization.
- **Upload:** instructor uploads go **browser → Bunny directly** (backend creates the video object
  and returns an upload credential) so large files never pass through the Railway server.
- **Playback:** backend verifies the subscription, then mints a **short-lived Bunny signed
  (token-authenticated) URL** for the GUID; the player streams from Bunny's CDN.
- **Protection (configure in Bunny dashboard):** enable **token authentication**, **disable direct
  MP4 download**, **lock allowed referrers** to our domain. Screen recording is the only gap no web
  platform fully closes; DRM is a later option if needed.

### File storage (Cloudflare R2)
- Course materials (PDF/DOC) and thumbnails go to R2. R2 is **S3-compatible**, so we use the **AWS
  S3 SDK for Java** pointed at the R2 endpoint (`https://<account-id>.r2.cloudflarestorage.com`) with
  R2 keys. No AWS account involved; R2 free tier is 10 GB.

### Payments (Paystack)
- One subscription **Plan**. Student checks out via Paystack; on success, a **webhook**
  (`/api/webhooks/paystack`, verified by signature) activates/renews the `Subscription` and records a
  `Payment`. Money settles to **Dr Samuel's** Paystack/bank account.

### Progress timer
Client posts "topic X watched ≥ 30 s" → backend sets `TopicProgress.completed = true` →
recompute course %. Keep the 30-second threshold configurable if easy.

---

## 7. Configuration (environment variables)

All secrets come from env vars — never commit them. `application.properties` reads these with sane
local defaults. See `.env.example`.

```
# Database (Railway Postgres)
DB_URL, DB_USER, DB_PASSWORD

# Admin (preconfigured platform owner)
ADMIN_EMAIL

# Frontend origin (CORS + OAuth redirect)
FRONTEND_URL

# JWT
JWT_SECRET, JWT_ACCESS_TTL_MIN, JWT_REFRESH_TTL_DAYS

# Google OAuth2
GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET

# Email (Resend / Brevo SMTP)
MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD

# Bunny Stream
BUNNY_LIBRARY_ID, BUNNY_API_KEY, BUNNY_CDN_HOSTNAME, BUNNY_TOKEN_AUTH_KEY

# Cloudflare R2
R2_ACCOUNT_ID, R2_ACCESS_KEY, R2_SECRET_KEY, R2_BUCKET, R2_PUBLIC_BASE_URL

# Paystack
PAYSTACK_SECRET_KEY, PAYSTACK_PUBLIC_KEY, PAYSTACK_PLAN_CODE
```

**Ownership rule:** all paid third-party accounts (Railway, Bunny, R2, Paystack, domain, email) are
under **Dr Samuel / MedicHub Academy** — his email, card, and bank. The developer gets access to
configure and manage them, but does not own or pay for them.

---

## 8. Third-party service setup checklist

- **GitHub** — repo; wire to Railway + Cloudflare/Vercel for auto-deploy.
- **Railway** — create project, add PostgreSQL, connect GitHub repo (Dockerfile auto-detected), set
  env vars, add custom domain `api.medichubacademy.com`. Map Railway's `DATABASE_URL` to the JDBC
  `DB_URL` Spring expects.
- **Google OAuth** — Google Cloud Console → OAuth consent screen → create Web OAuth Client →
  redirect URIs: `https://api.medichubacademy.com/login/oauth2/code/google` (+ localhost for dev) →
  copy client id/secret.
- **Bunny Stream** — create a Stream video library → grab Library ID, API key, CDN hostname → enable
  token auth (copy token key), disable MP4 download, lock referrers to the domain.
- **Cloudflare R2** — create bucket `medichub-materials` → create S3 API token (access key/secret) →
  note account id.
- **Paystack** — business account → public + secret keys → create a subscription Plan (copy plan
  code) → set webhook URL `https://api.medichubacademy.com/api/webhooks/paystack`.
- **Email (Resend/Brevo)** — create account → verify sending domain (SPF/DKIM DNS) → SMTP creds.

---

## 9. Deployment

- **Backend:** Docker (multi-stage) → Railway service, always-on, auto-deploy on push to GitHub.
- **Database:** Railway Postgres, private networking, automatic backups.
- **Frontend:** React build → Cloudflare Pages / Vercel (free), auto-deploy on push. Needs only the
  API URL, Paystack public key, and Google client id.
- **Domains:** `medichubacademy.com` → frontend; `api.medichubacademy.com` → backend. HTTPS automatic.
- **Migrations:** Flyway manages the schema in all environments. (During early local dev, Hibernate
  `ddl-auto` may be used, but production is Flyway-versioned.)

---

## 10. Conventions & coding guidelines

- **Follow the layered structure** in §3. New feature = model → repository → dto → mapper → service
  (+impl) → controller, wired with security.
- **DTOs, not entities**, cross the controller boundary. Use MapStruct mappers.
- **Validation** on request DTOs with Jakarta Bean Validation (`@NotNull`, `@Email`, `@Size`, …).
- **Exceptions:** throw domain exceptions (e.g. `ResourceNotFoundException`, `AccessDeniedException`,
  `SubscriptionRequiredException`); the global `@RestControllerAdvice` formats responses. No raw 500s.
- **Security:** role-based endpoint protection; verify resource ownership (an instructor can only edit
  their own courses; a student only their own progress/attempts).
- **Money in kobo** (`Long`); convert at the edges for display.
- **Timestamps in UTC.**
- **No secrets in code or git.**
- **Tests:** unit-test services (business logic), slice-test controllers where valuable.

---

## 11. MVP scope

**In:** email/password + Google auth; one subscription plan (Paystack); multiple instructors;
course → topics (one video each) + materials; per-course multiple-choice tests with auto-grading and
attempt history; student progress tracking (30s → complete); course Q&A; student/instructor/admin
dashboards with the metrics in §5; admin account management + video-download toggle.

**Deferred (v2):** certificates; advanced analytics; multiple plans / trials / coupons; non-MCQ
question types; ratings/reviews; search & recommendations; the mobile app (separate later phase, same API).

---

## 12. Decisions log

- Monolith, layered, single React SPA with role-based routing (`/student`, `/instructor`, `/admin`).
- One `User` table + `Role` enum (not separate per-role tables).
- Admin identified by matching `app.admin.email` (no admin self-signup).
- Subscription unlocks all courses; `Enrollment` tracks engagement/progress per course.
- Video on Bunny (GUID on topic), materials on R2, text data in Postgres — three stores, never mixed.
- Browser-direct video upload to Bunny (keeps large files off the server).
- Paystack subscription + signed webhook drives access.
- Java 21, Spring Boot 4.1.1, Maven, `application.properties`.
- **Mock exams** (added post-MVP): standalone, timed, subscriber-only exams **not tied to a course**.
  Modelled by reusing `Test` with `course` nullable (a mock ⇔ `course == null`) plus `owner`,
  `published`, and `durationMinutes`. Authored by instructors **and** admins (instructors manage their
  own; admin manages all); students take them subscriber-gated with a **server-anchored timer** (start
  creates the attempt, submit is validated against startedAt + duration). Reuses the MCQ
  question/grading/attempt engine. MCQ-only (no free-text/essay).

---

## 13. Open items to confirm

- Exact subscription price and interval (for the Paystack plan).
- Whether instructors are auto-approved on signup or require admin approval (current assumption:
  auto-approved; admin can disable).
- Whether the admin back office is part of the same React SPA (current assumption) or a separate app.
