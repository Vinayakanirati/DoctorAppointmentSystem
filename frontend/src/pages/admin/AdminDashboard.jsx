import React, { useEffect, useState } from 'react';
import api from '../../utils/api';
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement
} from 'chart.js';
import { Bar, Pie } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement);

const AdminDashboard = () => {
  const [analytics, setAnalytics] = useState(null);
  const [pendingDoctors, setPendingDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [verifyingId, setVerifyingId] = useState(null);
  const [verifyError, setVerifyError] = useState(null);

  const fetchData = async () => {
    try {
      setError(null);
      setLoading(true);
      
      // Fetch analytics and pending doctors in parallel
      const [resAnalytics, resDoctors] = await Promise.all([
        api.get('/admin/analytics'),
        api.get('/admin/doctors/pending')
      ]);
      
      // Validate API responses
      if (!resAnalytics.data) {
        throw new Error('Failed to fetch analytics data');
      }
      
      if (!Array.isArray(resDoctors.data)) {
        throw new Error('Invalid pending doctors data format');
      }
      
      setAnalytics(resAnalytics.data);
      setPendingDoctors(resDoctors.data);
    } catch (err) {
      console.error('Error fetching admin data:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Failed to load admin dashboard data';
      setError(errorMessage);
      setAnalytics(null);
      setPendingDoctors([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleVerify = async (doctorId, doctorName) => {
    try {
      setVerifyError(null);
      setVerifyingId(doctorId);
      
      // Validate input
      if (!doctorId || doctorId <= 0) {
        throw new Error('Invalid doctor profile ID');
      }
      
      const response = await api.patch(`/admin/doctors/${doctorId}/verify`);
      
      if (response.status === 200 || response.status === 204) {
        console.log(`Successfully verified doctor: ${doctorName}`);
        // Refresh data after successful verification
        await fetchData();
      } else {
        throw new Error('Unexpected response from server');
      }
    } catch (err) {
      console.error(`Error verifying doctor ${doctorId}:`, err);
      
      let errorMsg = 'Failed to verify doctor';
      if (err.response?.status === 409) {
        errorMsg = 'Doctor profile is already verified';
      } else if (err.response?.status === 404) {
        errorMsg = 'Doctor profile not found';
      } else if (err.response?.status === 400) {
        errorMsg = 'Invalid doctor profile ID';
      } else if (err.response?.data?.error) {
        errorMsg = err.response.data.error;
      }
      
      setVerifyError(errorMsg);
      setTimeout(() => setVerifyError(null), 5000); // Clear error after 5 seconds
    } finally {
      setVerifyingId(null);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '40px' }}>
        <div>Loading Admin Dashboard...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ padding: '20px' }}>
        <div className="error-message" style={{
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          border: '1px solid rgba(239, 68, 68, 0.5)',
          color: 'rgb(239, 68, 68)',
          padding: '12px',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          <strong>Error:</strong> {error}
        </div>
        <button className="btn btn-primary" onClick={fetchData}>
          Retry
        </button>
      </div>
    );
  }

  if (!analytics) {
    return (
      <div style={{ padding: '20px' }}>
        <div className="warning-message" style={{
          backgroundColor: 'rgba(245, 158, 11, 0.1)',
          border: '1px solid rgba(245, 158, 11, 0.5)',
          color: 'rgb(245, 158, 11)',
          padding: '12px',
          borderRadius: '8px'
        }}>
          No analytics data available
        </div>
      </div>
    );
  }

  const revenueData = {
    labels: Object.keys(analytics?.revenueByDoctor || {}),
    datasets: [{
      label: 'Revenue ($)',
      data: Object.values(analytics?.revenueByDoctor || {}),
      backgroundColor: 'rgba(59, 130, 246, 0.8)',
    }]
  };

  const statusData = {
    labels: Object.keys(analytics?.appointmentsByStatus || {}),
    datasets: [{
      label: 'Appointments',
      data: Object.values(analytics?.appointmentsByStatus || {}),
      backgroundColor: [
        'rgba(16, 185, 129, 0.8)', // COMPLETED (green)
        'rgba(245, 158, 11, 0.8)', // PENDING (yellow)
        'rgba(59, 130, 246, 0.8)', // CONFIRMED (blue)
        'rgba(239, 68, 68, 0.8)'   // CANCELLED (red)
      ],
      borderWidth: 0
    }]
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { labels: { color: '#f8fafc' } }
    },
    scales: {
      x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(51, 65, 85, 0.3)' } },
      y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(51, 65, 85, 0.3)' } }
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: '24px' }}>Admin Overview</h2>
      
      {/* Error notification for verification failures */}
      {verifyError && (
        <div className="error-message" style={{
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          border: '1px solid rgba(239, 68, 68, 0.5)',
          color: 'rgb(239, 68, 68)',
          padding: '12px',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          {verifyError}
        </div>
      )}
      
      <div className="dashboard-grid">
        <div className="glass-panel stat-card">
          <span className="stat-title">Total Revenue</span>
          <span className="stat-value" style={{ color: 'var(--color-primary)' }}>
            ${analytics?.totalRevenue ? analytics.totalRevenue.toFixed(2) : '0.00'}
          </span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Total Appointments</span>
          <span className="stat-value">{analytics?.totalAppointments || 0}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Registered Doctors</span>
          <span className="stat-value">{analytics?.totalDoctors || 0}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Registered Patients</span>
          <span className="stat-value">{analytics?.totalPatients || 0}</span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) minmax(0, 1fr)', gap: '24px', marginBottom: '32px' }}>
        <div className="glass-panel">
          <h3 style={{ marginBottom: '16px' }}>Revenue by Doctor</h3>
          {Object.keys(analytics?.revenueByDoctor || {}).length > 0 ? (
            <Bar data={revenueData} options={chartOptions} />
          ) : (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '20px' }}>
              No revenue data available yet
            </p>
          )}
        </div>
        <div className="glass-panel" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <h3 style={{ marginBottom: '16px', alignSelf: 'flex-start' }}>Appointments Breakdown</h3>
          {Object.keys(analytics?.appointmentsByStatus || {}).length > 0 ? (
            <div style={{ width: '80%', margin: 'auto' }}>
              <Pie data={statusData} options={{ plugins: { legend: { position: 'bottom', labels: { color: '#f8fafc' } } } }} />
            </div>
          ) : (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '20px' }}>
              No appointment data available
            </p>
          )}
        </div>
      </div>

      <div className="glass-panel">
        <h3 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          Pending Doctor Verifications
          {pendingDoctors && pendingDoctors.length > 0 && (
            <span className="badge badge-warning">{pendingDoctors.length} Action Needed</span>
          )}
        </h3>
        
        {(!pendingDoctors || pendingDoctors.length === 0) ? (
          <p style={{ color: 'var(--text-muted)' }}>✓ No pending doctor profiles to verify.</p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Specialty</th>
                  <th>Mode</th>
                  <th>Fees</th>
                  <th>Registered</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {pendingDoctors.map(doctor => (
                  <tr key={doctor.id}>
                    <td>
                      <strong>{doctor.name || 'N/A'}</strong>
                    </td>
                    <td>{doctor.email || 'N/A'}</td>
                    <td>{doctor.specialty || 'N/A'}</td>
                    <td>
                      <span style={{
                        backgroundColor: doctor.mode === 'ONLINE' ? 'rgba(59, 130, 246, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '0.85rem'
                      }}>
                        {doctor.mode || 'N/A'}
                      </span>
                    </td>
                    <td>${doctor.fees || '0.00'}</td>
                    <td>{doctor.registrationTimeAgo || 'Unknown'}</td>
                    <td>
                      <button 
                        className="btn btn-primary"
                        onClick={() => handleVerify(doctor.id, doctor.name)}
                        disabled={verifyingId === doctor.id || !doctor.id}
                        style={{
                          opacity: verifyingId === doctor.id ? 0.6 : 1,
                          cursor: verifyingId === doctor.id ? 'not-allowed' : 'pointer'
                        }}
                      >
                        {verifyingId === doctor.id ? 'Verifying...' : 'Verify Profile'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
