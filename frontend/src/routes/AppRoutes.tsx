import React from 'react';
import { Routes, Route, Navigate, useLocation, Outlet, Link } from 'react-router-dom';
import { useAppSelector } from '@/store';

// Layout & Common
import { Navbar } from '@/components/layout/Navbar';
import { Footer } from '@/components/layout/Footer';
import { CartDrawer } from '@/components/layout/CartDrawer';
import { ToastContainer } from '@/components/common/ToastContainer';

// Storefront Pages
import { HomePage } from '@/pages/store/HomePage';
import { ProductListingPage } from '@/pages/store/ProductListingPage';
import { ProductDetailPage } from '@/pages/store/ProductDetailPage';
import { CartPage } from '@/pages/cart/CartPage';
import { CheckoutPage } from '@/pages/checkout/CheckoutPage';
import { OrderConfirmationPage } from '@/pages/checkout/OrderConfirmationPage';

// Customer Pages
import { OrdersPage } from '@/pages/customer/OrdersPage';
import { OrderDetailPage } from '@/pages/customer/OrderDetailPage';
import { TrackOrderPage } from '@/pages/customer/TrackOrderPage';
import { ProfilePage } from '@/pages/customer/ProfilePage';
import { AddressBookPage } from '@/pages/customer/AddressBookPage';
import { WishlistPage } from '@/pages/customer/WishlistPage';

// Auth Pages
import { LoginPage } from '@/pages/auth/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage';
import { ForgotPasswordPage } from '@/pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from '@/pages/auth/ResetPasswordPage';

// Admin Pages
import { AdminLayout } from '@/pages/admin/AdminLayout';
import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage';
import { AdminProductsPage } from '@/pages/admin/AdminProductsPage';
import { AdminCategoriesPage } from '@/pages/admin/AdminCategoriesPage';
import { AdminOrdersPage } from '@/pages/admin/AdminOrdersPage';
import { AdminInventoryPage } from '@/pages/admin/AdminInventoryPage';
import { AdminCouponsPage } from '@/pages/admin/AdminCouponsPage';
import { AdminReturnsPage } from '@/pages/admin/AdminReturnsPage';
import { AdminAuditLogsPage } from '@/pages/admin/AdminAuditLogsPage';

// Protected Route Wrapper (Requires Logged-In User)
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAuthenticated, isLoading } = useAppSelector((state) => state.auth);
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated && !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

// Admin Route Wrapper (Requires ADMIN role)
const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAuthenticated, isLoading } = useAppSelector((state) => state.auth);
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900 text-white">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!user.admin && !user.roles?.includes('ADMIN') && !user.roles?.includes('ROLE_ADMIN')) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

// Public & Customer Layout
const StorefrontLayout: React.FC = () => (
  <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
    <Navbar />
    <main className="flex-1">
      <Outlet />
    </main>
    <Footer />
    <CartDrawer />
    <ToastContainer />
  </div>
);

// 404 Not Found Page
const NotFoundPage: React.FC = () => (
  <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
    <h1 className="text-7xl font-black text-slate-200 mb-2">404</h1>
    <h2 className="text-2xl font-bold text-slate-900 mb-2">Page Not Found</h2>
    <p className="text-slate-500 text-sm max-w-sm mb-6">
      The page you are looking for doesn't exist or may have been moved.
    </p>
    <Link
      to="/"
      className="px-6 py-2.5 bg-primary text-white text-xs font-bold rounded-xl hover:bg-primary/90 transition shadow-md"
    >
      Return to Storefront
    </Link>
  </div>
);

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Storefront Layout Routes */}
      <Route element={<StorefrontLayout />}>
        {/* Public Storefront */}
        <Route path="/" element={<HomePage />} />
        <Route path="/products" element={<ProductListingPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/cart" element={<CartPage />} />

        {/* Authentication */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Customer Protected Routes */}
        <Route
          path="/wishlist"
          element={
            <ProtectedRoute>
              <WishlistPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/order-confirmation/:id"
          element={
            <ProtectedRoute>
              <OrderConfirmationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <OrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute>
              <OrderDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders/:id/track"
          element={
            <ProtectedRoute>
              <TrackOrderPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/addresses"
          element={
            <ProtectedRoute>
              <AddressBookPage />
            </ProtectedRoute>
          }
        />

        {/* 404 Storefront */}
        <Route path="*" element={<NotFoundPage />} />
      </Route>

      {/* Admin Protected Console */}
      <Route
        path="/admin"
        element={
          <AdminRoute>
            <AdminLayout />
          </AdminRoute>
        }
      >
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboardPage />} />
        <Route path="products" element={<AdminProductsPage />} />
        <Route path="categories" element={<AdminCategoriesPage />} />
        <Route path="orders" element={<AdminOrdersPage />} />
        <Route path="inventory" element={<AdminInventoryPage />} />
        <Route path="coupons" element={<AdminCouponsPage />} />
        <Route path="returns" element={<AdminReturnsPage />} />
        <Route path="audit-logs" element={<AdminAuditLogsPage />} />
      </Route>
    </Routes>
  );
};
