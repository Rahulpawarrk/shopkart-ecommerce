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

  useEffect(() => {
    productService.getCategories().then(setCategories).catch(() => {});
  }, []);

  // Close dropdown on click outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setUserDropdownOpen(false);
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
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200/80 shadow-xs">
      {/* Top Utility Ticker Bar */}
      <div className="bg-gradient-to-r from-blue-700 via-indigo-700 to-slate-900 text-white text-[11px] py-2 px-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2 font-medium">
            <Truck className="w-3.5 h-3.5 text-blue-300" />
            <span>⚡ Free Express Delivery on orders above ₹499 across India</span>
          </div>
          <div className="hidden sm:flex items-center gap-4 text-blue-200">
            <span className="flex items-center gap-1">
              <Zap className="w-3 h-3 text-amber-300" /> Use Code: <b className="text-white font-bold tracking-wider">WELCOME10</b> (10% OFF)
            </span>
            <span className="text-white/40">|</span>
            <span className="text-blue-100">24x7 Customer Support</span>
          </div>
        </div>
      </div>

      {/* Main Navbar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-18 gap-4 sm:gap-6">
          
          {/* Logo & Mobile Menu Button */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => dispatch(toggleMobileMenu())}
              className="lg:hidden p-2 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition"
              aria-label="Toggle Navigation Menu"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>

            <Link to="/" className="flex items-center gap-2.5 group">
              <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 text-white flex items-center justify-center font-black text-xl shadow-md shadow-blue-600/20 group-hover:scale-105 transition">
                SK
              </div>
              <div className="flex flex-col">
                <span className="font-black text-2xl tracking-tight text-slate-900 leading-none">
                  Shop<span className="text-blue-600">Kart</span>
                </span>
                <span className="text-[9px] font-bold text-slate-400 uppercase tracking-widest mt-0.5">
                  India's Store
                </span>
              </div>
            </Link>
          </div>

          {/* Search Bar */}
          <form onSubmit={handleSearch} className="flex-1 max-w-xl hidden md:block">
            <div className="relative">
              <input
                type="text"
                placeholder="Search laptops, smartphones, fashion, audio..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-11 pr-4 py-2.5 border border-slate-200 rounded-2xl text-xs font-medium focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100 transition bg-slate-50/70 focus:bg-white shadow-2xs"
              />
              <Search className="w-4 h-4 text-slate-400 absolute left-4 top-3" />
            </div>
          </form>

          {/* Right Action Icons */}
          <div className="flex items-center gap-2 sm:gap-3">
            
            {/* Wishlist Link */}
            <Link
              to="/wishlist"
              className="relative p-2.5 text-slate-600 hover:text-rose-600 rounded-2xl hover:bg-rose-50/60 transition group flex items-center gap-1.5"
              title="View Wishlist"
            >
              <Heart className="w-5 h-5 transition group-hover:scale-110" />
              {totalWishlistCount > 0 && (
                <span className="absolute top-1 right-1 bg-rose-500 text-white font-black text-[10px] w-4 h-4 rounded-full flex items-center justify-center shadow-xs">
                  {totalWishlistCount}
                </span>
              )}
            </Link>

            {/* Cart Trigger */}
            <button
              onClick={() => dispatch(toggleCartDrawer())}
              className="relative p-2.5 text-slate-700 hover:text-blue-600 rounded-2xl hover:bg-blue-50/60 transition group flex items-center gap-2 cursor-pointer"
              title="Shopping Cart"
            >
              <div className="relative">
                <ShoppingCart className="w-5 h-5 transition group-hover:scale-110" />
                {totalCartCount > 0 && (
                  <span className="absolute -top-1.5 -right-2 bg-blue-600 text-white font-black text-[10px] min-w-[18px] h-[18px] px-1 rounded-full flex items-center justify-center shadow-xs">
                    {totalCartCount}
                  </span>
                )}
              </div>
              {cart && cart.finalTotal > 0 && (
                <span className="hidden xl:inline text-xs font-black text-slate-900">
                  ₹{cart.finalTotal.toLocaleString('en-IN')}
                </span>
              )}
            </button>

            {/* User Account / Auth Dropdown */}
            <div className="relative" ref={dropdownRef}>
              {isAuthenticated && user ? (
                <div>
                  <button
                    onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                    className="flex items-center gap-2 p-1.5 pr-3 rounded-2xl hover:bg-slate-100 transition text-xs font-bold text-slate-800 border border-slate-200/80 cursor-pointer"
                  >
                    <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 text-white flex items-center justify-center font-black shadow-xs">
                      {user.firstName ? user.firstName.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <div className="hidden lg:flex flex-col text-left">
                      <span className="text-slate-900 leading-tight truncate max-w-[100px]">{user.firstName}</span>
                      <span className="text-[10px] text-slate-400 font-normal">
                        {user.admin ? 'Administrator' : 'Customer'}
                      </span>
                    </div>
                    <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                  </button>

                  {userDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-60 bg-white rounded-2xl shadow-card border border-slate-200/90 py-2.5 z-50 animate-in fade-in slide-in-from-top-2">
                      <div className="px-4 py-2.5 border-b border-slate-100">
                        <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Signed in as</p>
                        <p className="text-xs font-bold text-slate-900 truncate mt-0.5">{user.fullName || user.email}</p>
                      </div>

                      {user.admin && (
                        <Link
                          to="/admin/dashboard"
                          onClick={() => setUserDropdownOpen(false)}
                          className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-purple-700 hover:bg-purple-50 font-bold"
                        >
                          <ShieldAlert className="w-4 h-4 text-purple-600" />
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
                        <span>My Wishlist ({totalWishlistCount})</span>
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
                    className="text-xs font-bold text-slate-700 hover:text-blue-600 px-3.5 py-2 rounded-xl hover:bg-slate-100 transition"
                  >
                    Sign In
                  </Link>
                  <Link
                    to="/register"
                    className="text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-xl transition shadow-sm shadow-blue-600/20"
                  >
                    Sign Up
                  </Link>
                </div>
              )}
            </div>

          </div>
        </div>

        {/* Secondary Category Navigation Bar */}
        <div className="hidden lg:flex items-center gap-8 py-2.5 border-t border-slate-100 text-xs font-bold text-slate-600">
          <div className="relative">
            <button
              onClick={() => setCategoryMenuOpen(!categoryMenuOpen)}
              className="flex items-center gap-2 font-black text-slate-900 hover:text-blue-600 transition cursor-pointer"
            >
              <Layers className="w-4 h-4 text-blue-600" />
              <span>All Categories</span>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
            </button>

            {categoryMenuOpen && (
              <div className="absolute left-0 mt-2 w-72 bg-white rounded-2xl shadow-card border border-slate-200/90 py-2.5 z-50 animate-in fade-in slide-in-from-top-2">
                {categories.map((cat) => (
                  <Link
                    key={cat.categoryId}
                    to={`/products?categorySlug=${cat.slug}`}
                    onClick={() => setCategoryMenuOpen(false)}
                    className="block px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-blue-50 hover:text-blue-600 transition"
                  >
                    {cat.categoryName}
                  </Link>
                ))}
              </div>
            )}
          </div>

          <Link to="/products?sort=deals" className="text-red-600 font-black hover:underline flex items-center gap-1">
            <Zap className="w-3.5 h-3.5 fill-red-600" />
            <span>Super Flash Deals</span>
          </Link>
          <Link to="/products?sort=bestsellers" className="hover:text-blue-600 transition">
            Bestsellers
          </Link>
          <Link to="/products?sort=newest" className="hover:text-blue-600 transition">
            New Arrivals
          </Link>
          <Link to="/products" className="hover:text-blue-600 transition">
            All Products Catalog
          </Link>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-t border-slate-200 bg-white px-4 pt-4 pb-6 space-y-4">
          <form onSubmit={handleSearch} className="relative">
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2.5 border border-slate-200 rounded-xl text-xs"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
          </form>

          <div className="flex flex-col space-y-1.5 text-xs font-bold">
            <Link
              to="/products"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="text-slate-800 py-2 hover:text-blue-600 border-b border-slate-100"
            >
              All Products Catalog
            </Link>
            <Link
              to="/products?sort=deals"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="text-red-600 py-2 flex items-center gap-1.5 border-b border-slate-100"
            >
              <Zap className="w-3.5 h-3.5 fill-red-600" />
              <span>Super Flash Deals</span>
            </Link>
            <Link
              to="/wishlist"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="text-slate-800 py-2 flex items-center gap-1.5 border-b border-slate-100"
            >
              <Heart className="w-3.5 h-3.5 text-rose-500" />
              <span>My Wishlist ({totalWishlistCount})</span>
            </Link>

            {isAuthenticated ? (
              <>
                <Link
                  to="/orders"
                  onClick={() => dispatch(toggleMobileMenu(false))}
                  className="text-slate-800 py-2 flex items-center gap-1.5 border-b border-slate-100"
                >
                  <Package className="w-3.5 h-3.5 text-slate-400" />
                  <span>My Orders</span>
                </Link>
                <Link
                  to="/profile"
                  onClick={() => dispatch(toggleMobileMenu(false))}
                  className="text-slate-800 py-2 flex items-center gap-1.5 border-b border-slate-100"
                >
                  <UserIcon className="w-3.5 h-3.5 text-slate-400" />
                  <span>Profile & Addresses</span>
                </Link>
                <button
                  onClick={handleLogout}
                  className="text-left text-rose-600 py-2 flex items-center gap-1.5"
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
                  className="flex-1 text-center py-2.5 bg-slate-100 text-slate-800 rounded-xl"
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
                    className="p-2 bg-slate-50 rounded-xl text-slate-700 text-[11px] hover:bg-blue-50 hover:text-blue-600 transition"
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

