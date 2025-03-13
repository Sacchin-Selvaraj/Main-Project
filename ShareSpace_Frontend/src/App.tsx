import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Sidebar from './components/Sidebar';
import HomePage from './components/HomePage';
import OwnerDashboard from './components/OwnerDashboard';
import RoomDetail from './components/RoomDetail';
import BookingForm from './components/BookingForm';
import OwnerLogin from './components/OwnerLogin';
import RoommateDashboard from './components/RoommateDashboard2';

function App() {
  const [isDarkMode, setIsDarkMode] = useState(false);

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode);
    document.documentElement.classList.toggle('dark');
  };

  return (
    <Router>
      <div className={`min-h-screen ${isDarkMode ? 'dark' : ''}`}>
        <Sidebar isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
        <Toaster position="top-right" />
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/room/:roomId" element={<RoomDetail />} />
          <Route path="/book/:roomId" element={<BookingForm />} />
          <Route path="/owner/login" element={<OwnerLogin />} />
          <Route path="/owner/dashboard" element={<OwnerDashboard />} />
          <Route path="/roommate" element={<RoommateDashboard />} />
          <Route path="/roommate/dashboard" element={<RoommateDashboard />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;