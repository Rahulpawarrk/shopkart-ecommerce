import React from 'react';
import { Routes, Route, Navigate, useLocation, Outlet, Link } from 'react-router-dom';
import { useAppSelector } from '@/store';

// Layout & Common
import { Navbar } from '@/components/layout/Navbar';
import { Footer } from '@/components/layout/Footer';
import { CartDrawer } from '@/components/layout/CartDrawer';
import { ToastContainer } from '@/components/common/ToastContainer';

// Storefront Critical Page (statically imported for instant LCP)
import { HomePage } from '@/pages/store/HomePage';

// Storefront Secondary Pages (Lazy Loaded)
const ProductListingPage = React.lazy(() =>
  import('@/pages/store/ProductListingPage').then((m) => ({ default: m.ProductListingPage }))
);
const ProductDetailPage = React.lazy(() =>
  import('@/pages/store/ProductDetailPage').then((m) => ({ default: m.ProductDetailPage }))
);
const CartPage = React.lazy(() =>
  import('@/pages/cart/CartPage').then((m) => ({ default: m.CartPage }))
);
const CheckoutPage = React.lazy(() =>
  import('@/pages/checkout/CheckoutPage').then((m) => ({ default: m.CheckoutPage }))
);
const OrderConfirmationPage = React.lazy(() =>
  import('@/pages/checkout/OrderConfirmationPage').then((m) => ({ default: m.OrderConfirmationPage }))
);

// Customer Pages (Lazy Loaded)
const OrdersPage = React.lazy(() =>
  import('@/pages/customer/OrdersPage').then((m) => ({ default: m.OrdersPage }))
);
const OrderDetailPage = React.lazy(() =>
  import('@/pages/customer/OrderDetailPage').then((m) => ({ default: m.OrderDetailPage }))
);
const TrackOrderPage = React.lazy(() =>
  import('@/pages/customer/TrackOrderPage').then((m) => ({ default: m.TrackOrderPage }))
);
const ProfilePage = React.lazy(() =>
  import('@/pages/customer/ProfilePage').then((m) => ({ default: m.ProfilePage }))
);
const AddressBookPage = React.lazy(() =>
  import('@/pages/customer/AddressBookPage').then((m) => ({ default: m.AddressBookPage }))
);
const WishlistPage = React.lazy(() =>
  import('@/pages/customer/WishlistPage').then((m) => ({ default: m.WishlistPage }))
);

// Auth Pages (Lazy Loaded)
const LoginPage = React.lazy(() =>
  import('@/pages/auth/LoginPage').then((m) => ({ default: m.LoginPage }))
);
const RegisterPage = React.lazy(() =>
  import('@/pages/auth/RegisterPage').then((m) => ({ default: m.RegisterPage }))
);
const ForgotPasswordPage = React.lazy(() =>
  import('@/pages/auth/ForgotPasswordPage').then((m) => ({ default: m.ForgotPasswordPage }))
);
const ResetPasswordPage = React.lazy(() =>
  import('@/pages/auth/ResetPasswordPage').then((m) => ({ default: m.ResetPasswordPage }))
);

// Admin Pages (Lazy Loaded)
const AdminLayout = React.lazy(() =>
  import('@/pages/admin/AdminLayout').then((m) => ({ default: m.AdminLayout }))
);
const AdminDashboardPage = React.lazy(() =>
  import('@/pages/admin/AdminDashboardPage').then((m) => ({ default: m.AdminDashboardPage }))
);
const AdminProductsPage = React.lazy(() =>
  import('@/pages/admin/AdminProductsPage').then((m) => ({ default: m.AdminProductsPage }))
);
const AdminCategoriesPage = React.lazy(() =>
  import('@/pages/admin/AdminCategoriesPage').then((m) => ({ default: m.AdminCategoriesPage }))
);
const AdminOrdersPage = React.lazy(() =>
  import('@/pages/admin/AdminOrdersPage').then((m) => ({ default: m.AdminOrdersPage }))
);
const AdminInventoryPage = React.lazy(() =>
  import('@/pages/admin/AdminInventoryPage').then((m) => ({ default: m.AdminInventoryPage }))
);
const AdminCouponsPage = React.lazy(() =>
  import('@/pages/admin/AdminCouponsPage').then((m) => ({ default: m.AdminCouponsPage }))
);
const AdminReturnsPage = React.lazy(() =>
  import('@/pages/admin/AdminReturnsPage').then((m) => ({ default: m.AdminReturnsPage }))
);
const AdminAuditLogsPage = React.lazy(() =>
  import('@/pages/admin/AdminAuditLogsPage').then((m) => ({ default: m.AdminAuditLogsPage }))
);
const AdminUsersPage = React.lazy(() =>
  import('@/pages/admin/AdminUsersPage').then((m) => ({ default: m.AdminUsersPage }))
);
const AdminReconciliationPage = React.lazy(() =>
  import('@/pages/admin/AdminReconciliationPage').then((m) => ({ default: m.AdminReconciliationPage }))
);

// Smooth Page Loading Indicator for lazy chunks
const PageLoadingFallback: React.FC = () => (
  <div className="min-h-[50vh] flex items-center justify-center" role="status" aria-label="Loading page">
    <div className="w-8 h-8 border-3 border-blue-600 border-t-transparent rounded-full animate-spin" />
  </div>
);

// Protected Route Wrapper (Requires Logged-In User)
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAuthenticated, isCheckingAuth } = useAppSelector((state) => state.auth);
  const location = useLocation();

  if (isCheckingAuth) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
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
  const { user, isAuthenticated, isCheckingAuth } = useAppSelector((state) => state.auth);
  const location = useLocation();

  if (isCheckingAuth) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900 text-white">
        <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
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
      <React.Suspense fallback={<PageLoadingFallback />}>
        <Outlet />
      </React.Suspense>
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

        {/* Authentication Routes & Aliases */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="/signup" element={<RegisterPage />} />
        <Route path="/auth/signup" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Wishlist (Accessible to all with guest support) */}
        <Route path="/wishlist" element={<WishlistPage />} />
        <Route path="/customer/wishlist" element={<WishlistPage />} />

        {/* Customer Protected Checkout & Direct Buy Routes */}
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/buy"
          element={
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/buy-now"
          element={
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/order-confirmation/:orderId"
          element={
            <ProtectedRoute>
              <OrderConfirmationPage />
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

        {/* Customer Orders & Order Details */}
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <OrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/customer/orders"
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
          path="/customer/orders/:id"
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
          path="/customer/orders/:id/track"
          element={
            <ProtectedRoute>
              <TrackOrderPage />
            </ProtectedRoute>
          }
        />

        {/* Customer Profile & Address Book */}
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/customer/profile"
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
        <Route
          path="/customer/addresses"
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
            <React.Suspense fallback={<PageLoadingFallback />}>
              <AdminLayout />
            </React.Suspense>
          </AdminRoute>
        }
      >
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboardPage />} />
        <Route path="products" element={<AdminProductsPage />} />
        <Route path="categories" element={<AdminCategoriesPage />} />
        <Route path="orders" element={<AdminOrdersPage />} />
        <Route path="reconciliation" element={<AdminReconciliationPage />} />
        <Route path="inventory" element={<AdminInventoryPage />} />
        <Route path="coupons" element={<AdminCouponsPage />} />
        <Route path="returns" element={<AdminReturnsPage />} />
        <Route path="admins" element={<AdminUsersPage />} />
        <Route path="users" element={<AdminUsersPage />} />
        <Route path="audit-logs" element={<AdminAuditLogsPage />} />
      </Route>
    </Routes>
  );
};
