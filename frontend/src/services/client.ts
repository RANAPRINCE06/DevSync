import axios, { AxiosError } from 'axios';
import { ApiResponse } from '@/types/api';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor to attach auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('devsync_auth_token') || sessionStorage.getItem('devsync_auth_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors and 401 session expiration
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('devsync_auth_token');
      localStorage.removeItem('devsync_auth_user');
      sessionStorage.removeItem('devsync_auth_token');
      sessionStorage.removeItem('devsync_auth_user');

      if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register')) {
        const redirectPath = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `/login?redirect=${redirectPath}`;
      }
    }

    let errorMessage = 'An unexpected error occurred. Please try again.';

    if (error.response?.data) {
      const data = error.response.data;
      if (data.message) {
        errorMessage = data.message;
      }
      if (data.errors && data.errors.length > 0) {
        errorMessage = `${errorMessage}: ${data.errors.join(', ')}`;
      }
    } else if (error.message) {
      if (error.message.includes('Network Error')) {
        errorMessage = 'Unable to connect to the server. Please ensure the backend is running.';
      } else {
        errorMessage = error.message;
      }
    }

    return Promise.reject(new Error(errorMessage));
  }
);
