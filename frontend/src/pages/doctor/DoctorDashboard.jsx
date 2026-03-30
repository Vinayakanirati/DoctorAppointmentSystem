import React, { useState, useEffect } from 'react';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import api from '../../utils/api';
import { format } from 'date-fns';
import { Activity } from 'lucide-react';

const DoctorDashboard = () => {
  const [stats, setStats] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [slots, setSlots] = useState([]);
  const [startTime, setStartTime] = useState('08:00');
  const [endTime, setEndTime] = useState('17:00');
  const [error, setError] = useState('');

  const fetchDashboard = async () => {
    try {
      const res = await api.get('/doctor/dashboard');
      setStats(res.data);
    } catch (err) {
      console.error('Failed to fetch stats');
    }
  };

  const fetchSlots = async () => {
    try {
      const res = await api.get('/doctor/slots');
      setSlots(res.data || []);
    } catch (err) {
      console.error('Failed to fetch slots');
    }
  };

  useEffect(() => {
    fetchDashboard();
    fetchSlots();
  }, []);

  // Validation function for slot times
  const validateSlotTimes = (start, end, date) => {
    if (!start || !end) {
      setError('Please select both start and end times');
      return false;
    }

    const [startHour, startMin] = start.split(':').map(Number);
    const [endHour, endMin] = end.split(':').map(Number);

    // Check if start time is before end time
    if (startHour > endHour || (startHour === endHour && startMin >= endMin)) {
      setError('Start time must be before end time');
      return false;
    }

    // Check working hours (8 AM - 5 PM)
    if (startHour < 8 || endHour > 17) {
      setError('Slots must be between 8:00 AM and 5:00 PM');
      return false;
    }

    // Check lunch break (1 PM - 2 PM)
    if ((startHour === 13 && startMin < 0) || (startHour < 14 && endHour > 13) || 
        (startHour === 13) || (endHour === 13 && endMin > 0)) {
      setError('Slots cannot include lunch time (1:00 PM - 2:00 PM)');
      return false;
    }

    // Check if date is not in the past
    const slotDate = new Date(date);
    slotDate.setHours(0, 0, 0, 0);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (slotDate < today) {
      setError('Cannot create slots for past dates');
      return false;
    }

    return true;
  };

  const handleCreateSlot = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!validateSlotTimes(startTime, endTime, selectedDate)) {
      return;
    }
    
    try {
      const year = selectedDate.getFullYear();
      const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
      const day = String(selectedDate.getDate()).padStart(2, '0');
      
      const startISO = `${year}-${month}-${day}T${startTime}:00`;
      const endISO = `${year}-${month}-${day}T${endTime}:00`;

      await api.post('/doctor/slots', {
        startTime: startISO,
        endTime: endISO
      });
      
      setStartTime('08:00');
      setEndTime('17:00');
      setError('');
      fetchSlots();
      alert('Slot created successfully');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create slot');
    }
  };

  const handleDeleteSlot = async (id) => {
    if (!window.confirm('Are you sure?')) return;
    try {
      await api.delete(`/doctor/slots/${id}`);
      fetchSlots();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete slot');
    }
  };

  // Filter slots for selected date
  const selectedDateString = format(selectedDate, 'yyyy-MM-dd');
  const daySlots = slots.filter(s => s.startTime && s.startTime.startsWith(selectedDateString));

  return (
    <div>
      <h2 style={{ marginBottom: '24px' }}>Doctor Dashboard</h2>
      
      <div className="dashboard-grid">
        <div className="glass-panel stat-card">
          <span className="stat-title">Earnings Today</span>
          <span className="stat-value" style={{ color: 'var(--color-primary)' }}>${((stats?.todayEarnings) !== undefined ? Number(stats.todayEarnings).toFixed(2) : '0.00')}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Total Revenue</span>
          <span className="stat-value" style={{ color: 'var(--color-success)' }}>${((stats?.totalEarnings) !== undefined ? Number(stats.totalEarnings).toFixed(2) : '0.00')}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Appointments Today</span>
          <span className="stat-value">{stats?.totalAppointmentsToday ?? 0}</span>
        </div>
        <div className="glass-panel stat-card">
          <span className="stat-title">Pending</span>
          <span className="stat-value">{stats?.pendingAppointments ?? 0}</span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 2fr)', gap: '24px' }}>
        <div className="glass-panel">
          <h3 style={{ marginBottom: '16px' }}>Calendar</h3>
          <div style={{ background: 'white', color: 'black', borderRadius: '8px', padding: '10px' }}>
            <Calendar 
              onChange={setSelectedDate} 
              value={selectedDate} 
              minDate={new Date()}
              className="w-100"
            />
          </div>
          <div style={{ marginTop: '20px' }}>
            <h4>Availability Settings</h4>
            <div style={{ 
              backgroundColor: 'rgba(14, 165, 233, 0.05)', 
              color: 'var(--color-primary)', 
              padding: '16px', 
              borderRadius: '12px',
              border: '1px solid var(--panel-border)',
              marginTop: '12px',
              fontSize: '14px'
            }}>
              <Activity size={20} style={{ marginBottom: '8px' }} />
              <p><b>Automated Scheduling:</b> Slots are automatically generated for your working hours (8:00 AM - 5:00 PM) excluding lunch (1:00 PM - 2:00 PM).</p>
            </div>
          </div>
        </div>

        <div className="glass-panel">
          <h3 style={{ marginBottom: '16px' }}>Slots on {format(selectedDate, 'MMM do, yyyy')}</h3>
          {daySlots.length === 0 ? (
            <p style={{ color: 'var(--text-muted)' }}>No slots scheduled for this day.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {daySlots.sort((a,b) => new Date(a.startTime) - new Date(b.startTime)).map(slot => {
                  if (!slot || !slot.startTime || !slot.endTime) return null;
                  const sTime = format(new Date(slot.startTime), 'hh:mm a');
                  const eTime = format(new Date(slot.endTime), 'hh:mm a');
                  const isBooked = slot.booked || slot.isBooked;
                  
                  return (
                    <tr key={slot.id}>
                      <td>{sTime} - {eTime}</td>
                      <td>
                        {isBooked ? (
                          <span className="badge badge-success">Booked</span>
                        ) : (
                          <span className="badge badge-warning">Available</span>
                        )}
                      </td>
                      <td>
                        <button 
                          className="btn btn-outline" 
                          style={{ color: '#f87171', border: '1px solid #f87171' }}
                          onClick={() => handleDeleteSlot(slot.id)}
                          disabled={isBooked}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};

export default DoctorDashboard;
