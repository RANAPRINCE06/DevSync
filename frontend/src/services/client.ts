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

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
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
