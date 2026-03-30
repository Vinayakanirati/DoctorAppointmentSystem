import React, { useEffect, useState } from 'react';
import api from '../../utils/api';
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement
} from 'chart.js';
import { Bar, Pie } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement);

const AdminDashboard = () => {
  const [allDoctors, setAllDoctors] = useState([]);
  const [allPatients, setAllPatients] = useState([]);
  const [allAppointments, setAllAppointments] = useState([]);
  const [pendingDoctors, setPendingDoctors] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [verifyingId, setVerifyingId] = useState(null);
  const [verifyError, setVerifyError] = useState(null);
  const [activeTab, setActiveTab] = useState('pending'); // pending, doctors, patients, appointments

  const fetchData = async () => {
    try {
      setError(null);
      setLoading(true);
      
      const [resAnalytics, resPending, resDoctors, resPatients, resAppointments] = await Promise.all([
        api.get('/admin/analytics'),
        api.get('/admin/doctors/pending'),
        api.get('/admin/doctors'),
        api.get('/admin/patients'),
        api.get('/admin/appointments')
      ]);
      
      setAnalytics(resAnalytics.data);
      setPendingDoctors(resPending.data);
      setAllDoctors(resDoctors.data);
      setAllPatients(resPatients.data);
      setAllAppointments(resAppointments.data);
    } catch (err) {
      console.error('Error fetching admin data:', err);
      setError('Failed to load admin dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleDeleteUser = async (userId, name) => {
    if (!window.confirm(`Are you sure you want to delete ${name}? This action cannot be undone.`)) return;
    try {
      await api.delete(`/admin/users/${userId}`);
      fetchData();
    } catch (err) {
      alert('Failed to delete user');
    }
  };

  const handleDeleteAppointment = async (id) => {
    if (!window.confirm('Delete this appointment?')) return;
    try {
      await api.delete(`/admin/appointments/${id}`);
      fetchData();
    } catch (err) {
      alert('Failed to delete appointment');
    }
  };

  const handleVerify = async (doctorId, doctorName) => {
    try {
      setVerifyError(null);
      setVerifyingId(doctorId);
      await api.patch(`/admin/doctors/${doctorId}/verify`);
      await fetchData();
    } catch (err) {
      setVerifyError(err.response?.data?.error || 'Failed to verify doctor');
      setTimeout(() => setVerifyError(null), 5000);
    } finally {
      setVerifyingId(null);
    }
  };

  if (loading) return <div className="main-content">Accessing Secure Admin Vault...</div>;

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { labels: { color: 'var(--text-main)' } }
    },
    scales: {
      x: { ticks: { color: 'var(--text-muted)' }, grid: { color: 'rgba(203, 213, 225, 0.2)' } },
      y: { ticks: { color: 'var(--text-muted)' }, grid: { color: 'rgba(203, 213, 225, 0.2)' } }
    }
  };

  const statusData = {
    labels: Object.keys(analytics?.appointmentsByStatus || {}),
    datasets: [{
      data: Object.values(analytics?.appointmentsByStatus || {}),
      backgroundColor: ['#10b981', '#f59e0b', '#3b82f6', '#ef4444', '#6366f1'],
    }]
  };

  return (
    <div>
      <h2 style={{ marginBottom: '24px' }}>Admin Management Center</h2>
      
      <div className="dashboard-grid">
        <div className="glass-panel stat-card">
          <span className="stat-title">Total Revenue</span>
          <span className="stat-value" style={{ color: 'var(--color-primary)' }}>${analytics?.totalRevenue?.toFixed(2) || '0.00'}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Appointments</span>
          <span className="stat-value">{analytics?.totalAppointments || 0}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Registered Doctors</span>
          <span className="stat-value">{analytics?.totalDoctors || 0}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Verified Patients</span>
          <span className="stat-value">{analytics?.totalPatients || 0}</span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) minmax(0, 1fr)', gap: '24px', marginBottom: '32px' }}>
        <div className="glass-panel">
          <h3 style={{ marginBottom: '16px' }}>Revenue Distribution</h3>
          <Bar data={{
            labels: Object.keys(analytics?.revenueByDoctor || {}),
            datasets: [{ label: 'Revenue ($)', data: Object.values(analytics?.revenueByDoctor || {}), backgroundColor: 'var(--color-primary)' }]
          }} options={chartOptions} />
        </div>
        <div className="glass-panel">
          <h3 style={{ marginBottom: '16px' }}>Appointment Status</h3>
          <div style={{ padding: '20px' }}>
            <Pie data={statusData} options={{ plugins: { legend: { position: 'bottom', labels: { color: 'var(--text-main)' } } } }} />
          </div>
        </div>
      </div>

      <div className="glass-panel">
        <div style={{ display: 'flex', gap: '12px', marginBottom: '24px', borderBottom: '1px solid var(--panel-border)', paddingBottom: '16px' }}>
          <button className={`btn ${activeTab === 'pending' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('pending')}>
            Pending Verifications ({pendingDoctors.length})
          </button>
          <button className={`btn ${activeTab === 'doctors' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('doctors')}>
            Manage Doctors
          </button>
          <button className={`btn ${activeTab === 'patients' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('patients')}>
            Manage Patients
          </button>
          <button className={`btn ${activeTab === 'appointments' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('appointments')}>
            All Appointments
          </button>
        </div>

        {activeTab === 'pending' && (
          <div style={{ overflowX: 'auto' }}>
            <table className="data-table">
              <thead><tr><th>Doctor</th><th>Email</th><th>Specialty</th><th>Action</th></tr></thead>
              <tbody>
                {pendingDoctors.map(doc => (
                  <tr key={doc.id}>
                    <td><b>{doc.name}</b></td>
                    <td>{doc.email}</td>
                    <td><span className="badge badge-info">{doc.specialty}</span></td>
                    <td>
                      <button className="btn btn-primary" onClick={() => handleVerify(doc.id, doc.name)} disabled={verifyingId === doc.id}>
                        {verifyingId === doc.id ? 'Verifying...' : 'Verify Now'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'doctors' && (
          <div style={{ overflowX: 'auto' }}>
            <table className="data-table">
              <thead><tr><th>Name</th><th>Email</th><th>Specialty</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {allDoctors.map(doc => (
                  <tr key={doc.id}>
                    <td><b>{doc.name}</b></td>
                    <td>{doc.email}</td>
                    <td>{doc.specialty}</td>
                    <td>{doc.verified ? <span className="badge badge-success">Verified</span> : <span className="badge badge-warning">Pending</span>}</td>
                    <td>
                      <button className="btn btn-outline" style={{ color: 'var(--color-danger)', borderColor: 'var(--color-danger)' }} 
                        onClick={() => handleDeleteUser(doc.id, doc.name)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'patients' && (
          <div style={{ overflowX: 'auto' }}>
            <table className="data-table">
              <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Actions</th></tr></thead>
              <tbody>
                {allPatients.map(p => (
                  <tr key={p.id}>
                    <td><b>{p.name}</b></td>
                    <td>{p.email}</td>
                    <td>{p.phone}</td>
                    <td>
                      <button className="btn btn-outline" style={{ color: 'var(--color-danger)', borderColor: 'var(--color-danger)' }} 
                        onClick={() => handleDeleteUser(p.id, p.name)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'appointments' && (
          <div style={{ overflowX: 'auto' }}>
            <table className="data-table">
              <thead><tr><th>Patient</th><th>Doctor</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                {allAppointments.map(a => (
                  <tr key={a.id}>
                    <td>{a.patientName}</td>
                    <td>{a.doctorName}</td>
                    <td>{new Date(a.appointmentStart).toLocaleDateString()}</td>
                    <td><span className="badge badge-info">{a.status}</span></td>
                    <td>
                      <button className="btn btn-outline" style={{ color: 'var(--color-danger)', borderColor: 'var(--color-danger)' }} 
                        onClick={() => handleDeleteAppointment(a.id)}>Cancel/Remove</button>
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
