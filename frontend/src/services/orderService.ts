import api from './api';
import type { ApiResponse, CatalogPageResponse, Order, OrderTracking } from '@/types';

export const orderService = {
  async checkout(data: {
    addressId: number;
    paymentMethod: string;
    couponCode?: string;
    notes?: string;
  }): Promise<Order> {
    const res = await api.post<ApiResponse<Order>>('/orders/checkout', data);
    return res.data.data!;
  },

  async directBuy(data: {
    addressId: number;
    productId: number;
    quantity: number;
    paymentMethod: string;
    couponCode?: string;
    notes?: string;
  }): Promise<Order> {
    const res = await api.post<ApiResponse<Order>>('/orders/direct-buy', data);
    return res.data.data!;
  },

  async getMyOrders(status?: string, page = 1, pageSize = 10): Promise<CatalogPageResponse<Order>> {
    const res = await api.get<ApiResponse<CatalogPageResponse<Order>>>('/orders/my-orders', {
      params: { status, page, pageSize },
    });
    return res.data.data!;
  },

  async getOrderById(orderId: number): Promise<Order> {
    const res = await api.get<ApiResponse<Order>>(`/orders/${orderId}`);
    return res.data.data!;
  },

  async cancelOrder(orderId: number, reason: string): Promise<Order> {
    const res = await api.post<ApiResponse<Order>>(`/orders/${orderId}/cancel`, { reason });
    return res.data.data!;
  },

  async requestReturn(orderId: number, data: { orderItemId: number; reason: string; comments?: string }): Promise<void> {
    await api.post<ApiResponse<void>>(`/orders/${orderId}/return`, data);
  },

  async getLiveTracking(orderId: number): Promise<OrderTracking> {
    const res = await api.get<ApiResponse<OrderTracking>>(`/orders/${orderId}/tracking`);
    return res.data.data!;
  },

  async initiatePayment(orderId: number): Promise<{
    orderId: number;
    orderNumber: string;
    razorpayOrderId: string;
    keyId: string;
    amountInPaise: number;
    currency: string;
    customerName: string;
    customerEmail: string;
    customerPhone: string;
  }> {
    const res = await api.post<ApiResponse<any>>(`/payments/initiate/${orderId}`);
    return res.data.data!;
  },

  async verifyPayment(data: {
    orderId: number;
    razorpayPaymentId?: string;
    razorpayOrderId?: string;
    razorpaySignature?: string;
    transactionReference?: string;
  }): Promise<any> {
    const res = await api.post<ApiResponse<any>>('/payments/verify', data);
    return res.data.data!;
  },

  async recordPaymentFailure(data: {
    orderId: number;
    reason?: string;
    razorpayOrderId?: string;
  }): Promise<void> {
    await api.post<ApiResponse<void>>('/payments/failure', data);
  },
};
