import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { orderService } from '@/services/orderService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import type { Order, OrderTracking } from '@/types';
import {
  Package,
  Truck,
  RotateCcw,
  XCircle,
  Clock,
  CheckCircle,
  ExternalLink,
  ChevronRight,
  X,
} from 'lucide-react';

export const OrdersPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  // Tracking Modal State
  const [trackingOrder, setTrackingOrder] = useState<Order | null>(null);
  const [trackingData, setTrackingData] = useState<OrderTracking | null>(null);
  const [trackingLoading, setTrackingLoading] = useState(false);

  // Cancel Modal State
  const [cancellingOrderId, setCancellingOrderId] = useState<number | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling] = useState(false);

  // Return Modal State
  const [returningOrder, setReturningOrder] = useState<Order | null>(null);
  const [returnReason, setReturnReason] = useState('DEFECTIVE');
  const [returnComments, setReturnComments] = useState('');
  const [returning, setReturning] = useState(false);

  const loadOrders = () => {
    setLoading(true);
    orderService
      .getMyOrders(statusFilter || undefined, page, 10)
      .then((res) => {
        setOrders(res.items);
        setTotalPages(res.totalPages || 1);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadOrders();
  }, [statusFilter, page]);

  const handleOpenTracking = (order: Order) => {
    setTrackingOrder(order);
    setTrackingLoading(true);
    orderService
      .getLiveTracking(order.orderId)
      .then(setTrackingData)
      .catch(() => {})
      .finally(() => setTrackingLoading(false));
  };

  const handleCancelOrder = async () => {
    if (!cancellingOrderId || !cancelReason.trim()) return;
    setCancelling(true);
    try {
      await orderService.cancelOrder(cancellingOrderId, cancelReason.trim());
      dispatch(showToast({ message: 'Order cancelled successfully', type: 'success' }));
      setCancellingOrderId(null);
      setCancelReason('');
      loadOrders();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to cancel order', type: 'error' }));
    } finally {
      setCancelling(false);
    }
  };

  const handleRequestReturn = async () => {
    if (!returningOrder || !returningOrder.items.length) return;
    setReturning(true);
    try {
      await orderService.requestReturn(returningOrder.orderId, {
        orderItemId: returningOrder.items[0].orderItemId,
        reason: returnReason,
        comments: returnComments.trim() || undefined,
      });
      dispatch(showToast({ message: 'Return request submitted successfully', type: 'success' }));
      setReturningOrder(null);
      setReturnComments('');
      loadOrders();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to submit return', type: 'error' }));
    } finally {
      setReturning(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'DELIVERED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'DISPATCHED':
      case 'IN_TRANSIT':
      case 'OUT_FOR_DELIVERY':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      default:
        return 'bg-amber-100 text-amber-800 border-amber-200';
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Page Title & Status Tabs */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-gray-200 pb-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-gray-900">My Orders</h1>
          <p className="text-xs text-gray-500 mt-1">Track live shipments, review order history, or request returns</p>
        </div>

        {/* Filter Pills */}
        <div className="flex items-center gap-1 overflow-x-auto pb-1 text-xs">
          {['', 'PENDING', 'CONFIRMED', 'DISPATCHED', 'DELIVERED', 'CANCELLED'].map((st) => (
            <button
              key={st}
              onClick={() => {
                setStatusFilter(st);
                setPage(1);
              }}
              className={`px-3 py-1.5 rounded-full font-bold whitespace-nowrap transition ${
                statusFilter === st
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {st || 'All Orders'}
            </button>
          ))}
        </div>
      </div>

      {/* Orders List */}
      {loading ? (
        <div className="space-y-4 animate-pulse">
          {[1, 2, 3].map((n) => (
            <div key={n} className="h-44 bg-gray-200 rounded-3xl"></div>
          ))}
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-gray-200 p-8 space-y-4">
          <Package className="w-16 h-16 text-gray-300 mx-auto" />
          <h3 className="font-bold text-lg text-gray-800">No orders found</h3>
          <p className="text-xs text-gray-500 max-w-sm mx-auto">
            You don't have any orders under this filter. Browse our catalog and start shopping!
          </p>
          <Link to="/products" className="inline-block px-6 py-2.5 bg-blue-600 text-white rounded-full text-xs font-bold">
            Shop Now
          </Link>
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((order) => (
            <div
              key={order.orderId}
              className="bg-white rounded-3xl border border-gray-200 shadow-xs overflow-hidden transition hover:border-gray-300"
            >
              {/* Order Card Header */}
              <div className="bg-gray-50 px-6 py-4 border-b border-gray-100 flex flex-wrap items-center justify-between gap-4 text-xs">
                <div className="flex items-center gap-6">
                  <div>
                    <span className="text-gray-400 block font-medium">ORDER PLACED</span>
                    <span className="font-bold text-gray-900">{order.createdAt?.slice(0, 10)}</span>
                  </div>
                  <div>
                    <span className="text-gray-400 block font-medium">TOTAL AMOUNT</span>
                    <span className="font-bold text-gray-900">₹{order.totalAmount.toLocaleString('en-IN')}</span>
                  </div>
                  <div>
                    <span className="text-gray-400 block font-medium">SHIP TO</span>
                    <span className="font-bold text-gray-900 truncate max-w-xs block">
                      {order.shippingFullName}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span className={`text-[11px] font-black px-3 py-1 rounded-full border ${getStatusColor(order.orderStatus)}`}>
                    {order.orderStatus}
                  </span>
                  <span className="text-gray-400 font-mono text-[11px]">#{order.orderNumber}</span>
                </div>
              </div>

              {/* Order Items & Actions */}
              <div className="p-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
                <div className="space-y-4 flex-1">
                  {order.items.map((item) => (
                    <div key={item.orderItemId} className="flex items-center gap-4">
                      <img
                        src={item.primaryImageUrl || '/assets/images/products/placeholder.png'}
                        alt={item.productName}
                        className="w-16 h-16 object-contain rounded-xl border p-1 bg-gray-50"
                      />
                      <div>
                        <Link
                          to={`/products/${item.productId}`}
                          className="font-bold text-sm text-gray-900 hover:text-blue-600 line-clamp-1"
                        >
                          {item.productName}
                        </Link>
                        <p className="text-xs text-gray-500 mt-0.5">
                          Qty: {item.quantity} &times; ₹{item.unitPrice}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Right Actions */}
                <div className="flex flex-wrap md:flex-col gap-2 w-full md:w-44 text-xs font-bold">
                  <button
                    onClick={() => handleOpenTracking(order)}
                    className="flex-1 md:flex-none py-2 px-3 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-xl transition flex items-center justify-center gap-1.5"
                  >
                    <Truck className="w-3.5 h-3.5" />
                    <span>Track Shipment</span>
                  </button>

                  {order.canCancel && (
                    <button
                      onClick={() => setCancellingOrderId(order.orderId)}
                      className="flex-1 md:flex-none py-2 px-3 border border-red-200 text-red-600 hover:bg-red-50 rounded-xl transition flex items-center justify-center gap-1.5"
                    >
                      <XCircle className="w-3.5 h-3.5" />
                      <span>Cancel Order</span>
                    </button>
                  )}

                  {order.canReturn && (
                    <button
                      onClick={() => setReturningOrder(order)}
                      className="flex-1 md:flex-none py-2 px-3 border border-purple-200 text-purple-600 hover:bg-purple-50 rounded-xl transition flex items-center justify-center gap-1.5"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      <span>Return / Refund</span>
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Live Tracking Modal */}
      {trackingOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-lg w-full space-y-6 shadow-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b pb-3">
              <div>
                <h3 className="font-bold text-base text-gray-900 flex items-center gap-2">
                  <Truck className="w-5 h-5 text-blue-600" />
                  <span>Shipment Telemetry</span>
                </h3>
                <p className="text-xs text-gray-400">Order #{trackingOrder.orderNumber}</p>
              </div>
              <button onClick={() => setTrackingOrder(null)} className="p-1 text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {trackingLoading ? (
              <p className="text-center py-6 text-xs text-gray-500">Querying carrier logistics network...</p>
            ) : trackingData ? (
              <div className="space-y-6">
                <div className="grid grid-cols-2 gap-3 p-4 bg-gray-50 rounded-2xl text-xs">
                  <div>
                    <span className="text-gray-400 block font-medium">COURIER PARTNER</span>
                    <span className="font-bold text-gray-900">{trackingData.courierPartner || 'BlueDart Express'}</span>
                  </div>
                  <div>
                    <span className="text-gray-400 block font-medium">AWB NUMBER</span>
                    <span className="font-bold text-gray-900">{trackingData.trackingNumber || 'N/A'}</span>
                  </div>
                  {trackingData.estimatedDelivery && (
                    <div className="col-span-2 pt-2 border-t border-gray-200">
                      <span className="text-gray-400 block font-medium">ESTIMATED ARRIVAL</span>
                      <span className="font-bold text-emerald-700">{trackingData.estimatedDelivery}</span>
                    </div>
                  )}
                </div>

                {/* Tracking Milestones */}
                <div className="space-y-3">
                  <h4 className="text-xs font-bold text-gray-800 uppercase tracking-wider">Checkpoint Scan History</h4>
                  {trackingData.events?.length ? (
                    <div className="space-y-3 border-l-2 border-blue-500 pl-4 ml-2">
                      {trackingData.events.map((ev, idx) => (
                        <div key={idx} className="relative text-xs space-y-0.5">
                          <div className="w-2.5 h-2.5 rounded-full bg-blue-600 absolute -left-[21px] top-1"></div>
                          <p className="font-bold text-gray-900">{ev.description || ev.status}</p>
                          <p className="text-gray-500">{ev.location} &bull; {ev.timestamp}</p>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-xs text-gray-500 italic">Package has been dispatched and telemetry scans are pending.</p>
                  )}
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* Cancel Order Modal */}
      {cancellingOrderId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-gray-900">Cancel Order</h3>
            <p className="text-xs text-gray-600">
              Are you sure you want to cancel this order? Any payments will be immediately refunded.
            </p>
            <div>
              <label className="text-xs font-bold text-gray-700 block mb-1">Reason for cancellation:</label>
              <select
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full p-2 border rounded-xl text-xs"
              >
                <option value="">Select reason...</option>
                <option value="Changed my mind">Changed my mind</option>
                <option value="Ordered by mistake">Ordered by mistake</option>
                <option value="Found a better deal">Found a better deal</option>
                <option value="Delivery time is too long">Delivery time is too long</option>
              </select>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setCancellingOrderId(null)}
                className="px-4 py-2 border rounded-xl text-xs font-bold text-gray-700"
              >
                Keep Order
              </button>
              <button
                onClick={handleCancelOrder}
                disabled={!cancelReason || cancelling}
                className="px-4 py-2 bg-red-600 text-white rounded-xl text-xs font-bold disabled:opacity-50"
              >
                {cancelling ? 'Cancelling...' : 'Confirm Cancel'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Return Order Modal */}
      {returningOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-3xl p-6 max-w-md w-full space-y-4 shadow-2xl">
            <h3 className="font-bold text-base text-gray-900">Request Order Return</h3>
            <p className="text-xs text-gray-600">
              Returns are covered under our 7-day satisfaction policy. We will arrange a free pickup from your address.
            </p>
            <div>
              <label className="text-xs font-bold text-gray-700 block mb-1">Reason for Return</label>
              <select
                value={returnReason}
                onChange={(e) => setReturnReason(e.target.value)}
                className="w-full p-2 border rounded-xl text-xs"
              >
                <option value="DEFECTIVE">Product is defective / not working</option>
                <option value="WRONG_ITEM">Received wrong item or size</option>
                <option value="DAMAGED_TRANSIT">Item damaged during delivery</option>
                <option value="QUALITY_NOT_EXPECTED">Quality did not meet expectations</option>
              </select>
            </div>
            <div>
              <label className="text-xs font-bold text-gray-700 block mb-1">Additional Remarks (Optional)</label>
              <textarea
                rows={3}
                placeholder="Provide any specific details regarding the issue..."
                value={returnComments}
                onChange={(e) => setReturnComments(e.target.value)}
                className="w-full p-2 border rounded-xl text-xs"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setReturningOrder(null)}
                className="px-4 py-2 border rounded-xl text-xs font-bold text-gray-700"
              >
                Cancel
              </button>
              <button
                onClick={handleRequestReturn}
                disabled={returning}
                className="px-5 py-2 bg-purple-600 text-white rounded-xl text-xs font-bold disabled:opacity-50"
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
