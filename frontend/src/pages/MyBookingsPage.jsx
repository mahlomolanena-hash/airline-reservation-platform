import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";

export default function MyBookingsPage() {
  const { auth } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api.myBookings(auth.accessToken)
      .then((page) => setBookings(page.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [auth.accessToken]);

  return (
    <div className="shell" style={{ padding: "40px 0 64px" }}>
      <div className="eyebrow">Account</div>
      <h1 style={{ fontSize: 26, margin: "6px 0 26px" }}>My Bookings</h1>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="skeleton" style={{ height: 160 }} />
      ) : bookings.length === 0 ? (
        <div className="empty-state">
          <p>No bookings yet.</p>
          <Link to="/" className="btn btn-outline" style={{ marginTop: 16, display: "inline-flex" }}>
            Search flights
          </Link>
        </div>
      ) : (
        <div>
          <div className="booking-row eyebrow">
            <span>Reference</span>
            <span>Flight</span>
            <span>Status</span>
            <span>Total</span>
          </div>
          {bookings.map((b) => (
            <Link key={b.id} to={`/bookings/${b.id}`} className="booking-row" style={{ color: "inherit" }}>
              <span className="mono">{b.bookingReference}</span>
              <span>{b.flight.origin.code} &rarr; {b.flight.destination.code} &middot; {b.flight.flightNumber}</span>
              <span className={`status-pill status-${b.status}`}>{b.status}</span>
              <span className="mono">${Number(b.totalAmount).toFixed(2)}</span>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
