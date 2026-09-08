import React, { useState, useEffect } from 'react';
import { adminService } from '@/services/adminService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import type { PaymentReconciliation, ReconciliationStats } from '@/types';
import {
  Scale,
  Search,
  AlertTriangle,
  CheckCircle2,
  Clock,
  DollarSign,
  X,
  CreditCard,
  ChevronLeft,
  ChevronRight,
  FileText,
} from 'lucide-react';

export const AdminReconciliationPage: React.FC = () => {
  const dispatch = useAppDispatch();

  const [records, setRecords] = useState<PaymentReconciliation[]>([]);
  const [stats, setStats] = useState<ReconciliationStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  // Investigation / Resolution Modal
  const [selectedRecord, setSelectedRecord] = useState<PaymentReconciliation | null>(null);
  const [resolutionStatus, setResolutionStatus] = useState('RESOLVED');
  const [adminNotes, setAdminNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadData = () => {
    setLoading(true);
    Promise.all([
      adminService.getReconciliationList({
        q: searchTerm.trim() || undefined,
        status: statusFilter !== 'ALL' ? statusFilter : undefined,
        page,
        pageSize: 10,
      }),
      adminService.getReconciliationStats(),
    ])
      .then(([listRes, statsRes]) => {
        setRecords(listRes.items || []);
        setTotalPages(listRes.totalPages || 1);
        setStats(statsRes);
      })
      .catch((err) => {
        dispatch(showToast({ message: err.message || 'Failed to load reconciliation records', type: 'error' }));
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();
  }, [page, statusFilter]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    loadData();
  };

  const handleOpenAction = (rec: PaymentReconciliation) => {
    setSelectedRecord(rec);
    setResolutionStatus(rec.reconciliationStatus === 'PENDING' ? 'INVESTIGATING' : rec.reconciliationStatus);
    setAdminNotes(rec.adminNotes || '');
  };

  const handleResolutionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRecord) return;

    setSubmitting(true);
    try {
      await adminService.updateReconciliation(selectedRecord.reconciliationId, {
        reconciliationStatus: resolutionStatus,
        adminNotes: adminNotes.trim() || undefined,
      });
      dispatch(showToast({ message: 'Reconciliation status updated successfully', type: 'success' }));
      setSelectedRecord(null);
      loadData();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to update reconciliation', type: 'error' }));
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'RESOLVED':
      case 'REFUND_COMPLETED':
      case 'MANUALLY_CREDITED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      case 'INVESTIGATING':
      case 'VERIFIED_DEBITED':
      case 'REFUND_INITIATED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'NOT_DEBITED':
        return 'bg-slate-100 text-slate-700 border-slate-200';
      default:
        return 'bg-amber-100 text-amber-800 border-amber-200';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <Scale className="w-7 h-7 text-primary" />
            <h1 className="text-2xl font-black text-slate-900">Payment Reconciliation</h1>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Audit failed online transactions, resolve bank debits, log refund references, and reconcile ledger records.
          </p>
        </div>
      </div>

      {/* KPI Stats Overview */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
              <AlertTriangle className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Pending Audit</p>
              <h3 className="text-xl font-black text-slate-900">{stats.pendingAudit || 0}</h3>
            </div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Verified Debited</p>
              <h3 className="text-xl font-black text-slate-900">{stats.verifiedDebited || 0}</h3>
            </div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Resolved / Settled</p>
              <h3 className="text-xl font-black text-slate-900">{stats.resolvedCount || 0}</h3>
            </div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center font-bold">
              <DollarSign className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Total Disputed</p>
              <h3 className="text-xl font-black text-slate-900">
                ₹{Number(stats.totalDisputedAmount || 0).toLocaleString('en-IN')}
              </h3>
            </div>
          </div>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          {['ALL', 'PENDING', 'INVESTIGATING', 'VERIFIED_DEBITED', 'REFUND_COMPLETED', 'RESOLVED'].map((tab) => (
            <button
              key={tab}
              onClick={() => {
                setStatusFilter(tab);
                setPage(1);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                statusFilter === tab
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {tab.replace(/_/g, ' ')}
            </button>
          ))}
        </div>

        <form onSubmit={handleSearch} className="flex items-center gap-2 w-full sm:w-auto">
          <div className="relative flex-1 sm:w-64">
            <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
            <input
              type="text"
              placeholder="Search order #, customer, txn..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-primary bg-slate-50 focus:bg-white transition"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 bg-primary text-white text-xs font-bold rounded-xl hover:bg-primary/90 transition shadow-xs flex-shrink-0"
          >
            Search
          </button>
        </form>
      </div>

      {/* Table Section */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16 text-center">
            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-xs text-slate-500">Loading reconciliation records...</p>
          </div>
        ) : records.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs italic">
            No failed or disputed payments matching the current filter.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Order #</th>
                  <th className="px-6 py-3">Customer</th>
                  <th className="px-6 py-3">Amount</th>
                  <th className="px-6 py-3">Method & Txn Ref</th>
                  <th className="px-6 py-3">Failure Reason</th>
                  <th className="px-6 py-3">Date</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {records.map((rec) => (
                  <tr key={rec.reconciliationId} className="hover:bg-slate-50/50 transition">
                    <td className="px-6 py-4">
                      <span className="font-mono font-bold text-slate-900 block">
                        #{rec.orderNumber || rec.orderId}
                      </span>
                      <span className="text-[10px] text-slate-400 block font-mono">ID: {rec.reconciliationId}</span>
                    </td>
                    <td className="px-6 py-4">
                      <p className="font-bold text-slate-900">{rec.customerName || 'Customer'}</p>
                      <p className="text-[11px] text-slate-500">{rec.customerEmail || '-'}</p>
                      {rec.customerPhone && <p className="text-[10px] text-slate-400">{rec.customerPhone}</p>}
                    </td>
                    <td className="px-6 py-4 font-bold text-slate-900">
                      ₹{Number(rec.amount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1 font-bold text-[11px] text-slate-800 uppercase">
                        <CreditCard className="w-3 h-3 text-primary" />
                        {rec.paymentMethod || 'ONLINE'}
                      </span>
                      <span className="block font-mono text-[10px] text-slate-400 truncate max-w-[140px]" title={rec.transactionReference}>
                        {rec.transactionReference}
                      </span>
                    </td>
                    <td className="px-6 py-4 max-w-xs">
                      <span className="text-slate-800 block truncate" title={rec.failureReason}>
                        {rec.failureReason || 'Transaction Declined'}
                      </span>
                      {rec.adminNotes && (
                        <span className="text-[10px] text-blue-600 block truncate mt-0.5" title={rec.adminNotes}>
                          Note: {rec.adminNotes}
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-slate-500 whitespace-nowrap">
                      {new Date(rec.createdAt).toLocaleDateString('en-IN', {
                        day: 'numeric',
                        month: 'short',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-block px-2.5 py-1 rounded-full text-[10px] font-bold border ${getStatusBadge(rec.reconciliationStatus)}`}>
                        {rec.reconciliationStatus.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleOpenAction(rec)}
                        className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-bold transition shadow-xs inline-flex items-center gap-1"
                      >
                        <FileText className="w-3 h-3" />
                        <span>Resolve</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
            <div>
              Page {page} of {totalPages}
            </div>
            <div className="flex items-center gap-1">
              <button
                disabled={page <= 1}
                onClick={() => setPage((p) => p - 1)}
                className="p-2 border border-slate-200 rounded-lg hover:bg-slate-50 disabled:opacity-40 transition"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                disabled={page >= totalPages}
                onClick={() => setPage((p) => p + 1)}
                className="p-2 border border-slate-200 rounded-lg hover:bg-slate-50 disabled:opacity-40 transition"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Investigation / Resolution Modal */}
      {selectedRecord && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <h3 className="text-base font-bold text-slate-900">Reconcile Order #{selectedRecord.orderNumber}</h3>
                <p className="text-xs text-slate-400">Transaction Ref: {selectedRecord.transactionReference}</p>
              </div>
              <button
                onClick={() => setSelectedRecord(null)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Quick Summary Context */}
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200/80 space-y-1.5 text-xs text-slate-700">
              <div className="flex justify-between">
                <span className="text-slate-500">Customer:</span>
                <span className="font-semibold">{selectedRecord.customerName} ({selectedRecord.customerEmail})</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Disputed Amount:</span>
                <span className="font-bold text-slate-900">₹{Number(selectedRecord.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Payment Gateway Order:</span>
                <span className="font-mono text-slate-700">{selectedRecord.gatewayOrderId || 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Failure Reason:</span>
                <span className="text-rose-600 font-medium">{selectedRecord.failureReason || 'Declined'}</span>
              </div>
            </div>

            <form onSubmit={handleResolutionSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Reconciliation Status</label>
                <select
                  value={resolutionStatus}
                  onChange={(e) => setResolutionStatus(e.target.value)}
                  className="w-full text-xs border border-slate-200 rounded-xl px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-primary font-medium"
                >
                  <option value="PENDING">Pending Investigation</option>
                  <option value="INVESTIGATING">Under Bank Verification</option>
                  <option value="VERIFIED_DEBITED">Verified Amount Debited from Customer</option>
                  <option value="REFUND_INITIATED">Refund Initiated to Customer Bank</option>
                  <option value="REFUND_COMPLETED">Refund Completed</option>
                  <option value="NOT_DEBITED">Amount Not Debited (Confirmed with Bank)</option>
                  <option value="MANUALLY_CREDITED">Manually Credited / Order Confirmed</option>
                  <option value="RESOLVED">Resolved / Case Closed</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Investigation & Resolution Notes
                </label>
                <textarea
                  rows={3}
                  value={adminNotes}
                  onChange={(e) => setAdminNotes(e.target.value)}
                  placeholder="e.g. Bank UTR verified, customer query resolved, refund reference #..."
                  className="w-full text-xs border border-slate-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              {selectedRecord.resolverName && (
                <p className="text-[11px] text-slate-400 italic">
                  Last resolved by {selectedRecord.resolverName} on{' '}
                  {selectedRecord.resolvedAt ? new Date(selectedRecord.resolvedAt).toLocaleString('en-IN') : 'N/A'}
                </p>
              )}

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setSelectedRecord(null)}
                  className="px-4 py-2 border border-slate-200 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-primary text-white text-xs font-bold rounded-xl hover:bg-primary/90 transition shadow disabled:opacity-50"
                >
                  {submitting ? 'Saving...' : 'Save Resolution'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
