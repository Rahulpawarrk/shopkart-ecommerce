import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { orderService } from '@/services/orderService';
import type { Order } from '@/types';
import { CheckCircle, Package, ArrowRight, Truck, Home } from 'lucide-react';

export const OrderConfirmationPage: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!orderId) return;
    orderService
      .getOrderById(Number(orderId))
      .then(setOrder)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [orderId]);

  if (loading) {
    return <div className="p-20 text-center text-sm font-semibold">Retrieving order confirmation...</div>;
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-16 text-center space-y-8">
      {/* Success Badge */}
      <div className="space-y-4">
        <div className="w-20 h-20 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto shadow-md">
          <CheckCircle className="w-12 h-12" />
        </div>
        <h1 className="text-3xl sm:text-4xl font-black text-gray-900">
          Order Placed Successfully!
        </h1>
        <p className="text-sm text-gray-600 max-w-md mx-auto">
          Thank you for shopping with ShopKart! We have received your order and our fulfillment team is preparing your package.
        </p>
      </div>

      {/* Order Summary Box */}
      {order && (
        <div className="bg-white rounded-3xl border border-gray-200 p-6 sm:p-8 text-left shadow-sm space-y-6">
          <div className="flex flex-wrap items-center justify-between border-b border-gray-100 pb-4 gap-2">
            <div>
              <p className="text-xs text-gray-400 font-medium">Order Reference</p>
              <p className="font-black text-lg text-gray-900">#{order.orderNumber}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 font-medium text-right">Payment Status</p>
              <span className={`inline-block text-xs font-black px-2.5 py-1 rounded-full ${
                order.paymentStatus === 'PAID' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
              }`}>
                {order.paymentStatus} ({order.paymentMethod})
              </span>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div>
              <p className="font-bold text-gray-800 mb-1">Delivery Address:</p>
              <p className="text-gray-600 leading-relaxed">
                {order.shippingFullName}<br />
                {order.shippingAddressLine1}, {order.shippingCity}<br />
                {order.shippingState} - {order.shippingPostalCode}<br />
                Phone: {order.shippingPhone}
              </p>
            </div>

            <div>
              <p className="font-bold text-gray-800 mb-1">Amount Paid:</p>
              <p className="text-2xl font-black text-gray-900">₹{order.totalAmount.toLocaleString('en-IN')}</p>
              <p className="text-[11px] text-gray-400 mt-1">Includes all applicable GST and free express delivery.</p>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-4 flex flex-col sm:flex-row gap-3">
            <Link
              to={`/orders`}
              className="flex-1 text-center py-3 bg-blue-600 text-white font-bold text-xs rounded-xl hover:bg-blue-700 transition shadow-sm flex items-center justify-center gap-2"
            >
              <Truck className="w-4 h-4" />
              <span>Track Live Delivery</span>
            </Link>

            <Link
              to="/"
              className="flex-1 text-center py-3 border border-gray-300 text-gray-700 font-bold text-xs rounded-xl hover:bg-gray-50 transition flex items-center justify-center gap-2"
            >
              <Home className="w-4 h-4" />
              <span>Back to Home</span>
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};
