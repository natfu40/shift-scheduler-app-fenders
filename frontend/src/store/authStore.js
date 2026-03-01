import create from 'zustand';

export const useAuthStore = create((set, get) => ({
  user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null,
  token: localStorage.getItem('token') || null,
  isAdmin: localStorage.getItem('isAdmin') === 'true',
  firstTimeLogin: localStorage.getItem('firstTimeLogin') === 'true',

  login: (user, token, isAdmin = false, firstTimeLogin = false) => {
    localStorage.setItem('user', JSON.stringify(user));
    localStorage.setItem('token', token);
    localStorage.setItem('isAdmin', isAdmin.toString());
    localStorage.setItem('firstTimeLogin', firstTimeLogin.toString());

    set({ user, token, isAdmin, firstTimeLogin });
  },

  setAdmin: (isAdmin) => {
    localStorage.setItem('isAdmin', isAdmin.toString());
    set({ isAdmin });
  },

  setFirstTimeLogin: (firstTimeLogin) => {
    localStorage.setItem('firstTimeLogin', firstTimeLogin.toString());
    set({ firstTimeLogin });
  },

  logout: () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('firstTimeLogin');
    set({ user: null, token: null, isAdmin: false, firstTimeLogin: false });
  },
}));

