import React from 'react';
import { Link } from 'react-router-dom';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-[#172337] text-white/90 mt-12 border-t border-slate-800">
      {/* Main Footer Links - Matching shopkart11.in */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 sm:py-12">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          {/* Column 1: About */}
          <div>
            <h3 className="text-white font-bold text-xs uppercase tracking-wider mb-4">About</h3>
            <ul className="space-y-2.5">
              <li>
                <Link to="/contact" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Contact Us
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  About Us
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Careers
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  ShopKart Stories
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Press
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Corporate Information
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 2: Help */}
          <div>
            <h3 className="text-white font-bold text-xs uppercase tracking-wider mb-4">Help</h3>
            <ul className="space-y-2.5">
              <li>
                <Link to="/terms" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Payments
                </Link>
              </li>
              <li>
                <Link to="/shipping-policy" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Shipping
                </Link>
              </li>
              <li>
                <Link to="/return-policy" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Cancellation &amp; Returns
                </Link>
              </li>
              <li>
                <Link to="/contact" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  FAQ
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 3: Policy */}
          <div>
            <h3 className="text-white font-bold text-xs uppercase tracking-wider mb-4">Policy</h3>
            <ul className="space-y-2.5">
              <li>
                <Link to="/return-policy" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Return Policy
                </Link>
              </li>
              <li>
                <Link to="/terms" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Terms of Use
                </Link>
              </li>
              <li>
                <Link to="/privacy" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Security
                </Link>
              </li>
              <li>
                <Link to="/privacy" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Privacy
                </Link>
              </li>
              <li>
                <Link to="/shipping-policy" className="text-xs sm:text-sm text-white/60 hover:text-amber-400 transition-colors">
                  Shipping Policy
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 4: Social & App */}
          <div>
            <h3 className="text-white font-bold text-xs uppercase tracking-wider mb-4">Social</h3>
            <div className="flex gap-2.5 mb-6">
              <a
                href="https://facebook.com"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 rounded-lg bg-white/10 hover:bg-[#2874F0] text-white/70 hover:text-white transition-all duration-200 hover:scale-105"
                aria-label="Facebook"
              >
                <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z" />
                </svg>
              </a>
              <a
                href="https://twitter.com"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 rounded-lg bg-white/10 hover:bg-[#2874F0] text-white/70 hover:text-white transition-all duration-200 hover:scale-105"
                aria-label="Twitter"
              >
                <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z" />
                </svg>
              </a>
              <a
                href="https://instagram.com"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 rounded-lg bg-white/10 hover:bg-[#2874F0] text-white/70 hover:text-white transition-all duration-200 hover:scale-105"
                aria-label="Instagram"
              >
                <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect width="20" height="20" x="2" y="2" rx="5" ry="5" />
                  <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" />
                  <line x1="17.5" x2="17.51" y1="6.5" y2="6.5" />
                </svg>
              </a>
              <a
                href="https://youtube.com"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 rounded-lg bg-white/10 hover:bg-[#2874F0] text-white/70 hover:text-white transition-all duration-200 hover:scale-105"
                aria-label="YouTube"
              >
                <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M2.5 17a24.12 24.12 0 0 1 0-10 2 2 0 0 1 1.4-1.4 49.56 49.56 0 0 1 16.2 0A2 2 0 0 1 21.5 7a24.12 24.12 0 0 1 0 10 2 2 0 0 1-1.4 1.4 49.55 49.55 0 0 1-16.2 0A2 2 0 0 1 2.5 17" />
                  <path d="m10 15 5-3-5-3z" />
                </svg>
              </a>
            </div>

            <h3 className="text-white font-bold text-xs uppercase tracking-wider mb-3">Download App</h3>
            <div className="flex flex-col gap-2">
              <button
                type="button"
                className="px-3 py-2 bg-white/10 hover:bg-white/15 rounded-lg text-xs sm:text-sm text-white/80 transition-colors text-left flex items-center gap-2 cursor-pointer"
              >
                <span>📱</span> Google Play
              </button>
              <button
                type="button"
                className="px-3 py-2 bg-white/10 hover:bg-white/15 rounded-lg text-xs sm:text-sm text-white/80 transition-colors text-left flex items-center gap-2 cursor-pointer"
              >
                <span>🍎</span> App Store
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Trust & Legal Bar */}
      <div className="border-t border-white/10 bg-[#0f172a]/60">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-5 text-xs text-white/50 flex-wrap">
            <span>🔒 Secure Payments</span>
            <span>🔄 Easy Returns</span>
            <span>✅ Genuine Products</span>
          </div>
          <div className="flex items-center gap-3 text-xs text-white/50 flex-wrap">
            <Link className="hover:text-white/80 transition-colors" to="/terms">
              Terms
            </Link>
            <span>·</span>
            <Link className="hover:text-white/80 transition-colors" to="/privacy">
              Privacy
            </Link>
            <span>·</span>
            <Link className="hover:text-white/80 transition-colors" to="/return-policy">
              Returns
            </Link>
            <span>·</span>
            <span>&copy; {new Date().getFullYear()} ShopKart</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
