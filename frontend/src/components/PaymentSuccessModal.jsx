import React from 'react';

const PaymentSuccessModal = ({ isOpen, onClose, amount, mode }) => {
  if (!isOpen) return null;

  const safeAmount = typeof amount === 'number' && amount >= 0 ? amount : 0;
  const safeMode = mode === 'OFFLINE' ? 'OFFLINE' : 'ONLINE';

  return (
    <div style={{
      position: 'fixed',
      top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.7)',
      backdropFilter: 'blur(5px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000
    }}>
      <div className="glass-panel" style={{ textAlign: 'center', maxWidth: '400px', padding: '40px' }}>
        <div style={{
          width: '60px', height: '60px',
          backgroundColor: 'rgba(16, 185, 129, 0.2)',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 20px auto'
        }}>
          <span style={{ fontSize: '30px', color: '#10b981' }}>✓</span>
        </div>
        
        <h2 style={{ marginBottom: '10px' }}>Payment Successful!</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '24px' }}>
          ${safeAmount.toFixed(2)} has been securely processed.
        </p>
        
        <div style={{ backgroundColor: 'rgba(0,0,0,0.3)', padding: '16px', borderRadius: '8px', marginBottom: '24px', fontSize: '14px' }}>
          {safeMode === 'ONLINE' ? (
            <p>You will receive an email shortly containing your Google Meet link and instructions.</p>
          ) : (
            <p>You will receive an email shortly with the clinic address. Please remember to wear a mask and arrive 15 mins early!</p>
          )}
        </div>
        
        <button className="btn btn-primary" onClick={onClose} style={{ width: '100%' }}>
          Back to Dashboard
        </button>
      </div>
    </div>
  );
};

export default PaymentSuccessModal;
