import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, ShieldCheck, Truck, RotateCcw, Headphones, Lock } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-900 text-gray-300 mt-auto border-t border-gray-800">
      {/* Value Proposition Highlights */}
      <div className="border-b border-gray-800 py-8 bg-gray-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-2 md:grid-cols-4 gap-6 text-center md:text-left">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-blue-600/20 text-blue-400 flex items-center justify-center">
              <Truck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Free & Fast Shipping</h4>
              <p className="text-xs text-gray-400">On all eligible orders above ₹499</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-emerald-600/20 text-emerald-400 flex items-center justify-center">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">100% Genuine Products</h4>
              <p className="text-xs text-gray-400">Directly from verified manufacturers</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-purple-600/20 text-purple-400 flex items-center justify-center">
              <RotateCcw className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Easy 7-Day Returns</h4>
              <p className="text-xs text-gray-400">Hassle-free refunds & pickups</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-amber-600/20 text-amber-400 flex items-center justify-center">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">24/7 Help Support</h4>
              <p className="text-xs text-gray-400">Dedicated assistance anytime</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Links */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 grid grid-cols-2 md:grid-cols-5 gap-8">
        <div className="col-span-2 space-y-4">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-blue-600 text-white flex items-center justify-center font-black">
              <ShoppingBag className="w-4 h-4" />
            </div>
            <span className="font-extrabold text-xl text-white">ShopKart</span>
          </div>
          <p className="text-xs text-gray-400 max-w-sm leading-relaxed">
            India's most trusted online shopping destination delivering quality electronics, fashion, home essentials, and gadgets right to your doorstep.
          </p>
          <div className="flex items-center gap-2 text-xs text-gray-400">
            <Lock className="w-4 h-4 text-emerald-400" />
            <span>256-Bit SSL Encrypted Razorpay Checkout</span>
          </div>
        </div>

        <div>
          <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-4">Shop Online</h4>
          <ul className="space-y-2 text-xs">
            <li><Link to="/products?categorySlug=electronics" className="hover:text-white transition">Electronics</Link></li>
            <li><Link to="/products?categorySlug=fashion" className="hover:text-white transition">Fashion & Apparel</Link></li>
            <li><Link to="/products?categorySlug=home-appliances" className="hover:text-white transition">Home & Kitchen</Link></li>
            <li><Link to="/products?sort=deals" className="hover:text-white transition">Today's Deals</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-4">Customer Care</h4>
          <ul className="space-y-2 text-xs">
            <li><Link to="/customer/orders" className="hover:text-white transition">Track Order</Link></li>
            <li><Link to="/customer/orders" className="hover:text-white transition">Returns & Refunds</Link></li>
            <li><Link to="/customer/addresses" className="hover:text-white transition">Shipping Info</Link></li>
            <li><span className="hover:text-white transition cursor-pointer">Help Center</span></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-4">Policy & Legal</h4>
          <ul className="space-y-2 text-xs">
            <li><span className="hover:text-white transition cursor-pointer">Privacy Policy</span></li>
            <li><span className="hover:text-white transition cursor-pointer">Terms of Service</span></li>
            <li><span className="hover:text-white transition cursor-pointer">Security Practices</span></li>
            <li><span className="hover:text-white transition cursor-pointer">Grievance Officer</span></li>
          </ul>
        </div>
      </div>

      {/* Copyright */}
      <div className="border-t border-gray-800 py-6 text-center text-xs text-gray-500">
        <p>&copy; {new Date().getFullYear()} ShopKart E-Commerce Pvt. Ltd. All rights reserved.</p>
      </div>
    </footer>
  );
};
