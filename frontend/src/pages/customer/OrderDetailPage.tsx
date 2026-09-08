import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { orderService } from '@/services/orderService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import type { Order, OrderTracking } from '@/types';
import {
  Package,
  Truck,
  ArrowLeft,
  Calendar,
  CreditCard,
  MapPin,
  Printer,
  RotateCcw,
  XCircle,
  Clock,
  CheckCircle,
  AlertCircle,
  X,
} from 'lucide-react';

export const OrderDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const dispatch = useAppDispatch();

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Tracking Modal State
  const [showTracking, setShowTracking] = useState(false);
  const [trackingData, setTrackingData] = useState<OrderTracking | null>(null);
  const [trackingLoading, setTrackingLoading] = useState(false);

  // Cancel Modal State
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling] = useState(false);

  // Return Modal State
  const [showReturnModal, setShowReturnModal] = useState(false);
  const [returnItemId, setReturnItemId] = useState<number | null>(null);
  const [returnReason, setReturnReason] = useState('DEFECTIVE');
  const [returnComments, setReturnComments] = useState('');
  const [returning, setReturning] = useState(false);

  const fetchOrder = () => {
    if (!id) return;
    setLoading(true);
    orderService
      .getOrderById(Number(id))
      .then((data) => {
        setOrder(data);
        setError(null);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load order details');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchOrder();
  }, [id]);

  const handleOpenTracking = () => {
    if (!order) return;
    setShowTracking(true);
    setTrackingLoading(true);
    orderService
      .getLiveTracking(order.orderId)
      .then(setTrackingData)
      .catch(() => {})
      .finally(() => setTrackingLoading(false));
  };

  const handleCancelOrder = async () => {
    if (!order || !cancelReason.trim()) return;
    setCancelling(true);
    try {
      await orderService.cancelOrder(order.orderId, cancelReason.trim());
      dispatch(showToast({ message: 'Order cancelled successfully', type: 'success' }));
      setShowCancelModal(false);
      setCancelReason('');
      fetchOrder();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to cancel order', type: 'error' }));
    } finally {
      setCancelling(false);
    }
  };

  const handleRequestReturn = async () => {
    if (!order || !returnItemId) return;
    setReturning(true);
    try {
      await orderService.requestReturn(order.orderId, {
        orderItemId: returnItemId,
        reason: returnReason,
        comments: returnComments.trim() || undefined,
      });
      dispatch(showToast({ message: 'Return request submitted successfully', type: 'success' }));
      setShowReturnModal(false);
      setReturnItemId(null);
      setReturnComments('');
      fetchOrder();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to submit return', type: 'error' }));
    } finally {
      setReturning(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DELIVERED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      case 'SHIPPED':
      case 'OUT_FOR_DELIVERY':
        return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'PROCESSING':
      case 'CONFIRMED':
        return 'bg-amber-100 text-amber-800 border-amber-300';
      case 'CANCELLED':
        return 'bg-rose-100 text-rose-800 border-rose-300';
      case 'RETURN_REQUESTED':
      case 'RETURNED':
        return 'bg-purple-100 text-purple-800 border-purple-300';
      default:
        return 'bg-slate-100 text-slate-800 border-slate-300';
    }
  };

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-16 text-center">
        <div className="inline-block w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p className="mt-3 text-slate-500">Loading order details...</p>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <AlertCircle className="w-16 h-16 text-rose-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-slate-900 mb-2">Order Not Found</h2>
        <p className="text-slate-600 mb-6">{error || 'Unable to retrieve this order.'}</p>
        <Link
          to="/orders"
          className="inline-flex items-center gap-2 px-6 py-3 bg-primary text-white font-medium rounded-lg hover:bg-primary/90 transition shadow"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to My Orders
        </Link>
      </div>
    );
  }

  const isCancellable = order.canCancel ?? ['PENDING', 'CONFIRMED', 'PROCESSING'].includes(order.orderStatus);
  const isReturnable = order.canReturn ?? order.orderStatus === 'DELIVERED';

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Back button and Print header */}
      <div className="flex items-center justify-between">
        <Link
          to="/orders"
          className="inline-flex items-center text-sm font-medium text-slate-600 hover:text-primary transition"
        >
          <ArrowLeft className="w-4 h-4 mr-1.5" />
          Back to Orders
        </Link>
        <button
          onClick={() => window.print()}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md border border-slate-200 text-slate-700 hover:bg-slate-50 transition"
        >
          <Printer className="w-3.5 h-3.5" />
          Print Invoice
        </button>
      </div>

      {/* Header Banner */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-black text-slate-900">Order #{order.orderNumber}</h1>
            <span
              className={`text-xs uppercase tracking-wider font-bold px-3 py-1 rounded-full border ${getStatusBadge(
                order.orderStatus
              )}`}
            >
              {order.orderStatus.replace(/_/g, ' ')}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1.5 flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5" />
            Placed on {new Date(order.createdAt).toLocaleDateString('en-IN', {
              day: 'numeric',
              month: 'long',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {order.trackingNumber && (
            <button
              onClick={handleOpenTracking}
              className="inline-flex items-center gap-1.5 px-4 py-2 bg-blue-50 text-blue-700 text-xs font-bold rounded-lg border border-blue-200 hover:bg-blue-100 transition shadow-sm"
            >
              <Truck className="w-3.5 h-3.5" />
              Track Package
            </button>
          )}

          {isCancellable && (
            <button
              onClick={() => setShowCancelModal(true)}
              className="inline-flex items-center gap-1.5 px-4 py-2 bg-rose-50 text-rose-700 text-xs font-bold rounded-lg border border-rose-200 hover:bg-rose-100 transition shadow-sm"
            >
              <XCircle className="w-3.5 h-3.5" />
              Cancel Order
            </button>
          )}
        </div>
      </div>

      {/* Grid: Delivery Address & Payment Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Shipping Address */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
          <div className="flex items-center gap-2 text-slate-900 font-bold text-base mb-4">
            <MapPin className="w-5 h-5 text-primary" />
            <h3>Delivery Address</h3>
          </div>
          {order.shippingFullName ? (
            <div className="text-sm text-slate-600 space-y-1">
              <p className="font-semibold text-slate-900">{order.shippingFullName}</p>
              <p>{order.shippingAddressLine1}</p>
              {order.shippingAddressLine2 && <p>{order.shippingAddressLine2}</p>}
              <p>
                {order.shippingCity}, {order.shippingState} - {order.shippingPostalCode}
              </p>
              <p>{order.shippingCountry}</p>
              {order.shippingPhone && (
                <p className="pt-2 font-medium text-slate-800">Phone: {order.shippingPhone}</p>
              )}
            </div>
          ) : (
            <p className="text-sm text-slate-400 italic">
              {order.formattedShippingAddress || 'No delivery address recorded.'}
            </p>
          )}

          {order.courierPartner && (
            <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
              <span>Courier: <strong className="text-slate-900">{order.courierPartner}</strong></span>
              {order.trackingNumber && <span>Tracking: <strong className="text-slate-900">{order.trackingNumber}</strong></span>}
            </div>
          )}
        </div>

        {/* Payment Summary */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
          <div className="flex items-center gap-2 text-slate-900 font-bold text-base mb-4">
            <CreditCard className="w-5 h-5 text-primary" />
            <h3>Payment Summary</h3>
          </div>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between text-slate-600">
              <span>Payment Method</span>
              <span className="font-bold uppercase text-slate-900">{order.paymentMethod}</span>
            </div>
            <div className="flex justify-between text-slate-600">
              <span>Payment Status</span>
              <span
                className={`font-bold px-2 py-0.5 text-xs rounded ${
                  order.paymentStatus === 'PAID'
                    ? 'bg-emerald-100 text-emerald-800'
                    : 'bg-amber-100 text-amber-800'
                }`}
              >
                {order.paymentStatus}
              </span>
            </div>
            <div className="pt-2 border-t border-slate-100 flex justify-between text-slate-600">
              <span>Subtotal</span>
              <span>₹{Number(order.subtotal).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
            {Number(order.discountAmount) > 0 && (
              <div className="flex justify-between text-emerald-600 font-medium">
                <span>Discount Applied</span>
                <span>-₹{Number(order.discountAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
              </div>
            )}
            <div className="flex justify-between text-slate-600">
              <span>Tax (GST)</span>
              <span>₹{Number(order.taxAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
            <div className="flex justify-between text-slate-600">
              <span>Shipping Fee</span>
              <span>
                {Number(order.shippingAmount) === 0 ? (
                  <span className="text-emerald-600 font-medium uppercase text-xs">Free</span>
                ) : (
                  `₹${Number(order.shippingAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`
                )}
              </span>
            </div>
            <div className="pt-3 border-t border-slate-200 flex justify-between text-base font-black text-slate-900">
              <span>Grand Total</span>
              <span className="text-primary">₹{Number(order.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Order Items Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h2 className="font-bold text-slate-900 text-base">Items in this Order ({order.items.length})</h2>
        </div>
        <div className="divide-y divide-slate-100">
          {order.items.map((item) => (
            <div key={item.orderItemId} className="p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="flex items-center gap-4 w-full sm:w-auto">
                <img
                  src={item.primaryImageUrl || '/placeholder.png'}
                  alt={item.productName}
                  className="w-20 h-20 object-cover rounded-lg border border-slate-200 bg-slate-50 flex-shrink-0"
                  onError={(e) => {
                    (e.target as HTMLImageElement).src = 'https://placehold.co/100x100?text=Product';
                  }}
                />
                <div>
                  <Link
                    to={`/products/${item.productId}`}
                    className="font-bold text-slate-900 hover:text-primary transition line-clamp-1 text-base"
                  >
                    {item.productName}
                  </Link>
                  <p className="text-xs text-slate-500 mt-0.5">SKU: {item.sku}</p>
                  <p className="text-xs text-slate-600 mt-1">
                    Qty: <strong className="text-slate-900">{item.quantity}</strong> × ₹
                    {Number(item.effectivePrice || item.unitPrice).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-between sm:justify-end gap-6 w-full sm:w-auto border-t sm:border-t-0 pt-3 sm:pt-0">
                <div className="text-right">
                  <p className="text-xs text-slate-400">Total</p>
                  <p className="font-black text-slate-900 text-lg">
                    ₹{Number(item.lineTotal).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </p>
                </div>

                {isReturnable && (
                  <button
                    onClick={() => {
                      setReturnItemId(item.orderItemId);
                      setShowReturnModal(true);
                    }}
                    className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold rounded-lg border border-purple-200 bg-purple-50 text-purple-700 hover:bg-purple-100 transition shadow-sm"
                  >
                    <RotateCcw className="w-3.5 h-3.5" />
                    Return
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Live Tracking Modal */}
      {showTracking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl relative">
            <button
              onClick={() => setShowTracking(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>
            <h3 className="text-lg font-bold text-slate-900 mb-1 flex items-center gap-2">
              <Truck className="w-5 h-5 text-primary" />
              Tracking Order #{order.orderNumber}
            </h3>
            <p className="text-xs text-slate-500 mb-6">Courier: {order.courierPartner || 'ShopKart Express'}</p>

            {trackingLoading ? (
              <div className="py-12 text-center text-slate-500">
                <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-2" />
                Fetching shipment timeline...
              </div>
            ) : trackingData ? (
              <div className="space-y-6">
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 text-xs flex justify-between">
                  <div>
                    <span className="text-slate-400">Current Status:</span>{' '}
                    <strong className="text-slate-900">{trackingData.status}</strong>
                  </div>
                  <div>
                    <span className="text-slate-400">Tracking #:</span>{' '}
                    <strong className="text-slate-900">{trackingData.trackingNumber || 'N/A'}</strong>
                  </div>
                </div>

                <div className="relative pl-6 space-y-6 border-l-2 border-slate-200 ml-3">
                  {trackingData.events?.map((ev, idx) => (
                    <div key={idx} className="relative">
                      <div className="absolute -left-[31px] top-0 w-4 h-4 rounded-full border-2 bg-emerald-500 border-emerald-500" />
                      <div>
                        <h4 className="text-sm font-bold text-slate-900">
                          {ev.status}
                        </h4>
                        <p className="text-xs text-slate-500 mt-0.5">{ev.description}</p>
                        {ev.location && <p className="text-[11px] text-slate-400">Location: {ev.location}</p>}
                        {ev.timestamp && (
                          <p className="text-[10px] text-slate-400 mt-1">
                            {new Date(ev.timestamp).toLocaleString('en-IN')}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <p className="text-sm text-slate-500 text-center py-6">No tracking updates available yet.</p>
            )}
          </div>
        </div>
      )}

      {/* Cancel Confirmation Modal */}
      {showCancelModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl relative">
            <h3 className="text-lg font-bold text-slate-900 mb-2 flex items-center gap-2">
              <AlertCircle className="w-5 h-5 text-rose-600" />
              Cancel Order #{order.orderNumber}
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              Are you sure you want to cancel this order? Any payment made will be refunded to your original payment method.
            </p>
            <div className="space-y-3">
              <label className="text-xs font-semibold text-slate-700">Reason for Cancellation</label>
              <textarea
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                placeholder="Please let us know why you are cancelling..."
                className="w-full text-xs p-3 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-rose-500 min-h-[80px]"
              />
            </div>
            <div className="flex justify-end gap-2 mt-6">
              <button
                type="button"
                onClick={() => setShowCancelModal(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg transition"
              >
                Close
              </button>
              <button
                type="button"
                disabled={!cancelReason.trim() || cancelling}
                onClick={handleCancelOrder}
                className="px-4 py-2 text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 rounded-lg transition shadow disabled:opacity-50"
              >
                {cancelling ? 'Cancelling...' : 'Confirm Cancellation'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Return Request Modal */}
      {showReturnModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl relative">
            <h3 className="text-lg font-bold text-slate-900 mb-2 flex items-center gap-2">
              <RotateCcw className="w-5 h-5 text-purple-600" />
              Request Return
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              Returns are accepted within 7 days of delivery.
            </p>
            <div className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Reason for Return</label>
                <select
                  value={returnReason}
                  onChange={(e) => setReturnReason(e.target.value)}
                  className="w-full p-2.5 rounded-lg border border-slate-200 bg-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                  <option value="DEFECTIVE">Product is defective / damaged</option>
                  <option value="WRONG_ITEM">Wrong item delivered</option>
                  <option value="NOT_AS_DESCRIBED">Item does not match description</option>
                  <option value="QUALITY_ISSUE">Poor quality / unsatisfied</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Comments (Optional)</label>
                <textarea
                  value={returnComments}
                  onChange={(e) => setReturnComments(e.target.value)}
                  placeholder="Provide additional details..."
                  className="w-full p-2.5 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-purple-500 min-h-[70px]"
                />
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-6">
              <button
                type="button"
                onClick={() => setShowReturnModal(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg transition"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={returning}
                onClick={handleRequestReturn}
                className="px-4 py-2 text-xs font-bold text-white bg-purple-600 hover:bg-purple-700 rounded-lg transition shadow disabled:opacity-50"
              >
                {returning ? 'Submitting...' : 'Submit Request'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
