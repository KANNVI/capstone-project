# BloodConnect

Emergency blood requests matched with **compatible + available + nearby** donors.

Stack: Spring Boot 3 (Java 17+), Spring Security + JWT (role based), MySQL, React 18, Redux Toolkit, Vite.

```
bloodconnect/
├── backend/    Spring Boot REST API  (port 8080)
├── frontend/   React app             (port 5173)
└── docker-compose.yml   optional MySQL
```

## Run

### 1. Backend

Option A - with MySQL (default)

```bash
# MySQL running on localhost:3306 (or: docker compose up -d)
cd backend
# set DB_USERNAME / DB_PASSWORD if not root/root
mvn spring-boot:run
```

Option B - no MySQL, in-memory H2 (quickest for a demo)

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Tables are created automatically. On first start the app seeds:

| Role      | Email                   | Password      |
|-----------|-------------------------|---------------|
| Admin     | admin@bloodconnect.com  | admin123      |
| Requester | rahul@demo.com          | password123   |
| Donor     | amit@demo.com (O+, Kharar) | password123 |
| Donors    | simran, karan, neha, rohit, pooja, harpreet @demo.com | password123 |
| Donor (resting, donated 20 days ago) | vikas@demo.com | password123 |

Demo donors are around Kharar / Mohali / Chandigarh. Turn the seeding off with `bloodconnect.seed-demo=false`.

### 2. Frontend

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

API URL is in `frontend/.env` (`VITE_API_URL`, default `http://localhost:8080/api`).

## 2-minute demo script

1. Log in as **rahul@demo.com** (requester) -> Create blood request: `O+`, 1 unit, urgency Critical, hospital "Fortis Mohali", latitude `30.6900`, longitude `76.7280`.
2. Request page shows the donors alerted, nearest exact matches first. `vikas@demo.com` is **not** listed (donated recently).
3. In another browser/incognito: log in as **simran@demo.com** (donor) -> Accept request -> contact number is revealed.
4. Donor: "Start donation" -> "Mark donation completed". Request moves PENDING -> DONOR_ACCEPTED -> DONATION_IN_PROGRESS -> COMPLETED, donor goes on 90-day rest, donation appears in "My donations".
5. Log in as admin: stats, verify/block users, remove requests.

## How matching works (`MatchingService`)

1. Blood group compatibility table (`BloodCompatibility`) -> donor groups that can give to the patient.
2. Keep donors that are **available**, **active**, and **eligible** (last donation >= 90 days ago).
3. Distance to hospital by **Haversine formula** (`GeoUtils`); keep those inside the radius (25 km default).
4. Sort: exact blood group first, then nearest first.
5. Top 10 get a `donor_requests` row + in-app notification.
6. A donor's phone is revealed to the requester (and vice versa) only after accept.

All values are in `application.properties` (`bloodconnect.matching.*`).

## REST API

| Method | Path | Who | Purpose |
|---|---|---|---|
| POST | /api/auth/register | public | register donor / requester |
| POST | /api/auth/login | public | returns JWT + profile |
| GET | /api/users/me | any | current profile |
| PUT | /api/donors/availability | donor | `{available}` |
| PUT | /api/donors/profile | donor | phone, city, location, last donation date |
| GET | /api/donors/history | donor | donation history |
| GET | /api/donors/matching?bloodGroup=&lat=&lng=&radiusKm= | requester, admin | ad-hoc donor search |
| POST | /api/requests | requester | create request + auto matching |
| GET | /api/requests | requester | my requests |
| GET | /api/requests/{id} | owner, admin | details |
| GET | /api/requests/{id}/donors | owner, admin | donors alerted + their status |
| POST | /api/requests/{id}/notify-more?radiusKm=50 | requester | alert more donors |
| POST | /api/requests/{id}/cancel | requester | cancel |
| GET | /api/donor-requests/mine | donor | incoming requests |
| POST | /api/donor-requests/{id}/accept / reject / start / complete | donor | respond |
| GET | /api/notifications, /unread-count, POST /read-all | any | in-app notifications |
| GET | /api/admin/stats, /users, /requests | admin | monitoring |
| PUT | /api/admin/users/{id}/verify, /active | admin | verify / block |
| DELETE | /api/admin/requests/{id} | admin | remove fake request |

## Database (5 + 1 tables)

`users`, `donors`, `blood_requests`, `donor_requests`, `donation_history`, `notifications`

## Split of work (2 students)

* Frontend: `frontend/src/pages`, `store`, `components`
* Backend: `service/MatchingService`, `security`, `controller`, `model`

## Ideas for later

Email/SMS/push notifications, WebSocket instead of polling, map view (Leaflet), blood bank inventory, rate limiting, unit tests for `MatchingService` and `DonorRequestService`.

## Deployment

1. **MySQL**: create a cloud MySQL 8 DB (Aiven / Railway / TiDB Cloud).
2. **Backend** (Render / Railway, root dir `backend`, runtime Docker - `backend/Dockerfile` included):
   set the variables listed in `.env.production.example`. Generate `JWT_SECRET` with `openssl rand -base64 48`.
3. **Frontend** (Vercel / Netlify, root dir `frontend`, build `npm run build`, output `dist`):
   set `VITE_API_URL=https://<backend-url>/api`. SPA rewrites are already configured (`vercel.json`, `public/_redirects`).
4. Put the frontend URL in `BLOODCONNECT_CORS_ALLOWED_ORIGINS` on the backend and redeploy it.
5. Quick demo without MySQL: set `SPRING_PROFILES_ACTIVE=h2` (data resets on restart).
