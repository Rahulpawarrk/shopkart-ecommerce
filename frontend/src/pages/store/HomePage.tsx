import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '@/services/productService';
import { ProductCard } from '@/components/product/ProductCard';
import type { HomeShowcase } from '@/types';
import {
  Sparkles,
  ArrowRight,
  Zap,
  TrendingUp,
  Layers,
  Clock,
  ShieldCheck,
  Truck,
  RotateCcw,
  Award,
  Gift,
  CheckCircle2,
  Mail,
} from 'lucide-react';

export const HomePage: React.FC = () => {
  const [showcase, setShowcase] = useState<HomeShowcase | null>(null);
  const [loading, setLoading] = useState(true);
  const [emailInput, setEmailInput] = useState('');
  const [subscribed, setSubscribed] = useState(false);

  // Dynamic Flash Deal Countdown Timer (ticks to midnight)
  const [timeLeft, setTimeLeft] = useState({ hours: 7, minutes: 42, seconds: 19 });

  useEffect(() => {
    productService
      .getHomeShowcase()
      .then(setShowcase)
      .catch(() => {})
      .finally(() => setLoading(false));

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev.seconds > 0) return { ...prev, seconds: prev.seconds - 1 };
        if (prev.minutes > 0) return { ...prev, minutes: 59, seconds: 59 };
        if (prev.hours > 0) return { hours: prev.hours - 1, minutes: 59, seconds: 59 };
        return { hours: 11, minutes: 59, seconds: 59 };
      });
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const handleSubscribe = (e: React.FormEvent) => {
    e.preventDefault();
    if (emailInput.trim()) {
      setSubscribed(true);
      setEmailInput('');
    }
  };

  const categories = showcase?.categories || [];
  const deals = showcase?.dealsOfTheDay || [];
  const bestsellers = showcase?.bestsellers || [];
  const newArrivals = showcase?.newArrivals || [];

  return (
    <div className="space-y-8 pb-12">
      {/* Hero Banner with Modern Gradient & Glassmorphism (Vertically Compact) */}
      <section className="relative overflow-hidden bg-gradient-to-br from-slate-950 via-blue-950 to-indigo-950 text-white py-8 md:py-12 px-4 sm:px-6 lg:px-8">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_30%,rgba(37,99,235,0.25),transparent_60%)] pointer-events-none" />
        <div className="absolute top-0 right-0 w-80 h-80 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="max-w-7xl mx-auto relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          {/* Left Column: Headline & Action Buttons */}
          <div className="lg:col-span-7 space-y-3.5 text-center lg:text-left">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-300 text-[11px] font-semibold shadow-inner">
              <Sparkles className="w-3.5 h-3.5 text-blue-400 animate-pulse" />
              <span>India's Mega Electronics &amp; Lifestyle Carnival</span>
            </div>

            <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight leading-[1.15] text-white">
              Elevate Your World With <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-indigo-300">Next-Gen Tech</span>
            </h1>

            <p className="text-gray-300 text-xs sm:text-sm max-w-xl mx-auto lg:mx-0 leading-relaxed font-normal">
              Discover verified premium gadgets, trendy fashion, and daily essentials with up to 60% instant price drops, 0% EMI, and lightning-fast nationwide delivery.
            </p>

            <div className="flex flex-wrap items-center justify-center lg:justify-start gap-3 pt-1">
              <Link
                to="/products"
                className="px-6 py-2.5 bg-blue-600 hover:bg-blue-500 text-white font-bold rounded-xl shadow-lg shadow-blue-600/30 transition transform hover:-translate-y-0.5 flex items-center gap-2 text-xs sm:text-sm"
              >
                <span>Explore Catalog</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/products?sort=deals"
                className="px-6 py-2.5 bg-white/10 hover:bg-white/20 text-white border border-white/20 font-bold rounded-xl transition backdrop-blur-md text-xs sm:text-sm flex items-center gap-2"
              >
                <Zap className="w-4 h-4 text-amber-400" />
                <span>Today's Flash Deals</span>
              </Link>
            </div>

            {/* Micro Highlights */}
            <div className="pt-2 flex flex-wrap items-center justify-center lg:justify-start gap-5 text-[11px] text-gray-300">
              <div className="flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                <span>100% Original Products</span>
              </div>
              <div className="flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                <span>Razorpay Secured</span>
              </div>
              <div className="flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                <span>7-Day Easy Returns</span>
              </div>
            </div>
          </div>

          {/* Right Column: Hero Visual Card with Deal Showcase */}
          <div className="lg:col-span-5 flex justify-center">
            <div className="w-full max-w-sm bg-white/10 backdrop-blur-xl rounded-2xl p-4 border border-white/20 shadow-2xl space-y-3.5">
              <div className="flex items-center justify-between text-xs font-bold">
                <span className="flex items-center gap-1.5 text-amber-300">
                  <Zap className="w-3.5 h-3.5 fill-amber-300" />
                  <span className="text-[11px]">FLASH SALE ENDS IN</span>
                </span>
                <div className="flex items-center gap-1 font-mono bg-black/40 px-2 py-0.5 rounded-lg text-white border border-white/10 text-[11px]">
                  <span>{String(timeLeft.hours).padStart(2, '0')}h</span>:
                  <span>{String(timeLeft.minutes).padStart(2, '0')}m</span>:
                  <span className="text-amber-400">{String(timeLeft.seconds).padStart(2, '0')}s</span>
                </div>
              </div>

              {/* Product Teaser Visual */}
              <div className="relative aspect-[16/8] rounded-xl overflow-hidden bg-gradient-to-tr from-blue-700 via-indigo-600 to-purple-700 p-4 flex flex-col justify-end shadow-inner">
                <div className="absolute top-2.5 right-2.5 bg-red-500 text-white text-[10px] font-black px-2 py-0.5 rounded-full shadow">
                  UP TO 60% OFF
                </div>
                <div className="relative z-10">
                  <span className="text-[10px] font-bold text-blue-200 tracking-wider uppercase">Hot Pick</span>
                  <h3 className="text-lg font-black text-white leading-snug">Pro Wireless Noise-Cancelling Headphones</h3>
                  <div className="flex items-baseline gap-2 mt-0.5">
                    <span className="text-base font-extrabold text-white">₹2,499</span>
                    <span className="text-[11px] text-blue-200 line-through">₹4,999</span>
                  </div>
                </div>
              </div>

              {/* Trust Metric Badges */}
              <div className="grid grid-cols-2 gap-2 text-center">
                <div className="bg-white/5 rounded-xl p-2 border border-white/10">
                  <span className="block font-black text-base text-white">4.9 / 5.0</span>
                  <span className="text-[10px] text-gray-300">50K+ Happy Shoppers</span>
                </div>
                <div className="bg-white/5 rounded-xl p-2 border border-white/10">
                  <span className="block font-black text-base text-emerald-400">Zero Cost</span>
                  <span className="text-[10px] text-gray-300">Free Express Delivery</span>
                </div>
              </div>

              <Link
                to="/products"
                className="w-full py-2 bg-white text-gray-900 font-bold text-xs rounded-xl hover:bg-gray-100 transition flex items-center justify-center gap-1.5 shadow"
              >
                <span>Browse Exclusive Collections</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Value Pillars Strip */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
          <div className="p-5 bg-white rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 hover:shadow-md transition">
            <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0">
              <Truck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-gray-900">Free Nationwide Delivery</h4>
              <p className="text-xs text-gray-500">Orders above ₹499 qualify</p>
            </div>
          </div>

          <div className="p-5 bg-white rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 hover:shadow-md transition">
            <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center flex-shrink-0">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-gray-900">100% Genuine Guaranteed</h4>
              <p className="text-xs text-gray-500">Authorized brand partners</p>
            </div>
          </div>

          <div className="p-5 bg-white rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 hover:shadow-md transition">
            <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
              <RotateCcw className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-gray-900">7-Day Free Replacement</h4>
              <p className="text-xs text-gray-500">No-questions-asked pickups</p>
            </div>
          </div>

          <div className="p-5 bg-white rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4 hover:shadow-md transition">
            <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center flex-shrink-0">
              <Award className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-gray-900">Razorpay Verified</h4>
              <p className="text-xs text-gray-500">256-bit encrypted checkout</p>
            </div>
          </div>
        </div>
      </section>

      {/* Dynamic Showcases or Localized Loading Skeleton */}
      {loading ? (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10 animate-pulse" aria-busy="true" aria-label="Loading products">
          <div className="space-y-4">
            <div className="h-6 w-48 bg-slate-200 rounded-lg" />
            <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-3 sm:gap-4">
              {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
                <div key={n} className="h-28 bg-slate-200 rounded-2xl" />
              ))}
            </div>
          </div>
          <div className="space-y-4">
            <div className="h-24 bg-slate-200 rounded-3xl" />
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
              {[1, 2, 3, 4].map((n) => (
                <div key={n} className="h-80 bg-slate-200 rounded-2xl" />
              ))}
            </div>
          </div>
        </div>
      ) : (
        <>
          {/* Featured Categories */}
          {categories.length > 0 && (
            <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center">
                    <Layers className="w-4 h-4" />
                  </div>
                  <h2 className="text-xl sm:text-2xl font-black text-gray-900">Shop by Category</h2>
                </div>
                <Link to="/products" className="text-xs font-bold text-blue-600 hover:underline flex items-center gap-1">
                  <span>View All Categories</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-8 gap-3 sm:gap-4">
                {categories.map((cat) => {
                  const nameLower = cat.categoryName.toLowerCase();
                  const slugLower = (cat.slug || '').toLowerCase();

                  // High-resolution photography mapping for each category
                  let catImage = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=400&q=80';

                  if (slugLower.includes('laptop') || nameLower.includes('laptop') || nameLower.includes('computer')) {
                    catImage = 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=80';
                  } else if (slugLower.includes('phone') || nameLower.includes('phone') || nameLower.includes('tablet')) {
                    catImage = 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=400&q=80';
                  } else if (slugLower.includes('audio') || nameLower.includes('audio') || nameLower.includes('headphone')) {
                    catImage = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=400&q=80';
                  } else if (slugLower.includes('watch') || nameLower.includes('watch') || nameLower.includes('wearable')) {
                    catImage = 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=400&q=80';
                  } else if (slugLower.includes('men') || nameLower.includes('men')) {
                    catImage = 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=400&q=80';
                  } else if (slugLower.includes('women') || nameLower.includes('women')) {
                    catImage = 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=400&q=80';
                  } else if (slugLower.includes('footwear') || nameLower.includes('footwear') || nameLower.includes('shoe') || nameLower.includes('sneaker')) {
                    catImage = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=400&q=80';
                  } else if (slugLower.includes('home') || nameLower.includes('home') || nameLower.includes('kitchen')) {
                    catImage = 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=400&q=80';
                  }

                  return (
                    <Link
                      key={cat.categoryId}
                      to={`/products?categorySlug=${cat.slug}`}
                      className="group relative flex flex-col items-center text-center p-3 sm:p-4 bg-white rounded-3xl border border-slate-200/80 shadow-xs hover:shadow-xl hover:border-blue-400 hover:-translate-y-1.5 transition-all duration-300 overflow-hidden"
                    >
                      {/* High Quality Category Image Container */}
                      <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-2xl overflow-hidden bg-slate-50 border border-slate-200/70 p-1 mb-3 group-hover:scale-105 group-hover:border-blue-300 transition-all duration-300 relative shadow-2xs">
                        <img
                          src={catImage}
                          alt={cat.categoryName}
                          className="w-full h-full object-cover rounded-xl group-hover:scale-110 transition-transform duration-500"
                          loading="lazy"
                          decoding="async"
                          width="96"
                          height="96"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=400&q=80';
                          }}
                        />
                      </div>
                      <span className="text-xs sm:text-sm font-black text-slate-900 group-hover:text-blue-600 transition line-clamp-1 px-1">
                        {cat.categoryName}
                      </span>
                      <span className="inline-flex items-center gap-1 text-[11px] font-bold text-blue-600 mt-1 opacity-75 group-hover:opacity-100 group-hover:translate-x-0.5 transition">
                        Explore <ArrowRight className="w-3 h-3" />
                      </span>
                    </Link>
                  );
                })}
              </div>
            </section>
          )}

          {/* Deals of the Day */}
          {deals.length > 0 && (
            <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="bg-gradient-to-r from-red-600 via-rose-600 to-pink-600 rounded-3xl p-6 md:p-8 text-white mb-8 shadow-xl flex flex-col md:flex-row items-center justify-between gap-6">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-2xl bg-white text-red-600 flex items-center justify-center shadow-md flex-shrink-0">
                    <Zap className="w-7 h-7 fill-red-600 animate-bounce" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-2xl sm:text-3xl font-black tracking-tight">Deals of the Day</h2>
                      <span className="px-2.5 py-0.5 bg-yellow-400 text-gray-950 font-black text-[10px] rounded-full uppercase tracking-wider">
                        Limited Stock
                      </span>
                    </div>
                    <p className="text-xs sm:text-sm text-red-100 mt-1">
                      Handpicked bargains with the highest verified price drops and free delivery
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-1.5 bg-black/30 backdrop-blur-xs px-4 py-2 rounded-xl text-white font-mono text-sm border border-white/20">
                    <Clock className="w-4 h-4 text-yellow-300" />
                    <span>{String(timeLeft.hours).padStart(2, '0')}:{String(timeLeft.minutes).padStart(2, '0')}:{String(timeLeft.seconds).padStart(2, '0')}</span>
                  </div>
                  <Link
                    to="/products?sort=deals"
                    className="px-6 py-2.5 bg-white text-red-600 font-bold text-xs rounded-full hover:bg-red-50 transition shadow-md whitespace-nowrap"
                  >
                    See All Deals &rarr;
                  </Link>
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
                {deals.slice(0, 4).map((product) => (
                  <ProductCard key={product.productId} product={product} />
                ))}
              </div>
            </section>
          )}

          {/* Bestsellers Showcase */}
          {bestsellers.length > 0 && (
            <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center">
                    <TrendingUp className="w-4 h-4" />
                  </div>
                  <h2 className="text-xl sm:text-2xl font-black text-gray-900">Trending Bestsellers</h2>
                </div>
                <Link to="/products?sort=bestsellers" className="text-xs font-bold text-blue-600 hover:underline flex items-center gap-1">
                  <span>View All</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
                {bestsellers.slice(0, 8).map((product) => (
                  <ProductCard key={product.productId} product={product} />
                ))}
              </div>
            </section>
          )}

          {/* New Arrivals Showcase */}
          {newArrivals.length > 0 && (
            <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center">
                    <Sparkles className="w-4 h-4" />
                  </div>
                  <h2 className="text-xl sm:text-2xl font-black text-gray-900">Just Dropped: New Arrivals</h2>
                </div>
                <Link to="/products?sort=newest" className="text-xs font-bold text-blue-600 hover:underline flex items-center gap-1">
                  <span>View All</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
                {newArrivals.slice(0, 4).map((product) => (
                  <ProductCard key={product.productId} product={product} />
                ))}
              </div>
            </section>
          )}
        </>
      )}

      {/* Newsletter / Discount Callout */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-br from-blue-900 via-indigo-900 to-slate-900 rounded-3xl p-8 sm:p-12 text-white relative overflow-hidden shadow-2xl">
          <div className="absolute -right-12 -bottom-12 w-64 h-64 bg-blue-500/20 rounded-full blur-3xl pointer-events-none" />
          <div className="max-w-2xl space-y-4">
            <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-white/10 border border-white/20 text-blue-200 text-xs font-semibold">
              <Gift className="w-3.5 h-3.5 text-amber-300" />
              <span>Special Welcome Offer</span>
            </div>
            <h3 className="text-2xl sm:text-4xl font-black tracking-tight leading-tight">
              Get ₹200 OFF on Your First Order!
            </h3>
            <p className="text-xs sm:text-sm text-gray-300 leading-relaxed">
              Subscribe to the ShopKart VIP dispatch for exclusive member-only early sale access, secret coupon codes, and tech launches.
            </p>

            {subscribed ? (
              <div className="p-4 bg-emerald-500/20 border border-emerald-400/30 rounded-2xl flex items-center gap-3 text-emerald-300 text-xs font-bold">
                <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
                <span>You're in! Use coupon code <strong className="text-white bg-emerald-600/40 px-2 py-0.5 rounded">SHOPKART200</strong> at checkout.</span>
              </div>
            ) : (
              <form onSubmit={handleSubscribe} className="flex flex-col sm:flex-row gap-3 pt-2 max-w-md">
                <div className="relative flex-1">
                  <Mail className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    required
                    value={emailInput}
                    onChange={(e) => setEmailInput(e.target.value)}
                    placeholder="Enter your email address..."
                    className="w-full pl-10 pr-4 py-3 bg-white/10 border border-white/20 rounded-xl text-white placeholder-gray-400 text-xs focus:outline-hidden focus:ring-2 focus:ring-blue-400 transition"
                  />
                </div>
                <button
                  type="submit"
                  className="px-6 py-3 bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs rounded-xl shadow-lg transition whitespace-nowrap cursor-pointer"
                >
                  Claim ₹200 OFF
                </button>
              </form>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};
