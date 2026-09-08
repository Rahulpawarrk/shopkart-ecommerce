import React, { useState, useEffect } from 'react';
import { adminService } from '@/services/adminService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import type { User } from '@/types';
import {
  Users,
  UserPlus,
  Search,
  ShieldCheck,
  Mail,
  Phone,
  Calendar,
  X,
  Eye,
  EyeOff,
  CheckCircle2,
  AlertCircle,
  ShieldAlert,
} from 'lucide-react';

export const AdminUsersPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const [admins, setAdmins] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState<string | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);

  const loadAdmins = () => {
    setLoading(true);
    adminService
      .getAdmins()
      .then((data) => setAdmins(data || []))
      .catch((err: any) => {
        dispatch(showToast({ message: err.message || 'Failed to load admin users', type: 'error' }));
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadAdmins();
  }, []);

  const handleOpenModal = () => {
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
    });
    setModalError(null);
    setShowAddModal(true);
  };

  const handleCreateAdmin = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      setModalError('First and last name are required.');
      return;
    }
    if (!formData.email.trim()) {
      setModalError('Email address is required.');
      return;
    }
    if (!/^\d{10}$/.test(formData.phone.trim())) {
      setModalError('Phone number must be a valid 10-digit mobile number.');
      return;
    }
    if (formData.password.length < 8) {
      setModalError('Password must be at least 8 characters long.');
      return;
    }
    if (formData.password !== formData.confirmPassword) {
      setModalError('Passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      await adminService.createAdmin({
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone.trim(),
        password: formData.password,
        confirmPassword: formData.confirmPassword,
      });

      dispatch(showToast({ message: `Administrator ${formData.email} created successfully!`, type: 'success' }));
      setShowAddModal(false);
      loadAdmins();
    } catch (err: any) {
      setModalError(err.message || 'Failed to create admin user.');
    } finally {
      setSubmitting(false);
    }
  };

  const filteredAdmins = admins.filter((a) => {
    const q = search.toLowerCase();
    return (
      a.email.toLowerCase().includes(q) ||
      (a.fullName && a.fullName.toLowerCase().includes(q)) ||
      (a.firstName && a.firstName.toLowerCase().includes(q)) ||
      (a.lastName && a.lastName.toLowerCase().includes(q)) ||
      (a.phone && a.phone.includes(q))
    );
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 flex items-center gap-2.5">
            <Users className="w-7 h-7 text-primary" />
            <span>Admin User Management</span>
          </h1>
          <p className="text-xs text-slate-500">
            Create and manage verified staff accounts with administrative privileges
          </p>
        </div>
        <button
          onClick={handleOpenModal}
          className="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-primary hover:bg-primary/90 text-white text-xs font-bold rounded-xl shadow-md transition"
        >
          <UserPlus className="w-4 h-4" />
          <span>Add New Admin</span>
        </button>
      </div>

      {/* Summary Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-black">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-semibold text-slate-400 block uppercase">Total Administrators</span>
            <span className="text-xl font-black text-slate-900">{admins.length}</span>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-black">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-semibold text-slate-400 block uppercase">Active Accounts</span>
            <span className="text-xl font-black text-slate-900">{admins.filter((a) => a.status !== 'SUSPENDED').length}</span>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center font-black">
            <ShieldAlert className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-semibold text-slate-400 block uppercase">Role Authority</span>
            <span className="text-xl font-black text-purple-700">SUPER_ADMIN</span>
          </div>
        </div>
      </div>

      {/* Search Bar */}
      <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs">
        <div className="relative max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search admins by name, email, or mobile..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-medium focus:bg-white focus:outline-none focus:border-primary transition"
          />
        </div>
      </div>

      {/* Admins Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="text-center py-16 text-slate-400 text-xs">Loading administrators...</div>
        ) : filteredAdmins.length === 0 ? (
          <div className="text-center py-16 text-slate-400 text-xs">
            {search ? 'No administrators matching search criteria' : 'No administrator accounts configured'}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                  <th className="py-3.5 px-4">Administrator</th>
                  <th className="py-3.5 px-4">Contact</th>
                  <th className="py-3.5 px-4">Role Privileges</th>
                  <th className="py-3.5 px-4">Account Status</th>
                  <th className="py-3.5 px-4">Registered Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredAdmins.map((admin) => (
                  <tr key={admin.userId} className="hover:bg-slate-50/80 transition">
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-primary/10 text-primary font-black flex items-center justify-center text-xs">
                          {admin.firstName?.charAt(0) || 'A'}
                        </div>
                        <div>
                          <span className="font-bold text-slate-900 block">
                            {admin.firstName} {admin.lastName}
                          </span>
                          <span className="text-[11px] text-slate-400 flex items-center gap-1">
                            <Mail className="w-3 h-3" /> {admin.email}
                          </span>
                        </div>
                      </div>
                    </td>
                    <td className="py-3.5 px-4 font-medium text-slate-600">
                      {admin.phone ? (
                        <span className="flex items-center gap-1.5 text-slate-700">
                          <Phone className="w-3 h-3 text-slate-400" />
                          {admin.phone}
                        </span>
                      ) : (
                        <span className="text-slate-400">N/A</span>
                      )}
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="flex flex-wrap gap-1">
                        {admin.roles && admin.roles.length > 0 ? (
                          admin.roles.map((r) => (
                            <span
                              key={r}
                              className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-purple-100 text-purple-800 border border-purple-200"
                            >
                              {r}
                            </span>
                          ))
                        ) : (
                          <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-blue-100 text-blue-800">
                            ADMIN
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider ${
                          admin.status === 'ACTIVE' || !admin.status
                            ? 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                            : 'bg-rose-100 text-rose-800 border border-rose-200'
                        }`}
                      >
                        {admin.status || 'ACTIVE'}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-500 font-medium text-[11px]">
                      <span className="flex items-center gap-1.5">
                        <Calendar className="w-3 h-3 text-slate-400" />
                        {admin.createdAt ? new Date(admin.createdAt).toLocaleDateString('en-IN') : 'Existing'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add New Admin Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-7 shadow-2xl relative border border-slate-200 text-xs">
            <button
              onClick={() => setShowAddModal(false)}
              className="absolute top-5 right-5 p-1 rounded-full text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-2xl bg-primary/10 text-primary flex items-center justify-center">
                <UserPlus className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-black text-slate-900">Provision Admin Account</h3>
                <p className="text-[11px] text-slate-500">Create a privileged account with full admin console access</p>
              </div>
            </div>

            {modalError && (
              <div className="mb-4 p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{modalError}</span>
              </div>
            )}

            <form onSubmit={handleCreateAdmin} className="space-y-3.5">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">First Name *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Priya"
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    className="w-full p-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-primary font-medium"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Last Name *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Sharma"
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    className="w-full p-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-primary font-medium"
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Email Address *</label>
                <input
                  type="email"
                  required
                  placeholder="admin.name@shopkart.com"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="w-full p-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-primary font-medium"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Mobile Phone (10 digits) *</label>
                <input
                  type="tel"
                  required
                  maxLength={10}
                  placeholder="9876543210"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value.replace(/\D/g, '') })}
                  className="w-full p-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-primary font-mono font-medium"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Password *</label>
                  <div className="relative">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      required
                      minLength={8}
                      placeholder="Min 8 characters"
                      value={formData.password}
                      onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                      className="w-full p-2.5 pr-8 rounded-xl border border-slate-200 focus:outline-none focus:border-primary font-medium"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-2.5 top-2.5 text-slate-400 hover:text-slate-600"
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Confirm Password *</label>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    minLength={8}
                    placeholder="Repeat password"
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    className="w-full p-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-primary font-medium"
                  />
                </div>
              </div>

              <div className="p-3 bg-amber-50 rounded-xl border border-amber-200 text-amber-800 text-[11px] flex items-start gap-2">
                <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
                <span>
                  Admin accounts have full control over catalog, inventory, order processing, and system settings.
                </span>
              </div>

              <div className="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 font-bold text-slate-600 hover:bg-slate-100 rounded-xl transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary/90 rounded-xl shadow-md transition disabled:opacity-50 flex items-center gap-1.5"
                >
                  {submitting ? 'Registering Admin...' : 'Create Admin Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
