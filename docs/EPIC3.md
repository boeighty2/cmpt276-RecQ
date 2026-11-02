# Epic 3 — Administrative Facility Management

**Owner:** Jiawen Wang  
**Project:** SFU RecQ · CMPT 276 · Iteration 1

## Overview

Epic 3 implements the staff-facing **Venue → Sport → Courts** workflow for managing drop-in sports availability. Sport pages have **Courts** and **Waiting Queue** tabs; the queue tab is an integration placeholder, not a working queue.

## Implemented Features

- **Venues:** browse venues and their sports, including each sport's OPEN/CLOSED status. Rename or delete a venue in Manage mode. A new venue is created by adding its first sport; there is no separate empty-venue record.
- **Sports:** add a sport with 1–30 courts, rename/delete it, or manually Open/Close it. Newly created sports start **CLOSED**. Opening and closing are staff-controlled rather than schedule-driven.
- **Courts:** view status counts, update individual **AVAILABLE / OCCUPIED / CLOSED** states, and add/delete courts in Manage mode. A court cannot be marked OCCUPIED while its sport is closed.
- **Safeguards:** an OCCUPIED court cannot be deleted, and a sport must retain at least one court. Venue deletion requires every sport to be closed and no courts to be OCCUPIED; it removes the venue's sports and courts. Sport deletion blocks OCCUPIED courts but currently does **not** require the sport to be closed. Destructive actions have confirmation prompts and server-side validation.
- **Persistence and runs:** courts and sport-opening history are stored using Spring Data JPA. A **CLOSED → OPEN** transition creates a `SportRun` with an opening timestamp; **OPEN → CLOSED** ends that run. Repeated Open requests do not create duplicate runs. Historical `SportRun` records are retained when a sport or venue is deleted. Previously seeded OPEN demo sports may not have a run record until closed and reopened.
- **Staff UI:** breadcrumb/back navigation, shared account initials menu, and session logout. Current staff access uses a temporary session-role check until Epic 1 authentication is integrated.

## Main Components

| Component | Responsibility |
|---|---|
| `admin/AdminCourtController.java` | Staff pages and venue, sport, and court actions |
| `admin/StaffRoleGuard.java` | Temporary authorization check using the HTTP session `role` |
| `admin/StaffAccountController.java`, `admin/StaffAccountModel.java` | Logout and account initials |
| `admin/NewSessionForm.java` | Add Sport form (historical class name) |
| `facility/CourtService.java` | Business rules, validations, and state changes |
| `facility/Court.java`, `facility/CourtStatus.java`, `facility/CourtRepository.java` | Court entity, status enum, and persistence |
| `facility/SportRun.java`, `facility/SportRunRepository.java` | Records for individual sport opening/closing periods |
| `facility/CourtApiController.java` | Read-only availability API |
| `dev/DevStaffController.java` | **Development-only** staff login shortcut |
| `templates/admin/`, `static/css/epic3*.css` | Staff pages and styling |

Java paths are relative to `src/main/java/com/cmpt276g12/RecQ/`. Templates and CSS are under `src/main/resources/`.

## Routes and Data Contracts

- `GET /admin/courts` — venue dashboard (**STAFF/ADMIN session role required**).
- `GET /admin/courts/venue?gym=...` — a venue's sports.
- `GET /admin/courts/sport?gym=...&sport=...` — a sport's Courts / Waiting Queue tabs.
- `GET /admin/courts/new` and `POST /admin/courts/new` — add a sport (and its initial courts).
- **Venue actions:** `POST /admin/courts/venue/rename` (`oldName`, `newName`); `POST /admin/courts/venue/delete` (`venueName`).
- **Sport actions:** `POST /admin/courts/sport/rename` (`gym`, `sport`, `newName`); `POST /admin/courts/sport/state` (`gym`, `sport`, `open`); `POST /admin/courts/sport/delete` (`gym`, `sport`).
- **Court actions:** `POST /admin/courts/sport/court/{id}/status` (`status`); `POST /admin/courts/sport/court/{id}/add`; `POST /admin/courts/sport/court/{id}/delete`.
- **Logout:** `POST /admin/logout` invalidates the current HTTP session.
- `GET /api/courts` — public, read-only JSON **array of courts** with `id`, `gymName`, `sportName`, `sessionName`, `courtName`, `status`, `sportOpen`, and `updatedAt`. If a sport is closed, the API reports its courts as `CLOSED` without changing their stored individual statuses. Clients can use `sportOpen` to distinguish a closed sport from a closed individual court. There are **no queue fields or check-in operations** in this API.
- `CourtService.activeRunId(gym, sport)` — internal Java method for finding the current open `SportRun` ID; **not currently a REST endpoint**. Queue integration must agree on how this ID is shared.

The current UI groups courts by venue and sport. `sessionName` is retained for compatibility with older demo data; newly created manual sports use `MANUAL` rather than a scheduled date/time. Older `/admin/courts/session` links redirect to the current sport page; dated-session editing endpoints and the retired template have been removed.

## Run and Test

Requires **Java 21**. From the repository root:

```bash
./mvnw test
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Local staff demo: `http://localhost:8080/dev/staff-login` (redirects to `/admin/courts`). The development shortcut signs in a generic **Jane Wilson** staff account (`JW` initials); the actual account menu uses the authenticated session name when Epic 1 is integrated. The `dev` profile uses a file-backed H2 database and can seed fictional courts in an empty database. **Never use the `dev` profile in a shared deployment.** Without environment overrides, the default profile uses an **in-memory H2 database**; configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` for a shared persistent database. Do not commit local `recq-dev.mv.db` files.

## Team Integration

- **Epic 1 — Authentication:** replace the development login and coordinate the authorization contract. `StaffRoleGuard` currently accepts session `role` values including `STAFF` and `ADMIN`; these must be supplied by a trusted authentication flow. Populate account initials from the authenticated user. The current logout only invalidates the HTTP session. Production authentication must include CSRF protection for staff POST forms.
- **Epic 2 — Availability:** consume `GET /api/courts`; refresh or poll as needed, since the API does not push real-time updates. A closed sport reports `status: CLOSED` and `sportOpen: false` for its courts.
- **Epic 4 — FIFO Queue / on-site check-in:** implement queue storage, ordering, staff check-in, and the Waiting Queue tab's live data. Associate entries with the active `SportRun` ID so each new opening has a separate queue. Add queue-aware checks before deleting venues, sports, or courts; existing deletion checks do not inspect queued participants. Stable venue/sport identifiers and database uniqueness/concurrency rules should be agreed on before simultaneous staff updates are supported.
- **Epic 5 — Turn handling:** connect court release, sport closure, turn progression, notifications, timeout behavior, and end-of-run queue cleanup. These actions are **not yet triggered** by Epic 3 state updates. Closing a sport currently does not itself clear a queue or prevent closure while a court is OCCUPIED.

**Integration status:** venue/sport/court administration and availability are implemented locally; production login, live queue operations, and queue-dependent safeguards still require team integration.
