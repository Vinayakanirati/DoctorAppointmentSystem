import React, { useState, useEffect } from 'react';
import api from '../utils/api';
import { User, Phone, Mail, Award, DollarSign, Activity, Save } from 'lucide-react';

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const fetchProfile = async () => {
    try {
      const response = await api.get('/profile');
      setProfile(response.data);
    } catch (error) {
      console.error('Error fetching profile:', error);
      setMessage({ type: 'danger', text: 'Failed to load profile' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleUpdate = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage({ type: '', text: '' });
    try {
      const response = await api.put('/profile', profile);
      setProfile(response.data);
      setMessage({ type: 'success', text: 'Profile updated successfully!' });
      
      // Update local storage user name if it changed
      const localUser = JSON.parse(localStorage.getItem('user'));
      if (localUser) {
        localUser.name = response.data.name;
        localStorage.setItem('user', JSON.stringify(localUser));
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      setMessage({ type: 'danger', text: 'Failed to update profile' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="main-content">Loading...</div>;

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div className="glass-panel" style={{ position: 'relative' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '32px' }}>
          <div style={{ 
            width: '80px', 
            height: '80px', 
            borderRadius: '20px', 
            background: 'linear-gradient(135deg, var(--color-primary), var(--color-primary-dark))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white'
          }}>
            <User size={40} />
          </div>
          <div>
            <h1>{profile.role} Profile</h1>
            <p className="text-muted">Manage your personal information and settings</p>
          </div>
          {profile.isVerified && (
            <div className="badge badge-success" style={{ marginLeft: 'auto' }}>
              <Activity size={14} /> Verified Professional
            </div>
          )}
        </div>

        {message.text && (
          <div className={`badge badge-${message.type}`} style={{ display: 'block', width: '100%', padding: '16px', marginBottom: '24px' }}>
            {message.text}
          </div>
        )}

        <form onSubmit={handleUpdate}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
            <div className="form-group">
              <label><User size={14} style={{ verticalAlign: 'middle', marginRight: '6px' }} /> Full Name</label>
              <input 
                type="text" 
                className="form-control" 
                value={profile.name || ''} 
                onChange={e => setProfile({...profile, name: e.target.value})}
                required
              />
            </div>
            <div className="form-group">
              <label><Mail size={14} style={{ verticalAlign: 'middle', marginRight: '6px' }} /> Email Address</label>
              <input 
                type="email" 
                className="form-control" 
                value={profile.email || ''} 
                disabled 
                style={{ opacity: 0.7, cursor: 'not-allowed' }}
              />
            </div>
            <div className="form-group">
              <label><Phone size={14} style={{ verticalAlign: 'middle', marginRight: '6px' }} /> Phone Number</label>
              <input 
                type="text" 
                className="form-control" 
                value={profile.phone || ''} 
                onChange={e => setProfile({...profile, phone: e.target.value})}
              />
            </div>
            <div className="form-group">
              <label>Account Role</label>
              <div className="badge badge-info" style={{ display: 'block', padding: '14px', textAlign: 'center' }}>
                {profile.role}
              </div>
            </div>
          </div>

          {profile.role === 'DOCTOR' && (
            <div style={{ marginTop: '32px', padding: '24px', background: 'var(--bg-secondary)', borderRadius: '16px' }}>
              <h3 style={{ marginBottom: '20px' }}>Doctor's Consultation Details</h3>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                <div className="form-group">
                  <label><Award size={14} style={{ verticalAlign: 'middle', marginRight: '6px' }} /> Specialty</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    value={profile.specialty || ''} 
                    onChange={e => setProfile({...profile, specialty: e.target.value})}
                    placeholder="e.g. Cardiologist"
                  />
                </div>
                <div className="form-group">
                  <label><DollarSign size={14} style={{ verticalAlign: 'middle', marginRight: '6px' }} /> Consultation Fees ($)</label>
                  <input 
                    type="number" 
                    className="form-control" 
                    value={profile.fees || 0} 
                    onChange={e => setProfile({...profile, fees: parseFloat(e.target.value)})}
                  />
                </div>
                <div className="form-group">
                  <label>Consultation Mode</label>
                  <select 
                    className="form-control" 
                    value={profile.mode || 'ONLINE'} 
                    onChange={e => setProfile({...profile, mode: e.target.value})}
                  >
                    <option value="ONLINE">ONLINE (Google Meet)</option>
                    <option value="OFFLINE">OFFLINE (Clinic)</option>
                  </select>
                </div>
              </div>
            </div>
          )}

          <div style={{ marginTop: '32px', display: 'flex', justifyContent: 'flex-end' }}>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              <Save size={18} /> {saving ? 'Saving Changes...' : 'Save Profile Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Profile;
