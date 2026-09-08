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
    <header className="sticky top-0 z-40 bg-white border-b border-gray-200 shadow-sm">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-blue-700 to-indigo-800 text-white text-xs py-1.5 px-4 text-center font-medium">
        <span>⚡ Super Fast Delivery across India | Extra 10% OFF with code <b>WELCOME10</b></span>
      </div>

      {/* Main Navbar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-4">
          
          {/* Logo & Mobile Menu Toggle */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => dispatch(toggleMobileMenu())}
              className="lg:hidden p-2 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100"
              aria-label="Toggle Navigation Menu"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>

            <Link to="/" className="flex items-center gap-2 group">
              <div className="w-10 h-10 rounded-xl bg-blue-600 text-white flex items-center justify-center font-black text-xl shadow-md group-hover:bg-blue-700 transition">
                <ShoppingBag className="w-5 h-5" />
              </div>
              <span className="font-extrabold text-2xl tracking-tight bg-gradient-to-r from-blue-600 to-indigo-700 bg-clip-text text-transparent">
                ShopKart
              </span>
            </Link>
          </div>

          {/* Search Bar */}
          <form onSubmit={handleSearch} className="flex-1 max-w-2xl hidden md:block">
            <div className="relative">
              <input
                type="text"
                placeholder="Search products, brands, categories..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-full text-sm focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100 transition bg-gray-50 focus:bg-white"
              />
              <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
            </div>
          </form>

          {/* Right Action Icons */}
          <div className="flex items-center gap-2 sm:gap-4">
            
            {/* Wishlist */}
            <Link
              to="/customer/wishlist"
              className="relative p-2 text-gray-600 hover:text-blue-600 rounded-full hover:bg-gray-100 transition"
              title="Wishlist"
            >
              <Heart className="w-6 h-6" />
              {totalWishlistCount > 0 && (
                <span className="absolute top-1 right-1 bg-red-500 text-white font-bold text-[10px] w-4 h-4 rounded-full flex items-center justify-center">
                  {totalWishlistCount}
                </span>
              )}
            </Link>

            {/* Cart Button */}
            <button
              onClick={() => dispatch(toggleCartDrawer())}
              className="relative p-2 text-gray-600 hover:text-blue-600 rounded-full hover:bg-gray-100 transition"
              title="Cart"
            >
              <ShoppingCart className="w-6 h-6" />
              {totalCartCount > 0 && (
                <span className="absolute top-1 right-1 bg-blue-600 text-white font-bold text-[10px] w-4 h-4 rounded-full flex items-center justify-center">
                  {totalCartCount}
                </span>
              )}
            </button>

            {/* Auth Dropdown */}
            <div className="relative" ref={dropdownRef}>
              {isAuthenticated && user ? (
                <div>
                  <button
                    onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                    className="flex items-center gap-2 p-1.5 rounded-full hover:bg-gray-100 transition text-sm font-semibold text-gray-700"
                  >
                    <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold">
                      {user.firstName ? user.firstName.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <span className="hidden lg:inline">{user.firstName}</span>
                    <ChevronDown className="w-4 h-4 text-gray-400" />
                  </button>

                  {userDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-xl border border-gray-100 py-2 z-50 animate-in fade-in slide-in-from-top-2">
                      <div className="px-4 py-2 border-b border-gray-100">
                        <p className="text-xs text-gray-400 font-medium">Signed in as</p>
                        <p className="text-sm font-bold text-gray-800 truncate">{user.fullName || user.email}</p>
                      </div>

                      {user.admin && (
                        <Link
                          to="/admin"
                          onClick={() => setUserDropdownOpen(false)}
                          className="flex items-center gap-2.5 px-4 py-2 text-sm text-purple-700 hover:bg-purple-50 font-semibold"
                        >
                          <ShieldAlert className="w-4 h-4" />
                          Admin Console
                        </Link>
                      )}

                      <Link
                        to="/customer/orders"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                      >
                        <Package className="w-4 h-4 text-gray-400" />
                        My Orders
                      </Link>

                      <Link
                        to="/customer/addresses"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                      >
                        <MapPin className="w-4 h-4 text-gray-400" />
                        Saved Addresses
                      </Link>

                      <Link
                        to="/customer/profile"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                      >
                        <UserIcon className="w-4 h-4 text-gray-400" />
                        Account Profile
                      </Link>

                      <div className="border-t border-gray-100 my-1"></div>

                      <button
                        onClick={handleLogout}
                        className="w-full text-left flex items-center gap-2.5 px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                      >
                        <LogOut className="w-4 h-4" />
                        Log Out
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <Link
                    to="/auth/login"
                    className="text-sm font-semibold text-gray-700 hover:text-blue-600 px-3 py-1.5 rounded-lg hover:bg-gray-50 transition"
                  >
                    Log In
                  </Link>
                  <Link
                    to="/auth/register"
                    className="text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 px-4 py-1.5 rounded-full transition shadow-sm"
                  >
                    Sign Up
                  </Link>
                </div>
              )}
            </div>

          </div>
        </div>

        {/* Secondary Navigation Row (Categories & Quick Links) */}
        <div className="hidden lg:flex items-center gap-6 py-2.5 border-t border-gray-100 text-sm font-medium text-gray-600">
          <div className="relative">
            <button
              onClick={() => setCategoryMenuOpen(!categoryMenuOpen)}
              className="flex items-center gap-1.5 font-bold text-gray-900 hover:text-blue-600"
            >
              All Categories <ChevronDown className="w-4 h-4" />
            </button>

            {categoryMenuOpen && (
              <div className="absolute left-0 mt-2 w-64 bg-white rounded-xl shadow-xl border border-gray-100 py-2 z-50">
                {categories.map((cat) => (
                  <Link
                    key={cat.categoryId}
                    to={`/products?categorySlug=${cat.slug}`}
                    onClick={() => setCategoryMenuOpen(false)}
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-600"
                  >
                    {cat.categoryName}
                  </Link>
                ))}
              </div>
            )}
          </div>

          <Link to="/products?sort=deals" className="text-red-600 font-bold hover:underline">
            🔥 Super Deals
          </Link>
          <Link to="/products?sort=bestsellers" className="hover:text-blue-600">
            Bestsellers
          </Link>
          <Link to="/products?sort=newest" className="hover:text-blue-600">
            New Arrivals
          </Link>
          <Link to="/products" className="hover:text-blue-600">
            Catalog
          </Link>
        </div>
      </div>

      {/* Mobile Menu Drawer */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-t border-gray-200 bg-white px-4 pt-3 pb-6 space-y-4">
          <form onSubmit={handleSearch} className="relative">
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm"
            />
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          </form>

          <div className="flex flex-col space-y-2">
            <Link
              to="/products"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="font-semibold text-gray-800 py-1.5 hover:text-blue-600"
            >
              All Products
            </Link>
            <Link
              to="/products?sort=deals"
              onClick={() => dispatch(toggleMobileMenu(false))}
              className="font-semibold text-red-600 py-1.5"
            >
              🔥 Super Deals
            </Link>
            <div className="border-t border-gray-100 pt-2 font-bold text-xs text-gray-400 uppercase tracking-wider">
              Popular Categories
            </div>
            {categories.slice(0, 6).map((cat) => (
              <Link
                key={cat.categoryId}
                to={`/products?categorySlug=${cat.slug}`}
                onClick={() => dispatch(toggleMobileMenu(false))}
                className="text-sm text-gray-600 py-1 hover:text-blue-600"
              >
                {cat.categoryName}
              </Link>
            ))}
          </div>
        </div>
      )}
    </header>
  );
};
