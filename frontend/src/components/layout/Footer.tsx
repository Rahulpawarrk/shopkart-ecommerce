import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, ShieldCheck, Truck, RotateCcw, Headphones, Lock, Heart, CreditCard } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-950 text-gray-300 mt-auto border-t border-slate-800">
      {/* Value Proposition Highlights */}
      <div className="border-b border-slate-800 py-10 bg-slate-900/60">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-2 md:grid-cols-4 gap-6 text-left">
          <div className="flex items-start gap-3.5">
            <div className="w-11 h-11 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center flex-shrink-0">
              <Truck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Free & Express Delivery</h4>
              <p className="text-xs text-gray-400 mt-0.5">On all eligible orders above ₹499</p>
            </div>
          </div>

          <div className="flex items-start gap-3.5">
            <div className="w-11 h-11 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center flex-shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">100% Genuine Certified</h4>
              <p className="text-xs text-gray-400 mt-0.5">Direct from verified brand partners</p>
            </div>
          </div>

          <div className="flex items-start gap-3.5">
            <div className="w-11 h-11 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400 flex items-center justify-center flex-shrink-0">
              <RotateCcw className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">7-Day Free Replacement</h4>
              <p className="text-xs text-gray-400 mt-0.5">Doorstep pickup & zero hassle</p>
            </div>
          </div>

          <div className="flex items-start gap-3.5">
            <div className="w-11 h-11 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400 flex items-center justify-center flex-shrink-0">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">24/7 Priority Support</h4>
              <p className="text-xs text-gray-400 mt-0.5">Instant live chat & ticket help</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Links */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 grid grid-cols-2 md:grid-cols-5 gap-8">
        <div className="col-span-2 space-y-4">
          <div className="flex items-center gap-2">
            <div className="w-9 h-9 rounded-xl bg-blue-600 text-white flex items-center justify-center font-black shadow-md shadow-blue-500/30">
              <ShoppingBag className="w-5 h-5" />
            </div>
            <span className="font-black text-2xl text-white tracking-tight">ShopKart</span>
          </div>
          <p className="text-xs text-gray-400 max-w-sm leading-relaxed">
            India's most trusted online shopping platform delivering verified electronics, smartphones, trendy apparel, and home essentials right to your doorstep.
          </p>

          <div className="pt-2 flex flex-wrap items-center gap-4 text-xs text-gray-400">
            <div className="flex items-center gap-1.5 text-emerald-400">
              <Lock className="w-4 h-4" />
              <span className="font-semibold">256-Bit SSL Encrypted</span>
            </div>
            <div className="flex items-center gap-1.5 text-blue-400">
              <ShieldCheck className="w-4 h-4" />
              <span className="font-semibold">PCI-DSS Compliant</span>
            </div>
          </div>
        </div>

        <div>
          <h4 className="text-xs font-black text-white uppercase tracking-wider mb-4">Shop Online</h4>
          <ul className="space-y-2.5 text-xs">
            <li><Link to="/products" className="hover:text-white transition">All Products</Link></li>
            <li><Link to="/products?categorySlug=electronics" className="hover:text-white transition">Electronics</Link></li>
            <li><Link to="/products?categorySlug=fashion" className="hover:text-white transition">Fashion & Apparel</Link></li>
            <li><Link to="/products?sort=deals" className="hover:text-white transition">Today's Deals</Link></li>
            <li><Link to="/products?sort=bestsellers" className="hover:text-white transition">Bestsellers</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-black text-white uppercase tracking-wider mb-4">Customer Care</h4>
          <ul className="space-y-2.5 text-xs">
            <li><Link to="/orders" className="hover:text-white transition">My Orders</Link></li>
            <li><Link to="/wishlist" className="hover:text-white transition">My Wishlist</Link></li>
            <li><Link to="/addresses" className="hover:text-white transition">Saved Addresses</Link></li>
            <li><Link to="/profile" className="hover:text-white transition">Account Profile</Link></li>
            <li><Link to="/orders" className="hover:text-white transition">Returns & Refunds</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-black text-white uppercase tracking-wider mb-4">Trust & Security</h4>
          <ul className="space-y-2.5 text-xs">
            <li><span className="hover:text-white transition cursor-pointer">Privacy Policy</span></li>
            <li><span className="hover:text-white transition cursor-pointer">Terms of Service</span></li>
            <li><span className="hover:text-white transition cursor-pointer">Razorpay Security</span></li>
            <li><span className="hover:text-white transition cursor-pointer">Grievance Redressal</span></li>
            <li><span className="hover:text-white transition cursor-pointer">Partner With Us</span></li>
          </ul>
        </div>
      </div>

      {/* Payment Partners & Security Strip */}
      <div className="border-t border-slate-800/80 bg-slate-950/80 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-xs text-gray-400">
            <CreditCard className="w-4 h-4 text-blue-400" />
            <span>Supported Payment Methods:</span>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-2">
            <span className="px-2.5 py-1 bg-slate-900 border border-slate-700 rounded text-[11px] font-bold text-blue-400">
              Razorpay
            </span>
            <span className="px-2.5 py-1 bg-slate-900 border border-slate-700 rounded text-[11px] font-bold text-amber-400">
              UPI / GPay / PhonePe
            </span>
            <span className="px-2.5 py-1 bg-slate-900 border border-slate-700 rounded text-[11px] font-bold text-white">
              RuPay
            </span>
            <span className="px-2.5 py-1 bg-slate-900 border border-slate-700 rounded text-[11px] font-bold text-blue-300">
              VISA
            </span>
            <span className="px-2.5 py-1 bg-slate-900 border border-slate-700 rounded text-[11px] font-bold text-red-400">
              Mastercard
            </span>
            <span className="px-2.5 py-1 bg-slate-900 border border-slate-700 rounded text-[11px] font-bold text-gray-300">
              NetBanking
            </span>
          </div>
        </div>
      </div>

      {/* Copyright */}
      <div className="border-t border-slate-800 py-6 text-center text-xs text-gray-500 flex items-center justify-center">
        <p>&copy; {new Date().getFullYear()} ShopKart E-Commerce Pvt. Ltd. All rights reserved.</p>
      </div>
    </footer>
  );
};
