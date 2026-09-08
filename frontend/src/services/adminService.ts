import api from './api';
import type {
  ApiResponse,
  CatalogPageResponse,
  AdminDashboardStats,
  Product,
  Category,
  Order,
  InventoryItem,
  Coupon,
  AuditLog,
  User,
} from '@/types';

export const adminService = {
  async getDashboardStats(): Promise<AdminDashboardStats> {
    const res = await api.get<ApiResponse<AdminDashboardStats>>('/admin/dashboard/stats');
    return res.data.data!;
  },

  async getSalesReport(year?: number): Promise<{
    monthlySales: Array<{ label: string; amount: number; count: number }>;
    categoryRevenue: Array<{ label: string; amount: number; count: number }>;
  }> {
    const res = await api.get<ApiResponse<any>>('/admin/reports/sales', { params: { year } });
    return res.data.data!;
  },

  async getProducts(params: {
    q?: string;
    status?: string;
    page?: number;
    pageSize?: number;
  }): Promise<CatalogPageResponse<Product>> {
    const res = await api.get<ApiResponse<CatalogPageResponse<Product>>>('/admin/products', { params });
    return res.data.data!;
  },

  async createProduct(data: any): Promise<Product> {
    const res = await api.post<ApiResponse<Product>>('/admin/products', data);
    return res.data.data!;
  },

  async updateProduct(productId: number, data: any): Promise<Product> {
    const res = await api.put<ApiResponse<Product>>(`/admin/products/${productId}`, data);
    return res.data.data!;
  },

  async toggleProductStatus(productId: number, status?: string): Promise<void> {
    await api.put<ApiResponse<void>>(`/admin/products/${productId}/status`, null, {
      params: { status },
    });
  },

  async getCategories(): Promise<Category[]> {
    const res = await api.get<ApiResponse<Category[]>>('/admin/categories');
    return res.data.data!;
  },

  async createCategory(data: any): Promise<Category> {
    const res = await api.post<ApiResponse<Category>>('/admin/categories', data);
    return res.data.data!;
  },

  async updateCategory(categoryId: number, data: any): Promise<Category> {
    const res = await api.put<ApiResponse<Category>>(`/admin/categories/${categoryId}`, data);
    return res.data.data!;
  },

  async getOrders(params: {
    q?: string;
    status?: string;
    page?: number;
    pageSize?: number;
  }): Promise<CatalogPageResponse<Order>> {
    const res = await api.get<ApiResponse<CatalogPageResponse<Order>>>('/admin/orders', { params });
    return res.data.data!;
  },

  async getOrderById(orderId: number): Promise<Order> {
    const res = await api.get<ApiResponse<Order>>(`/admin/orders/${orderId}`);
    return res.data.data!;
  },

  async updateOrderStatus(orderId: number, data: {
    status: string;
    paymentStatus?: string;
    courierPartner?: string;
    trackingNumber?: string;
    notes?: string;
  }): Promise<Order> {
    const res = await api.put<ApiResponse<Order>>(`/admin/orders/${orderId}/status`, data);
    return res.data.data!;
  },

  async getInventory(params: {
    q?: string;
    filter?: string;
    page?: number;
    pageSize?: number;
  }): Promise<CatalogPageResponse<InventoryItem>> {
    const res = await api.get<ApiResponse<CatalogPageResponse<InventoryItem>>>('/admin/inventory', { params });
    return res.data.data!;
  },

  async adjustStock(data: {
    productId: number;
    quantity: number;
    notes?: string;
  }): Promise<void> {
    await api.post<ApiResponse<void>>('/admin/inventory/adjust', data);
  },

  async getCoupons(params: {
    q?: string;
    page?: number;
    pageSize?: number;
  }): Promise<CatalogPageResponse<Coupon>> {
    const res = await api.get<ApiResponse<CatalogPageResponse<Coupon>>>('/admin/coupons', { params });
    return res.data.data!;
  },

  async createCoupon(data: any): Promise<Coupon> {
    const res = await api.post<ApiResponse<Coupon>>('/admin/coupons', data);
    return res.data.data!;
  },

  async deleteCoupon(couponId: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/admin/coupons/${couponId}`);
  },

  async getReturns(params: {
    status?: string;
    page?: number;
    pageSize?: number;
  }): Promise<CatalogPageResponse<any>> {
    const res = await api.get<ApiResponse<CatalogPageResponse<any>>>('/admin/returns', { params });
    return res.data.data!;
  },

  async updateReturnStatus(returnId: number, data: {
    status: string;
    refundAmount?: number;
    adminNotes?: string;
  }): Promise<void> {
    await api.put<ApiResponse<void>>(`/admin/returns/${returnId}/status`, data);
  },

  async getUsers(): Promise<User[]> {
    const res = await api.get<ApiResponse<User[]>>('/admin/users');
    return res.data.data!;
  },

  async getAuditLogs(limit = 50): Promise<AuditLog[]> {
    const res = await api.get<ApiResponse<AuditLog[]>>('/admin/audit-logs', { params: { limit } });
    return res.data.data!;
  },
};
