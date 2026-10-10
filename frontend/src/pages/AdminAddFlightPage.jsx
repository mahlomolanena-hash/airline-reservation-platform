import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";

export default function AdminAddFlightPage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [airports, setAirports] = useState([]);
  const [form, setForm] = useState({
    flightNumber: "", airline: "", originId: "", destinationId: "",
    departureTime: "", arrivalTime: "", totalSeats: "", price: "",
  });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.listAirports().then(setAirports).catch(() => {});
  }, []);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(""); setFieldErrors({}); setSuccess(false); setLoading(true);
    try {
      const flight = await api.createFlight({
        ...form,
        originId: Number(form.originId),
        destinationId: Number(form.destinationId),
        totalSeats: Number(form.totalSeats),
        price: Number(form.price),
        departureTime: new Date(form.departureTime).toISOString(),
        arrivalTime: new Date(form.arrivalTime).toISOString(),
      }, auth.accessToken);
      setSuccess(true);
      setTimeout(() => navigate(`/flights/${flight.id}`), 900);
    } catch (err) {
      setError(err.message || "Could not create flight");
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="shell auth-shell" style={{ maxWidth: 480 }}>
      <div className="card">
        <div className="eyebrow">Admin</div>
        <h1 style={{ fontSize: 22, margin: "6px 0 22px" }}>Add a flight</h1>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">Flight created — redirecting…</div>}

        <form onSubmit={handleSubmit}>
          <div style={{ display: "flex", gap: 10 }}>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="flightNumber">Flight number</label>
              <input id="flightNumber" required className="mono" value={form.flightNumber} onChange={update("flightNumber")} />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="airline">Airline</label>
              <input id="airline" required value={form.airline} onChange={update("airline")} />
            </div>
          </div>

          <div style={{ display: "flex", gap: 10 }}>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="originId">Origin</label>
              <select id="originId" required value={form.originId} onChange={update("originId")}>
                <option value="">Select</option>
                {airports.map((a) => <option key={a.id} value={a.id}>{a.city} ({a.code})</option>)}
              </select>
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="destinationId">Destination</label>
              <select id="destinationId" required value={form.destinationId} onChange={update("destinationId")}>
                <option value="">Select</option>
                {airports.map((a) => <option key={a.id} value={a.id}>{a.city} ({a.code})</option>)}
              </select>
            </div>
          </div>

          <div style={{ display: "flex", gap: 10 }}>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="departureTime">Departure</label>
              <input id="departureTime" type="datetime-local" required value={form.departureTime} onChange={update("departureTime")} />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="arrivalTime">Arrival</label>
              <input id="arrivalTime" type="datetime-local" required value={form.arrivalTime} onChange={update("arrivalTime")} />
            </div>
          </div>

          <div style={{ display: "flex", gap: 10 }}>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="totalSeats">Total seats</label>
              <input id="totalSeats" type="number" min="1" required className="mono" value={form.totalSeats} onChange={update("totalSeats")} />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="price">Price</label>
              <input id="price" type="number" step="0.01" min="0.01" required className="mono" value={form.price} onChange={update("price")} />
            </div>
          </div>

          <button className="btn btn-amber btn-block" disabled={loading}>
            {loading ? "Creating…" : "Create flight"}
          </button>
        </form>
      </div>
    </div>
  );
}
