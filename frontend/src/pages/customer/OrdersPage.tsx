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
  Copy,
  Check,
  Navigation,
  MapPin,
  ShieldCheck,
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
  const [copiedAwb, setCopiedAwb] = useState(false);

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
                        src={item.primaryImageUrl || '/placeholder.svg'}
                        alt={item.productName}
                        className="w-16 h-16 object-contain rounded-xl border p-1 bg-gray-50"
                        onError={(e) => {
                          (e.target as HTMLElement).setAttribute('src', '/placeholder.svg');
                        }}
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
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-3 sm:p-4">
          <div className="bg-white rounded-3xl p-5 sm:p-7 max-w-xl w-full space-y-6 shadow-2xl max-h-[90vh] overflow-y-auto border border-slate-100">
            {/* Modal Header */}
            <div className="flex items-start justify-between border-b border-slate-100 pb-4">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
                    <Truck className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-black text-lg text-slate-900">Shipment Tracking</h3>
                    <p className="text-xs text-slate-500 font-medium">Real-time carrier scans and transit telemetry</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 pt-1">
                  <span className="text-xs font-mono font-bold text-slate-700 bg-slate-100 px-2.5 py-1 rounded-lg">
                    #{trackingOrder.orderNumber}
                  </span>
                  <span
                    className={`text-[11px] font-black px-2.5 py-0.5 rounded-full uppercase tracking-wider ${
                      trackingOrder.orderStatus === 'DELIVERED'
                        ? 'bg-emerald-100 text-emerald-800'
                        : trackingOrder.orderStatus === 'CANCELLED'
                        ? 'bg-red-100 text-red-800'
                        : ['SHIPPED', 'DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'].includes(trackingOrder.orderStatus)
                        ? 'bg-blue-100 text-blue-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {trackingOrder.orderStatus?.replace(/_/g, ' ')}
                  </span>
                </div>
              </div>
              <button
                onClick={() => setTrackingOrder(null)}
                className="p-2 text-slate-400 hover:text-slate-700 rounded-xl hover:bg-slate-100 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {trackingLoading ? (
              <div className="py-12 text-center space-y-3">
                <div className="w-10 h-10 border-3 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
                <p className="text-xs text-slate-500 font-medium">Fetching real-time updates from fulfillment & carrier network...</p>
              </div>
            ) : trackingData ? (
              <div className="space-y-6">
                {/* Milestone Stepper */}
                {trackingOrder.orderStatus !== 'CANCELLED' ? (
                  <div className="bg-slate-50/80 p-4 sm:p-5 rounded-2xl border border-slate-100">
                    <h4 className="text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-4">Delivery Milestone</h4>
                    {(() => {
                      const curStatus = (trackingData.status || trackingOrder.orderStatus || '').toUpperCase();
                      let currentStep = 0;
                      if (curStatus === 'DELIVERED') currentStep = 4;
                      else if (curStatus === 'OUT_FOR_DELIVERY') currentStep = 3;
                      else if (['SHIPPED', 'DISPATCHED', 'IN_TRANSIT'].includes(curStatus)) currentStep = 2;
                      else if (['PROCESSING', 'PACKED'].includes(curStatus)) currentStep = 1;

                      const steps = [
                        { title: 'Confirmed', sub: 'Verified' },
                        { title: 'Packed', sub: 'Warehouse' },
                        { title: 'Shipped', sub: 'In Transit' },
                        { title: 'Out For Delivery', sub: 'Nearby' },
                        { title: 'Delivered', sub: 'Received' },
                      ];

                      return (
                        <div className="relative flex items-center justify-between">
                          {/* Connecting Bar */}
                          <div className="absolute left-3 right-3 top-4 h-1 bg-slate-200 -z-0">
                            <div
                              className="h-full bg-blue-600 transition-all duration-500 rounded-full"
                              style={{ width: `${(currentStep / (steps.length - 1)) * 100}%` }}
                            ></div>
                          </div>

                          {steps.map((st, idx) => {
                            const isDone = idx <= currentStep;
                            const isCurrent = idx === currentStep;

                            return (
                              <div key={idx} className="relative z-10 flex flex-col items-center text-center">
                                <div
                                  className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs transition-all duration-300 ${
                                    isCurrent
                                      ? 'bg-blue-600 text-white ring-4 ring-blue-100 shadow-md scale-110'
                                      : isDone
                                      ? 'bg-blue-600 text-white'
                                      : 'bg-white border-2 border-slate-300 text-slate-400'
                                  }`}
                                >
                                  {isDone ? <Check className="w-4 h-4 stroke-[3]" /> : idx + 1}
                                </div>
                                <span className={`text-[10px] sm:text-xs font-bold mt-2 whitespace-nowrap ${isDone ? 'text-slate-900' : 'text-slate-400'}`}>
                                  {st.title}
                                </span>
                                <span className="text-[9px] text-slate-400 hidden sm:block">{st.sub}</span>
                              </div>
                            );
                          })}
                        </div>
                      );
                    })()}
                  </div>
                ) : (
                  <div className="p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center gap-3 text-red-700">
                    <XCircle className="w-5 h-5 flex-shrink-0" />
                    <div>
                      <p className="font-bold text-xs">Order Cancelled</p>
                      <p className="text-[11px] text-red-600">This shipment was cancelled. Any refund has been credited to source.</p>
                    </div>
                  </div>
                )}

                {/* Carrier & AWB Details */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-slate-400 block font-bold uppercase tracking-wider">Courier Partner</span>
                      <span className="font-bold text-sm text-slate-900 flex items-center gap-1.5 mt-0.5">
                        <Truck className="w-4 h-4 text-blue-600" />
                        {trackingData.courierPartner || trackingOrder.courierPartner || 'ShopKart Express Network'}
                      </span>
                    </div>
                  </div>

                  <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-slate-400 block font-bold uppercase tracking-wider">Air Waybill (AWB)</span>
                      <span className="font-mono font-bold text-sm text-slate-900 block mt-0.5">
                        {trackingData.trackingNumber || trackingOrder.trackingNumber || 'Pending Courier Dispatch'}
                      </span>
                    </div>
                    {(trackingData.trackingNumber || trackingOrder.trackingNumber) && (
                      <button
                        onClick={() => {
                          const awb = trackingData.trackingNumber || trackingOrder.trackingNumber || '';
                          navigator.clipboard.writeText(awb);
                          setCopiedAwb(true);
                          setTimeout(() => setCopiedAwb(false), 2000);
                          dispatch(showToast({ message: 'AWB copied to clipboard', type: 'info' }));
                        }}
                        className="p-2 hover:bg-white rounded-xl text-slate-500 hover:text-blue-600 transition shadow-xs border border-transparent hover:border-slate-200"
                        title="Copy AWB"
                      >
                        {copiedAwb ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
                      </button>
                    )}
                  </div>

                  {(trackingData.estimatedDelivery || trackingOrder.formattedEstimatedDeliveryDate) && (
                    <div className="sm:col-span-2 p-3 bg-emerald-50 rounded-2xl border border-emerald-100 flex items-center justify-between text-xs">
                      <span className="text-emerald-800 font-medium flex items-center gap-1.5">
                        <Clock className="w-4 h-4 text-emerald-600" />
                        Expected Delivery By:
                      </span>
                      <span className="font-black text-emerald-900">
                        {trackingData.estimatedDelivery || trackingOrder.formattedEstimatedDeliveryDate}
                      </span>
                    </div>
                  )}
                </div>

                {/* Checkpoint Scan Timeline */}
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
                      <Navigation className="w-3.5 h-3.5 text-blue-600" />
                      <span>Checkpoint Telemetry & Status Log</span>
                    </h4>
                    <span className="text-[10px] text-slate-400 font-semibold">
                      {trackingData.events?.length || 0} scan checkpoints
                    </span>
                  </div>

                  {trackingData.events && trackingData.events.length > 0 ? (
                    <div className="space-y-4 relative before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-blue-200 pl-7">
                      {trackingData.events.map((ev, idx) => (
                        <div key={idx} className="relative text-xs space-y-1">
                          <div
                            className={`w-3 h-3 rounded-full absolute -left-[23px] top-1 border-2 bg-white ${
                              idx === 0 ? 'border-blue-600 bg-blue-600 ring-4 ring-blue-100' : 'border-slate-400'
                            }`}
                          ></div>
                          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1">
                            <p className="font-bold text-slate-900">{ev.description || ev.status}</p>
                            <span className="text-[10px] font-semibold text-slate-400 font-mono whitespace-nowrap">
                              {ev.timestamp}
                            </span>
                          </div>
                          {ev.location && (
                            <p className="text-[11px] text-slate-500 flex items-center gap-1">
                              <MapPin className="w-3 h-3 text-slate-400" />
                              <span>{ev.location}</span>
                            </p>
                          )}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-center text-xs text-slate-500">
                      Order has been confirmed and scheduled for logistics pickup. Telemetry scans will populate as carrier processes the shipment.
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="text-center py-6 text-xs text-slate-500">
                No tracking information is currently available for this order.
              </div>
            )}
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
