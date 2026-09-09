import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { toggleCartDrawer } from '@/store/slices/uiSlice';
import { Home, Layers, Zap, ShoppingCart, User as UserIcon } from 'lucide-react';

export const BottomNav: React.FC = () => {
  const location = useLocation();
  const dispatch = useAppDispatch();
  const cart = useAppSelector((state) => state.cart.cart);
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);

  const cartCount = cart?.totalQuantity || 0;
  const path = location.pathname;

  // Don't show on checkout, order confirmation, or individual product detail pages
  if (
    path.startsWith('/checkout') ||
    path.startsWith('/order-confirmation') ||
    /^\/products\/[^/]+$/.test(path)
  ) {
    return null;
  }

  const isActive = (targetPath: string) => {
    if (targetPath === '/') return path === '/';
    return path.startsWith(targetPath);
  };

  return (
    <nav
      aria-label="Mobile Bottom Navigation"
      className="fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-slate-200/90 py-1.5 px-2 lg:hidden shadow-[0_-4px_12px_rgba(0,0,0,0.06)]"
    >
      <div className="flex items-center justify-around max-w-md mx-auto">
        {/* Home */}
        <Link
          to="/"
          className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition ${
            isActive('/') && !location.search.includes('deals')
              ? 'text-[#2874F0] font-bold'
              : 'text-slate-500 hover:text-slate-800'
          }`}
        >
          <Home className="w-5 h-5" />
          <span className="text-[10px] mt-0.5">Home</span>
        </Link>

        {/* Categories / Explore */}
        <Link
          to="/products"
          className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition ${
            isActive('/products') && !location.search.includes('deals')
              ? 'text-[#2874F0] font-bold'
              : 'text-slate-500 hover:text-slate-800'
          }`}
        >
          <Layers className="w-5 h-5" />
          <span className="text-[10px] mt-0.5">Categories</span>
        </Link>

        {/* Deals */}
        <Link
          to="/products?sort=deals"
          className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition ${
            location.search.includes('deals')
              ? 'text-red-600 font-bold'
              : 'text-slate-500 hover:text-slate-800'
          }`}
        >
          <Zap className="w-5 h-5 fill-current text-amber-500" />
          <span className="text-[10px] mt-0.5">Deals</span>
        </Link>

        {/* Cart Drawer Trigger */}
        <button
          onClick={() => dispatch(toggleCartDrawer())}
          className="flex flex-col items-center justify-center py-1 px-3 rounded-xl transition text-slate-500 hover:text-slate-800 relative cursor-pointer"
        >
          <div className="relative">
            <ShoppingCart className="w-5 h-5" />
            {cartCount > 0 && (
              <span className="absolute -top-1.5 -right-2.5 bg-yellow-400 text-slate-950 font-black text-[9px] min-w-[16px] h-[16px] px-1 rounded-full flex items-center justify-center shadow-xs">
                {cartCount}
              </span>
            )}
          </div>
          <span className="text-[10px] mt-0.5">Cart</span>
        </button>

        {/* Account / Profile */}
        <Link
          to={isAuthenticated ? '/profile' : '/login'}
          className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition ${
            isActive('/profile') || isActive('/login') || isActive('/orders')
              ? 'text-[#2874F0] font-bold'
              : 'text-slate-500 hover:text-slate-800'
          }`}
        >
          <UserIcon className="w-5 h-5" />
          <span className="text-[10px] mt-0.5">Account</span>
        </Link>
      </div>
    </nav>
  );
};
