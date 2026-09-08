import React, { useState } from 'react';
import { Link, useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { loginUser } from '@/store/slices/authSlice';
import { fetchCart } from '@/store/slices/cartSlice';
import { fetchWishlist } from '@/store/slices/wishlistSlice';
import { showToast } from '@/store/slices/uiSlice';
import { Mail, Lock, LogIn, Eye, EyeOff, ShieldCheck, Truck, Zap, Sparkles, ArrowRight } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const { user, isAuthenticated, isLoading, error } = useAppSelector((state) => state.auth);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  // Read target URL from query param (e.g. ?redirect=/checkout) or router state
  const rawRedirect = searchParams.get('redirect') || (location.state as any)?.from?.pathname || '/';
  const redirectTarget = (rawRedirect.startsWith('/login') || rawRedirect.startsWith('/register') || rawRedirect.startsWith('/signup')) ? '/' : rawRedirect;

  // If already authenticated, redirect to destination
  React.useEffect(() => {
    if (isAuthenticated && user) {
      if (user.admin || user.roles?.includes('ADMIN') || user.roles?.includes('ROLE_ADMIN')) {
        navigate('/admin/dashboard', { replace: true });
      } else {
        navigate(redirectTarget, { replace: true });
      }
    }
  }, [isAuthenticated, user, navigate, redirectTarget]);

  const handleLoginSubmit = async (loginEmail: string, loginPass: string) => {
    if (!loginEmail.trim() || !loginPass) return;

    try {
      const user = await dispatch(loginUser({ email: loginEmail.trim(), password: loginPass })).unwrap();
      dispatch(fetchCart());
      dispatch(fetchWishlist());
      dispatch(showToast({ message: `Welcome back, ${user.firstName}!`, type: 'success' }));

      if (user.admin || user.roles?.includes('ADMIN') || user.roles?.includes('ROLE_ADMIN')) {
        navigate('/admin/dashboard', { replace: true });
      } else {
        navigate(redirectTarget, { replace: true });
      }
    } catch (err: any) {
      dispatch(showToast({ message: err?.message || String(err) || 'Invalid email or password', type: 'error' }));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleLoginSubmit(email, password);
  };

  return (
    <div className="min-h-[88vh] flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-slate-50">
      <div className="max-w-4xl w-full bg-white rounded-3xl shadow-card border border-slate-200/80 overflow-hidden grid grid-cols-1 lg:grid-cols-12">
        
        {/* Left Side: Modern Visual Brand Showcase */}
        <div className="lg:col-span-5 bg-gradient-to-br from-blue-700 via-indigo-700 to-slate-900 text-white p-8 lg:p-10 flex flex-col justify-between relative overflow-hidden">
          <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-blue-500/20 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute bottom-0 left-0 -ml-16 -mb-16 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />

          <div className="relative z-10 space-y-6">
            <Link to="/" className="inline-flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-2xl bg-white text-blue-600 flex items-center justify-center font-black text-xl shadow-md">
                SK
              </div>
              <span className="font-black text-xl tracking-tight text-white">ShopKart</span>
            </Link>

            <div>
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/10 text-blue-200 text-xs font-semibold backdrop-blur-md mb-3 border border-white/10">
                <Sparkles className="w-3.5 h-3.5 text-blue-300" />
                Premium Shopping Experience
              </span>
              <h2 className="text-2xl sm:text-3xl font-black leading-tight text-white">
                India's Premier Destination for Tech & Fashion.
              </h2>
              <p className="text-blue-100/80 text-xs sm:text-sm mt-2 leading-relaxed">
                Log in to view saved addresses, track live orders, unlock member-only flash discounts, and manage your wishlist.
              </p>
            </div>
          </div>

          {/* Value Props */}
          <div className="relative z-10 pt-8 border-t border-white/10 space-y-3.5 text-xs text-blue-100">
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-xl bg-white/10 flex items-center justify-center text-blue-300">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <span>100% Genuine Certified Brands & Warranty</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-xl bg-white/10 flex items-center justify-center text-blue-300">
                <Truck className="w-4 h-4" />
              </div>
              <span>Free Express Delivery on orders above ₹499</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-xl bg-white/10 flex items-center justify-center text-blue-300">
                <Zap className="w-4 h-4 text-amber-300" />
              </div>
              <span>Instant Razorpay & Cash on Delivery Checkout</span>
            </div>
          </div>
        </div>

        {/* Right Side: Clean Login Form */}
        <div className="lg:col-span-7 p-8 sm:p-10 flex flex-col justify-center">
          <div className="mb-6">
            <div className="flex items-center justify-between">
              <h1 className="text-2xl font-black text-slate-900 tracking-tight">Sign In to Your Account</h1>
              <span className="text-xs text-slate-400 font-medium">Step into ShopKart</span>
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Enter your registered email and password to access your account.
            </p>
          </div>

          {error && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-2xl">
              {typeof error === 'string' ? error : 'An error occurred. Please try again.'}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block font-bold text-slate-700 mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  className="w-full pl-10 pr-3.5 py-3 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="font-bold text-slate-700">Password</label>
                <Link
                  to="/forgot-password"
                  className="text-[11px] text-blue-600 hover:text-blue-700 hover:underline font-bold"
                >
                  Forgot password?
                </Link>
              </div>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-11 py-3 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3.5 text-slate-400 hover:text-slate-600 transition"
                  title={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3.5 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-xl transition duration-200 shadow-md shadow-blue-600/25 flex items-center justify-center gap-2 text-xs uppercase tracking-wider disabled:opacity-50 mt-6 cursor-pointer"
            >
              <LogIn className="w-4 h-4" />
              <span>{isLoading ? 'Signing In...' : 'Sign In to ShopKart'}</span>
            </button>
          </form>

          <div className="mt-8 pt-6 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
            <span>Don't have an account?</span>
            <Link
              to={`/register${redirectTarget !== '/' ? `?redirect=${encodeURIComponent(redirectTarget)}` : ''}`}
              className="font-bold text-blue-600 hover:text-blue-700 hover:underline flex items-center gap-1"
            >
              Create Account <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

