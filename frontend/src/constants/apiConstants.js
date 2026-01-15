// API configuration constants
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080/api',
  DEFAULT_PAGE_SIZE: 10,
  DEFAULT_PAGE: 0,
};

// Common API parameters
export const DEFAULT_PAGINATION = {
  page: API_CONFIG.DEFAULT_PAGE,
  size: API_CONFIG.DEFAULT_PAGE_SIZE,
};

// API endpoint paths
export const API_ENDPOINTS = {
  AUTH: {
    SIGNUP: '/auth/signup',
    LOGIN: '/auth/login',
  },
  SHIFTS: {
    BASE: '/shifts',
    AVAILABLE: '/shifts/available',
    UPCOMING: '/shifts/upcoming',
    BY_ID: (id) => `/shifts/${id}`,
    DETAILS: (id) => `/shifts/${id}/details`,
    BY_USER: (userId) => `/shifts/user/${userId}`,
  },
  ASSIGNMENTS: {
    BASE: '/shift-assignments',
    SIGNUP: (shiftId) => `/shift-assignments/signup/${shiftId}`,
    USER: '/shift-assignments/user',
    BY_SHIFT: (shiftId) => `/shift-assignments/shift/${shiftId}`,
    ACCEPT: (assignmentId) => `/shift-assignments/${assignmentId}/accept`,
    REJECT: (assignmentId) => `/shift-assignments/${assignmentId}/reject`,
  },
  AUDIT: {
    BASE: '/audit-logs',
    BY_USER: (userId) => `/audit-logs/user/${userId}`,
    BY_ACTION: (action) => `/audit-logs/action/${action}`,
  },
  ADMIN: {
    MAKE_ADMIN: (userId) => `/admin/users/${userId}/make-admin`,
    REMOVE_ADMIN: (userId) => `/admin/users/${userId}/remove-admin`,
    IS_ADMIN: (userId) => `/admin/users/${userId}/is-admin`,
  },
  USERS: {
    BASE: '/users',
    BY_ID: (id) => `/users/${id}`,
  },
};
