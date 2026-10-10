import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Header() {
  const { auth, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  return (
    <header className="header">
      <div className="shell header-inner">
        <Link to="/" className="brand">
          <span className="brand-mark">&#9992;</span>
          SKYFARE
        </Link>

        <nav className="nav">
          <Link to="/">Search</Link>
          {auth && <Link to="/bookings">My Bookings</Link>}
          {isAdmin && <Link to="/admin/flights/new">Add Flight</Link>}
          {isAdmin && <Link to="/admin/airports/new">Add Airport</Link>}

          {auth ? (
            <button className="link" onClick={handleLogout}>Log out</button>
          ) : (
            <>
              <Link to="/login">Log in</Link>
              <Link to="/register"><span className="btn btn-amber btn-sm">Sign up</span></Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
