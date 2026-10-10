import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

function emptyPassenger() {
  return { firstName: "", lastName: "", passportNumber: "", dateOfBirth: "" };
}

function formatDateTime(iso) {
  return new Date(iso).toLocaleString([], { weekday: "short", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
}

export default function FlightDetailPage() {
  const { id } = useParams();
  const [flight, setFlight] = useState(null);
  const [passengers, setPassengers] = useState([emptyPassenger()]);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [booking, setBooking] = useState(false);

  const { auth } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    api.getFlight(id).then(setFlight).catch((e) => setError(e.message));
  }, [id]);

  const updatePassenger = (idx, key, value) => {
    setPassengers((prev) => prev.map((p, i) => (i === idx ? { ...p, [key]: value } : p)));
  };

  const addPassenger = () => setPassengers((prev) => [...prev, emptyPassenger()]);
  const removePassenger = (idx) => setPassengers((prev) => prev.filter((_, i) => i !== idx));

  const handleBook = async (e) => {
    e.preventDefault();
    if (!auth) {
      navigate("/login", { state: { from: { pathname: `/flights/${id}` } } });
      return;
    }
    setError("");
    setFieldErrors({});
    setBooking(true);
    try {
      const result = await api.createBooking({ flightId: Number(id), passengers }, auth.accessToken);
      navigate(`/bookings/${result.id}`);
    } catch (err) {
      setError(err.message || "Could not create booking");
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setBooking(false);
    }
  };

  if (error && !flight) {
    return <div className="shell" style={{ padding: "48px 0" }}><div className="alert alert-error">{error}</div></div>;
  }
  if (!flight) {
    return <div className="shell" style={{ padding: "48px 0" }}><div className="skeleton" style={{ height: 320 }} /></div>;
  }

  const total = (Number(flight.price) * passengers.length).toFixed(2);

  return (
    <div className="shell" style={{ padding: "40px 0 64px" }}>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 48 }}>
        <div>
          <div className="eyebrow">{flight.airline}</div>
          <h1 style={{ fontSize: 28, margin: "8px 0 20px" }}>{flight.flightNumber}</h1>

          <div className="ticket-route" style={{ marginBottom: 24 }}>
            <div>
              <div className="ticket-city">{flight.origin.city}</div>
              <div className="ticket-code">{flight.origin.code} &middot; {formatDateTime(flight.departureTime)}</div>
            </div>
            <span className="ticket-plane">&#9992;</span>
            <div>
              <div className="ticket-city">{flight.destination.city}</div>
              <div className="ticket-code">{flight.destination.code} &middot; {formatDateTime(flight.arrivalTime)}</div>
            </div>
          </div>

          <div style={{ display: "flex", gap: 30 }}>
            <div>
              <div className="eyebrow">Price per passenger</div>
              <div className="mono" style={{ fontSize: 22, fontWeight: 700, marginTop: 4 }}>${Number(flight.price).toFixed(2)}</div>
            </div>
            <div>
              <div className="eyebrow">Seats available</div>
              <div className="mono" style={{ fontSize: 22, fontWeight: 700, marginTop: 4 }}>{flight.availableSeats}</div>
            </div>
          </div>
        </div>

        <div className="card">
          <h3 style={{ fontSize: 16, marginBottom: 16 }}>Passenger details</h3>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleBook}>
            {passengers.map((p, idx) => (
              <div key={idx} style={{ borderBottom: idx < passengers.length - 1 ? "1px solid var(--line)" : "none", paddingBottom: 14, marginBottom: 14 }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
                  <span className="eyebrow">Passenger {idx + 1}</span>
                  {passengers.length > 1 && (
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => removePassenger(idx)}>Remove</button>
                  )}
                </div>
                <div style={{ display: "flex", gap: 10 }}>
                  <div className="field" style={{ flex: 1 }}>
                    <label>First name</label>
                    <input required value={p.firstName} onChange={(e) => updatePassenger(idx, "firstName", e.target.value)} />
                  </div>
                  <div className="field" style={{ flex: 1 }}>
                    <label>Last name</label>
                    <input required value={p.lastName} onChange={(e) => updatePassenger(idx, "lastName", e.target.value)} />
                  </div>
                </div>
                <div style={{ display: "flex", gap: 10 }}>
                  <div className="field" style={{ flex: 1 }}>
                    <label>Passport number</label>
                    <input required className="mono" value={p.passportNumber} onChange={(e) => updatePassenger(idx, "passportNumber", e.target.value)} />
                  </div>
                  <div className="field" style={{ flex: 1 }}>
                    <label>Date of birth</label>
                    <input type="date" value={p.dateOfBirth} onChange={(e) => updatePassenger(idx, "dateOfBirth", e.target.value)} />
                  </div>
                </div>
              </div>
            ))}

            <button type="button" className="btn btn-outline btn-sm" onClick={addPassenger} style={{ marginBottom: 20 }}>
              + Add another passenger
            </button>

            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16, paddingTop: 10, borderTop: "1px solid var(--line)" }}>
              <span className="eyebrow">Total</span>
              <span className="mono" style={{ fontWeight: 700, fontSize: 18 }}>${total}</span>
            </div>

            <button className="btn btn-amber btn-block" disabled={booking || flight.availableSeats < passengers.length}>
              {booking ? "Booking…" : flight.availableSeats < passengers.length ? "Not enough seats" : "Confirm booking"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
