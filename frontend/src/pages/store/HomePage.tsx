import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '@/services/productService';
import { ProductCard } from '@/components/product/ProductCard';
import type { HomeShowcase } from '@/types';
import { Sparkles, ArrowRight, Zap, TrendingUp, Layers } from 'lucide-react';

export const HomePage: React.FC = () => {
  const [showcase, setShowcase] = useState<HomeShowcase | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    productService
      .getHomeShowcase()
      .then(setShowcase)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 space-y-12 animate-pulse">
        <div className="h-96 bg-gray-200 rounded-3xl"></div>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {[1, 2, 3, 4].map((n) => (
            <div key={n} className="h-72 bg-gray-200 rounded-2xl"></div>
          ))}
        </div>
      </div>
    );
  }

  const categories = showcase?.categories || [];
  const deals = showcase?.dealsOfTheDay || [];
  const bestsellers = showcase?.bestsellers || [];
  const newArrivals = showcase?.newArrivals || [];

  return (
    <div className="space-y-14 pb-16">
      {/* Hero Banner */}
      <section className="relative overflow-hidden bg-gradient-to-br from-blue-900 via-indigo-900 to-slate-900 text-white py-16 md:py-24 px-4 sm:px-6 lg:px-8">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_30%,rgba(59,130,246,0.2),transparent_70%)] pointer-events-none" />
        <div className="max-w-7xl mx-auto relative z-10 grid grid-cols-1 md:grid-cols-2 gap-10 items-center">
          <div className="space-y-6 text-center md:text-left">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-300 text-xs font-semibold">
              <Sparkles className="w-4 h-4 text-blue-400" />
              <span>India's Mega Electronic & Fashion Carnival</span>
            </div>
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight leading-tight">
              Upgrade Your Tech & Style Today.
            </h1>
            <p className="text-gray-300 text-sm sm:text-base max-w-lg leading-relaxed">
              Explore thousands of handpicked top-rated products with up to 60% instant discounts, genuine brand warranties, and lightning-fast delivery.
            </p>
            <div className="flex flex-wrap items-center justify-center md:justify-start gap-4 pt-2">
              <Link
                to="/products"
                className="px-8 py-3.5 bg-blue-600 hover:bg-blue-500 text-white font-bold rounded-full shadow-lg shadow-blue-600/30 transition duration-200 flex items-center gap-2 text-sm"
              >
                <span>Shop Catalog</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/products?sort=deals"
                className="px-8 py-3.5 bg-white/10 hover:bg-white/20 text-white border border-white/20 font-bold rounded-full transition duration-200 text-sm backdrop-blur-xs"
              >
                View Top Deals
              </Link>
            </div>
          </div>

          {/* Hero Visual Card */}
          <div className="relative flex justify-center">
            <div className="w-full max-w-md bg-white/10 backdrop-blur-md rounded-3xl p-6 border border-white/20 shadow-2xl space-y-4">
              <div className="flex items-center justify-between text-xs font-bold text-blue-300">
                <span>LIMITED TIME OFFER</span>
                <span className="bg-red-500 text-white px-2 py-0.5 rounded-full">UP TO 50% OFF</span>
              </div>
              <div className="aspect-video bg-gradient-to-tr from-blue-600 to-indigo-500 rounded-2xl flex items-center justify-center p-6 text-center">
                <div>
                  <h3 className="text-2xl font-black text-white">Smart Electronics Hub</h3>
                  <p className="text-xs text-blue-100 mt-1">Noise Cancelling Audio, Laptops, Wearables</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3 text-center">
                <div className="bg-white/5 rounded-xl p-3 border border-white/10">
                  <span className="block font-black text-lg text-white">100%</span>
                  <span className="text-[11px] text-gray-300">Genuine Guarantee</span>
                </div>
                <div className="bg-white/5 rounded-xl p-3 border border-white/10">
                  <span className="block font-black text-lg text-white">Razorpay</span>
                  <span className="text-[11px] text-gray-300">Safe Instant Payments</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Categories */}
      {categories.length > 0 && (
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2">
              <Layers className="w-5 h-5 text-blue-600" />
              <h2 className="text-xl font-bold text-gray-900">Explore by Category</h2>
            </div>
            <Link to="/products" className="text-xs font-bold text-blue-600 hover:underline flex items-center gap-1">
              All Categories <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-4">
            {categories.map((cat) => (
              <Link
                key={cat.categoryId}
                to={`/products?categorySlug=${cat.slug}`}
                className="group flex flex-col items-center text-center p-4 bg-white rounded-2xl border border-gray-200 hover:border-blue-500 hover:shadow-md transition duration-200"
              >
                <div className="w-14 h-14 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold text-xl mb-3 group-hover:bg-blue-600 group-hover:text-white transition duration-200">
                  {cat.categoryName.charAt(0).toUpperCase()}
                </div>
                <span className="text-xs font-bold text-gray-800 group-hover:text-blue-600 transition line-clamp-1">
                  {cat.categoryName}
                </span>
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* Deals of the Day */}
      {deals.length > 0 && (
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-gradient-to-r from-red-500 via-rose-600 to-pink-600 rounded-3xl p-6 md:p-8 text-white mb-8 shadow-lg flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-white text-red-600 flex items-center justify-center shadow-md">
                <Zap className="w-6 h-6 fill-red-600" />
              </div>
              <div>
                <h2 className="text-2xl font-black">Deals of the Day</h2>
                <p className="text-xs text-red-100">Handpicked bargains with the highest verified price drops</p>
              </div>
            </div>
            <Link
              to="/products?sort=deals"
              className="px-6 py-2.5 bg-white text-red-600 font-bold text-xs rounded-full hover:bg-red-50 transition shadow-md whitespace-nowrap"
            >
              See All Deals
            </Link>
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
              <TrendingUp className="w-5 h-5 text-blue-600" />
              <h2 className="text-xl font-bold text-gray-900">Most Popular Bestsellers</h2>
            </div>
            <Link to="/products?sort=bestsellers" className="text-xs font-bold text-blue-600 hover:underline flex items-center gap-1">
              View All <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
            {bestsellers.slice(0, 8).map((product) => (
              <ProductCard key={product.productId} product={product} />
            ))}
          </div>
        </section>
      )}

      {/* New Arrivals */}
      {newArrivals.length > 0 && (
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-indigo-600" />
              <h2 className="text-xl font-bold text-gray-900">Just Dropped: New Arrivals</h2>
            </div>
            <Link to="/products?sort=newest" className="text-xs font-bold text-blue-600 hover:underline flex items-center gap-1">
              View All <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
            {newArrivals.slice(0, 4).map((product) => (
              <ProductCard key={product.productId} product={product} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
};
