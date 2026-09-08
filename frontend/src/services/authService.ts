import api from './api';
import type { ApiResponse, User } from '@/types';

export const authService = {
  async login(email: string, password: string): Promise<User> {
    const res = await api.post<ApiResponse<User>>('/auth/login', { email, password });
    return res.data.data!;
  },

  async register(data: {
    email: string;
    password: string;
    confirmPassword: string;
    firstName: string;
    lastName: string;
    phone: string;
  }): Promise<User> {
    const res = await api.post<ApiResponse<User>>('/auth/register', data);
    return res.data.data!;
  },

  async getCurrentUser(): Promise<User | null> {
    try {
      const res = await api.get<ApiResponse<User>>('/auth/me');
      return res.data.data || null;
    } catch {
      return null;
    }
  },

  async logout(): Promise<void> {
    await api.post<ApiResponse<void>>('/auth/logout');
  },

  async changePassword(currentPassword: string, newPassword: string, confirmPassword: string): Promise<void> {
    await api.post<ApiResponse<void>>('/auth/change-password', {
      currentPassword,
      newPassword,
      confirmPassword,
    });
  },

  async forgotPassword(email: string): Promise<void> {
    await api.post<ApiResponse<void>>('/auth/forgot-password', { email });
  },

  async resetPassword(token: string, password: string, confirmPassword: string): Promise<void> {
    await api.post<ApiResponse<void>>('/auth/reset-password', {
      token,
      password,
      confirmPassword,
    });
  },
};
