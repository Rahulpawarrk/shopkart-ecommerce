import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { adminService } from '@/services/adminService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import type { Order } from '@/types';
import {
  Package,
  Search,
  Eye,
  Edit,
  X,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

export const AdminOrdersPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const dispatch = useAppDispatch();

  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  // Detail Modal State
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [statusUpdating, setStatusUpdating] = useState(false);

  const [statusForm, setStatusForm] = useState({
    status: 'PROCESSING',
    courierPartner: '',
    trackingNumber: '',
    notes: '',
  });

  const loadOrders = () => {
    setLoading(true);
    adminService
      .getOrders({
        q: searchTerm || undefined,
        status: statusFilter || undefined,
        page,
        pageSize: 10,
      })
      .then((res) => {
        setOrders(res.items);
        setTotalPages(res.totalPages || 1);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    const qOrderId = searchParams.get('orderId');
    if (qOrderId) {
      adminService.getOrderById(Number(qOrderId)).then((order) => {
        setSelectedOrder(order);
      }).catch(() => {});
    }
  }, [searchParams]);

  useEffect(() => {
    loadOrders();
  }, [page, statusFilter]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    loadOrders();
  };

  const handleOpenStatusModal = (order: Order) => {
    setSelectedOrder(order);
    setStatusForm({
      status: order.orderStatus,
      courierPartner: order.courierPartner || '',
      trackingNumber: order.trackingNumber || '',
      notes: '',
    });
    setStatusModalOpen(true);
  };

  const handleUpdateStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedOrder) return;
    setStatusUpdating(true);
    try {
      await adminService.updateOrderStatus(selectedOrder.orderId, {
        status: statusForm.status,
        courierPartner: statusForm.courierPartner.trim() || undefined,
        trackingNumber: statusForm.trackingNumber.trim() || undefined,
        notes: statusForm.notes.trim() || undefined,
      });
      dispatch(showToast({ message: 'Order status updated successfully', type: 'success' }));
      setStatusModalOpen(false);
      loadOrders();
      if (selectedOrder) {
        adminService.getOrderById(selectedOrder.orderId).then(setSelectedOrder).catch(() => {});
      }
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to update status', type: 'error' }));
    } finally {
      setStatusUpdating(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DELIVERED':
        return 'bg-emerald-100 text-emerald-800';
      case 'SHIPPED':
      case 'OUT_FOR_DELIVERY':
        return 'bg-blue-100 text-blue-800';
      case 'PROCESSING':
      case 'CONFIRMED':
        return 'bg-amber-100 text-amber-800';
      case 'CANCELLED':
        return 'bg-rose-100 text-rose-800';
      default:
        return 'bg-slate-100 text-slate-800';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900">Order Management</h1>
          <p className="text-xs text-slate-500">Track shipments, dispatch orders, and manage fulfillment</p>
        </div>
      </div>

      {/* Filter and Search */}
      <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
        <form onSubmit={handleSearch} className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Search by order #, email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </form>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(1);
            }}
            className="text-xs border border-slate-200 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="PROCESSING">Processing</option>
            <option value="SHIPPED">Shipped</option>
            <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16 text-center">
            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-xs text-slate-500">Loading orders...</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs italic">No orders found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Order Number</th>
                  <th className="px-6 py-3">Customer</th>
                  <th className="px-6 py-3">Date</th>
                  <th className="px-6 py-3">Payment</th>
                  <th className="px-6 py-3">Total</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {orders.map((o) => (
                  <tr key={o.orderId} className="hover:bg-slate-50/50 transition">
                    <td className="px-6 py-4 font-bold text-slate-900 font-mono">#{o.orderNumber}</td>
                    <td className="px-6 py-4">
                      <span className="font-semibold text-slate-800 block">
                        {o.shippingFullName || 'Customer'}
                      </span>
                      <span className="text-[10px] text-slate-400">{o.shippingPhone}</span>
                    </td>
                    <td className="px-6 py-4 text-slate-500">
                      {new Date(o.createdAt).toLocaleDateString('en-IN', {
                        day: 'numeric',
                        month: 'short',
                        year: 'numeric',
                      })}
                    </td>
                    <td className="px-6 py-4">
                      <span className="font-bold text-slate-700 block uppercase text-[11px]">{o.paymentMethod}</span>
                      <span
                        className={`text-[10px] font-bold ${
                          o.paymentStatus === 'PAID' ? 'text-emerald-600' : 'text-amber-600'
                        }`}
                      >
                        {o.paymentStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-black text-slate-900">
                      ₹{Number(o.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase ${getStatusBadge(o.orderStatus)}`}>
                        {o.orderStatus.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => setSelectedOrder(o)}
                          className="p-1.5 text-slate-500 hover:text-slate-900 rounded hover:bg-slate-100"
                          title="View order snapshot"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleOpenStatusModal(o)}
                          className="p-1.5 text-slate-500 hover:text-primary rounded hover:bg-slate-100"
                          title="Update fulfillment status"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="px-6 py-3 border-t border-slate-100 flex items-center justify-between text-xs">
            <span className="text-slate-500">
              Page {page} of {totalPages}
            </span>
            <div className="flex gap-1">
              <button
                disabled={page <= 1}
                onClick={() => setPage(page - 1)}
                className="p-1.5 border border-slate-200 rounded disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                disabled={page >= totalPages}
                onClick={() => setPage(page + 1)}
                className="p-1.5 border border-slate-200 rounded disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Order Detail Modal */}
      {selectedOrder && !statusModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-2xl w-full p-6 shadow-2xl relative my-8 text-xs">
            <button
              onClick={() => setSelectedOrder(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3 mb-4">
              <Package className="w-6 h-6 text-primary" />
              <div>
                <h3 className="text-base font-bold text-slate-900">Order #{selectedOrder.orderNumber}</h3>
                <span className={`inline-block px-2 py-0.5 rounded-full text-[10px] font-bold uppercase mt-1 ${getStatusBadge(selectedOrder.orderStatus)}`}>
                  {selectedOrder.orderStatus.replace(/_/g, ' ')}
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 p-4 bg-slate-50 rounded-xl mb-4 border border-slate-100">
              <div>
                <span className="font-bold text-slate-700 block mb-1">Customer & Shipping:</span>
                <p className="font-semibold text-slate-900">{selectedOrder.shippingFullName}</p>
                <p className="text-slate-600">{selectedOrder.shippingAddressLine1}</p>
                <p className="text-slate-600">
                  {selectedOrder.shippingCity}, {selectedOrder.shippingState} - {selectedOrder.shippingPostalCode}
                </p>
                {selectedOrder.shippingPhone && (
                  <p className="text-slate-600 font-medium">Phone: {selectedOrder.shippingPhone}</p>
                )}
              </div>
              <div>
                <span className="font-bold text-slate-700 block mb-1">Fulfillment Details:</span>
                <p>Courier: <strong>{selectedOrder.courierPartner || 'Not assigned'}</strong></p>
                <p>Tracking #: <strong>{selectedOrder.trackingNumber || 'Not assigned'}</strong></p>
                <p>Payment: <strong className="uppercase">{selectedOrder.paymentMethod}</strong> ({selectedOrder.paymentStatus})</p>
                <p>Order Total: <strong className="text-slate-900">₹{Number(selectedOrder.totalAmount).toLocaleString('en-IN')}</strong></p>
              </div>
            </div>

            <h4 className="font-bold text-slate-900 mb-2">Order Items ({selectedOrder.items?.length || 0})</h4>
            <div className="divide-y divide-slate-100 border border-slate-100 rounded-xl overflow-hidden mb-4">
              {selectedOrder.items?.map((item) => (
                <div key={item.orderItemId} className="p-3 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <img
                      src={item.primaryImageUrl || '/placeholder.png'}
                      alt={item.productName}
                      className="w-10 h-10 object-cover rounded bg-slate-50 border border-slate-200"
                    />
                    <div>
                      <p className="font-bold text-slate-900">{item.productName}</p>
                      <p className="text-[11px] text-slate-400">SKU: {item.sku}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-slate-600 font-medium">
                      {item.quantity} × ₹{Number(item.effectivePrice || item.unitPrice).toLocaleString('en-IN')}
                    </p>
                    <p className="font-bold text-slate-900">
                      ₹{Number(item.lineTotal).toLocaleString('en-IN')}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            <div className="flex justify-between items-center pt-2">
              <button
                type="button"
                onClick={() => setSelectedOrder(null)}
                className="px-4 py-2 border border-slate-200 font-semibold text-slate-600 rounded-lg hover:bg-slate-50"
              >
                Close
              </button>
              <button
                type="button"
                onClick={() => setStatusModalOpen(true)}
                className="px-4 py-2 bg-primary text-white font-bold rounded-lg hover:bg-primary/90 shadow"
              >
                Update Status
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Update Status Modal */}
      {statusModalOpen && selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl relative text-xs">
            <button
              onClick={() => setStatusModalOpen(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-base font-bold text-slate-900 mb-1">
              Fulfillment Status — #{selectedOrder.orderNumber}
            </h3>
            <p className="text-slate-500 mb-4">Advance fulfillment state and assign logistics</p>

            <form onSubmit={handleUpdateStatus} className="space-y-4">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Status Transition *</label>
                <select
                  value={statusForm.status}
                  onChange={(e) => setStatusForm({ ...statusForm, status: e.target.value })}
                  className="w-full p-2.5 rounded-lg border border-slate-200 bg-white font-bold"
                >
                  <option value="CONFIRMED">CONFIRMED</option>
                  <option value="PROCESSING">PROCESSING</option>
                  <option value="SHIPPED">SHIPPED</option>
                  <option value="OUT_FOR_DELIVERY">OUT FOR DELIVERY</option>
                  <option value="DELIVERED">DELIVERED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Courier Partner</label>
                <input
                  type="text"
                  placeholder="e.g. BlueDart, Delhivery, DTDC"
                  value={statusForm.courierPartner}
                  onChange={(e) => setStatusForm({ ...statusForm, courierPartner: e.target.value })}
                  className="w-full p-2.5 rounded-lg border border-slate-200"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Tracking Number (AWB)</label>
                <input
                  type="text"
                  placeholder="AWB12345678"
                  value={statusForm.trackingNumber}
                  onChange={(e) => setStatusForm({ ...statusForm, trackingNumber: e.target.value })}
                  className="w-full p-2.5 rounded-lg border border-slate-200 font-mono"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Internal Notes</label>
                <textarea
                  placeholder="Optional fulfillment remarks..."
                  value={statusForm.notes}
                  onChange={(e) => setStatusForm({ ...statusForm, notes: e.target.value })}
                  className="w-full p-2.5 rounded-lg border border-slate-200 min-h-[60px]"
                />
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setStatusModalOpen(false)}
                  className="px-4 py-2 font-semibold text-slate-600 hover:bg-slate-100 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={statusUpdating}
                  className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary/90 rounded-lg shadow disabled:opacity-50"
                >
                  {statusUpdating ? 'Updating...' : 'Save Status'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
