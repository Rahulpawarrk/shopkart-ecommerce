import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminService } from '@/services/adminService';
import type { AdminDashboardStats, Order } from '@/types';
import {
  DollarSign,
  ShoppingBag,
  Package,
  Users,
  AlertTriangle,
  TrendingUp,
  ArrowRight,
  ExternalLink,
} from 'lucide-react';

export const AdminDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<AdminDashboardStats | null>(null);
  const [salesReport, setSalesReport] = useState<{
    monthlySales: Array<{ label: string; amount: number; count: number }>;
    categoryRevenue: Array<{ label: string; amount: number; count: number }>;
  } | null>(null);
  const [recentOrders, setRecentOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      adminService.getDashboardStats().catch(() => null),
      adminService.getSalesReport().catch(() => null),
      adminService.getOrders({ page: 1, pageSize: 5 }).catch(() => null),
    ])
      .then(([statsData, reportData, ordersData]) => {
        if (statsData) setStats(statsData);
        if (reportData) setSalesReport(reportData);
        if (ordersData) setRecentOrders(ordersData.items || []);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="py-16 text-center">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm text-slate-500">Loading dashboard analytics...</p>
      </div>
    );
  }

  const kpis = [
    {
      title: 'Total Revenue',
      value: `₹${(stats?.totalRevenue || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,
      icon: DollarSign,
      color: 'bg-emerald-50 text-emerald-600 border-emerald-200',
    },
    {
      title: 'Total Orders',
      value: (stats?.totalOrders || 0).toLocaleString('en-IN'),
      icon: Package,
      color: 'bg-blue-50 text-blue-600 border-blue-200',
    },
    {
      title: 'Products in Catalog',
      value: (stats?.totalProducts || 0).toLocaleString('en-IN'),
      icon: ShoppingBag,
      color: 'bg-purple-50 text-purple-600 border-purple-200',
    },
    {
      title: 'Active Customers',
      value: (stats?.totalCustomers || 0).toLocaleString('en-IN'),
      icon: Users,
      color: 'bg-indigo-50 text-indigo-600 border-indigo-200',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div>
        <h1 className="text-2xl font-black text-slate-900">Executive Dashboard</h1>
        <p className="text-xs text-slate-500 mt-1">Real-time overview of business metrics and store operations</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {kpis.map((kpi, idx) => (
          <div
            key={idx}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm flex items-center justify-between"
          >
            <div>
              <p className="text-xs font-semibold text-slate-500">{kpi.title}</p>
              <h3 className="text-xl font-black text-slate-900 mt-1">{kpi.value}</h3>
            </div>
            <div className={`w-12 h-12 rounded-xl border flex items-center justify-center ${kpi.color}`}>
              <kpi.icon className="w-6 h-6" />
            </div>
          </div>
        ))}
      </div>

      {/* Inventory Alert Banner */}
      {stats && stats.lowStockCount > 0 && (
        <div className="bg-amber-50 border border-amber-200 rounded-2xl p-4 flex items-center justify-between text-amber-800">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0" />
            <span className="text-xs font-bold">
              {stats.lowStockCount} items are running low on inventory!
            </span>
          </div>
          <Link
            to="/admin/inventory"
            className="text-xs font-black underline hover:text-amber-900 flex items-center gap-1"
          >
            Manage Stock <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      )}

      {/* Sales Charts / Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Monthly Sales Performance */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-primary" />
              Monthly Sales Velocity
            </h3>
            <span className="text-xs text-slate-400">Current Year</span>
          </div>

          {salesReport?.monthlySales && salesReport.monthlySales.length > 0 ? (
            <div className="space-y-3 pt-2">
              {salesReport.monthlySales.map((month, i) => {
                const maxAmount = Math.max(...salesReport.monthlySales.map((m) => m.amount), 1);
                const percent = Math.min(100, Math.round((month.amount / maxAmount) * 100));

                return (
                  <div key={i} className="text-xs">
                    <div className="flex justify-between font-semibold text-slate-700 mb-1">
                      <span>{month.label}</span>
                      <span className="text-slate-900">
                        ₹{Number(month.amount).toLocaleString('en-IN')} ({month.count} orders)
                      </span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                      <div
                        className="bg-primary h-2.5 rounded-full transition-all duration-500"
                        style={{ width: `${percent}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-8 text-center italic">No monthly sales data recorded yet.</p>
          )}
        </div>

        {/* Category Revenue Distribution */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-slate-900 text-sm">Revenue by Category</h3>
          </div>

          {salesReport?.categoryRevenue && salesReport.categoryRevenue.length > 0 ? (
            <div className="space-y-3 pt-2">
              {salesReport.categoryRevenue.map((cat, i) => {
                const maxAmount = Math.max(...salesReport.categoryRevenue.map((c) => c.amount), 1);
                const percent = Math.min(100, Math.round((cat.amount / maxAmount) * 100));

                return (
                  <div key={i} className="text-xs">
                    <div className="flex justify-between font-semibold text-slate-700 mb-1">
                      <span>{cat.label}</span>
                      <span className="text-slate-900">₹{Number(cat.amount).toLocaleString('en-IN')}</span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                      <div
                        className="bg-emerald-500 h-2.5 rounded-full transition-all duration-500"
                        style={{ width: `${percent}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-8 text-center italic">No category revenue data available.</p>
          )}
        </div>
      </div>

      {/* Recent Orders Overview */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between">
          <h3 className="font-bold text-slate-900 text-base">Recent Orders</h3>
          <Link
            to="/admin/orders"
            className="text-xs font-bold text-primary hover:underline flex items-center gap-1"
          >
            View All Orders <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {recentOrders.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-xs italic">No orders received yet.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Order #</th>
                  <th className="px-6 py-3">Date</th>
                  <th className="px-6 py-3">Customer</th>
                  <th className="px-6 py-3">Amount</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {recentOrders.map((order) => (
                  <tr key={order.orderId} className="hover:bg-slate-50/50 transition">
                    <td className="px-6 py-4 font-bold text-slate-900">#{order.orderNumber}</td>
                    <td className="px-6 py-4 text-slate-500">
                      {new Date(order.createdAt).toLocaleDateString('en-IN', {
                        day: 'numeric',
                        month: 'short',
                        year: 'numeric',
                      })}
                    </td>
                    <td className="px-6 py-4 text-slate-800">
                      {order.shippingFullName || 'Customer'}
                    </td>
                    <td className="px-6 py-4 font-bold text-slate-900">
                      ₹{Number(order.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-1 text-[10px] font-bold rounded-full bg-slate-100 text-slate-800 uppercase">
                        {order.orderStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Link
                        to={`/admin/orders?orderId=${order.orderId}`}
                        className="text-primary font-bold hover:underline"
                      >
                        Manage
                      </Link>
                    </td>
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
