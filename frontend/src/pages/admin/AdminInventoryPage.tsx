import React, { useState, useEffect } from 'react';
import { adminService } from '@/services/adminService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import type { InventoryItem } from '@/types';
import {
  Boxes,
  Search,
  SlidersHorizontal,
  X,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

export const AdminInventoryPage: React.FC = () => {
  const dispatch = useAppDispatch();

  const [items, setItems] = useState<InventoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filter, setFilter] = useState(''); // 'LOW_STOCK' | 'OUT_OF_STOCK' | ''
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  // Adjustment Modal
  const [adjustingItem, setAdjustingItem] = useState<InventoryItem | null>(null);
  const [adjustQty, setAdjustQty] = useState<number>(0);
  const [adjustNotes, setAdjustNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadInventory = () => {
    setLoading(true);
    adminService
      .getInventory({
        q: searchTerm || undefined,
        filter: filter || undefined,
        page,
        pageSize: 15,
      })
      .then((res) => {
        setItems(res.items);
        setTotalPages(res.totalPages || 1);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadInventory();
  }, [page, filter]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    loadInventory();
  };

  const handleOpenAdjust = (item: InventoryItem) => {
    setAdjustingItem(item);
    setAdjustQty(0);
    setAdjustNotes('');
  };

  const handleAdjustSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!adjustingItem || adjustQty === 0) return;

    setSubmitting(true);
    try {
      await adminService.adjustStock({
        productId: adjustingItem.productId,
        quantity: adjustQty,
        notes: adjustNotes.trim() || undefined,
      });
      dispatch(showToast({ message: 'Stock adjusted successfully', type: 'success' }));
      setAdjustingItem(null);
      loadInventory();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to adjust stock', type: 'error' }));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900">Inventory & Stock Control</h1>
          <p className="text-xs text-slate-500">Monitor stock levels, reorder thresholds, and adjust counts</p>
        </div>
      </div>

      {/* Filter and Search */}
      <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
        <form onSubmit={handleSearch} className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Search by product, SKU..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </form>

        <div className="flex items-center gap-2">
          <button
            onClick={() => {
              setFilter('');
              setPage(1);
            }}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              filter === '' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            All Stock
          </button>
          <button
            onClick={() => {
              setFilter('LOW_STOCK');
              setPage(1);
            }}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              filter === 'LOW_STOCK'
                ? 'bg-amber-500 text-white'
                : 'bg-amber-50 text-amber-700 hover:bg-amber-100'
            }`}
          >
            Low Stock
          </button>
          <button
            onClick={() => {
              setFilter('OUT_OF_STOCK');
              setPage(1);
            }}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              filter === 'OUT_OF_STOCK'
                ? 'bg-rose-500 text-white'
                : 'bg-rose-50 text-rose-700 hover:bg-rose-100'
            }`}
          >
            Out of Stock
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16 text-center">
            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-xs text-slate-500">Checking stock levels...</p>
          </div>
        ) : items.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs italic">No inventory records found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Product</th>
                  <th className="px-6 py-3">SKU</th>
                  <th className="px-6 py-3">Available Stock</th>
                  <th className="px-6 py-3">Low Stock Threshold</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Adjustment</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {items.map((item) => {
                  const isLow = item.isLowStock ?? (item.quantityAvailable <= (item.lowStockThreshold || 5));
                  const isOut = item.isOutOfStock ?? (item.quantityAvailable <= 0);

                  return (
                    <tr key={item.inventoryId || item.productId} className="hover:bg-slate-50/50 transition">
                      <td className="px-6 py-4">
                        <span className="font-bold text-slate-900 block">{item.productName}</span>
                      </td>
                      <td className="px-6 py-4 font-mono text-slate-600">{item.sku}</td>
                      <td className="px-6 py-4 font-black text-slate-900 text-sm">{item.quantityAvailable}</td>
                      <td className="px-6 py-4 text-slate-500">{item.lowStockThreshold || 5} units</td>
                      <td className="px-6 py-4">
                        {isOut ? (
                          <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-rose-100 text-rose-800 uppercase">
                            Out of Stock
                          </span>
                        ) : isLow ? (
                          <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800 uppercase">
                            Low Stock
                          </span>
                        ) : (
                          <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 uppercase">
                            Sufficient
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button
                          onClick={() => handleOpenAdjust(item)}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded border border-slate-200 text-slate-700 hover:bg-slate-100 font-semibold"
                        >
                          <SlidersHorizontal className="w-3.5 h-3.5" />
                          Adjust
                        </button>
                      </td>
                    </tr>
                  );
                })}
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

      {/* Adjust Modal */}
      {adjustingItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 text-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-6 shadow-2xl relative">
            <button
              onClick={() => setAdjustingItem(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-base font-bold text-slate-900 mb-1">Adjust Stock</h3>
            <p className="text-slate-500 mb-3">{adjustingItem.productName}</p>

            <div className="p-3 bg-slate-50 rounded-xl mb-4 text-slate-700 flex justify-between">
              <span>Current Stock:</span>
              <strong className="text-slate-900">{adjustingItem.quantityAvailable} units</strong>
            </div>

            <form onSubmit={handleAdjustSubmit} className="space-y-4">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Change Quantity (e.g. +10 to add, -5 to deduct)
                </label>
                <input
                  type="number"
                  required
                  value={adjustQty}
                  onChange={(e) => setAdjustQty(parseInt(e.target.value) || 0)}
                  placeholder="e.g. 10 or -5"
                  className="w-full p-2.5 rounded-lg border border-slate-200 font-mono text-sm"
                />
                <span className="text-[11px] text-slate-400 block mt-1">
                  New stock will be: <strong>{adjustingItem.quantityAvailable + adjustQty}</strong>
                </span>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Reason / Notes</label>
                <textarea
                  value={adjustNotes}
                  onChange={(e) => setAdjustNotes(e.target.value)}
                  placeholder="Restock shipment #104, breakage, physical audit..."
                  className="w-full p-2.5 rounded-lg border border-slate-200 min-h-[60px]"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setAdjustingItem(null)}
                  className="px-4 py-2 font-semibold text-slate-600 hover:bg-slate-100 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting || adjustQty === 0}
                  className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary/90 rounded-lg shadow disabled:opacity-50"
                >
                  {submitting ? 'Updating...' : 'Save Stock'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
