import { Link } from "react-router-dom";

function formatTime(iso) {
  return new Date(iso).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}
function formatDate(iso) {
  return new Date(iso).toLocaleDateString([], { month: "short", day: "numeric" });
}
function seatsClass(n) {
  if (n <= 0) return "out";
  if (n <= 10) return "low";
  return "";
}

export default function FlightRow({ flight }) {
  return (
    <Link to={`/flights/${flight.id}`} className="flight-row">
      <div>
        <div className="flight-number">{flight.flightNumber}</div>
        <div className="flight-airline">{flight.airline}</div>
      </div>

      <div>
        <div className="flight-route-city">{flight.origin.city}</div>
        <div className="flight-route-code">{flight.origin.code} &middot; {formatTime(flight.departureTime)}</div>
      </div>

      <div>
        <div className="flight-route-city">{flight.destination.city}</div>
        <div className="flight-route-code">{flight.destination.code} &middot; {formatTime(flight.arrivalTime)}</div>
      </div>

      <div className="flight-arrow">&#8594;</div>

      <div>
        <div className="flight-price">${Number(flight.price).toFixed(0)}</div>
        <div className={`seats-left ${seatsClass(flight.availableSeats)}`}>
          {flight.availableSeats > 0 ? `${flight.availableSeats} seats left` : "sold out"}
        </div>
      </div>
    </Link>
  );
}
