import api from './api';
import type {
  ApiResponse,
  Category,
  Product,
  HomeShowcase,
  CatalogPageResponse,
  RatingSummary,
  Review,
} from '@/types';

export const productService = {
  async getHomeShowcase(): Promise<HomeShowcase> {
    const res = await api.get<ApiResponse<HomeShowcase>>('/home/showcase');
    return res.data.data!;
  },

  async getCategories(): Promise<Category[]> {
    const res = await api.get<ApiResponse<Category[]>>('/categories');
    return res.data.data!;
  },

  async searchProducts(params: {
    q?: string;
    categoryId?: number;
    categorySlug?: string;
    minPrice?: number;
    maxPrice?: number;
    brand?: string;
    minDiscount?: number;
    inStockOnly?: boolean;
    sort?: string;
    page?: number;
    pageSize?: number;
  }): Promise<CatalogPageResponse<Product>> {
    const res = await api.get<ApiResponse<CatalogPageResponse<Product>>>('/products', { params });
    return res.data.data!;
  },

  async getProduct(slugOrId: string | number): Promise<Product> {
    const res = await api.get<ApiResponse<Product>>(`/products/${slugOrId}`);
    return res.data.data!;
  },

  async getProductReviews(
    productId: number,
    page = 1,
    pageSize = 10
  ): Promise<{
    summary: RatingSummary;
    reviews: Review[];
    totalReviews: number;
    page: number;
    totalPages: number;
  }> {
    const res = await api.get<ApiResponse<any>>(`/reviews/product/${productId}`, {
      params: { page, pageSize },
    });
    return res.data.data!;
  },

  async submitReview(data: {
    productId: number;
    rating: number;
    title: string;
    comment: string;
    imageUrl?: string;
  }): Promise<Review> {
    const res = await api.post<ApiResponse<Review>>('/reviews', data);
    return res.data.data!;
  },
};
