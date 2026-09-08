import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { addToCart } from '@/store/slices/cartSlice';
import { toggleWishlist } from '@/store/slices/wishlistSlice';
import { showToast, toggleCartDrawer } from '@/store/slices/uiSlice';
import { RatingStars } from '@/components/common/RatingStars';
import type { Product } from '@/types';
import { Heart, ShoppingCart, Zap, CheckCircle2 } from 'lucide-react';

interface ProductCardProps {
  product: Product;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const isInWishlist = useAppSelector(
    (state) => !!state.wishlist.productIdMap[product.productId]
  );
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!product.inStock) return;

    try {
      await dispatch(addToCart({ productId: product.productId, quantity: 1 })).unwrap();
      dispatch(showToast({ message: `Added ${product.productName} to cart!`, type: 'success' }));
      dispatch(toggleCartDrawer(true));
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to add to cart', type: 'error' }));
    }
  };

  const handleBuyNow = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!product.inStock) return;

    const targetUrl = `/checkout?buyNowProductId=${product.productId}&quantity=1`;
    if (!isAuthenticated) {
      navigate(`/login?redirect=${encodeURIComponent(targetUrl)}`);
    } else {
      navigate(targetUrl);
    }
  };

  const handleToggleWishlist = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    try {
      await dispatch(toggleWishlist(product)).unwrap();
      dispatch(
        showToast({
          message: isInWishlist
            ? `Removed ${product.productName} from wishlist`
            : `Added ${product.productName} to wishlist!`,
          type: isInWishlist ? 'info' : 'success',
        })
      );
    } catch {
      dispatch(showToast({ message: 'Failed to update wishlist', type: 'error' }));
    }
  };

  const fallbackImage =
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80';

  return (
    <div className="group bg-white rounded-3xl border border-slate-200/90 overflow-hidden hover:shadow-card hover:border-blue-300 transition duration-300 flex flex-col relative justify-between">
      {/* Top Badges */}
      <div className="absolute top-3 left-3 z-10 flex flex-col gap-1.5 pointer-events-none">
        {product.discountPercentage > 0 && (
          <span className="bg-red-500 text-white font-black text-[10px] px-2.5 py-0.5 rounded-full shadow-sm">
            {product.discountPercentage}% OFF
          </span>
        )}
        {!product.inStock && (
          <span className="bg-slate-900/90 text-white font-bold text-[10px] px-2.5 py-0.5 rounded-full backdrop-blur-xs">
            Out of Stock
          </span>
        )}
      </div>

      {/* Wishlist Heart Button */}
      <button
        onClick={handleToggleWishlist}
        className="absolute top-3 right-3 z-10 w-8 h-8 rounded-full bg-white/90 backdrop-blur-xs flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-white shadow-sm transition active:scale-90"
        title={isInWishlist ? 'Remove from Wishlist' : 'Add to Wishlist'}
      >
        <Heart
          className={`w-4 h-4 transition ${
            isInWishlist ? 'fill-rose-600 text-rose-600' : 'text-slate-400'
          }`}
        />
      </button>

      {/* Product Image */}
      <Link
        to={`/products/${product.slug || product.productId}`}
        className="relative block aspect-square overflow-hidden bg-slate-50/70"
      >
        <img
          src={product.primaryImageUrl || fallbackImage}
          alt={product.productName}
          className="w-full h-full object-contain p-4 group-hover:scale-105 transition-transform duration-300"
          onError={(e) => {
            (e.target as HTMLElement).setAttribute('src', fallbackImage);
          }}
        />
      </Link>

      {/* Product Content Details */}
      <div className="p-4 sm:p-5 flex flex-col flex-1 justify-between space-y-3.5">
        <div>
          {product.brand && (
            <p className="text-[10px] font-bold tracking-wider text-blue-600 uppercase mb-1">
              {product.brand}
            </p>
          )}

          <Link
            to={`/products/${product.slug || product.productId}`}
            className="font-bold text-sm text-slate-900 hover:text-blue-600 line-clamp-2 leading-snug group-hover:underline"
          >
            {product.productName}
          </Link>

          {/* Rating */}
          <div className="mt-2">
            <RatingStars rating={product.ratingAverage || 0} count={product.ratingCount || 0} />
          </div>

          {/* Price */}
          <div className="mt-2.5 flex items-baseline gap-2">
            <span className="font-black text-lg text-slate-900">
              ₹{Number(product.effectivePrice || product.price).toLocaleString('en-IN')}
            </span>
            {product.discountPercentage > 0 && (
              <span className="text-xs text-slate-400 line-through font-medium">
                ₹{Number(product.price).toLocaleString('en-IN')}
              </span>
            )}
          </div>

          {product.inStock && product.stockQuantity <= 5 && (
            <p className="text-[10px] font-bold text-amber-600 mt-1">
              Only {product.stockQuantity} items left in stock!
            </p>
          )}
        </div>

        {/* Dual Actions: Buy Now & Add to Cart */}
        <div className="pt-3 border-t border-slate-100 flex items-center gap-2">
          <button
            onClick={handleAddToCart}
            disabled={!product.inStock}
            className={`flex-1 py-2.5 px-2 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 shadow-xs ${
              product.inStock
                ? 'bg-slate-100 hover:bg-slate-200 text-slate-800 active:scale-95 cursor-pointer'
                : 'bg-slate-100 text-slate-400 cursor-not-allowed'
            }`}
            title="Add to Cart"
          >
            <ShoppingCart className="w-3.5 h-3.5" />
            <span>Add</span>
          </button>

          <button
            onClick={handleBuyNow}
            disabled={!product.inStock}
            className={`flex-1 py-2.5 px-2 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 shadow-sm text-white ${
              product.inStock
                ? 'bg-blue-600 hover:bg-blue-700 active:scale-95 cursor-pointer'
                : 'bg-slate-300 text-slate-500 cursor-not-allowed'
            }`}
            title="Buy Now"
          >
            <Zap className="w-3.5 h-3.5 fill-white" />
            <span>Buy Now</span>
          </button>
        </div>
      </div>
    </div>
  );
};

