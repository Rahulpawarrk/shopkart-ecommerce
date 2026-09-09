import React, { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { productService } from '@/services/productService';
import { ProductCard } from '@/components/product/ProductCard';
import type { HomeShowcase } from '@/types';
import {
  Sparkles,
  ArrowRight,
  Zap,
  TrendingUp,
  Clock,
  ShieldCheck,
  Truck,
  RotateCcw,
  Lock,
  Gift,
  CheckCircle2,
  Mail,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

const HERO_BANNERS = [
  {
    id: 1,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/shopkart-brand.jpg',
    alt: "ShopKart - India's Trusted Online Store",
    title: "India's Trusted Online Shopping Store",
    subtitle: 'Best Deals on Mobiles, Electronics, Fashion & More',
    link: '/products',
  },
  {
    id: 2,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/mega-sale-v2.jpg',
    alt: 'ShopKart Mega Sale - Up to 80% Off',
    title: 'ShopKart Mega Sale',
    subtitle: 'Up to 80% Off Across Top Brands',
    link: '/products?sort=deals',
  },
  {
    id: 3,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/electronics-fest-v2.jpg',
    alt: 'Electronics Fest - Starting ₹499',
    title: 'Electronics Fest',
    subtitle: 'Starting ₹499 with 0% EMI Options',
    link: '/products?categorySlug=electronics',
  },
  {
    id: 4,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/fashion-collection-v2.jpg',
    alt: 'Fashion Collection - Flat 60% Off',
    title: 'Fashion Collection',
    subtitle: 'Flat 60% Off On Trending Apparel & Footwear',
    link: '/products?categorySlug=fashion',
  },
  {
    id: 5,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/footwear-fest.jpg',
    alt: 'Footwear Fest - Up to 70% Off',
    title: 'Footwear Fest',
    subtitle: 'Up to 70% Off On Sneakers, Running Shoes & Formal',
    link: '/products?categorySlug=footwear',
  },
  {
    id: 6,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/beauty-store.jpg',
    alt: 'ShopKart Beauty Store - Up to 50% Off',
    title: 'ShopKart Beauty Store',
    subtitle: 'Up to 50% Off On Skincare, Fragrances & Wellness',
    link: '/products?categorySlug=beauty',
  },
  {
    id: 7,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/appliances-sale.jpg',
    alt: 'Appliances Sale - Up to 60% Off',
    title: 'Appliances Mega Sale',
    subtitle: 'Up to 60% Off On Kitchen & Home Essentials',
    link: '/products?categorySlug=appliances',
  },
  {
    id: 8,
    imageUrl: 'https://jeiehvxxqfyvxbawrdqx.supabase.co/storage/v1/object/public/product-images/banners/new-arrivals.jpg',
    alt: 'ShopKart New Arrivals - Shop Now',
    title: 'New Arrivals Just Dropped',
    subtitle: 'Shop the Latest Innovations First',
    link: '/products?sort=newest',
  },
];

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [showcase, setShowcase] = useState<HomeShowcase | null>(null);
  const [loading, setLoading] = useState(true);
  const [emailInput, setEmailInput] = useState('');
  const [subscribed, setSubscribed] = useState(false);

  // Carousel State
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isHovered, setIsHovered] = useState(false);
  const slideCount = HERO_BANNERS.length;

  // Dynamic Flash Deal Countdown Timer (ticks to midnight)
  const [timeLeft, setTimeLeft] = useState({ hours: 7, minutes: 42, seconds: 19 });

  const nextSlide = useCallback(() => {
    setCurrentSlide((prev) => (prev + 1) % slideCount);
  }, [slideCount]);

  const prevSlide = useCallback(() => {
    setCurrentSlide((prev) => (prev - 1 + slideCount) % slideCount);
  }, [slideCount]);

  // Auto-advance carousel every 4 seconds unless hovered
  useEffect(() => {
    if (isHovered) return;
    const interval = setInterval(nextSlide, 4000);
    return () => clearInterval(interval);
  }, [isHovered, nextSlide]);

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
    <div className="space-y-6 sm:space-y-8 pb-12">
      {/* Hidden SEO Heading */}
      <h1 className="sr-only">
        ShopKart — India's Trusted Online Shopping Store | Best Deals on Mobiles, Electronics, Fashion
      </h1>

      {/* Hero Banner Carousel matching live shopkart11.in */}
      <section className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 pt-2 sm:pt-4">
        <div
          className="relative w-full overflow-hidden rounded-2xl shadow-lg bg-slate-900 border border-slate-200/80 group"
          onMouseEnter={() => setIsHovered(true)}
          onMouseLeave={() => setIsHovered(false)}
        >
          {/* Carousel Slide Track */}
          <div
            className="flex transition-transform duration-500 ease-out"
            style={{ transform: `translateX(-${currentSlide * 100}%)` }}
          >
            {HERO_BANNERS.map((banner, index) => (
              <div
                key={banner.id}
                onClick={() => navigate(banner.link)}
                className="w-full flex-shrink-0 relative cursor-pointer"
              >
                <img
                  src={banner.imageUrl}
                  alt={banner.alt}
                  className="w-full flex-shrink-0 object-cover h-44 sm:h-64 md:h-80 lg:h-96"
                  loading={index === 0 ? 'eager' : 'lazy'}
                  decoding="async"
                />
              </div>
            ))}
          </div>

          {/* Left Navigation Arrow */}
          <button
            onClick={(e) => {
              e.stopPropagation();
              prevSlide();
            }}
            className="absolute left-3 top-1/2 -translate-y-1/2 bg-white/90 hover:bg-white rounded-full p-2 sm:p-2.5 shadow-md transition-all hover:scale-110 active:scale-95 text-slate-800 cursor-pointer opacity-90 hover:opacity-100"
            aria-label="Previous Slide"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>

          {/* Right Navigation Arrow */}
          <button
            onClick={(e) => {
              e.stopPropagation();
              nextSlide();
            }}
            className="absolute right-3 top-1/2 -translate-y-1/2 bg-white/90 hover:bg-white rounded-full p-2 sm:p-2.5 shadow-md transition-all hover:scale-110 active:scale-95 text-slate-800 cursor-pointer opacity-90 hover:opacity-100"
            aria-label="Next Slide"
          >
            <ChevronRight className="w-5 h-5" />
          </button>

          {/* Indicator Dots */}
          <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex items-center gap-1.5 bg-black/30 backdrop-blur-xs px-3 py-1.5 rounded-full">
            {HERO_BANNERS.map((_, index) => (
              <button
                key={index}
                onClick={(e) => {
                  e.stopPropagation();
                  setCurrentSlide(index);
                }}
                aria-label={`Go to slide ${index + 1}`}
                className={`h-1.5 rounded-full transition-all duration-300 cursor-pointer ${
                  currentSlide === index ? 'bg-[#2874F0] w-6' : 'bg-white/60 w-1.5 hover:bg-white/90'
                }`}
              />
            ))}
          </div>
        </div>
      </section>

      {/* Trust & Value Pillars Strip - Exact shopkart11.in features */}
      <section className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
          <div className="p-3.5 sm:p-4 bg-white rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3 sm:gap-3.5 hover:shadow-md transition">
            <div className="w-10 h-10 sm:w-11 sm:h-11 rounded-xl bg-blue-50 text-[#2874F0] flex items-center justify-center flex-shrink-0">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs sm:text-sm font-bold text-slate-900">Secure Payments</h4>
              <p className="text-[11px] text-slate-500">100% Protected Checkout</p>
            </div>
          </div>

          <div className="p-3.5 sm:p-4 bg-white rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3 sm:gap-3.5 hover:shadow-md transition">
            <div className="w-10 h-10 sm:w-11 sm:h-11 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center flex-shrink-0">
              <RotateCcw className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs sm:text-sm font-bold text-slate-900">Easy Returns</h4>
              <p className="text-[11px] text-slate-500">7-Day Hassle-Free Pickups</p>
            </div>
          </div>

          <div className="p-3.5 sm:p-4 bg-white rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3 sm:gap-3.5 hover:shadow-md transition">
            <div className="w-10 h-10 sm:w-11 sm:h-11 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs sm:text-sm font-bold text-slate-900">Genuine Products</h4>
              <p className="text-[11px] text-slate-500">Direct From Verified Brands</p>
            </div>
          </div>

          <div className="p-3.5 sm:p-4 bg-white rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3 sm:gap-3.5 hover:shadow-md transition">
            <div className="w-10 h-10 sm:w-11 sm:h-11 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center flex-shrink-0">
              <Truck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs sm:text-sm font-bold text-slate-900">Free Delivery</h4>
              <p className="text-[11px] text-slate-500">On Orders Above ₹499</p>
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
          {/* Featured Categories Strip matching shopkart11.in */}
          {categories.length > 0 && (
            <section className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8">
              <div className="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-5 shadow-xs">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-base sm:text-lg font-bold text-slate-900">Shop By Category</h2>
                  <Link
                    to="/products"
                    className="text-xs sm:text-sm font-semibold text-[#2874F0] hover:underline underline-offset-4 flex items-center gap-1"
                  >
                    <span>View All</span>
                    <span>&rarr;</span>
                  </Link>
                </div>

                <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-2.5 sm:gap-3">
                  {categories.map((cat) => {
                    const nameLower = cat.categoryName.toLowerCase();
                    const slugLower = (cat.slug || '').toLowerCase();

                    // High-resolution photography mapping for each category
                    let catImage = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=400&q=80';

                    if (slugLower.includes('laptop') || nameLower.includes('laptop') || nameLower.includes('computer')) {
                      catImage = 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('phone') || nameLower.includes('phone') || nameLower.includes('mobile') || nameLower.includes('tablet')) {
                      catImage = 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('audio') || nameLower.includes('audio') || nameLower.includes('headphone')) {
                      catImage = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('watch') || nameLower.includes('watch') || nameLower.includes('wearable')) {
                      catImage = 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('men') || nameLower.includes('men')) {
                      catImage = 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('women') || nameLower.includes('women') || slugLower.includes('fashion') || nameLower.includes('fashion')) {
                      catImage = 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('footwear') || nameLower.includes('footwear') || nameLower.includes('shoe') || nameLower.includes('sneaker')) {
                      catImage = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('home') || nameLower.includes('home') || nameLower.includes('kitchen') || slugLower.includes('furniture')) {
                      catImage = 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('appliance') || nameLower.includes('appliance')) {
                      catImage = 'https://images.unsplash.com/photo-1585338107529-13afc5f02586?auto=format&fit=crop&w=400&q=80';
                    } else if (slugLower.includes('beauty') || nameLower.includes('beauty')) {
                      catImage = 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=400&q=80';
                    }

                    return (
                      <Link
                        key={cat.categoryId}
                        to={`/products?categorySlug=${cat.slug}`}
                        className="group flex flex-col items-center text-center p-2.5 rounded-xl hover:bg-slate-50 transition duration-200"
                      >
                        <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-2xl overflow-hidden bg-slate-50 border border-slate-200/80 p-1 mb-2 group-hover:scale-105 group-hover:border-blue-400 transition-all duration-300 relative shadow-2xs">
                          <img
                            src={catImage}
                            alt={cat.categoryName}
                            className="w-full h-full object-cover rounded-xl group-hover:scale-110 transition-transform duration-500"
                            loading="lazy"
                            decoding="async"
                            width="64"
                            height="64"
                            onError={(e) => {
                              (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=400&q=80';
                            }}
                          />
                        </div>
                        <span className="text-[11px] sm:text-xs font-semibold text-slate-800 group-hover:text-[#2874F0] transition line-clamp-1">
                          {cat.categoryName}
                        </span>
                      </Link>
                    );
                  })}
                </div>
              </div>
            </section>
          )}

          {/* Deals of the Day matching shopkart11.in */}
          {deals.length > 0 && (
            <section className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8">
              <div className="bg-gradient-to-r from-red-600 via-rose-600 to-pink-600 rounded-2xl p-4 sm:p-6 text-white mb-6 shadow-md flex flex-col md:flex-row items-center justify-between gap-4">
                <div className="flex items-center gap-3 sm:gap-4">
                  <div className="w-12 h-12 rounded-xl bg-white text-red-600 flex items-center justify-center shadow-md flex-shrink-0">
                    <Zap className="w-6 h-6 fill-red-600 animate-bounce" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-xl sm:text-2xl font-black tracking-tight">Deals of the Day</h2>
                      <span className="px-2 py-0.5 bg-yellow-400 text-slate-950 font-black text-[10px] rounded-full uppercase tracking-wider">
                        Limited Stock
                      </span>
                    </div>
                    <p className="text-xs sm:text-sm text-red-100 mt-0.5">
                      Handpicked bargains with the highest verified price drops and free delivery
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-3 sm:gap-4">
                  <div className="flex items-center gap-1.5 bg-black/30 backdrop-blur-xs px-3.5 py-1.5 rounded-xl text-white font-mono text-xs sm:text-sm border border-white/20">
                    <Clock className="w-4 h-4 text-yellow-300" />
                    <span>
                      {String(timeLeft.hours).padStart(2, '0')}:{String(timeLeft.minutes).padStart(2, '0')}:{String(timeLeft.seconds).padStart(2, '0')}
                    </span>
                  </div>
                  <Link
                    to="/products?sort=deals"
                    className="px-5 py-2 bg-white text-red-600 font-bold text-xs rounded-full hover:bg-red-50 transition shadow-md whitespace-nowrap"
                  >
                    See All Deals &rarr;
                  </Link>
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
                {deals.slice(0, 4).map((product) => (
                  <ProductCard key={product.productId} product={product} />
                ))}
              </div>
            </section>
          )}

          {/* Bestsellers Showcase */}
          {bestsellers.length > 0 && (
            <section className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8">
              <div className="flex items-center justify-between mb-4 sm:mb-6">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center">
                    <TrendingUp className="w-4 h-4" />
                  </div>
                  <h2 className="text-lg sm:text-2xl font-black text-slate-900">Trending Bestsellers</h2>
                </div>
                <Link to="/products?sort=bestsellers" className="text-xs sm:text-sm font-bold text-[#2874F0] hover:underline flex items-center gap-1">
                  <span>View All</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
                {bestsellers.slice(0, 8).map((product) => (
                  <ProductCard key={product.productId} product={product} />
                ))}
              </div>
            </section>
          )}

          {/* New Arrivals Showcase */}
          {newArrivals.length > 0 && (
            <section className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8">
              <div className="flex items-center justify-between mb-4 sm:mb-6">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center">
                    <Sparkles className="w-4 h-4" />
                  </div>
                  <h2 className="text-lg sm:text-2xl font-black text-slate-900">Just Dropped: New Arrivals</h2>
                </div>
                <Link to="/products?sort=newest" className="text-xs sm:text-sm font-bold text-[#2874F0] hover:underline flex items-center gap-1">
                  <span>View All</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
                {newArrivals.slice(0, 4).map((product) => (
                  <ProductCard key={product.productId} product={product} />
                ))}
              </div>
            </section>
          )}
        </>
      )}

      {/* Newsletter / Discount Callout */}
      <section className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-br from-[#172337] via-blue-950 to-slate-900 rounded-3xl p-6 sm:p-10 text-white relative overflow-hidden shadow-xl">
          <div className="absolute -right-12 -bottom-12 w-64 h-64 bg-blue-500/20 rounded-full blur-3xl pointer-events-none" />
          <div className="max-w-2xl space-y-3 sm:space-y-4">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 text-blue-200 text-xs font-semibold">
              <Gift className="w-3.5 h-3.5 text-amber-300" />
              <span>Special Welcome Offer</span>
            </div>
            <h3 className="text-xl sm:text-3xl font-black tracking-tight leading-tight">
              Get ₹200 OFF on Your First Order!
            </h3>
            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
              Subscribe to the ShopKart VIP dispatch for exclusive member-only early sale access, secret coupon codes, and tech launches.
            </p>

            {subscribed ? (
              <div className="p-3.5 bg-emerald-500/20 border border-emerald-400/30 rounded-2xl flex items-center gap-3 text-emerald-300 text-xs font-bold">
                <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
                <span>You're in! Use coupon code <strong className="text-white bg-emerald-600/40 px-2 py-0.5 rounded">SHOPKART200</strong> at checkout.</span>
              </div>
            ) : (
              <form onSubmit={handleSubscribe} className="flex flex-col sm:flex-row gap-3 pt-2 max-w-md">
                <div className="relative flex-1">
                  <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="email"
                    required
                    value={emailInput}
                    onChange={(e) => setEmailInput(e.target.value)}
                    placeholder="Enter your email address..."
                    className="w-full pl-10 pr-4 py-2.5 bg-white/10 border border-white/20 rounded-xl text-white placeholder-slate-400 text-xs focus:outline-hidden focus:ring-2 focus:ring-[#2874F0] transition"
                  />
                </div>
                <button
                  type="submit"
                  className="px-6 py-2.5 bg-[#2874F0] hover:bg-blue-600 text-white font-bold text-xs rounded-xl shadow-lg transition whitespace-nowrap cursor-pointer"
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
