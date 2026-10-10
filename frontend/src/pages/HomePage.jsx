import { useEffect, useState, useCallback } from "react";
import { api } from "../api/client";
import FlightRow from "../components/FlightRow";

export default function HomePage() {
  const [airports, setAirports] = useState([]);
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState("");

  const [originId, setOriginId] = useState("");
  const [destinationId, setDestinationId] = useState("");
  const [departureDate, setDepartureDate] = useState("");

  useEffect(() => {
    api.listAirports().then(setAirports).catch(() => {});
  }, []);

  const runSearch = useCallback(async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    setError("");
    setSearched(true);
    try {
      const page = await api.searchFlights({
        originId: originId || undefined,
        destinationId: destinationId || undefined,
        departureDate: departureDate || undefined,
      });
      setFlights(page.content);
    } catch (err) {
      setError(err.message || "Could not search flights");
    } finally {
      setLoading(false);
    }
  }, [originId, destinationId, departureDate]);

  // show all scheduled flights on first load
  useEffect(() => { runSearch(); /* eslint-disable-next-line */ }, []);

  return (
    <div className="shell">
      <div style={{ paddingTop: 36 }}>
        <div className="eyebrow">Flight Search</div>
        <h1 style={{ fontSize: 34, marginTop: 6 }}>Find your next flight.</h1>
      </div>

      <form className="search-panel" onSubmit={runSearch}>
        <div className="search-field">
          <label htmlFor="origin">From</label>
          <select id="origin" value={originId} onChange={(e) => setOriginId(e.target.value)}>
            <option value="">Any origin</option>
            {airports.map((a) => (
              <option key={a.id} value={a.id}>{a.city} ({a.code})</option>
            ))}
          </select>
        </div>
        <div className="search-field">
          <label htmlFor="destination">To</label>
          <select id="destination" value={destinationId} onChange={(e) => setDestinationId(e.target.value)}>
            <option value="">Any destination</option>
            {airports.map((a) => (
              <option key={a.id} value={a.id}>{a.city} ({a.code})</option>
            ))}
          </select>
        </div>
        <div className="search-field">
          <label htmlFor="date">Departure date</label>
          <input id="date" type="date" value={departureDate} onChange={(e) => setDepartureDate(e.target.value)} />
        </div>
        <button className="btn btn-amber" type="submit">Search flights</button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      <div style={{ paddingBottom: 60 }}>
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="skeleton" style={{ height: 84, marginBottom: 10 }} />
          ))
        ) : flights.length === 0 && searched ? (
          <div className="empty-state">
            <p>No flights match that search.</p>
          </div>
        ) : (
          flights.map((f) => <FlightRow key={f.id} flight={f} />)
        )}
      </div>
    </div>
  );
}
