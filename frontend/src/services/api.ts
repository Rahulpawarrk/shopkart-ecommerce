import axios, { AxiosError } from 'axios';
import type { ApiResponse } from '@/types';

export const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
  },
});

// Helper to extract cookie by name
function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'));
  return match ? decodeURIComponent(match[3]) : null;
}

// Attach CSRF token if cookie is set by Spring Security
api.interceptors.request.use((config) => {
  const csrfToken = getCookie('XSRF-TOKEN');
  if (csrfToken && config.headers) {
    config.headers['X-XSRF-TOKEN'] = csrfToken;
  }
  return config;
});

// Response interceptor to handle unified ApiResponse error handling
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse>) => {
    // If backend returned structured ApiResponse in error payload
    if (error.response?.data?.message) {
      return Promise.reject(new Error(error.response.data.message));
    }
    if (error.response?.status === 401) {
      return Promise.reject(new Error('Authentication required. Please login.'));
    }
    if (error.response?.status === 403) {
      return Promise.reject(new Error('Access denied. You do not have permission.'));
    }
    return Promise.reject(error);
  }
);

export default api;
