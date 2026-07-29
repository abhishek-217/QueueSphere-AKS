# Healthcare Appointment & Queue System — Backend

Spring Boot 3 / Java 17 backend for booking appointments and tracking live consultation queues in real time.

## Stack
- Spring Boot 3.3 (Web, Data JPA, Security, WebSocket, Validation)
- MySQL 8
- JWT auth (jjwt 0.12)
- STOMP over WebSocket (SockJS) for live queue push updates

## 1. Prerequisites
- JDK 17+
- Maven 3.9+ (or use the included `mvnw` if you add one)
- MySQL 8 running locally on port 3306

## 2. Database setup
No manual schema needed — `spring.jpa.hibernate.ddl-auto=update` in
`application.properties` will auto-create the `healthcare_queue` database and tables
on first run (via `createDatabaseIfNotExist=true` in the JDBC URL).

Just make sure a MySQL user/password matching `application.properties` exists:
```sql
-- if you want a dedicated user instead of root:
CREATE USER 'queueapp'@'localhost' IDENTIFIED BY 'queueapp_pw';
GRANT ALL PRIVILEGES ON healthcare_queue.* TO 'queueapp'@'localhost';
FLUSH PRIVILEGES;
```
Then update `spring.datasource.username` / `spring.datasource.password` accordingly.

## 3. Run it
```bash
mvn spring-boot:run
```
API will be available at `http://localhost:8080`.

## 4. Auth flow
1. `POST /api/auth/register` with `role: PATIENT` or `role: DOCTOR` (doctor also needs
   `specialization` and `department`).
2. `POST /api/auth/login` returns a JWT.
3. Send `Authorization: Bearer <token>` on subsequent requests.

## 5. Key REST endpoints

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | public | Register patient or doctor |
| POST | `/api/auth/login` | public | Login, get JWT |
| GET | `/api/doctors` | public | List doctors (optional `?specialization=`) |
| GET | `/api/doctors/{id}` | public | Doctor detail |
| GET | `/api/doctors/{id}/availability` | public | Weekly schedule |
| POST | `/api/doctors/{id}/availability` | DOCTOR (own profile) / ADMIN | Add a weekly working-hours slot |
| POST | `/api/appointments` | PATIENT | Book an appointment |
| GET | `/api/appointments/me` | PATIENT | My appointment history |
| DELETE | `/api/appointments/{id}` | PATIENT | Cancel a booked appointment |
| POST | `/api/appointments/{id}/check-in` | authenticated | Check in on arrival → joins live queue |
| GET | `/api/appointments/doctor/{doctorId}?date=YYYY-MM-DD` | DOCTOR/ADMIN | Doctor's day schedule |
| GET | `/api/queue/public/{doctorId}` | public | Current queue snapshot (poll fallback) |
| POST | `/api/queue/doctor/{doctorId}/call-next` | DOCTOR/ADMIN | Call the next patient |
| POST | `/api/queue/entry/{queueEntryId}/no-show` | DOCTOR/ADMIN | Mark a no-show |

## 6. Real-time queue updates (WebSocket / STOMP)

Connect: `ws://localhost:8080/ws` (SockJS-wrapped STOMP endpoint)

Subscribe to a doctor's live queue:
```
/topic/queue/{doctorId}
```

Every check-in, call-next, or no-show action pushes a fresh `QueueStatusMessage`
to all subscribers of that topic — no polling required. Example payload:
```json
{
  "doctorId": 3,
  "doctorName": "Dr. Asha Rao",
  "nowServingNumber": 5,
  "totalWaiting": 4,
  "estimatedWaitMinutes": 60,
  "entries": [
    {"appointmentId": 12, "queueNumber": 5, "patientName": "Rahul Shah", "status": "IN_PROGRESS"},
    {"appointmentId": 13, "queueNumber": 6, "patientName": "Priya Mehta", "status": "CHECKED_IN"}
  ],
  "updatedAt": "2026-07-04T10:15:00"
}
```

## 7. Data model summary
- `users` — shared auth table (patients, doctors, admins) with hashed password + role
- `patients` / `doctors` — 1:1 profile extensions of `users`
- `doctor_availability` — weekly recurring schedule per doctor
- `appointments` — booked slots, status lifecycle: `BOOKED → CHECKED_IN → IN_PROGRESS → COMPLETED` (or `CANCELLED` / `NO_SHOW`)
- `queue_entries` — one row per checked-in appointment per day, holding the day's queue number and live status

## 8. Notes / next steps
- Passwords hashed with BCrypt.
- JWT secret in `application.properties` is a placeholder — replace with an env-var-backed secret before deploying.
- No refresh-token endpoint yet; access token expiry is 24h (`app.jwt.expiration-ms`).
- Doctors set their own weekly working hours via `POST /api/doctors/{id}/availability`. Once a doctor
  has at least one active slot, bookings outside those hours are rejected with a 400; doctors with no
  schedule configured yet accept any future time (so the demo doesn't break before you set hours up).
