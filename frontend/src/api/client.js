import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('bc_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    // expired / invalid token -> back to login
    if (err.response?.status === 401 && localStorage.getItem('bc_token')) {
      localStorage.removeItem('bc_token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const errMsg = (e) =>
  e?.response?.data?.message ||
  (e?.code === 'ERR_NETWORK' ? 'Cannot reach the server. Is the backend running on port 8080?' : e?.message) ||
  'Something went wrong';

export default api;
