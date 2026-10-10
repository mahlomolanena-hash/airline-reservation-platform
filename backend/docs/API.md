# API Reference

Base URL (local): `http://localhost:8080`
Interactive docs: `http://localhost:8080/swagger-ui.html`

All authenticated endpoints expect: `Authorization: Bearer <accessToken>`

---

## Auth

### POST /api/v1/auth/register
Public. Creates a new passenger account and returns a JWT + refresh token.

```json
{
  "email": "jane@example.com",
  "password": "SecurePass123",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "555-0100"
}
```

### POST /api/v1/auth/login
Public.
```json
{ "email": "jane@example.com", "password": "SecurePass123" }
```

### POST /api/v1/auth/refresh
Public. Exchanges a valid refresh token for a new access/refresh pair (rotation).
```json
{ "refreshToken": "..." }
```

### POST /api/v1/auth/logout
Authenticated. Revokes all of the current user's refresh tokens.

---

## Airports

| Method | Path                  | Access     | Description         |
|--------|------------------------|------------|-----------------------|
| GET    | /api/v1/airports       | Public     | List all airports     |
| POST   | /api/v1/airports       | ADMIN only | Create an airport      |

```json
{ "code": "JFK", "name": "John F. Kennedy International", "city": "New York", "country": "USA" }
```

---

## Flights

| Method | Path                       | Access     | Description                                             |
|--------|------------------------------|------------|-----------------------------------------------------------|
| GET    | /api/v1/flights              | Public     | Search flights (`originId`, `destinationId`, `departureDate`, `page`, `size`) |
| GET    | /api/v1/flights/{id}          | Public     | Get a flight by id                                        |
| POST   | /api/v1/flights               | ADMIN only | Create a flight                                            |

```json
{
  "flightNumber": "AA100",
  "airline": "American Airlines",
  "originId": 1,
  "destinationId": 2,
  "departureTime": "2026-09-10T14:00:00Z",
  "arrivalTime": "2026-09-10T20:00:00Z",
  "totalSeats": 180,
  "price": 249.99
}
```

`departureDate` on search matches any flight departing on that calendar day (UTC), e.g. `?departureDate=2026-09-10`.

---

## Bookings

| Method | Path                                | Access        | Description                                  |
|--------|---------------------------------------|---------------|-------------------------------------------------|
| POST   | /api/v1/bookings                     | Authenticated | Create a booking (reserves seats)              |
| GET    | /api/v1/bookings/{id}                | Authenticated | Get one booking (owner or admin)               |
| GET    | /api/v1/bookings/reference/{ref}     | Public        | Look up a booking by its reference code        |
| GET    | /api/v1/bookings/{id}/history        | Authenticated | Full status-change audit trail                 |
| GET    | /api/v1/bookings/me                  | Authenticated | List the current user's bookings               |
| POST   | /api/v1/bookings/{id}/cancel         | Authenticated | Cancel a booking (releases seats back)         |
| GET    | /api/v1/bookings/admin/all           | ADMIN only    | List all bookings                              |
| PATCH  | /api/v1/bookings/admin/{id}/status   | ADMIN only    | Update a booking's status                      |

Create booking request:
```json
{
  "flightId": 1,
  "passengers": [
    { "firstName": "Jane", "lastName": "Doe", "passportNumber": "P1234567", "dateOfBirth": "1990-05-14" }
  ]
}
```

If the requested passenger count exceeds available seats, the API returns `409 Conflict`
(`SeatUnavailableException`) and no booking is created — the whole request is transactional,
and seat reservation uses optimistic locking so two passengers can never both claim the last seat.

---

## Error shape

```json
{
  "timestamp": "2026-09-04T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Flight not found: 999"
}
```
Validation errors additionally include a `fieldErrors` map.
