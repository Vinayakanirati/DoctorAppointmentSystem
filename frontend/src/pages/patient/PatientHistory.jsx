import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { useNavigate } from 'react-router-dom';
import api from '../../utils/api';

const PatientHistory = () => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const fetchHistory = async () => {
    try {
      setError('');
      setLoading(true);
      const res = await api.get('/patient/appointments');
      setAppointments(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to fetch appointment history:', err);
      setError(err.response?.data?.message || 'Failed to load appointment history');
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleCancel = async (appId) => {
    if (!window.confirm('Are you sure you want to cancel this appointment? Refund depends on clinic policy.')) return;
    try {
      await api.delete(`/patient/appointments/${appId}`);
      fetchHistory();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel appointment');
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading your health timeline...</div>;

  return (
    <div className="glass-panel">
      <h2 style={{ marginBottom: '24px' }}>My Appointments</h2>
      
      {error && (
        <div style={{
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          color: '#ef4444',
          padding: '12px',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          {error}
        </div>
      )}
      
      {appointments.length === 0 ? (
        <p style={{ color: 'var(--text-muted)' }}>You have no appointment history.</p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Doctor</th>
                <th>Time & Date</th>
                <th>Mode</th>
                <th>Status</th>
                <th>Payment</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appointments.sort((a,b) => {
                const dateA = a?.appointmentStart;
                const dateB = b?.appointmentStart;
                if (!dateA || !dateB) return 0;
                return new Date(dateB) - new Date(dateA);
              }).map(app => {
                if (!app || !app.id) return null;
                
                const doctorName = app.doctorName || 'Doctor';
                const specialty = app.doctorSpecialty || 'General Specialist';
                const startTime = app.appointmentStart;
                const meetingLink = app.meetingLink;
                const amountPaid = app.amountPaid !== undefined ? Number(app.amountPaid).toFixed(2) : '0.00';
                const status = app.status || 'SCHEDULED';
                
                // Add "Dr. " if not present
                const doctorDisplay = doctorName.trim().toLowerCase().startsWith('dr') 
                  ? doctorName.trim() 
                  : `Dr. ${doctorName.trim()}`;
                
                return (
                  <tr key={app.id}>
                    <td>
                      <strong>{doctorDisplay}</strong><br/>
                      <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                        {specialty}
                      </span>
                    </td>
                    <td>
                      {startTime 
                        ? format(new Date(startTime), 'MMM dd, yyyy - hh:mm a')
                        : 'N/A'}
                    </td>
                    <td>
                      {meetingLink ? (
                        <a href={meetingLink} target="_blank" rel="noreferrer" style={{ color: 'var(--color-primary)' }}>
                          Join Meet
                        </a>
                      ) : (
                        <span style={{ color: 'var(--text-muted)' }}>In-Clinic</span>
                      )}
                    </td>
                    <td>
                      <span className={`badge ${
                        status === 'COMPLETED' 
                          ? 'badge-success' 
                          : status === 'CANCELLED' 
                          ? 'badge-danger' 
                          : 'badge-warning'
                      }`}>
                        {status}
                      </span>
                    </td>
                    <td>${amountPaid}</td>
                    <td>
                      {status === 'CONFIRMED' || status === 'SCHEDULED' ? (
                        <button 
                          className="btn btn-outline" 
                          style={{ color: '#f87171', border: '1px solid #f87171', padding: '6px 12px' }} 
                          onClick={() => handleCancel(app.id)}
                        >
                          Cancel
                        </button>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>No Actions</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default PatientHistory;
