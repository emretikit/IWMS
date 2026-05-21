# DEL2 Backend Traceability Matrix

## UC-AUTH_001 Authenticate User
- Implemented: `POST /api/auth/register`, `POST /api/auth/login`
- Added: password reset flow
  - `POST /api/auth/password-reset/request`
  - `POST /api/auth/password-reset/confirm`
- Added: student email domain policy and failed-login audit logging

## UC-COMP-002 Manage Company Registration
- Implemented: company register/approve/reject/pending list in `CompanyController`
- Added: persistent notification and audit persistence

## UC-ADM-003 Manage Academic Periods and Rules
- Added full period CRUD and rule config APIs in `AcademicPeriodController`
  - `POST /api/periods`
  - `PUT /api/periods/{id}`
  - `GET /api/periods`
  - `POST /api/periods/rules`
  - `GET /api/periods/rules`

## UC-APP-004 Submit and Approve Internship Application
- Existing student apply + supervisor token approve/reject flow retained
- Added: approved-company enforcement, null supervisor handling, token attempt enforcement

## UC-STU-005 Generate and Submit Internship Report
- Existing report upload flow retained
- Added report draft API:
  - `POST /api/internships/{internshipId}/report/draft`
- Added revision-compatible report submission condition

## UC-COMP-006 Evaluate Internship and Apply Signature
- Added company evaluation API:
  - `POST /api/company-evaluations/{internshipId}`
- Includes mandatory evaluation documents and signature hash capture

## UC-COO-007 Monitor Compliance and Coordinate Internships
- Added coordinator APIs:
  - `GET /api/coordinator/pending`
  - `PUT /api/coordinator/internships/{internshipId}/decision`
- Supports final decision and revision-required report unlock behavior

## UC-COO-008 Manage System Operation and Security
- Added admin operations APIs:
  - `PUT /api/admin/users/{userId}/status`
  - `GET /api/admin/audit-logs`
  - `POST /api/admin/announcements`
  - `POST /api/admin/faqs`
- Includes last-active-admin protection

## UC-SUP-009 Interact with Support System
- Added support APIs:
  - `GET /api/support/faqs`
  - `GET /api/support/autocomplete?query=...`
  - `GET /api/support/chatbot?question=...` (fallback strategy)

## UC-HIST-010 Review Internship History and Submit Feedback
- Added history/feedback APIs:
  - `GET /api/history/internships`
  - `POST /api/history/feedback`

## Hardening and Fixes
- `application.properties` switched to env-based secrets/credentials.
- Added `app.allow-test-endpoints` guard for test endpoint.
- Added security exception mapping in global handler.
- `CustomUserDetails` now honors user `isActive`.

## Test Additions
- `AuthServiceTest`
- `CompanyServiceImplTest`

