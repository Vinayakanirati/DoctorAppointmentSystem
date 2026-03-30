import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { LogOut } from 'lucide-react';

const Navbar = () => {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  let user = null;
  try {
    const userStr = localStorage.getItem('user');
    if (userStr && userStr !== 'undefined') {
      user = JSON.parse(userStr);
    }
  } catch (e) {
    localStorage.removeItem('user');
  }

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="brand">
          <div className="red-cross-logo"></div>
          Arundhati Clinic
        </Link>
        <div className="nav-links">
          {!token ? (
            <>
              <Link to="/login" className="nav-item">Login</Link>
              <Link to="/register" className="nav-item">Register</Link>
            </>
          ) : (
            <>
              {user?.role === 'ADMIN' && (
                <Link to="/admin/dashboard" className="nav-item">Dashboard</Link>
              )}
              {user?.role === 'DOCTOR' && (
                <>
                  <Link to="/doctor/dashboard" className="nav-item">Dashboard</Link>
                  <Link to="/doctor/history" className="nav-item">History</Link>
                </>
              )}
              {user?.role === 'PATIENT' && (
                <>
                  <Link to="/patient/browse" className="nav-item">Find Doctors</Link>
                  <Link to="/patient/history" className="nav-item">My Appointments</Link>
                </>
              )}
              <Link to="/profile" className="nav-item">Profile</Link>
              <span style={{color: 'var(--text-muted)'}}>| {user?.name || user?.email}</span>
              <button onClick={handleLogout} className="btn btn-outline" style={{padding: '6px 12px'}}>
                <LogOut size={16} /> Logout
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
