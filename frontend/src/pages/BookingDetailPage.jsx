import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";

function formatDateTime(iso) {
  return new Date(iso).toLocaleString([], { weekday: "short", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
}

function Barcode() {
  // decorative only — a run of bars with varying widths, seeded from nothing in particular
  const widths = [3, 1, 2, 1, 3, 2, 1, 1, 3, 2, 1, 2, 3, 1, 1, 2];
  return (
    <div className="ticket-barcode">
      {widths.map((w, i) => (
        <span key={i} style={{ width: w * 2, height: 22 + (i % 3) * 3 }} />
      ))}
    </div>
  );
}

export default function BookingDetailPage() {
  const { id } = useParams();
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState("");
  const [cancelling, setCancelling] = useState(false);

  const load = () => {
    Promise.all([api.getBooking(id, auth.accessToken), api.getBookingHistory(id, auth.accessToken)])
      .then(([b, h]) => { setBooking(b); setHistory(h); })
      .catch((e) => setError(e.message));
  };

  useEffect(load, [id, auth.accessToken]);

  const handleCancel = async () => {
    setCancelling(true);
    setError("");
    try {
      await api.cancelBooking(id, auth.accessToken);
      load();
    } catch (err) {
      setError(err.message || "Could not cancel booking");
    } finally {
      setCancelling(false);
    }
  };

  if (error && !booking) {
    return <div className="shell" style={{ padding: "48px 0" }}><div className="alert alert-error">{error}</div></div>;
  }
  if (!booking) {
    return <div className="shell" style={{ padding: "48px 0" }}><div className="skeleton" style={{ height: 260 }} /></div>;
  }

  const f = booking.flight;
  const canCancel = booking.status !== "CANCELLED";

  return (
    <div className="shell" style={{ padding: "40px 0 64px", maxWidth: 860 }}>
      <Link to="/bookings" style={{ fontSize: 13, color: "var(--muted)" }}>&larr; Back to bookings</Link>

      <div style={{ margin: "16px 0 28px" }}>
        <div className="eyebrow">Booking</div>
        <h1 style={{ fontSize: 26, marginTop: 6 }}>{booking.bookingReference}</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="ticket">
        <div className="ticket-main">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
            <div className="ticket-ref">{booking.bookingReference}</div>
            <span className={`status-pill status-${booking.status}`}>{booking.status}</span>
          </div>

          <div className="ticket-route">
            <div>
              <div className="ticket-city">{f.origin.city}</div>
              <div className="ticket-code">{f.origin.code}</div>
            </div>
            <span className="ticket-plane">&#9992;</span>
            <div>
              <div className="ticket-city">{f.destination.city}</div>
              <div className="ticket-code">{f.destination.code}</div>
            </div>
          </div>

          <div className="ticket-meta-row">
            <div>
              <div className="ticket-meta-label">Flight</div>
              <div className="ticket-meta-value">{f.flightNumber}</div>
            </div>
            <div>
              <div className="ticket-meta-label">Departs</div>
              <div className="ticket-meta-value">{formatDateTime(f.departureTime)}</div>
            </div>
            <div>
              <div className="ticket-meta-label">Arrives</div>
              <div className="ticket-meta-value">{formatDateTime(f.arrivalTime)}</div>
            </div>
          </div>

          <div className="ticket-meta-row" style={{ marginTop: 18 }}>
            <div>
              <div className="ticket-meta-label">Passengers</div>
              <div className="ticket-meta-value">{booking.passengers.length}</div>
            </div>
            <div>
              <div className="ticket-meta-label">Total paid</div>
              <div className="ticket-meta-value">${Number(booking.totalAmount).toFixed(2)}</div>
            </div>
          </div>
        </div>

        <div className="ticket-stub">
          <div>
            <div className="ticket-meta-label" style={{ color: "#93A2B8" }}>Gate</div>
            <div className="ticket-meta-value" style={{ color: "#fff" }}>--</div>
          </div>
          <Barcode />
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24, marginTop: 8 }}>
        <div className="card">
          <div className="eyebrow" style={{ marginBottom: 14 }}>Passengers</div>
          {booking.passengers.map((p, i) => (
            <div key={i} style={{ padding: "10px 0", borderBottom: i < booking.passengers.length - 1 ? "1px solid var(--line)" : "none" }}>
              <div style={{ fontWeight: 600, fontSize: 14 }}>{p.firstName} {p.lastName}</div>
              <div className="mono" style={{ fontSize: 12, color: "var(--muted)" }}>Passport {p.passportNumber}</div>
            </div>
          ))}

          {canCancel && (
            <button className="btn btn-danger btn-block" style={{ marginTop: 18 }} onClick={handleCancel} disabled={cancelling}>
              {cancelling ? "Cancelling…" : "Cancel booking"}
            </button>
          )}
        </div>

        <div className="card">
          <div className="eyebrow" style={{ marginBottom: 16 }}>Status history</div>
          {history.map((h, idx) => (
            <div className="timeline-item" key={idx}>
              <span className="timeline-dot" />
              <div>
                <div className={`status-pill status-${h.status}`} style={{ marginBottom: 4 }}>{h.status}</div>
                <div style={{ fontSize: 12.5, color: "var(--muted)" }}>{h.notes}</div>
                <div className="mono" style={{ fontSize: 11, color: "var(--muted)", marginTop: 4 }}>
                  {new Date(h.changedAt).toLocaleString()}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
