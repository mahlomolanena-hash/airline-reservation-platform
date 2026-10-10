#!/usr/bin/env bash
# Quick manual smoke test against a locally running instance (dev profile, port 8080).
set -e
BASE=http://localhost:8080

echo "== Register =="
curl -s -X POST "$BASE/api/v1/auth/register" -H "Content-Type: application/json" -d '{
  "email":"jane@example.com","password":"SecurePass123","firstName":"Jane","lastName":"Doe"
}' | tee /tmp/register.json
TOKEN=$(grep -o '"accessToken":"[^"]*' /tmp/register.json | cut -d'"' -f4)

echo -e "\n== Search flights (public) =="
curl -s "$BASE/api/v1/flights" | head -c 500

echo -e "\n\n== Book flight 1 for one passenger =="
curl -s -X POST "$BASE/api/v1/bookings" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"flightId":1,"passengers":[{"firstName":"Jane","lastName":"Doe","passportNumber":"P1234567","dateOfBirth":"1990-05-14"}]}' \
  | tee /tmp/booking.json
BOOKING_ID=$(grep -o '"id":[0-9]*' /tmp/booking.json | head -1 | cut -d':' -f2)

echo -e "\n\n== View booking status history =="
curl -s "$BASE/api/v1/bookings/$BOOKING_ID/history" -H "Authorization: Bearer $TOKEN"

echo -e "\n\n== Cancel the booking =="
curl -s -X POST "$BASE/api/v1/bookings/$BOOKING_ID/cancel" -H "Authorization: Bearer $TOKEN"

echo -e "\n\n== Admin login (seeded demo admin) =="
curl -s -X POST "$BASE/api/v1/auth/login" -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin123!"}'
