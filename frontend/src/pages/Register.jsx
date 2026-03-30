import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
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
  const [isOtpSent, setIsOtpSent] = useState(false);
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (location.state?.email) {
      setFormData(prev => ({ ...prev, email: location.state.email }));
      setIsOtpSent(true);
      handleResendOtp(location.state.email);
    }
  }, [location.state]);

  const handleResendOtp = async (targetEmail) => {
    const emailToUse = targetEmail || formData.email;
    if (!emailToUse) return;
    
    setLoading(true);
    try {
      const res = await api.post(`/auth/resend-otp?email=${emailToUse}`);
      setSuccess(res.data || 'A new OTP has been sent to your email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend OTP');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    if (!formData.name || !formData.email || !formData.password || !formData.phone) {
      setError('All fields are required');
      return false;
    }
    if (!/^[a-zA-Z\s]+$/.test(formData.name)) {
      setError('Name should only contain letters and spaces');
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
    if (!/^\d{10}$/.test(formData.phone)) {
      setError('Phone number must be exactly 10 digits');
      return false;
    }
    if (formData.role === 'DOCTOR') {
      if (!formData.specialty) {
        setError('Specialty is required for doctors');
        return false;
      }
      if (!formData.fees || Number(formData.fees) < 50) {
        setError('Minimum consultation fee is $50');
        return false;
      }
    }
    return true;
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (!validateForm()) return;
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
      setSuccess(response.data || 'OTP sent to your email!');
      setIsOtpSent(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const response = await api.post(`/auth/verify-otp?email=${formData.email}&otp=${otp}`);
      setSuccess('Verification successful! You can now login.');
      setTimeout(() => navigate('/login'), 2500);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid OTP');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '500px', margin: '40px auto' }}>
      <div className="glass-panel">
        <h2 style={{ textAlign: 'center', marginBottom: '24px' }}>
          {isOtpSent ? 'Verify Your Email' : 'Create an Account'}
        </h2>
        
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

        {isOtpSent ? (
          <form onSubmit={handleVerifyOtp}>
            <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: '20px' }}>
              We've sent a 4-digit code to <strong>{formData.email}</strong>.
            </p>
            <div className="form-group">
              <label>Enter 4-Digit OTP</label>
              <input 
                type="text" 
                className="form-control" 
                value={otp} 
                onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 4))} 
                placeholder="0000"
                style={{ textAlign: 'center', fontSize: '24px', letterSpacing: '8px' }}
                required 
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
              {loading ? 'Verifying...' : 'Verify & Continue'}
            </button>
            
            <div style={{ textAlign: 'center', marginTop: '16px' }}>
              <button type="button" className="btn-link" onClick={() => handleResendOtp()} disabled={loading} style={{ color: 'var(--color-primary)', background: 'none', border: 'none', cursor: 'pointer', textDecoration: 'underline' }}>
                Resend OTP
              </button>
            </div>

            <button type="button" className="btn btn-outline" style={{ width: '100%', marginTop: '12px' }} 
              onClick={() => setIsOtpSent(false)} disabled={loading}>
              Back to Details
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegister}>
            <div className="form-group" style={{ display: 'flex', gap: '16px', marginBottom: '20px' }}>
              <label style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '8px' }}>
                <input type="radio" checked={formData.role === 'PATIENT'} onChange={() => setFormData({...formData, role: 'PATIENT'})} /> Patient
              </label>
              <label style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '8px' }}>
                <input type="radio" checked={formData.role === 'DOCTOR'} onChange={() => setFormData({...formData, role: 'DOCTOR'})} /> Doctor
              </label>
            </div>

            <div className="form-group">
              <label>Full Name</label>
              <input type="text" className="form-control" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} placeholder="e.g. John Doe" required />
            </div>
            
            <div className="form-group">
              <label>Email Address</label>
              <input type="email" className="form-control" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} placeholder="your@email.com" required />
            </div>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label>Password</label>
                <input type="password" className="form-control" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} placeholder="••••••••" required />
              </div>
              <div className="form-group">
                <label>Phone Number (10 Digits)</label>
                <input type="tel" className="form-control" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value.replace(/\D/g, '').slice(0, 10)})} placeholder="1234567890" required />
              </div>
            </div>

            {formData.role === 'DOCTOR' && (
              <div style={{ padding: '16px', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', marginBottom: '20px' }}>
                <h4 style={{ marginBottom: '12px' }}>Professional Details</h4>
                <div className="form-group">
                  <label>Specialty</label>
                  <input type="text" className="form-control" value={formData.specialty} onChange={e => setFormData({...formData, specialty: e.target.value})} placeholder="e.g. Cardiology" required />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div className="form-group">
                    <label>Consultation Mode</label>
                    <select className="form-control" value={formData.mode} onChange={e => setFormData({...formData, mode: e.target.value})}>
                      <option value="ONLINE">ONLINE (Meet)</option>
                      <option value="OFFLINE">OFFLINE (Clinic)</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Fees (Min $50)</label>
                    <input type="number" className="form-control" value={formData.fees} onChange={e => setFormData({...formData, fees: e.target.value})} placeholder="50" min="50" required />
                  </div>
                </div>
              </div>
            )}
            
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
              {loading ? 'Processing...' : 'Send Verification OTP'}
            </button>
          </form>
        )}
        
        <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '14px' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--color-primary)' }}>Login here</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
