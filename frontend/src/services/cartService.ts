import api from './api';
import type { ApiResponse, Cart } from '@/types';

export const cartService = {
  async getCart(): Promise<Cart> {
    const res = await api.get<ApiResponse<Cart>>('/cart');
    return res.data.data!;
  },

  async addToCart(productId: number, quantity = 1): Promise<Cart> {
    const res = await api.post<ApiResponse<Cart>>('/cart/items', { productId, quantity });
    return res.data.data!;
  },

  async updateCartItem(cartItemId: number, quantity: number): Promise<Cart> {
    const res = await api.put<ApiResponse<Cart>>(`/cart/items/${cartItemId}`, { quantity });
    return res.data.data!;
  },

  async removeFromCart(cartItemId: number): Promise<Cart> {
    const res = await api.delete<ApiResponse<Cart>>(`/cart/items/${cartItemId}`);
    return res.data.data!;
  },

  async clearCart(): Promise<Cart> {
    const res = await api.delete<ApiResponse<Cart>>('/cart');
    return res.data.data!;
  },

  async applyCoupon(code: string): Promise<Cart> {
    const res = await api.post<ApiResponse<Cart>>('/cart/coupon', { code });
    return res.data.data!;
  },

  async removeCoupon(): Promise<Cart> {
    const res = await api.delete<ApiResponse<Cart>>('/cart/coupon');
    return res.data.data!;
  },
};
