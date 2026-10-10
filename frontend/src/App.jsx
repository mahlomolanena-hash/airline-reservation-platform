import { Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import ProtectedRoute from "./components/ProtectedRoute";

import HomePage from "./pages/HomePage";
import FlightDetailPage from "./pages/FlightDetailPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import MyBookingsPage from "./pages/MyBookingsPage";
import BookingDetailPage from "./pages/BookingDetailPage";
import AdminAddFlightPage from "./pages/AdminAddFlightPage";
import AdminAddAirportPage from "./pages/AdminAddAirportPage";

export default function App() {
  return (
    <>
      <Header />

      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/flights/:id" element={<FlightDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route path="/bookings" element={
          <ProtectedRoute><MyBookingsPage /></ProtectedRoute>
        } />
        <Route path="/bookings/:id" element={
          <ProtectedRoute><BookingDetailPage /></ProtectedRoute>
        } />
        <Route path="/admin/flights/new" element={
          <ProtectedRoute adminOnly><AdminAddFlightPage /></ProtectedRoute>
        } />
        <Route path="/admin/airports/new" element={
          <ProtectedRoute adminOnly><AdminAddAirportPage /></ProtectedRoute>
        } />
      </Routes>

      <footer className="footer">
        <div className="shell">SKYFARE — reference storefront for the Airline Reservation API</div>
      </footer>
    </>
  );
}
