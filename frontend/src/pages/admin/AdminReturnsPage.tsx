import React, { useState, useEffect } from 'react';
import { adminService } from '@/services/adminService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import { RotateCcw, Search, CheckCircle, XCircle, DollarSign, X } from 'lucide-react';

export const AdminReturnsPage: React.FC = () => {
  const dispatch = useAppDispatch();

  const [returns, setReturns] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  // Status Modal
  const [selectedReturn, setSelectedReturn] = useState<any | null>(null);
  const [status, setStatus] = useState('APPROVED');
  const [refundAmount, setRefundAmount] = useState<number>(0);
  const [adminNotes, setAdminNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadReturns = () => {
    setLoading(true);
    adminService
      .getReturns({ status: statusFilter || undefined, page, pageSize: 10 })
      .then((res) => {
        setReturns(res.items);
        setTotalPages(res.totalPages || 1);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadReturns();
  }, [page, statusFilter]);

  const handleOpenAction = (ret: any) => {
    setSelectedReturn(ret);
    setStatus(ret.status === 'PENDING' ? 'APPROVED' : ret.status);
    setRefundAmount(ret.refundAmount || ret.itemPrice || 0);
    setAdminNotes(ret.adminNotes || '');
  };

  const handleStatusSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedReturn) return;

    setSubmitting(true);
    try {
      await adminService.updateReturnStatus(selectedReturn.returnId || selectedReturn.returnRequestId, {
        status,
        refundAmount: status === 'REFUNDED' || status === 'APPROVED' ? refundAmount : 0,
        adminNotes: adminNotes.trim() || undefined,
      });
      dispatch(showToast({ message: 'Return request updated', type: 'success' }));
      setSelectedReturn(null);
      loadReturns();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to update return', type: 'error' }));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900">Returns & Refunds</h1>
          <p className="text-xs text-slate-500">Process customer return claims, inspection results, and refund credits</p>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm flex items-center justify-between">
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(1);
          }}
          className="text-xs border border-slate-200 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-primary"
        >
          <option value="">All Claims</option>
          <option value="PENDING">Pending Review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="REFUNDED">Refund Completed</option>
        </select>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16 text-center">
            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-xs text-slate-500">Loading return requests...</p>
          </div>
        ) : returns.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs italic">No return requests found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Return ID</th>
                  <th className="px-6 py-3">Order #</th>
                  <th className="px-6 py-3">Reason</th>
                  <th className="px-6 py-3">Date</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {returns.map((r) => (
                  <tr key={r.returnId || r.returnRequestId} className="hover:bg-slate-50/50 transition">
                    <td className="px-6 py-4 font-mono font-bold text-slate-900">
                      #{r.returnId || r.returnRequestId}
                    </td>
                    <td className="px-6 py-4 text-slate-800 font-medium">#{r.orderNumber || r.orderId}</td>
                    <td className="px-6 py-4">
                      <span className="font-semibold text-slate-900 block">{r.reason}</span>
                      {r.comments && <span className="text-[10px] text-slate-400 block">{r.comments}</span>}
                    </td>
                    <td className="px-6 py-4 text-slate-500">
                      {new Date(r.createdAt || Date.now()).toLocaleDateString('en-IN')}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                          r.status === 'REFUNDED'
                            ? 'bg-emerald-100 text-emerald-800'
                            : r.status === 'APPROVED'
                            ? 'bg-blue-100 text-blue-800'
                            : r.status === 'REJECTED'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {r.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleOpenAction(r)}
                        className="px-3 py-1 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded font-semibold text-[11px]"
                      >
                        Process
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Action Modal */}
      {selectedReturn && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 text-xs">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl relative">
            <button
              onClick={() => setSelectedReturn(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-base font-bold text-slate-900 mb-1">
              Process Return #{selectedReturn.returnId || selectedReturn.returnRequestId}
            </h3>
            <p className="text-slate-500 mb-4">Update status and authorize customer refunds</p>

            <form onSubmit={handleStatusSubmit} className="space-y-4">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Decision / Status *</label>
                <select
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                  className="w-full p-2.5 rounded-lg border border-slate-200 bg-white font-bold"
                >
                  <option value="APPROVED">APPROVED (Awaiting Pickup)</option>
                  <option value="REFUNDED">REFUNDED (Credit Initiated)</option>
                  <option value="REJECTED">REJECTED</option>
                </select>
              </div>

              {(status === 'APPROVED' || status === 'REFUNDED') && (
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Refund Amount (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={refundAmount}
                    onChange={(e) => setRefundAmount(parseFloat(e.target.value) || 0)}
                    className="w-full p-2.5 rounded-lg border border-slate-200 font-mono"
                  />
                </div>
              )}

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Resolution Comments</label>
                <textarea
                  value={adminNotes}
                  onChange={(e) => setAdminNotes(e.target.value)}
                  placeholder="Notes explaining approval or rejection reason..."
                  className="w-full p-2.5 rounded-lg border border-slate-200 min-h-[60px]"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setSelectedReturn(null)}
                  className="px-4 py-2 font-semibold text-slate-600 hover:bg-slate-100 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary/90 rounded-lg shadow disabled:opacity-50"
                >
                  {submitting ? 'Saving...' : 'Confirm Decision'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
