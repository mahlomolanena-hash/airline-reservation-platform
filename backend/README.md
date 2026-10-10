# Airline Ticket Reservation and Management System — Backend

A Spring Boot backend for airline flight search, booking, and reservation management.
Covers the full brief: flight search, availability checking, passenger registration,
ticket booking, booking confirmation, and cancellation — with a proper database,
REST APIs, validation, and error handling.

## Applying lessons from the last project

This project's predecessor (an e-commerce backend) taught three real lessons the hard
way, once a real frontend hit it with real traffic. All three are baked in here
**from the start** rather than discovered later:

1. **JWT parsing must never crash a public request.** `JwtAuthenticationFilter` wraps
   token parsing in a try/catch from day one — an expired or malformed token means
   "unauthenticated," not a 500 error.
2. **The global catch-all exception handler logs everything it catches.**
   `GlobalExceptionHandler.handleGeneric` calls `log.error(...)` before returning the
   generic response — never fail silently.
3. **Every service method touching a lazy JPA association is `@Transactional`.**
   With `open-in-view: false` (the correct setting), reading `flight.getOrigin()` or
   `booking.getPassengers()` outside an open session throws `LazyInitializationException`.
   Every read method in `FlightService` and `BookingService` is explicitly
   `@Transactional(readOnly = true)` from the start.

A fourth lesson: the integration test is named `FlightControllerTest.java`, **not**
`FlightControllerIT.java`. The previous project's `*IT.java` test never actually ran
under `mvn test`, because Maven's default Surefire plugin only picks up `*Test.java`
files — that gap is closed here by naming it correctly from the start.

## What's implemented

- **Auth** — JWT access tokens + opaque, hashed, rotating refresh tokens; BCrypt
  password hashing; role-based access control (`PASSENGER` / `ADMIN`)
- **Airports** — reference data for origin/destination lookups
- **Flights** — search by origin, destination, and departure date; admin create;
  optimistic-locked seat inventory (`@Version` on `Flight`) so two bookings can never
  both claim the last seat
- **Bookings** — create (validates + reserves seats atomically), confirm, cancel
  (releases seats back), full status-history audit trail, lookup by human-readable
  booking reference
- **Validation & error handling** — Bean Validation on every request DTO, a single
  `GlobalExceptionHandler` with a consistent JSON error shape (and real logging)
- **Testing** — Mockito unit tests for the booking critical path (successful booking,
  insufficient-seat rejection, cancellation + seat release, double-cancel rejection)
  and flight creation; a `@SpringBootTest` + `MockMvc` integration test against the
  real security filter chain and H2 — actually wired to run under `mvn test`
- **CI** — GitHub Actions workflow running the full test suite and packaging the jar
  on every push/PR
- **API documentation** — springdoc-openapi at `/swagger-ui.html`

## Project structure

```
com.example.airline/
├── config/          # SecurityConfig, CORS, OpenAPI, dev DataSeeder
├── security/        # JWT service/filter, UserDetails adapter
├── domain/          # JPA entities + enums
├── repository/      # Spring Data JPA repositories
├── dto/
│   ├── request/     # Validated inbound payloads
│   └── response/    # Outbound shapes
├── service/         # Business logic, @Transactional boundaries
├── controller/       # Thin REST controllers
├── exception/        # Custom exceptions + @RestControllerAdvice (with logging)
└── AirlineApplication.java
```

## Running it

Requires JDK 17+ and Maven. The `dev` profile (default) uses an in-memory H2
database — no external DB setup needed locally.

```bash
mvn spring-boot:run
```

Once running:
- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:airlinedb`, user `sa`, no password)

A `DataSeeder` (dev profile only) creates on first run:
- Demo admin: `admin@example.com` / `Admin123!`
- Three airports (JFK, LAX, LHR) and two scheduled flights

Try the flow with `docs/curl-examples.sh`, or see `docs/API.md` for full endpoint
documentation.

## Running tests

```bash
mvn test
```

Covers the booking critical path (successful booking + seat reservation,
insufficient-seat rejection, cancellation + seat release, rejecting a double-cancel),
flight creation/validation, and a real end-to-end slice through the security filter
chain (public search works, anonymous users can't create flights, admins can create
airports, registration validation errors return field-level detail).

## Connecting a frontend

- CORS is pre-configured for `localhost:3000` / `:5173` / `:4200`.
- Send `Authorization: Bearer <token>` from `/api/v1/auth/login` or `/register` on
  every subsequent request.
- List endpoints are paginated (`?page=0&size=20`) and share the `PageResponse<T>`
  shape (`content` / `totalPages` / `totalElements`).

## Version control & workflow

- `.gitignore` excludes build output, IDE files, and `application-local.yml` — real
  secrets are supplied via env vars (`JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`),
  never committed.
- Suggested branch flow: `main` ← `develop` ← feature branches, PRs into `develop`/`main`.
  CI runs on every push/PR to either branch.

## Natural next steps

1. **Payments** — the brief doesn't ask for a payment gateway, so it's intentionally
   out of scope here; the e-commerce project's Stripe integration pattern (webhook-verified
   confirmation, never trusting the client) would extend cleanly to this booking flow.
2. **Seat map / seat selection** — `BookingPassenger.seatNumber` exists in the schema
   but isn't assigned automatically yet; a seat map UI could let passengers pick one.
3. **Refresh token lookup at scale** — same caveat as the e-commerce project:
   `RefreshTokenService.consumeAndRotate` scans candidate tokens since BCrypt hashes
   aren't indexable; fine at demo scale, worth indexing before real traffic.
4. **Postgres in CI** — the CI workflow tests against H2; add a Postgres service
   container before relying on prod-parity behavior in CI.

I'm happy to build out any of these — just say which one.
