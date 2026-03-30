import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate, Link } from 'react-router-dom';
import BookingCalendar from './BookingCalendar';
import PatientHistory from './PatientHistory';
import api from '../../utils/api';

const BrowseDoctors = () => {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [specialtyFilter, setSpecialtyFilter] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        const res = await api.get('/patient/doctors');
        setDoctors(res.data);
      } catch (err) {
        console.error('Failed to load doctors');
      } finally {
        setLoading(false);
      }
    };
    fetchDoctors();
  }, []);

  const filteredDoctors = doctors.filter(doc => {
    if (!doc || !doc.specialty) return false;
    return doc.specialty.toLowerCase().includes(specialtyFilter.toLowerCase());
  });

  if (loading) return <div>Locating specialists...</div>;

  return (
    <div>
      <h2 style={{ marginBottom: '24px' }}>Find a Doctor</h2>
      
      <div className="form-group" style={{ maxWidth: '400px', marginBottom: '32px' }}>
        <input 
          type="text" 
          className="form-control" 
          placeholder="Filter by Specialty (e.g. Cardiology)"
          value={specialtyFilter}
          onChange={e => setSpecialtyFilter(e.target.value)}
        />
      </div>

      <div className="dashboard-grid">
        {filteredDoctors.map(doc => {
          if (!doc || !doc.id) return null;
          const doctorName = (doc.name || (doc.user?.name) || 'Unknown Doctor').trim();
          const specialty = doc.specialty || 'N/A';
          const fees = doc.fees || 0;
          const mode = doc.mode || 'ONLINE';
          
          const doctorDisplay = doctorName.toLowerCase().startsWith('dr') ? doctorName : `Dr. ${doctorName}`;
          
          return (
            <div key={doc.id} className="glass-panel" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <h3 style={{ margin: 0 }}>{doctorDisplay}</h3>
                <span className={mode === 'ONLINE' ? 'badge badge-success' : 'badge badge-warning'}>
                  {mode}
                </span>
              </div>
              
              <p style={{ color: 'var(--text-muted)', margin: 0 }}>{specialty}</p>
              <p style={{ fontSize: '18px', fontWeight: 'bold', margin: '8px 0' }}>${Number(fees).toFixed(2)}</p>
              
              <button 
                className="btn btn-primary" 
                style={{ marginTop: 'auto' }}
                onClick={() => navigate(`/patient/book/${doc.id}`)}
              >
                View Slots & Book
              </button>
            </div>
          );
        })}
        {filteredDoctors.length === 0 && (
          <p style={{ gridColumn: '1 / -1', textAlign: 'center', color: 'var(--text-muted)' }}>
            No verified doctors found matching your criteria.
          </p>
        )}
      </div>
    </div>
  );
};

export const BrowseDoctorsPage = BrowseDoctors;

export default BrowseDoctors;
