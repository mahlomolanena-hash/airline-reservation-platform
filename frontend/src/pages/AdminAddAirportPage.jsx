import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";

export default function AdminAddAirportPage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ code: "", name: "", city: "", country: "" });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(""); setFieldErrors({}); setSuccess(false); setLoading(true);
    try {
      await api.createAirport({ ...form, code: form.code.toUpperCase() }, auth.accessToken);
      setSuccess(true);
      setTimeout(() => navigate("/"), 900);
    } catch (err) {
      setError(err.message || "Could not create airport");
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="shell auth-shell">
      <div className="card">
        <div className="eyebrow">Admin</div>
        <h1 style={{ fontSize: 22, margin: "6px 0 22px" }}>Add an airport</h1>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">Airport created — redirecting…</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="code">IATA code</label>
            <input id="code" required maxLength={3} className="mono" value={form.code} onChange={update("code")} placeholder="e.g. SFO" />
            {fieldErrors.code && <span className="field-error">{fieldErrors.code}</span>}
          </div>
          <div className="field">
            <label htmlFor="name">Name</label>
            <input id="name" required value={form.name} onChange={update("name")} />
          </div>
          <div className="field">
            <label htmlFor="city">City</label>
            <input id="city" required value={form.city} onChange={update("city")} />
          </div>
          <div className="field">
            <label htmlFor="country">Country</label>
            <input id="country" required value={form.country} onChange={update("country")} />
          </div>
          <button className="btn btn-amber btn-block" disabled={loading}>
            {loading ? "Creating…" : "Create airport"}
          </button>
        </form>
      </div>
    </div>
  );
}
