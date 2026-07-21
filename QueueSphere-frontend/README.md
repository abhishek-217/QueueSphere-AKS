# Healthcare Appointment & Queue System — Frontend

React (Vite) frontend for the healthcare appointment and live-queue backend.

## Stack
- React 18 + React Router
- Axios (JWT attached automatically via interceptor)
- `@stomp/stompjs` + `sockjs-client` for live WebSocket queue updates

## Setup
```bash
npm install
npm run dev
```
Runs on `http://localhost:5173`. The Vite dev server proxies `/api` and `/ws`
to `http://localhost:8080`, so make sure the Spring Boot backend is running first.

## Pages
- `/login`, `/register` — auth (register supports both PATIENT and DOCTOR signup)
- `/doctors` — browse doctors, filter by specialization, book an appointment
- `/appointments` — patient's appointment history; check in on arrival to join the
  live queue, with a real-time "now serving" card powered by STOMP/WebSocket
- `/dashboard` — doctor view: today's schedule, live queue table, "Call Next Patient",
  mark no-shows

## Build
```bash
npm run build
```
Outputs static files to `dist/`, servable by any static host or behind nginx.
