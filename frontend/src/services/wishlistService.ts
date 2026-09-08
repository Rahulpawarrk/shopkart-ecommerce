import api from './api';
import type { ApiResponse, Wishlist } from '@/types';

export const wishlistService = {
  async getWishlist(): Promise<Wishlist> {
    const res = await api.get<ApiResponse<Wishlist>>('/wishlist');
    return res.data.data!;
  },

  async toggleWishlist(productId: number): Promise<{ inWishlist: boolean }> {
    const res = await api.post<ApiResponse<{ inWishlist: boolean }>>('/wishlist/toggle', { productId });
    return res.data.data!;
  },

  async moveToCart(productId: number): Promise<void> {
    await api.post<ApiResponse<void>>(`/wishlist/move-to-cart/${productId}`);
  },
};
