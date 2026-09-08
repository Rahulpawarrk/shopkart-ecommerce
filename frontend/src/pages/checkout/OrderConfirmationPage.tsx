import React, { useEffect, useState } from 'react';
import { useParams, useLocation, Link } from 'react-router-dom';
import { orderService } from '@/services/orderService';
import type { Order } from '@/types';
import { CheckCircle, Package, ArrowRight, Truck, Home, ShoppingBag, Loader2 } from 'lucide-react';

export const OrderConfirmationPage: React.FC = () => {
  const params = useParams<{ orderId?: string; id?: string }>();
  const location = useLocation();
  const stateOrder = (location.state as any)?.order as Order | undefined;

  const [order, setOrder] = useState<Order | null>(stateOrder || null);
  const [loading, setLoading] = useState(!stateOrder);
  const [error, setError] = useState<string | null>(null);

  const effectiveId = params.orderId || params.id;

  useEffect(() => {
    if (order) {
      setLoading(false);
      return;
    }

    if (!effectiveId) {
      setLoading(false);
      return;
    }

    const numId = Number(effectiveId);
    if (isNaN(numId)) {
      setLoading(false);
      return;
    }

    setLoading(true);
    orderService
      .getOrderById(numId)
      .then((data) => {
        setOrder(data);
      })
      .catch((err: any) => {
        console.warn('Could not fetch order by ID:', err);
        setError(err?.message || 'Could not load order details');
      })
      .finally(() => setLoading(false));
  }, [effectiveId, order]);

  if (loading) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center space-y-4 p-8">
        <Loader2 className="w-10 h-10 text-amber-500 animate-spin" />
        <p className="text-gray-600 font-semibold text-sm">Retrieving your order confirmation...</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-12 text-center space-y-8 animate-fadeIn">
      {/* Success Badge */}
      <div className="space-y-4">
        <div className="w-20 h-20 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto shadow-md ring-8 ring-emerald-50/50">
          <CheckCircle className="w-12 h-12" />
        </div>
        <h1 className="text-3xl sm:text-4xl font-black text-gray-900 tracking-tight">
          Order Placed Successfully!
        </h1>
        <p className="text-sm text-gray-600 max-w-md mx-auto">
          Thank you for shopping with ShopKart! We have received your order and our fulfillment team is preparing your package.
        </p>
      </div>

      {/* Order Summary Box */}
      {order ? (
        <div className="bg-white rounded-3xl border border-gray-200 p-6 sm:p-8 text-left shadow-sm space-y-6">
          <div className="flex flex-wrap items-center justify-between border-b border-gray-100 pb-4 gap-2">
            <div>
              <p className="text-xs text-gray-400 font-medium uppercase tracking-wider">Order Reference</p>
              <p className="font-black text-lg text-gray-900">#{order.orderNumber}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 font-medium text-right uppercase tracking-wider">Payment Status</p>
              <span className={`inline-block text-xs font-black px-2.5 py-1 rounded-full ${
                order.paymentStatus === 'PAID' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
              }`}>
                {order.paymentStatus} ({order.paymentMethod})
              </span>
            </div>
          </div>

          {/* Items Preview if available */}
          {order.items && order.items.length > 0 && (
            <div className="space-y-3">
              <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Items Ordered</p>
              <div className="divide-y divide-gray-100 max-h-56 overflow-y-auto pr-1">
                {order.items.map((item, idx) => (
                  <div key={idx} className="py-2.5 flex items-center justify-between text-xs">
                    <div className="flex items-center space-x-3">
                      <div className="w-8 h-8 rounded-lg bg-gray-50 border border-gray-100 flex items-center justify-center text-gray-400">
                        <ShoppingBag className="w-4 h-4" />
                      </div>
                      <div>
                        <p className="font-semibold text-gray-800 line-clamp-1">{item.productName}</p>
                        <p className="text-gray-400 text-[11px]">Qty: {item.quantity}</p>
                      </div>
                    </div>
                    <p className="font-bold text-gray-900">₹{(item.lineTotal || (item.effectivePrice * item.quantity)).toLocaleString('en-IN')}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs border-t border-gray-100 pt-4">
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
              <p className="text-[11px] text-gray-400 mt-1">Includes all applicable taxes and express delivery.</p>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-4 flex flex-col sm:flex-row gap-3">
            <Link
              to={`/orders/${order.orderId}`}
              className="flex-1 text-center py-3 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs rounded-xl transition shadow-sm flex items-center justify-center gap-2"
            >
              <Truck className="w-4 h-4" />
              <span>Track Live Delivery</span>
            </Link>

            <Link
              to="/"
              className="flex-1 text-center py-3 border border-gray-300 text-gray-700 font-bold text-xs rounded-xl hover:bg-gray-50 transition flex items-center justify-center gap-2"
            >
              <Home className="w-4 h-4" />
              <span>Continue Shopping</span>
            </Link>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-3xl border border-gray-200 p-6 sm:p-8 text-center shadow-sm space-y-5">
          <p className="text-gray-700 text-sm font-medium">
            Your order has been recorded in your ShopKart account. You can view full tracking details and invoices at any time.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-3">
            <Link
              to="/orders"
              className="px-6 py-3 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs rounded-xl transition shadow-sm flex items-center justify-center gap-2"
            >
              <Truck className="w-4 h-4" />
              <span>Go to My Orders</span>
            </Link>
            <Link
              to="/"
              className="px-6 py-3 border border-gray-300 text-gray-700 font-bold text-xs rounded-xl hover:bg-gray-50 transition flex items-center justify-center gap-2"
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
export default OrderConfirmationPage;
