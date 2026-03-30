import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Calendar from 'react-calendar';
import { format } from 'date-fns';
import api from '../../utils/api';
import PaymentSuccessModal from '../../components/PaymentSuccessModal';
import 'react-calendar/dist/Calendar.css';

const BookingCalendar = () => {
  const { doctorId } = useParams();
  const navigate = useNavigate();
  
  const [doctor, setDoctor] = useState(null);
  const [slots, setSlots] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [error, setError] = useState('');
  
  const [modalOpen, setModalOpen] = useState(false);
  const [bookingAmount, setBookingAmount] = useState(0);

  useEffect(() => {
    const init = async () => {
      try {
        setError('');
        const docRes = await api.get('/patient/doctors');
        if (!Array.isArray(docRes.data)) {
          setError('Invalid doctor data received');
          return;
        }
        
        const doc = docRes.data.find(d => d && d.id === parseInt(doctorId));
        if (!doc) {
          setError('Doctor not found');
          return;
        }
        setDoctor(doc);

        const slotRes = await api.get(`/patient/doctors/${doctorId}/slots`);
        setSlots(Array.isArray(slotRes.data) ? slotRes.data : []);
      } catch (err) {
        console.error(err);
        setError(err.response?.data?.message || 'Failed to load booking information');
      }
    };
    init();
  }, [doctorId]);

  // Validation function
  const isSlotValid = (slot) => {
    if (!slot || !slot.startTime || !slot.endTime) return false;
    
    // Check if already booked
    if (slot.booked || slot.isBooked) return false;
    
    // Check if slot is in the past
    const slotStart = new Date(slot.startTime);
    const now = new Date();
    if (slotStart < now) return false;
    
    // Check if slot is during lunch time (1 PM - 2 PM)
    const slotHour = slotStart.getHours();
    if (slotHour >= 13 && slotHour < 14) return false;
    
    return true;
  };

  const handleBook = async (slotId) => {
    try {
      if (!window.confirm('Confirm booking and proceed to payment?')) return;
      
      const res = await api.post(`/patient/appointments/book`, {
        slotId: slotId,
        amountPaid: doctor?.fees || 0
      });
      
      if (!res.data) {
        throw new Error('Invalid response from server');
      }
      
      setBookingAmount(res.data.amountPaid || 0);
      setModalOpen(true);
      
      // Refresh slots to reflect booking
      const slotRes = await api.get(`/patient/doctors/${doctorId}/slots`);
      setSlots(Array.isArray(slotRes.data) ? slotRes.data : []);

    } catch (err) {
      console.error(err);
      const errorMsg = err.response?.data?.message || 'Error booking slot. It might have been booked by someone else!';
      setError(errorMsg);
      
      // Refetch slots immediately
      try {
        const slotRes = await api.get(`/patient/doctors/${doctorId}/slots`);
        setSlots(Array.isArray(slotRes.data) ? slotRes.data : []);
      } catch (refetchErr) {
        console.error('Failed to refresh slots', refetchErr);
      }
    }
  };

  const closeModal = () => {
    setModalOpen(false);
    navigate('/patient/history');
  };

  if (error && !doctor) {
    return (
      <div style={{ padding: '40px', textAlign: 'center' }}>
        <div style={{
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          color: '#ef4444',
          padding: '20px',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          {error}
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/patient/browse')}>
          Back to Doctors
        </button>
      </div>
    );
  }

  if (!doctor) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading booking system...</div>;

  const doctorName = doctor.name || (doctor.user?.name) || 'Doctor';
  const selectedDateString = format(selectedDate, 'yyyy-MM-dd');
  const daySlots = slots.filter(s => s && s.startTime && s.startTime.startsWith(selectedDateString));
  const availableSlots = daySlots.filter(isSlotValid);

  const doctorDisplay = doctorName.trim().toLowerCase().startsWith('dr') ? doctorName.trim() : `Dr. ${doctorName.trim()}`;

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
        <button className="btn btn-outline" onClick={() => navigate('/patient/browse')}>← Back</button>
        <h2 style={{ margin: 0 }}>Book Appointment with {doctorDisplay}</h2>
      </div>

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

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 2fr)', gap: '24px' }}>
        <div className="glass-panel">
          <h3 style={{ marginBottom: '16px' }}>Select Date</h3>
          <div style={{ background: 'white', color: 'black', borderRadius: '8px', padding: '10px' }}>
            <Calendar 
              onChange={setSelectedDate} 
              value={selectedDate} 
              minDate={new Date()}
            />
          </div>
        </div>

        <div className="glass-panel">
          <h3 style={{ marginBottom: '16px' }}>Available Timings for {format(selectedDate, 'MMM do, yyyy')}</h3>
          
          {availableSlots.length === 0 ? (
            <div>
              <p style={{ color: 'var(--text-muted)', marginBottom: '12px' }}>
                {daySlots.length === 0 
                  ? 'No slots scheduled for this date. Please select another date.'
                  : 'All slots are booked or during lunch time. Please select another date or time.'}
              </p>
              <small style={{ color: 'var(--text-muted)' }}>
                Doctor working hours: 8:00 AM - 5:00 PM | Lunch Break: 1:00 PM - 2:00 PM
              </small>
            </div>
          ) : (
            <div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))', gap: '16px', marginBottom: '20px' }}>
                {availableSlots.sort((a,b) => new Date(a.startTime) - new Date(b.startTime)).map(slot => {
                  if (!slot || !slot.startTime) return null;
                  
                  return (
                    <div 
                      key={slot.id} 
                      style={{ 
                        padding: '16px', 
                        border: '1px solid var(--color-primary)', 
                        borderRadius: '8px',
                        backgroundColor: 'rgba(59, 130, 246, 0.1)',
                        textAlign: 'center',
                        cursor: 'pointer',
                        transition: 'background 0.2s'
                      }} 
                      onMouseEnter={e => e.currentTarget.style.backgroundColor = 'rgba(59, 130, 246, 0.3)'}
                      onMouseLeave={e => e.currentTarget.style.backgroundColor = 'rgba(59, 130, 246, 0.1)'}
                      onClick={() => handleBook(slot.id)}
                    >
                      <p style={{ margin: '0 0 8px 0', fontWeight: '600' }}>
                        {format(new Date(slot.startTime), 'hh:mm a')}
                      </p>
                      <p style={{ margin: 0, fontSize: '12px', color: 'var(--color-primary)' }}>Book Now</p>
                    </div>
                  );
                })}
              </div>
              <small style={{ color: 'var(--text-muted)' }}>
                Consultation Fee: ${doctor.fees ? Number(doctor.fees).toFixed(2) : '0.00'}
              </small>
            </div>
          )}
        </div>
      </div>

      <PaymentSuccessModal 
        isOpen={modalOpen} 
        onClose={closeModal} 
        amount={bookingAmount} 
        mode={doctor.mode || 'ONLINE'} 
      />
    </div>
  );
};

export default BookingCalendar;
