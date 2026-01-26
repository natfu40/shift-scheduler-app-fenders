import create from 'zustand';

export const useAuthStore = create((set, get) => ({
  user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null,
  token: localStorage.getItem('token') || null,
  isAdmin: localStorage.getItem('isAdmin') === 'true',
  firstTimeLogin: localStorage.getItem('firstTimeLogin') === 'true',

  // Computed property that includes fallback for admin@example.com
  get isAdminComputed() {
    const state = get();
    return state.isAdmin || (state.user && state.user.email === 'admin@example.com');
  },

  login: (user, token, isAdmin = false, firstTimeLogin = false) => {
    // Special case: force admin for admin@example.com
    if (user.email === 'admin@example.com') {
      isAdmin = true;
    }

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

