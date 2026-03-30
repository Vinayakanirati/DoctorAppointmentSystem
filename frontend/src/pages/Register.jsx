import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../utils/api';

const Register = () => {
  const [formData, setFormData] = useState({
    name: '', 
    email: '', 
    password: '', 
    phone: '', 
    role: 'PATIENT',
    specialty: '', 
    mode: 'ONLINE', 
    fees: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const validateForm = () => {
    if (!formData.name || !formData.email || !formData.password) {
      setError('Name, email, and password are required');
      return false;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      setError('Please enter a valid email address');
      return false;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long');
      return false;
    }

    if (formData.role === 'DOCTOR') {
      if (!formData.specialty) {
        setError('Specialty is required for doctors');
        return false;
      }
      if (!formData.fees || Number(formData.fees) <= 0) {
        setError('Please enter a valid consultation fee');
        return false;
      }
    }

    return true;
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    
    try {
      const payload = { ...formData };
      if (payload.role === 'PATIENT') {
        delete payload.specialty;
        delete payload.mode;
        delete payload.fees;
      } else {
        payload.fees = parseFloat(payload.fees);
      }
      
      const response = await api.post('/auth/register', payload);
      if (!response.data) {
        throw new Error('Invalid response from server');
      }
      
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Registration failed. Please try again.';
      setError(errorMsg);
      console.error('Registration error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '500px', margin: '40px auto' }}>
      <div className="glass-panel">
        <h2 style={{ textAlign: 'center', marginBottom: '24px' }}>Create an Account</h2>
        
        {error && (
          <div className="badge badge-danger" style={{ display: 'block', marginBottom: '16px', padding: '12px' }}>
            {error}
          </div>
        )}
        {success && (
          <div className="badge badge-success" style={{ display: 'block', marginBottom: '16px', padding: '12px' }}>
            {success}
          </div>
        )}
        
        <form onSubmit={handleRegister}>
          <div className="form-group" style={{ display: 'flex', gap: '16px', marginBottom: '20px' }}>
            <label style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input 
                type="radio" 
                checked={formData.role === 'PATIENT'} 
                onChange={() => setFormData({...formData, role: 'PATIENT'})} 
              /> 
              Patient
            </label>
            <label style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input 
                type="radio" 
                checked={formData.role === 'DOCTOR'} 
                onChange={() => setFormData({...formData, role: 'DOCTOR'})} 
              /> 
              Doctor
            </label>
          </div>

          <div className="form-group">
            <label>Full Name</label>
            <input 
              type="text" 
              className="form-control" 
              value={formData.name} 
              onChange={e => setFormData({...formData, name: e.target.value})} 
              placeholder="Enter your full name"
              required 
            />
          </div>
          
          <div className="form-group">
            <label>Email Address</label>
            <input 
              type="email" 
              className="form-control" 
              value={formData.email} 
              onChange={e => setFormData({...formData, email: e.target.value})} 
              placeholder="your.email@example.com"
              required 
            />
          </div>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label>Password</label>
              <input 
                type="password" 
                className="form-control" 
                value={formData.password} 
                onChange={e => setFormData({...formData, password: e.target.value})} 
                placeholder="••••••••"
                required 
              />
            </div>
            <div className="form-group">
              <label>Phone Number</label>
              <input 
                type="tel" 
                className="form-control" 
                value={formData.phone} 
                onChange={e => setFormData({...formData, phone: e.target.value})} 
                placeholder="+1 (555) 000-0000"
              />
            </div>
          </div>

          {formData.role === 'DOCTOR' && (
            <div style={{ padding: '16px', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', marginBottom: '20px' }}>
              <h4 style={{ marginBottom: '12px' }}>Doctor Verification Details</h4>
              <div className="form-group">
                <label>Specialty</label>
                <input 
                  type="text" 
                  className="form-control" 
                  value={formData.specialty} 
                  onChange={e => setFormData({...formData, specialty: e.target.value})} 
                  placeholder="e.g., Cardiology, Neurology"
                  required={formData.role === 'DOCTOR'} 
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label>Consultation Mode</label>
                  <select 
                    className="form-control" 
                    value={formData.mode} 
                    onChange={e => setFormData({...formData, mode: e.target.value})}
                  >
                    <option value="ONLINE">ONLINE (Google Meet)</option>
                    <option value="OFFLINE">OFFLINE (Clinic)</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Consultation Fees ($)</label>
                  <input 
                    type="number" 
                    className="form-control" 
                    value={formData.fees} 
                    onChange={e => setFormData({...formData, fees: e.target.value})} 
                    placeholder="100.00"
                    step="0.01"
                    min="0"
                    required={formData.role === 'DOCTOR'} 
                  />
                </div>
              </div>
            </div>
          )}
          
          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%' }} 
            disabled={loading}
          >
            {loading ? 'Creating Account...' : 'Register'}
          </button>
        </form>
        
        <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '14px' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--color-primary)' }}>Login here</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
