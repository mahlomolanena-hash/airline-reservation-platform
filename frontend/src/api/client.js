const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

class ApiError extends Error {
  constructor(message, status, fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function request(path, { method = "GET", body, token, skipJson = false } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch {
    // no body
  }

  if (!res.ok) {
    const message = data?.message || `Request failed (${res.status})`;
    throw new ApiError(message, res.status, data?.fieldErrors);
  }

  return skipJson ? res : data;
}

export const api = {
  // Auth
  register: (payload) => request("/api/v1/auth/register", { method: "POST", body: payload }),
  login: (payload) => request("/api/v1/auth/login", { method: "POST", body: payload }),
  refresh: (refreshToken) => request("/api/v1/auth/refresh", { method: "POST", body: { refreshToken } }),
  logout: (token) => request("/api/v1/auth/logout", { method: "POST", token }),

  // Airports
  listAirports: () => request("/api/v1/airports"),
  createAirport: (payload, token) => request("/api/v1/airports", { method: "POST", body: payload, token }),

  // Flights
  searchFlights: ({ originId, destinationId, departureDate, page = 0, size = 20 } = {}) => {
    const params = new URLSearchParams({ page, size });
    if (originId) params.set("originId", originId);
    if (destinationId) params.set("destinationId", destinationId);
    if (departureDate) params.set("departureDate", departureDate);
    return request(`/api/v1/flights?${params.toString()}`);
  },
  getFlight: (id) => request(`/api/v1/flights/${id}`),
  createFlight: (payload, token) => request("/api/v1/flights", { method: "POST", body: payload, token }),

  // Bookings
  createBooking: (payload, token) => request("/api/v1/bookings", { method: "POST", body: payload, token }),
  getBooking: (id, token) => request(`/api/v1/bookings/${id}`, { token }),
  getBookingByReference: (reference) => request(`/api/v1/bookings/reference/${reference}`),
  getBookingHistory: (id, token) => request(`/api/v1/bookings/${id}/history`, { token }),
  myBookings: (token, page = 0, size = 20) =>
    request(`/api/v1/bookings/me?page=${page}&size=${size}`, { token }),
  cancelBooking: (id, token) => request(`/api/v1/bookings/${id}/cancel`, { method: "POST", token }),
};

export { ApiError };
