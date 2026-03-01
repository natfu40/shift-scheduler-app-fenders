import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

function ProtectedAdminRoute({ children }) {
  const { user, isAdmin, firstTimeLogin } = useAuthStore();

  if (!user || firstTimeLogin) {
    return <Navigate to="/login" />;
  }

  if (!isAdmin) {
    return <Navigate to="/" />;
  }

  return children;
}

export default ProtectedAdminRoute;

