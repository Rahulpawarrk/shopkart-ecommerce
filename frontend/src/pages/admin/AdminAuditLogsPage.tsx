import React, { useState, useEffect } from 'react';
import { adminService } from '@/services/adminService';
import type { AuditLog } from '@/types';
import { ClipboardList, Shield, RefreshCw } from 'lucide-react';

export const AdminAuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);

  const loadLogs = () => {
    setLoading(true);
    adminService
      .getAuditLogs(100)
      .then(setLogs)
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadLogs();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900">Security & Audit Logs</h1>
          <p className="text-xs text-slate-500">Immutable trace of administrative events, price changes, and fulfillments</p>
        </div>
        <button
          onClick={loadLogs}
          className="inline-flex items-center gap-1.5 px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 shadow-sm"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          Refresh
        </button>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16 text-center">
            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-xs text-slate-500">Loading audit trail...</p>
          </div>
        ) : logs.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs italic">No audit records found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Timestamp</th>
                  <th className="px-6 py-3">Action</th>
                  <th className="px-6 py-3">Entity</th>
                  <th className="px-6 py-3">Entity ID</th>
                  <th className="px-6 py-3">User ID</th>
                  <th className="px-6 py-3">IP Address</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono text-[11px]">
                {logs.map((log) => (
                  <tr key={log.logId} className="hover:bg-slate-50/50 transition">
                    <td className="px-6 py-3.5 text-slate-500">
                      {new Date(log.createdAt).toLocaleString('en-IN')}
                    </td>
                    <td className="px-6 py-3.5 font-bold text-slate-900">{log.action}</td>
                    <td className="px-6 py-3.5 text-slate-700">{log.entityName}</td>
                    <td className="px-6 py-3.5 text-slate-500">{log.entityId || '—'}</td>
                    <td className="px-6 py-3.5 text-slate-700 font-semibold">{log.userId || 'SYSTEM'}</td>
                    <td className="px-6 py-3.5 text-slate-400">{log.ipAddress || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
