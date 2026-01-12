import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
});

// Add token to requests (but not for auth endpoints)
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  // Don't add token for signup/login endpoints
  if (token && !config.url.includes('/auth/signup') && !config.url.includes('/auth/login')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authAPI = {
  signup: (data) => axiosInstance.post('/auth/signup', data),
  login: (data) => axiosInstance.post('/auth/login', data),
};

export const shiftAPI = {
  getAvailableShifts: (page = 0, size = 10) =>
    axiosInstance.get('/shifts/available', { params: { page, size } }),
  getUpcomingShifts: (page = 0, size = 10) =>
    axiosInstance.get('/shifts/upcoming', { params: { page, size } }),
  getShiftById: (shiftId) => axiosInstance.get(`/shifts/${shiftId}`),
  getShiftDetails: (shiftId) => axiosInstance.get(`/shifts/${shiftId}/details`),
  createShift: (data) => axiosInstance.post('/shifts', data),
  updateShift: (shiftId, data) => axiosInstance.put(`/shifts/${shiftId}`, data),
  deleteShift: (shiftId) => axiosInstance.delete(`/shifts/${shiftId}`),
  getShiftsByUser: (userId, page = 0, size = 10) =>
    axiosInstance.get(`/shifts/user/${userId}`, { params: { page, size } }),
};

export const assignmentAPI = {
  signupForShift: (shiftId) => axiosInstance.post(`/shift-assignments/signup/${shiftId}`),
  getUserAssignments: () => axiosInstance.get('/shift-assignments/user'),
  getSignupsForShift: (shiftId) => axiosInstance.get(`/shift-assignments/shift/${shiftId}`),
  acceptSignup: (assignmentId) => axiosInstance.put(`/shift-assignments/${assignmentId}/accept`),
  rejectSignup: (assignmentId) => axiosInstance.delete(`/shift-assignments/${assignmentId}/reject`),
};

export const auditAPI = {
  getAllLogs: (page = 0, size = 10) =>
    axiosInstance.get('/audit-logs', { params: { page, size } }),
  getLogsByUser: (userId, page = 0, size = 10) =>
    axiosInstance.get(`/audit-logs/user/${userId}`, { params: { page, size } }),
  getLogsByAction: (action, page = 0, size = 10) =>
    axiosInstance.get(`/audit-logs/action/${action}`, { params: { page, size } }),
};

export const adminAPI = {
  makeUserAdmin: (userId) => axiosInstance.post(`/admin/users/${userId}/make-admin`),
  removeUserAdmin: (userId) => axiosInstance.post(`/admin/users/${userId}/remove-admin`),
  isUserAdmin: (userId) => axiosInstance.get(`/admin/users/${userId}/is-admin`),
};

export default axiosInstance;
