import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { logoutUser } from '@/store/slices/authSlice';
import { toggleCartDrawer, toggleMobileMenu } from '@/store/slices/uiSlice';
import { productService } from '@/services/productService';
import type { Category } from '@/types';
import {
  ShoppingBag,
  Search,
  Heart,
  ShoppingCart,
  User as UserIcon,
  Menu,
  X,
  ChevronDown,
  LogOut,
  Package,
  MapPin,
  ShieldAlert,
  Sparkles,
  Zap,
  Truck,
  Layers,
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const cart = useAppSelector((state) => state.cart.cart);
  const wishlistItems = useAppSelector((state) => state.wishlist.items);
  const mobileMenuOpen = useAppSelector((state) => state.ui.mobileMenuOpen);

  const [categories, setCategories] = useState<Category[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);
  const [categoryMenuOpen, setCategoryMenuOpen] = useState(false);

  const dropdownRef = useRef<HTMLDivElement>(null);
  const categoryMenuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    productService.getCategories().then(setCategories).catch(() => {});
  }, []);

  // Close dropdowns on click outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      const target = event.target as Node;
      if (dropdownRef.current && !dropdownRef.current.contains(target)) {
        setUserDropdownOpen(false);
      }
      if (categoryMenuRef.current && !categoryMenuRef.current.contains(target)) {
        setCategoryMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/products?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleLogout = async () => {
    await dispatch(logoutUser());
    setUserDropdownOpen(false);
    navigate('/');
  };

  const totalCartCount = cart?.totalQuantity || 0;
  const totalWishlistCount = wishlistItems.length;

  return (
    <header className="sticky top-0 z-40 bg-[#2874F0] shadow-md">
      {/* Top Utility Ticker Bar */}
      <div className="bg-[#1b5fc9] text-white/90 text-[11px] py-1.5 px-4 hidden sm:block">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2 font-medium">
            <Truck className="w-3.5 h-3.5 text-yellow-300" />
            <span>⚡ Free Express Delivery on orders above ₹499 across India</span>
          </div>
          <div className="flex items-center gap-4 text-white/80">
            <span className="flex items-center gap-1.5 font-medium">
              <span>📞 24x7 Customer Support: 1800-SHOPKART</span>
            </span>
            <span className="text-white/40">|</span>
            <span>100% Genuine &amp; Verified Products</span>
          </div>
        </div>
      </div>

      {/* Main Navbar */}
      <div className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 py-2.5 sm:py-3">
        <div className="flex items-center justify-between gap-3 sm:gap-6">
          
          {/* Logo & Mobile Menu Button */}
          <div className="flex items-center gap-2 sm:gap-3 flex-shrink-0">
            <button
              onClick={() => dispatch(toggleMobileMenu())}
              className="lg:hidden p-1.5 rounded-lg text-white hover:bg-white/10 transition"
              aria-label="Toggle Navigation Menu"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>

            <Link to="/" className="flex items-center gap-2 group flex-shrink-0">
              <img
                src="/logo.jpg"
                alt="ShopKart Logo"
                className="w-8 h-8 sm:w-10 sm:h-10 rounded-xl object-cover shadow-sm group-hover:scale-105 transition flex-shrink-0"
              />
              <div className="flex flex-col items-start min-w-0">
                <span className="text-base sm:text-xl font-black text-white leading-none tracking-tight">
                  ShopKart
                </span>
                <span className="text-[9px] sm:text-[10px] text-white/80 font-medium tracking-normal mt-0.5 whitespace-nowrap">
                  Best Deals, Always ✨
                </span>
              </div>
            </Link>
          </div>

          {/* Search Bar matching live shopkart11.in */}
          <form onSubmit={handleSearch} className="flex-1 max-w-2xl hidden md:block">
            <div className="relative flex items-center bg-white rounded-xl shadow-xs overflow-hidden">
              <Search className="w-4 h-4 text-slate-400 ml-3.5 flex-shrink-0" />
              <input
                type="text"
                placeholder="Search for products, brands and more"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full py-2.5 pl-2.5 pr-4 text-xs sm:text-sm text-slate-900 bg-transparent placeholder:text-slate-400 focus:outline-hidden"
              />
              <button
                type="submit"
                className="mr-2 px-3 py-1 bg-[#2874F0] hover:bg-[#1b5fc9] text-white rounded-lg text-xs font-bold transition cursor-pointer"
              >
                Search
              </button>
            </div>
          </form>

          {/* Right Action Items */}
          <div className="flex items-center gap-2 sm:gap-4 flex-shrink-0">
            
            {/* Wishlist Link */}
            <Link
              to="/wishlist"
              className="relative p-2 text-white/90 hover:text-white rounded-xl hover:bg-white/10 transition group flex items-center gap-1.5"
              title="View Wishlist"
            >
              <Heart className="w-5 h-5 transition group-hover:scale-110" />
              {totalWishlistCount > 0 && (
                <span className="absolute top-0.5 right-0.5 bg-yellow-400 text-slate-950 font-black text-[10px] w-4 h-4 rounded-full flex items-center justify-center shadow-xs">
                  {totalWishlistCount}
                </span>
              )}
            </Link>

            {/* Cart Trigger */}
            <button
              onClick={() => dispatch(toggleCartDrawer())}
              className="relative p-2 text-white/90 hover:text-white rounded-xl hover:bg-white/10 transition group flex items-center gap-2 cursor-pointer font-bold text-xs sm:text-sm"
              title="Shopping Cart"
            >
              <div className="relative">
                <ShoppingCart className="w-5 h-5 transition group-hover:scale-110" />
                {totalCartCount > 0 && (
                  <span className="absolute -top-1.5 -right-2 bg-yellow-400 text-slate-950 font-black text-[10px] min-w-[18px] h-[18px] px-1 rounded-full flex items-center justify-center shadow-xs">
                    {totalCartCount}
                  </span>
                )}
              </div>
              <span className="hidden sm:inline">Cart</span>
            </button>

            {/* User Account / Auth Dropdown */}
            <div className="relative" ref={dropdownRef}>
              {isAuthenticated && user ? (
                <div>
                  <button
                    onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                    className="flex items-center gap-2 py-1.5 px-3 rounded-xl hover:bg-white/10 transition text-xs font-bold text-white border border-white/20 bg-white/10 cursor-pointer"
                  >
                    <div className="w-6 h-6 rounded-full bg-white text-[#2874F0] flex items-center justify-center text-[11px] font-black shadow-xs">
                      {user.firstName ? user.firstName.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <span className="hidden lg:inline text-white leading-tight truncate max-w-[100px] font-bold text-xs">{user.firstName}</span>
                    <ChevronDown className="w-3.5 h-3.5 text-white/80" />
                  </button>

                  {userDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-60 bg-white rounded-2xl shadow-2xl border border-slate-200 py-2.5 z-50 text-slate-800">
                      <div className="px-4 py-2.5 border-b border-slate-100">
                        <p className="text-xs font-bold text-slate-900 truncate">
                          {user.firstName} {user.lastName || ''}
                        </p>
                        <p className="text-[11px] text-slate-500 truncate">{user.email}</p>
                        {(user.admin || user.roles?.includes('ADMIN') || user.roles?.includes('ROLE_ADMIN')) && (
                          <span className="inline-block mt-1.5 px-2 py-0.5 bg-blue-100 text-[#2874F0] text-[10px] font-bold rounded-md uppercase tracking-wider">
                            Administrator
                          </span>
                        )}
                      </div>

                      {(user.admin || user.roles?.includes('ADMIN') || user.roles?.includes('ROLE_ADMIN')) && (
                        <Link
                          to="/admin"
                          onClick={() => setUserDropdownOpen(false)}
                          className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-[#2874F0] hover:bg-blue-50 font-bold"
                        >
                          <ShieldAlert className="w-4 h-4" />
                          <span>Admin Console</span>
                        </Link>
                      )}

                      <Link
                        to="/orders"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-slate-700 hover:bg-slate-50 font-semibold"
                      >
                        <Package className="w-4 h-4 text-slate-400" />
                        <span>My Orders</span>
                      </Link>

                      <Link
                        to="/wishlist"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-slate-700 hover:bg-slate-50 font-semibold"
                      >
                        <Heart className="w-4 h-4 text-slate-400" />
                        <span>My Wishlist</span>
                      </Link>

                      <Link
                        to="/addresses"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-slate-700 hover:bg-slate-50 font-semibold"
                      >
                        <MapPin className="w-4 h-4 text-slate-400" />
                        <span>Saved Addresses</span>
                      </Link>

                      <Link
                        to="/profile"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-slate-700 hover:bg-slate-50 font-semibold"
                      >
                        <UserIcon className="w-4 h-4 text-slate-400" />
                        <span>Account Profile</span>
                      </Link>

                      <div className="border-t border-slate-100 my-1.5"></div>

                      <button
                        onClick={handleLogout}
                        className="w-full text-left flex items-center gap-2.5 px-4 py-2.5 text-xs text-rose-600 hover:bg-rose-50 font-bold cursor-pointer"
                      >
                        <LogOut className="w-4 h-4" />
                        <span>Sign Out</span>
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <Link
                    to="/login"
                    className="px-4 py-1.5 bg-white text-[#2874F0] font-bold text-xs sm:text-sm rounded-lg hover:bg-slate-50 transition shadow-sm"
                  >
                    Login
                  </Link>
                  <Link
                    to="/register"
                    className="hidden sm:inline-block px-3.5 py-1.5 bg-white/10 hover:bg-white/20 text-white font-bold text-xs sm:text-sm rounded-lg transition border border-white/30"
                  >
                    Become a Seller
                  </Link>
                </div>
              )}
            </div>

          </div>
        </div>
      </div>

      {/* Secondary Category Strip matching live shopkart11.in */}
      <div className="bg-white border-b border-slate-200/80 shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between gap-6 py-2 overflow-x-auto text-xs font-bold text-slate-700 no-scrollbar">
            <div className="flex items-center gap-6 sm:gap-8 flex-nowrap min-w-max">
              <div className="relative" ref={categoryMenuRef}>
                <button
                  onClick={() => setCategoryMenuOpen(!categoryMenuOpen)}
                  className="flex items-center gap-1.5 font-black text-slate-900 hover:text-[#2874F0] transition cursor-pointer"
                >
                  <Layers className="w-4 h-4 text-[#2874F0]" />
                  <span>All Categories</span>
                  <ChevronDown className={`w-3.5 h-3.5 text-slate-400 transition-transform ${categoryMenuOpen ? 'rotate-180' : ''}`} />
                </button>

                {categoryMenuOpen && (
                  <div className="absolute left-0 mt-2 w-72 bg-white rounded-2xl shadow-2xl border border-slate-200 py-2.5 z-50 text-slate-800">
                    {categories.map((cat) => (
                      <Link
                        key={cat.categoryId}
                        to={`/products?categorySlug=${cat.slug}`}
                        onClick={() => setCategoryMenuOpen(false)}
                        className="block px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-blue-50 hover:text-[#2874F0] transition"
                      >
                        {cat.categoryName}
                      </Link>
                    ))}
                  </div>
                )}
              </div>

              {/* Exact Categories from https://shopkart11.in/ */}
              <Link to="/products?categorySlug=grocery" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Grocery
              </Link>
              <Link to="/products?categorySlug=mobiles" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Mobiles
              </Link>
              <Link to="/products?categorySlug=fashion" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Fashion
              </Link>
              <Link to="/products?categorySlug=electronics" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Electronics
              </Link>
              <Link to="/products?categorySlug=home-furniture" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Home &amp; Furniture
              </Link>
              <Link to="/products?categorySlug=appliances" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Appliances
              </Link>
              <Link to="/products?categorySlug=travel" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Travel
              </Link>
              <Link to="/products?categorySlug=beauty" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Beauty
              </Link>
              <Link to="/products?categorySlug=toys" className="hover:text-[#2874F0] transition whitespace-nowrap">
                Toys &amp; More
              </Link>
            </div>

            <Link to="/products?sort=deals" className="hidden lg:flex items-center gap-1.5 text-amber-600 hover:text-amber-700 font-black whitespace-nowrap flex-shrink-0">
              <Zap className="w-3.5 h-3.5 fill-amber-500 text-amber-500" />
              <span>Super Flash Deals</span>
            </Link>
          </div>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-t border-slate-800 bg-slate-900 px-4 pt-4 pb-6 space-y-4 text-white">
          <form onSubmit={handleSearch} className="relative">
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2.5 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-400"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
          </form>

          <div className="flex flex-col space-y-1.5 text-xs font-bold">
            <Link
              to="/products"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="text-slate-200 py-2 hover:text-blue-400 border-b border-slate-800"
            >
              All Products Catalog
            </Link>
            <Link
              to="/products?sort=deals"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="text-amber-400 py-2 flex items-center gap-1.5 border-b border-slate-800"
            >
              <Zap className="w-3.5 h-3.5 fill-amber-400" />
              <span>Super Flash Deals</span>
            </Link>
            <Link
              to="/wishlist"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="text-slate-200 py-2 flex items-center gap-1.5 border-b border-slate-800"
            >
              <Heart className="w-3.5 h-3.5 text-rose-400" />
              <span>My Wishlist ({totalWishlistCount})</span>
            </Link>

            {isAuthenticated ? (
              <>
                <Link
                  to="/orders"
                  onClick={() => dispatch(toggleMobileMenu(false))}
                  className="text-slate-200 py-2 flex items-center gap-1.5 border-b border-slate-800"
                >
                  <Package className="w-3.5 h-3.5 text-slate-400" />
                  <span>My Orders</span>
                </Link>
                <Link
                  to="/profile"
                  onClick={() => dispatch(toggleMobileMenu(false))}
                  className="text-slate-200 py-2 flex items-center gap-1.5 border-b border-slate-800"
                >
                  <UserIcon className="w-3.5 h-3.5 text-slate-400" />
                  <span>Profile &amp; Addresses</span>
                </Link>
                <button
                  onClick={handleLogout}
                  className="text-left text-rose-400 py-2 flex items-center gap-1.5"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span>Sign Out</span>
                </button>
              </>
            ) : (
              <div className="pt-2 flex gap-2">
                <Link
                  to="/login"
                  onClick={() => dispatch(toggleMobileMenu(false))}
                  className="flex-1 text-center py-2.5 bg-slate-800 text-slate-200 rounded-xl"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  onClick={() => dispatch(toggleMobileMenu(false))}
                  className="flex-1 text-center py-2.5 bg-blue-600 text-white rounded-xl"
                >
                  Sign Up
                </Link>
              </div>
            )}

            <div className="pt-3">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                Popular Categories
              </p>
              <div className="grid grid-cols-2 gap-2">
                {categories.slice(0, 6).map((cat) => (
                  <Link
                    key={cat.categoryId}
                    to={`/products?categorySlug=${cat.slug}`}
                    onClick={() => dispatch(toggleMobileMenu(false))}
                    className="p-2 bg-slate-800 rounded-xl text-slate-200 text-[11px] hover:bg-slate-700 hover:text-blue-400 transition"
                  >
                    {cat.categoryName}
                  </Link>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </header>
  );
};

