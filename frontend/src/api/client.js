import axios from 'axios';
import { API_CONFIG, API_ENDPOINTS, DEFAULT_PAGINATION } from '../constants/apiConstants';

const axiosInstance = axios.create({
  baseURL: API_CONFIG.BASE_URL,
});

// Add token to requests (but not for auth endpoints)
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  const isAuthEndpoint = config.url.includes(API_ENDPOINTS.AUTH.SIGNUP) ||
                        config.url.includes(API_ENDPOINTS.AUTH.LOGIN);

  if (token && !isAuthEndpoint) {
    config.headers.Authorization = `Bearer ${token}`;
    // Debug logging for delete requests
    if (config.method === 'delete') {
      console.log('Delete request with token:', token ? 'Token present' : 'No token');
    }
  } else if (!isAuthEndpoint) {
    console.warn('No token available for authenticated request:', config.url);
  }
  return config;
});

// Add response interceptor for better error handling
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.error('401 Unauthorized error:', {
        url: error.config?.url,
        method: error.config?.method,
        token: localStorage.getItem('token') ? 'Present' : 'Missing',
        headers: error.config?.headers
      });
    }
    return Promise.reject(error);
  }
);

// Helper function to create paginated requests
const createPaginatedRequest = (url, page = DEFAULT_PAGINATION.page, size = DEFAULT_PAGINATION.size) =>
  axiosInstance.get(url, { params: { page, size } });

export const authAPI = {
  signup: (data) => axiosInstance.post(API_ENDPOINTS.AUTH.SIGNUP, data),
  login: (data) => axiosInstance.post(API_ENDPOINTS.AUTH.LOGIN, data),
};

export const shiftAPI = {
  getAvailableShifts: (page, size) => createPaginatedRequest(API_ENDPOINTS.SHIFTS.AVAILABLE, page, size),
  getUpcomingShifts: (page, size) => createPaginatedRequest(API_ENDPOINTS.SHIFTS.UPCOMING, page, size),
  getShiftById: (shiftId) => axiosInstance.get(API_ENDPOINTS.SHIFTS.BY_ID(shiftId)),
  getShiftDetails: (shiftId) => axiosInstance.get(API_ENDPOINTS.SHIFTS.DETAILS(shiftId)),
  createShift: (data) => axiosInstance.post(API_ENDPOINTS.SHIFTS.BASE, data),
  updateShift: (shiftId, data) => axiosInstance.put(API_ENDPOINTS.SHIFTS.BY_ID(shiftId), data),
  deleteShift: (shiftId) => axiosInstance.delete(API_ENDPOINTS.SHIFTS.BY_ID(shiftId)),
  getShiftsByUser: (userId, page, size) => createPaginatedRequest(API_ENDPOINTS.SHIFTS.BY_USER(userId), page, size),
};

export const assignmentAPI = {
  signupForShift: (shiftId) => axiosInstance.post(API_ENDPOINTS.ASSIGNMENTS.SIGNUP(shiftId)),
  getUserAssignments: () => axiosInstance.get(API_ENDPOINTS.ASSIGNMENTS.USER),
  getSignupsForShift: (shiftId) => axiosInstance.get(API_ENDPOINTS.ASSIGNMENTS.BY_SHIFT(shiftId)),
  acceptSignup: (assignmentId) => axiosInstance.put(API_ENDPOINTS.ASSIGNMENTS.ACCEPT(assignmentId)),
  rejectSignup: (assignmentId) => axiosInstance.delete(API_ENDPOINTS.ASSIGNMENTS.REJECT(assignmentId)),
};

export const auditAPI = {
  getAllLogs: (page, size) => createPaginatedRequest(API_ENDPOINTS.AUDIT.BASE, page, size),
  getLogsByUser: (userId, page, size) => createPaginatedRequest(API_ENDPOINTS.AUDIT.BY_USER(userId), page, size),
  getLogsByAction: (action, page, size) => createPaginatedRequest(API_ENDPOINTS.AUDIT.BY_ACTION(action), page, size),
};

export const adminAPI = {
  makeUserAdmin: (userId) => axiosInstance.post(API_ENDPOINTS.ADMIN.MAKE_ADMIN(userId)),
  removeUserAdmin: (userId) => axiosInstance.post(API_ENDPOINTS.ADMIN.REMOVE_ADMIN(userId)),
  isUserAdmin: (userId) => axiosInstance.get(API_ENDPOINTS.ADMIN.IS_ADMIN(userId)),
};

export const userAPI = {
  getAllUsers: () => axiosInstance.get(API_ENDPOINTS.USERS.BASE),
  deleteUser: (userId) => axiosInstance.delete(API_ENDPOINTS.USERS.BY_ID(userId)),
};

export default axiosInstance;
