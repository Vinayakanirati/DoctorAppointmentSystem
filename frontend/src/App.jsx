import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminDashboard from './pages/admin/AdminDashboard';
import DoctorDashboard from './pages/doctor/DoctorDashboard';
import DoctorHistory from './pages/doctor/DoctorHistory';
import BrowseDoctorsPage from './pages/patient/PatientDashboard';
import PatientHistory from './pages/patient/PatientHistory';
import BookingCalendar from './pages/patient/BookingCalendar';
import PrivateRoute from './components/PrivateRoute';
import PageWrapper from './components/PageWrapper';
import './index.css';

import Profile from './pages/Profile';

const AnimatedRoutes = () => {
  const location = useLocation();
  
  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<PageWrapper><Login /></PageWrapper>} />
        <Route path="/register" element={<PageWrapper><Register /></PageWrapper>} />
        
        <Route path="/profile" element={
          <PrivateRoute>
            <PageWrapper><Profile /></PageWrapper>
          </PrivateRoute>
        } />
        
        <Route path="/admin/dashboard" element={
          <PrivateRoute role="ADMIN">
            <PageWrapper><AdminDashboard /></PageWrapper>
          </PrivateRoute>
        } />
        
        <Route path="/doctor/dashboard" element={
          <PrivateRoute role="DOCTOR">
            <PageWrapper><DoctorDashboard /></PageWrapper>
          </PrivateRoute>
        } />
        
        <Route path="/doctor/history" element={
          <PrivateRoute role="DOCTOR">
            <PageWrapper><DoctorHistory /></PageWrapper>
          </PrivateRoute>
        } />
        
        <Route path="/patient/browse" element={
          <PrivateRoute role="PATIENT">
            <PageWrapper><BrowseDoctorsPage /></PageWrapper>
          </PrivateRoute>
        } />
        
        <Route path="/patient/book/:doctorId" element={
          <PrivateRoute role="PATIENT">
            <PageWrapper><BookingCalendar /></PageWrapper>
          </PrivateRoute>
        } />
        
        <Route path="/patient/history" element={
          <PrivateRoute role="PATIENT">
            <PageWrapper><PatientHistory /></PageWrapper>
          </PrivateRoute>
        } />
        
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </AnimatePresence>
  );
};

function App() {
  return (
    <BrowserRouter>
      <div className="app-container">
        <Navbar />
        <main className="main-content">
          <AnimatedRoutes />
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
