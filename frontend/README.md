# Skyfare — Airline Booking Frontend

A React (Vite) frontend for the Airline Ticket Reservation and Management System
backend. Covers flight search, passenger registration, ticket booking, booking
confirmation, and cancellation — the client-facing half of the brief.

This was verified with a real production build (`npm run build`) — 32 modules
compiled, zero errors.

## Design

A "boarding pass / departure board" visual identity, distinct from a generic
storefront look: a deep-ink header, amber accent (runway-light gold), monospace
flight numbers and times evoking an airport departure board, and a boarding-pass
style ticket card (complete with a dashed perforation and punch-hole notches) on
the booking detail page.

## Requirements

- Node.js 18+ and npm
- The backend running (see the main project's README) — defaults to `http://localhost:8080`

## Setup

```bash
npm install
cp .env.example .env
```

Edit `.env` if your backend runs somewhere other than `localhost:8080`:
```
VITE_API_BASE_URL=http://localhost:8080
```

## Run it

```bash
npm run dev
```

Opens on `http://localhost:5173` by default — already in the backend's configured
CORS allow-list, so no extra backend config is needed.

## Build for production

```bash
npm run build
```

Outputs static files to `dist/`.

## What's implemented

- **Flight search** — by origin, destination, and departure date; loads all
  scheduled flights by default
- **Booking flow** — add one or more passengers (name, passport, date of birth),
  see the live total, and book in one step
- **Auth** — register/login, JWT stored client-side, auto-attached to every
  authenticated request
- **My Bookings** — list of past bookings with status
- **Booking detail** — a boarding-pass style ticket view plus the full
  status-history timeline (pulls from the backend's audit-trail endpoint) and a
  cancel button
- **Admin** — minimal forms to add airports and flights, visible only to users
  with the `ADMIN` role (checked client-side for UX; the backend independently
  enforces this server-side regardless)

## Structure

```
src/
├── api/client.js        # Thin fetch wrapper — one function per backend endpoint
├── context/AuthContext.jsx  # JWT storage, login/register/logout
├── components/           # Header, FlightRow, ProtectedRoute
└── pages/                 # One component per route
```

## Known gaps / natural next steps

- No seat map / seat selection UI yet — the backend schema supports a seat number
  per passenger, but it isn't assigned or displayed in this frontend.
- No pagination controls on flight search results yet (backend supports
  `page`/`size`, UI always requests page 0).
- Admin flows only cover creating flights/airports, not editing them.
- No toast/notification system — success/error states are inline alerts.
