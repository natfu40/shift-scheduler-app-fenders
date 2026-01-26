import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import Navigation from './components/Navigation';
import ProtectedAdminRoute from './components/ProtectedAdminRoute';
import LoginPage from './pages/LoginPage';
import EmployeeDashboard from './pages/EmployeeDashboard';
import ShiftManagement from './pages/ShiftManagement';
import UsersPage from './pages/UsersPage';
import ShiftCalendar from './pages/ShiftCalendar';
import Audits from './pages/Audits';
import './App.css';

function App() {
  const { user, firstTimeLogin } = useAuthStore();


  return (
    <Router>
      <div className="App">
        {user && !firstTimeLogin && <Navigation />}
        <Routes>
          <Route path="/login" element={(!user || firstTimeLogin) ? <LoginPage /> : <Navigate to="/" />} />
          <Route path="/" element={user && !firstTimeLogin ? <EmployeeDashboard /> : <Navigate to="/login" />} />
          <Route path="/calendar" element={user && !firstTimeLogin ? <ShiftCalendar /> : <Navigate to="/login" />} />
          <Route
            path="/admin"
            element={
              <ProtectedAdminRoute>
                <ShiftManagement />
              </ProtectedAdminRoute>
            }
          />
          <Route
            path="/users"
            element={
              <ProtectedAdminRoute>
                <UsersPage />
              </ProtectedAdminRoute>
            }
          />
          <Route
            path="/audits"
            element={
              <ProtectedAdminRoute>
                <Audits />
              </ProtectedAdminRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;

