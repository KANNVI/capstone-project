# Blood Donation & Emergency Donor Finder

Full-stack implementation of the project synopsis.

- **Backend:** Spring Boot 3.3 (Java 17), Spring Security + JWT, Spring Data JPA, H2 (in-memory; MySQL optional)
- **Frontend:** React 18 + Vite

## Run it

Prerequisites: JDK 17+, Maven 3.9+, Node 18+.

```bash
# 1) Backend (http://localhost:8080)
cd backend
mvn spring-boot:run

# 2) Frontend (http://localhost:5173), in a second terminal
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Vite proxies `/api` to the backend.

Demo admin: `admin@blood.com` / `admin123`. Other users: register from the sign-in screen.
Demo data (donors, blood banks, one request) is loaded on every start because H2 is in-memory.

## Features mapped to the synopsis

| Module | Implementation |
|---|---|
| Registration & authentication | `/api/auth/register`, `/login`; BCrypt passwords, JWT, roles USER / ADMIN |
| Donor management | `Donor` entity, availability toggle, `isEligible()` (verified, available, 18-65 yrs, 45 kg+, 90 days since last donation) |
| Emergency request | `BloodRequest` entity, urgency, required-by, fulfil/close |
| Search & matching | `MatchingService`: compatibility matrix, Haversine distance, nearest-first |
| Notifications | `RequestController.alert`: logs the alert. Plug in Twilio / Firebase / JavaMail here |
| Privacy | Phones masked in `DonorView` until a donor accepts the request |
| Blood bank directory | `BloodBank` with per-group stock, admin can adjust |
| Admin dashboard | Verify donors and requests, stats, CSV report |
| Awareness & camps | Static page (`Awareness.jsx`) |

## API

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/donors/search?group=&city=&radius=` | public |
| POST | `/api/donors` | signed in |
| GET | `/api/donors/mine` | signed in |
| PATCH | `/api/donors/{id}/availability` | owner |
| POST | `/api/requests` | signed in |
| GET | `/api/requests/mine` | signed in |
| GET | `/api/requests/{id}/matches` | requester or admin |
| POST | `/api/requests/{id}/alert/{donorId}`, `/alert-all`, `/accept/{donorId}`, `/status?value=` | requester or admin |
| GET | `/api/banks` | public |
| PUT | `/api/banks/{id}/stock/{group}?delta=` | admin |
| GET | `/api/admin/stats`, `/donors`, `/requests` | admin |
| POST | `/api/admin/donors/{id}/verify?value=`, `/requests/{id}/verify?value=` | admin |

## Use MySQL instead of H2

1. Uncomment the `mysql-connector-j` dependency in `backend/pom.xml`.
2. In `application.properties` set:
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/blooddb?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   spring.jpa.hibernate.ddl-auto=update
   ```

## Simplifications to improve before production

- "Donor accepts" is simulated by the requester; add a donor-side inbox so donors accept from their own account.
- Alerts are only logged; add SMS/email/push. OTP verification is not implemented.
- Distances use city centres, not GPS; add Leaflet or Google Maps with lat/lng on donors.
- Change `app.jwt.secret`; add rate limiting and HTTPS.
- Entities use public fields for brevity; switch to getters/setters or DTOs for a larger codebase.
- Not yet built: donation history and certificates (planned next).
