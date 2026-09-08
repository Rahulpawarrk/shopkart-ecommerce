import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchWishlist, toggleWishlist, moveToCart } from '@/store/slices/wishlistSlice';
import { fetchCart, addToCart } from '@/store/slices/cartSlice';
import { showToast, toggleCartDrawer } from '@/store/slices/uiSlice';
import {
  Heart,
  ShoppingCart,
  Trash2,
  ArrowRight,
  Sparkles,
  Zap,
  LogIn,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react';

export const WishlistPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { items, isLoading } = useAppSelector((state) => state.wishlist);
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);

  useEffect(() => {
    dispatch(fetchWishlist());
  }, [dispatch, isAuthenticated]);

  const handleRemove = async (productId: number) => {
    try {
      await dispatch(toggleWishlist(productId)).unwrap();
      dispatch(showToast({ message: 'Removed from wishlist', type: 'info' }));
    } catch (err: any) {
      dispatch(showToast({ message: err || 'Failed to update wishlist', type: 'error' }));
    }
  };

  const handleMoveToCart = async (productId: number, productName: string) => {
    try {
      if (isAuthenticated) {
        await dispatch(moveToCart(productId)).unwrap();
      } else {
        await dispatch(addToCart({ productId, quantity: 1 })).unwrap();
        await dispatch(toggleWishlist(productId)).unwrap();
      }
      dispatch(fetchCart());
      dispatch(showToast({ message: `Added ${productName} to your cart!`, type: 'success' }));
      dispatch(toggleCartDrawer(true));
    } catch (err: any) {
      dispatch(showToast({ message: err || 'Failed to move item to cart', type: 'error' }));
    }
  };

  const handleBuyNow = (productId: number) => {
    if (!isAuthenticated) {
      navigate(`/login?redirect=${encodeURIComponent(`/checkout?buyNowProductId=${productId}&quantity=1`)}`);
    } else {
      navigate(`/checkout?buyNowProductId=${productId}&quantity=1`);
    }
  };

  if (isLoading && items.length === 0) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-24 text-center">
        <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p className="text-sm font-semibold text-slate-500">Loading your wishlist...</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <div className="flex items-center gap-2">
            <div className="w-9 h-9 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center">
              <Heart className="w-5 h-5 fill-rose-600" />
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-slate-900">
              My Wishlist <span className="text-blue-600 text-xl font-bold">({items.length})</span>
            </h1>
          </div>
          <p className="text-xs text-slate-500 mt-1">Saved items you love and plan to purchase</p>
        </div>

        <Link
          to="/products"
          className="inline-flex items-center gap-2 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs rounded-xl transition w-fit"
        >
          <span>Continue Shopping</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      {/* Guest Notice Banner */}
      {!isAuthenticated && (
        <div className="p-4 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200/80 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-3 shadow-xs">
          <div className="flex items-center gap-3 text-xs text-slate-700">
            <div className="w-8 h-8 rounded-xl bg-blue-600 text-white flex items-center justify-center flex-shrink-0">
              <Sparkles className="w-4 h-4" />
            </div>
            <div>
              <p className="font-bold text-slate-900">Temporary Guest Wishlist Active</p>
              <p className="text-slate-500 text-[11px]">Sign in to permanently sync your wishlist across all devices & get instant discount alerts.</p>
            </div>
          </div>
          <Link
            to="/login?redirect=/wishlist"
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-xl transition shadow-xs whitespace-nowrap flex items-center gap-1.5"
          >
            <LogIn className="w-3.5 h-3.5" />
            <span>Sign In to Sync</span>
          </Link>
        </div>
      )}

      {/* Empty State */}
      {items.length === 0 ? (
        <div className="max-w-2xl mx-auto px-4 py-16 text-center bg-white rounded-3xl border border-slate-200/80 shadow-soft p-10">
          <div className="w-20 h-20 bg-rose-50 rounded-full flex items-center justify-center mx-auto mb-4 text-rose-500">
            <Heart className="w-10 h-10 stroke-[1.5]" />
          </div>
          <h2 className="text-2xl font-black text-slate-900 mb-2">Your Wishlist is Empty</h2>
          <p className="text-slate-500 text-xs sm:text-sm max-w-md mx-auto mb-6 leading-relaxed">
            Don't let your favorite items slip away! Explore trending tech, mobile phones, designer apparel, and hot deals.
          </p>
          <div className="flex flex-wrap items-center justify-center gap-3">
            <Link
              to="/products"
              className="inline-flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl transition shadow-md shadow-blue-600/20 text-xs uppercase tracking-wider"
            >
              <Sparkles className="w-4 h-4" />
              <span>Explore Products</span>
            </Link>
            <Link
              to="/products?sort=deals"
              className="inline-flex items-center gap-2 px-6 py-3 bg-red-50 hover:bg-red-100 text-red-600 font-bold rounded-xl transition text-xs border border-red-200"
            >
              <Zap className="w-4 h-4 fill-red-600" />
              <span>View Flash Deals</span>
            </Link>
          </div>
        </div>
      ) : (
        /* Items Grid */
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {items.map((item) => (
            <div
              key={item.wishlistItemId || item.productId}
              className="group bg-white rounded-3xl border border-slate-200/90 overflow-hidden hover:shadow-card hover:border-blue-300 transition duration-300 flex flex-col justify-between relative"
            >
              {/* Product Visual */}
              <div className="relative bg-slate-50/70 aspect-square overflow-hidden">
                <Link to={`/products/${item.productSlug || item.productId}`}>
                  <img
                    src={item.primaryImageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80'}
                    alt={item.productName}
                    className="w-full h-full object-contain p-4 group-hover:scale-105 transition duration-300"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80';
                    }}
                  />
                </Link>

                {item.discountPercentage > 0 && (
                  <span className="absolute top-3 left-3 bg-red-500 text-white text-[10px] font-black px-2.5 py-0.5 rounded-full shadow-sm">
                    {item.discountPercentage}% OFF
                  </span>
                )}

                <button
                  onClick={() => handleRemove(item.productId)}
                  className="absolute top-3 right-3 w-8 h-8 bg-white/90 backdrop-blur-xs rounded-full flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-white transition shadow-sm"
                  title="Remove from wishlist"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              {/* Product Meta */}
              <div className="p-5 flex flex-col flex-1 justify-between space-y-4">
                <div>
                  <p className="text-[11px] font-bold text-blue-600 uppercase tracking-wider mb-1">
                    {item.brand || 'ShopKart'}
                  </p>
                  <Link
                    to={`/products/${item.productSlug || item.productId}`}
                    className="font-bold text-sm text-slate-900 hover:text-blue-600 transition line-clamp-2 leading-snug mb-2 group-hover:underline"
                  >
                    {item.productName}
                  </Link>

                  <div className="flex items-baseline gap-2">
                    <span className="font-black text-slate-900 text-lg">
                      ₹{Number(item.effectivePrice || item.unitPrice).toLocaleString('en-IN')}
                    </span>
                    {item.discountPercentage > 0 && (
                      <span className="text-xs text-slate-400 line-through font-medium">
                        ₹{Number(item.unitPrice).toLocaleString('en-IN')}
                      </span>
                    )}
                  </div>

                  <div className="mt-2 text-[11px] font-semibold flex items-center gap-1.5">
                    {item.inStock !== false ? (
                      <span className="text-emerald-600 flex items-center gap-1">
                        <CheckCircle2 className="w-3.5 h-3.5" /> In Stock
                      </span>
                    ) : (
                      <span className="text-red-600 flex items-center gap-1">
                        <AlertCircle className="w-3.5 h-3.5" /> Out of Stock
                      </span>
                    )}
                  </div>
                </div>

                {/* Actions */}
                <div className="pt-3 border-t border-slate-100 flex flex-col gap-2">
                  <button
                    onClick={() => handleBuyNow(item.productId)}
                    disabled={item.inStock === false}
                    className="w-full inline-flex items-center justify-center gap-2 py-2.5 px-3 bg-amber-500 hover:bg-amber-600 active:scale-98 text-white text-xs font-bold rounded-xl transition shadow-xs disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                  >
                    <Zap className="w-3.5 h-3.5 fill-white" />
                    <span>Buy Now</span>
                  </button>

                  <button
                    onClick={() => handleMoveToCart(item.productId, item.productName)}
                    disabled={item.inStock === false}
                    className="w-full inline-flex items-center justify-center gap-1.5 py-2.5 px-3 bg-slate-900 hover:bg-slate-800 active:scale-98 text-white text-xs font-bold rounded-xl transition shadow-xs disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                  >
                    <ShoppingCart className="w-3.5 h-3.5" />
                    <span>Move to Cart</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

