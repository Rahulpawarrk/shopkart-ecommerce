import api from './api';
import type { ApiResponse, Address, User } from '@/types';

export const customerService = {
  async getProfile(): Promise<User> {
    const res = await api.get<ApiResponse<User>>('/customer/profile');
    return res.data.data!;
  },

  async updateProfile(data: { firstName: string; lastName: string; phone?: string }): Promise<User> {
    const res = await api.put<ApiResponse<User>>('/customer/profile', data);
    return res.data.data!;
  },

  async getAddresses(): Promise<Address[]> {
    const res = await api.get<ApiResponse<Address[]>>('/customer/addresses');
    return res.data.data!;
  },

  async addAddress(data: Omit<Address, 'addressId'>): Promise<Address> {
    const res = await api.post<ApiResponse<Address>>('/customer/addresses', data);
    return res.data.data!;
  },

  async updateAddress(addressId: number, data: Omit<Address, 'addressId'>): Promise<Address> {
    const res = await api.put<ApiResponse<Address>>(`/customer/addresses/${addressId}`, data);
    return res.data.data!;
  },

  async deleteAddress(addressId: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/customer/addresses/${addressId}`);
  },

  async setDefaultAddress(addressId: number): Promise<void> {
    await api.put<ApiResponse<void>>(`/customer/addresses/${addressId}/default`);
  },
};
