import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!email || !password) {
      setError('Email and password are required');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) && email !== 'root') {
      setError('Please enter a valid email address');
      return;
    }

    setLoading(true);
    
    try {
      const response = await api.post('/auth/login', { email, password });
      
      if (!response.data || !response.data.token) {
        throw new Error('Invalid response: missing authentication token');
      }
      
      const { token, email: resEmail, name, role } = response.data;
      
      if (!role) {
        throw new Error('Invalid response: missing user role');
      }
      
      const user = { email: resEmail, name, role };
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      
      if (user.role === 'ADMIN') navigate('/admin/dashboard');
      else if (user.role === 'DOCTOR') navigate('/doctor/dashboard');
      else navigate('/patient/browse');
      
    } catch (err) {
      console.error('Login error full:', err);
      let errorMsg = 'Login failed. Please check your credentials.';
      
      if (err.response?.data?.message) {
        errorMsg = err.response.data.message;
      } else if (err.response?.status === 401) {
        errorMsg = 'Invalid email or password';
      } else if (err.response?.status === 403) {
        errorMsg = 'Your account is not verified yet. Please check your email for the OTP.';
      } else if (err.message) {
        errorMsg = err.message;
      }
      
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '40px auto' }}>
      <div className="glass-panel">
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div className="red-cross-logo" style={{ marginBottom: '16px' }}></div>
          <h2>Welcome Back</h2>
          <p style={{ color: 'var(--text-muted)' }}>Login to Arundhati Clinic</p>
        </div>
        
        {error && (
          <div className="badge badge-danger" style={{ display: 'block', marginBottom: '16px', padding: '12px', textAlign: 'center' }}>
            {error}
            {error.toLowerCase().includes('verify') && (
              <div style={{ marginTop: '12px' }}>
                <Link to="/register" state={{ email }} style={{ color: 'white', textDecoration: 'underline', fontWeight: 'bold' }}>
                  Verify Account Now
                </Link>
              </div>
            )}
          </div>
        )}
        
        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>Email ID / Username</label>
            <input 
              type="text" 
              className="form-control" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="patient@example.com OR root"
              disabled={loading}
              required 
            />
          </div>
          
          <div className="form-group">
            <label>Password</label>
            <input 
              type="password" 
              className="form-control" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              disabled={loading}
              required 
            />
          </div>
          
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>
        
        <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '14px' }}>
          Don't have an account? <Link to="/register" style={{ color: 'var(--color-primary)' }}>Register here</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
