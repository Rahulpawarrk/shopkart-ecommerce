import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { registerUser } from '@/store/slices/authSlice';
import { fetchCart } from '@/store/slices/cartSlice';
import { fetchWishlist } from '@/store/slices/wishlistSlice';
import { showToast } from '@/store/slices/uiSlice';
import { Mail, Lock, User, Phone, UserPlus, Eye, EyeOff, ShieldCheck, Gift, Truck, ArrowRight, Sparkles } from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const { user, isAuthenticated, isLoading, error } = useAppSelector((state) => state.auth);
  const rawRedirect = searchParams.get('redirect') || '/';
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

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });

  const [showPassword, setShowPassword] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (formData.password.length < 6) {
      setFormError('Password must be at least 6 characters long');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setFormError('Passwords do not match');
      return;
    }

    try {
      const user = await dispatch(
        registerUser({
          firstName: formData.firstName.trim(),
          lastName: formData.lastName.trim(),
          email: formData.email.trim(),
          phone: formData.phone.trim(),
          password: formData.password,
          confirmPassword: formData.confirmPassword,
        })
      ).unwrap();

      dispatch(fetchCart());
      dispatch(fetchWishlist());
      dispatch(showToast({ message: `Welcome to ShopKart, ${user.firstName}!`, type: 'success' }));
      navigate(redirectTarget, { replace: true });
    } catch (err: any) {
      dispatch(showToast({ message: err || 'Registration failed', type: 'error' }));
    }
  };

  return (
    <div className="min-h-[88vh] flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-slate-50">
      <div className="max-w-4xl w-full bg-white rounded-3xl shadow-card border border-slate-200/80 overflow-hidden grid grid-cols-1 lg:grid-cols-12">
        
        {/* Left Side: Brand Visual Card */}
        <div className="lg:col-span-5 bg-gradient-to-br from-indigo-700 via-blue-700 to-slate-900 text-white p-8 lg:p-10 flex flex-col justify-between relative overflow-hidden">
          <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute bottom-0 left-0 -ml-16 -mb-16 w-64 h-64 bg-blue-500/20 rounded-full blur-3xl pointer-events-none" />

          <div className="relative z-10 space-y-6">
            <Link to="/" className="inline-flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-2xl bg-white text-blue-600 flex items-center justify-center font-black text-xl shadow-md">
                SK
              </div>
              <span className="font-black text-xl tracking-tight text-white">ShopKart</span>
            </Link>

            <div>
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/10 text-blue-200 text-xs font-semibold backdrop-blur-md mb-3 border border-white/10">
                <Gift className="w-3.5 h-3.5 text-amber-300" />
                Join & Unlock Rewards
              </span>
              <h2 className="text-2xl sm:text-3xl font-black leading-tight text-white">
                Create Your Account in Under a Minute.
              </h2>
              <p className="text-blue-100/80 text-xs sm:text-sm mt-2 leading-relaxed">
                Enjoy members-only flash pricing, instant order notifications, and personalized product recommendations.
              </p>
            </div>
          </div>

          {/* Value Props */}
          <div className="relative z-10 pt-8 border-t border-white/10 space-y-3.5 text-xs text-blue-100">
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-xl bg-white/10 flex items-center justify-center text-amber-300">
                <Gift className="w-4 h-4" />
              </div>
              <span>Exclusive Welcome Coupons & Discount Codes</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-xl bg-white/10 flex items-center justify-center text-blue-300">
                <Truck className="w-4 h-4" />
              </div>
              <span>Free Express Delivery on Qualifying Orders</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-xl bg-white/10 flex items-center justify-center text-blue-300">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <span>Hassle-free 7-day replacements & returns</span>
            </div>
          </div>
        </div>

        {/* Right Side: Registration Form */}
        <div className="lg:col-span-7 p-8 sm:p-10 flex flex-col justify-center">
          <div className="mb-6">
            <div className="flex items-center justify-between">
              <h1 className="text-2xl font-black text-slate-900 tracking-tight">Create Your Account</h1>
              <span className="text-xs text-slate-400 font-medium">Free forever</span>
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Fill in your details below to begin shopping on ShopKart.
            </p>
          </div>

          {(formError || error) && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-2xl">
              {formError || error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label className="block font-bold text-slate-700 mb-1.5">First Name *</label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                  <input
                    type="text"
                    required
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    placeholder="John"
                    className="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1.5">Last Name *</label>
                <input
                  type="text"
                  required
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  placeholder="Doe"
                  className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                />
              </div>
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1.5">Email Address *</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                <input
                  type="email"
                  required
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="name@example.com"
                  className="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                />
              </div>
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1.5">Mobile Phone Number *</label>
              <div className="relative">
                <Phone className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                <input
                  type="tel"
                  required
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  placeholder="10-digit mobile number"
                  className="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label className="block font-bold text-slate-700 mb-1.5">Password *</label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    minLength={6}
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    placeholder="min. 6 chars"
                    className="w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-2.5 text-slate-400 hover:text-slate-600"
                  >
                    {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                  </button>
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1.5">Confirm Password *</label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    placeholder="re-enter password"
                    className="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition bg-slate-50/50 focus:bg-white text-xs font-medium"
                  />
                </div>
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3.5 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-xl transition duration-200 shadow-md shadow-blue-600/25 flex items-center justify-center gap-2 text-xs uppercase tracking-wider disabled:opacity-50 mt-5 cursor-pointer"
            >
              <UserPlus className="w-4 h-4" />
              <span>{isLoading ? 'Creating Account...' : 'Agree & Create Account'}</span>
            </button>
          </form>

          <div className="mt-8 pt-6 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
            <span>Already have an account?</span>
            <Link
              to={`/login${redirectTarget !== '/' ? `?redirect=${encodeURIComponent(redirectTarget)}` : ''}`}
              className="font-bold text-blue-600 hover:text-blue-700 hover:underline flex items-center gap-1"
            >
              Sign In <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

