import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="navbar">
      <Link className="brand" to="/">🏥 QueueCare</Link>
      <nav>
        {user ? (
          <>
            <Link to="/doctors">Find a Doctor</Link>
            {user.role === 'PATIENT' && <Link to="/appointments">My Appointments</Link>}
            {(user.role === 'DOCTOR' || user.role === 'ADMIN') && (
              <Link to="/dashboard">Dashboard</Link>
            )}
            <span className="muted" style={{ color: 'white' }}>Hi, {user.fullName}</span>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>
    </div>
  );
}
