import React, { useState, useEffect } from 'react';
import api from '../../utils/api';
import { format } from 'date-fns';

const DoctorHistory = () => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchHistory = async () => {
    try {
      setError('');
      setLoading(true);
      const res = await api.get('/doctor/appointments');
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

  const handleUpdateStatus = async (appId, newStatus) => {
    if (!window.confirm(`Update status to ${newStatus}?`)) return;
    try {
      await api.patch(`/doctor/appointments/${appId}/status?status=${newStatus}`);
      fetchHistory();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update status');
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading records...</div>;

  return (
    <div className="glass-panel">
      <h2 style={{ marginBottom: '24px' }}>Appointment Audit History</h2>
      
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
        <p style={{ color: 'var(--text-muted)' }}>No historical appointments found.</p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Patient Name</th>
                <th>Time & Date</th>
                <th>Status</th>
                <th>Revenue</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appointments.sort((a, b) => {
                const timeA = a?.slot?.startTime;
                const timeB = b?.slot?.startTime;
                if (!timeA || !timeB) return 0;
                return new Date(timeB) - new Date(timeA);
              }).map(app => {
                if (!app || !app.id) return null;
                
                const patientName = app.patient?.name || 'Unknown Patient';
                const startTime = app.slot?.startTime;
                const status = app.status || 'SCHEDULED';
                const revenue = app.amountPaid !== undefined ? Number(app.amountPaid).toFixed(2) : '0.00';
                
                return (
                  <tr key={app.id}>
                    <td>{patientName}</td>
                    <td>
                      {startTime 
                        ? format(new Date(startTime), 'MMM dd, yyyy - hh:mm a')
                        : 'N/A'}
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
                    <td>${revenue}</td>
                    <td>
                      {status === 'CONFIRMED' && (
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button 
                            className="btn btn-primary" 
                            onClick={() => handleUpdateStatus(app.id, 'COMPLETED')}
                            style={{ padding: '6px 12px', fontSize: '13px' }}
                          >
                            Completed
                          </button>
                          <button 
                            className="btn btn-outline" 
                            style={{ color: '#f87171', border: '1px solid #f87171', padding: '6px 12px', fontSize: '13px' }} 
                            onClick={() => handleUpdateStatus(app.id, 'NO_SHOW')}
                          >
                            No Show
                          </button>
                        </div>
                      )}
                      {status !== 'CONFIRMED' && (
                        <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                          No pending actions
                        </span>
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

export default DoctorHistory;
