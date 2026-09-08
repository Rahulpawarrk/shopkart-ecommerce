import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchWishlist, toggleWishlist, moveToCart } from '@/store/slices/wishlistSlice';
import { fetchCart } from '@/store/slices/cartSlice';
import { showToast } from '@/store/slices/uiSlice';
import {
  Heart,
  ShoppingCart,
  Trash2,
  ArrowRight,
  Sparkles,
} from 'lucide-react';

export const WishlistPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const { items, isLoading } = useAppSelector((state) => state.wishlist);

  useEffect(() => {
    dispatch(fetchWishlist());
  }, [dispatch]);

  const handleRemove = async (productId: number) => {
    try {
      await dispatch(toggleWishlist(productId)).unwrap();
      dispatch(showToast({ message: 'Removed from wishlist', type: 'info' }));
    } catch (err: any) {
      dispatch(showToast({ message: err || 'Failed to update wishlist', type: 'error' }));
    }
  };

  const handleMoveToCart = async (productId: number) => {
    try {
      await dispatch(moveToCart(productId)).unwrap();
      dispatch(fetchCart());
      dispatch(showToast({ message: 'Moved item to cart', type: 'success' }));
    } catch (err: any) {
      dispatch(showToast({ message: err || 'Failed to move item to cart', type: 'error' }));
    }
  };

  if (isLoading && items.length === 0) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-20 text-center">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm text-slate-500">Loading your wishlist...</p>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center">
        <div className="w-20 h-20 bg-rose-50 rounded-full flex items-center justify-center mx-auto mb-4 text-rose-500">
          <Heart className="w-10 h-10 stroke-[1.5]" />
        </div>
        <h2 className="text-2xl font-black text-slate-900 mb-2">Your wishlist is empty</h2>
        <p className="text-slate-500 text-sm max-w-md mx-auto mb-8">
          Explore our trending catalog, discover exciting offers, and save your favorite products to buy later!
        </p>
        <Link
          to="/products"
          className="inline-flex items-center gap-2 px-6 py-3 bg-primary text-white font-bold rounded-xl hover:bg-primary/90 transition shadow-lg shadow-primary/20 text-sm"
        >
          <Sparkles className="w-4 h-4" />
          Explore Products
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-slate-900">My Wishlist ({items.length})</h1>
          <p className="text-xs text-slate-500">Saved items you love</p>
        </div>
        <Link
          to="/products"
          className="text-xs font-bold text-primary hover:underline flex items-center gap-1"
        >
          Continue Shopping <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {items.map((item) => (
          <div
            key={item.wishlistItemId}
            className="group bg-white rounded-2xl border border-slate-200 overflow-hidden hover:shadow-lg transition flex flex-col justify-between"
          >
            <div className="relative bg-slate-50 aspect-square overflow-hidden">
              <Link to={`/products/${item.productId}`}>
                <img
                  src={item.primaryImageUrl || '/placeholder.png'}
                  alt={item.productName}
                  className="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                  onError={(e) => {
                    (e.target as HTMLImageElement).src = 'https://placehold.co/300x300?text=Product';
                  }}
                />
              </Link>

              {item.discountPercentage > 0 && (
                <span className="absolute top-3 left-3 bg-rose-500 text-white text-[10px] font-black px-2 py-0.5 rounded shadow">
                  {item.discountPercentage}% OFF
                </span>
              )}

              <button
                onClick={() => handleRemove(item.productId)}
                className="absolute top-3 right-3 w-8 h-8 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center text-slate-500 hover:text-rose-600 transition shadow hover:bg-white"
                title="Remove from wishlist"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <div className="p-4 flex flex-col flex-1 justify-between">
              <div>
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                  {item.brand || 'ShopKart'}
                </p>
                <Link
                  to={`/products/${item.productId}`}
                  className="font-bold text-sm text-slate-900 hover:text-primary transition line-clamp-2 leading-snug mb-2"
                >
                  {item.productName}
                </Link>

                <div className="flex items-baseline gap-2 mb-3">
                  <span className="font-black text-slate-900 text-base">
                    ₹{Number(item.effectivePrice || item.unitPrice).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </span>
                  {item.discountPercentage > 0 && (
                    <span className="text-xs text-slate-400 line-through">
                      ₹{Number(item.unitPrice).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </span>
                  )}
                </div>
              </div>

              <div className="pt-3 border-t border-slate-100 flex items-center gap-2">
                <button
                  onClick={() => handleMoveToCart(item.productId)}
                  className="flex-1 inline-flex items-center justify-center gap-1.5 py-2 px-3 bg-primary text-white text-xs font-bold rounded-lg hover:bg-primary/90 transition shadow-sm"
                >
                  <ShoppingCart className="w-3.5 h-3.5" />
                  Move to Cart
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
