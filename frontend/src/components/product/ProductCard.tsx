import React from 'react';
import { Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { addToCart } from '@/store/slices/cartSlice';
import { toggleWishlist } from '@/store/slices/wishlistSlice';
import { showToast, toggleCartDrawer } from '@/store/slices/uiSlice';
import { RatingStars } from '@/components/common/RatingStars';
import type { Product } from '@/types';
import { Heart, ShoppingCart } from 'lucide-react';

interface ProductCardProps {
  product: Product;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const dispatch = useAppDispatch();
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

  const handleToggleWishlist = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) {
      dispatch(showToast({ message: 'Please login to save items to your wishlist', type: 'info' }));
      return;
    }
    dispatch(toggleWishlist(product.productId));
  };

  return (
    <div className="group bg-white rounded-2xl border border-gray-200 overflow-hidden hover:shadow-xl hover:border-blue-200 transition-all duration-300 flex flex-col relative">
      {/* Badges */}
      <div className="absolute top-3 left-3 z-10 flex flex-col gap-1">
        {product.discountPercentage > 0 && (
          <span className="bg-red-500 text-white font-black text-[11px] px-2 py-0.5 rounded-full shadow-sm">
            {product.discountPercentage}% OFF
          </span>
        )}
        {!product.inStock && (
          <span className="bg-gray-900/80 text-white font-bold text-[10px] px-2 py-0.5 rounded-full backdrop-blur-xs">
            Out of Stock
          </span>
        )}
      </div>

      {/* Wishlist Button */}
      <button
        onClick={handleToggleWishlist}
        className="absolute top-3 right-3 z-10 w-8 h-8 rounded-full bg-white/90 backdrop-blur-xs flex items-center justify-center text-gray-400 hover:text-red-500 hover:bg-white shadow-sm transition"
        title="Wishlist"
      >
        <Heart
          className={`w-4 h-4 transition ${
            isInWishlist ? 'fill-red-500 text-red-500' : 'text-gray-500'
          }`}
        />
      </button>

      {/* Product Image */}
      <Link to={`/products/${product.slug || product.productId}`} className="relative block aspect-square overflow-hidden bg-gray-50">
        <img
          src={product.primaryImageUrl || '/assets/images/products/placeholder.png'}
          alt={product.productName}
          className="w-full h-full object-contain p-4 group-hover:scale-105 transition-transform duration-300"
          onError={(e) => {
            (e.target as HTMLElement).setAttribute('src', '/assets/images/products/placeholder.png');
          }}
        />
      </Link>

      {/* Content */}
      <div className="p-4 flex flex-col flex-1">
        {product.brand && (
          <p className="text-[11px] font-bold tracking-wider text-blue-600 uppercase mb-1">
            {product.brand}
          </p>
        )}

        <Link
          to={`/products/${product.slug || product.productId}`}
          className="font-bold text-sm text-gray-900 hover:text-blue-600 line-clamp-2 mb-2 group-hover:underline"
        >
          {product.productName}
        </Link>

        {/* Rating */}
        <div className="mb-3">
          <RatingStars rating={product.ratingAverage || 0} count={product.ratingCount || 0} />
        </div>

        {/* Price and Cart */}
        <div className="mt-auto pt-2 border-t border-gray-100 flex items-center justify-between">
          <div>
            <div className="flex items-baseline gap-1.5">
              <span className="font-black text-lg text-gray-900">
                ₹{product.effectivePrice ? product.effectivePrice.toLocaleString('en-IN') : product.price.toLocaleString('en-IN')}
              </span>
              {product.discountPercentage > 0 && (
                <span className="text-xs text-gray-400 line-through">
                  ₹{product.price.toLocaleString('en-IN')}
                </span>
              )}
            </div>
            {product.inStock && product.stockQuantity <= 5 && (
              <p className="text-[10px] font-semibold text-amber-600">Only {product.stockQuantity} left!</p>
            )}
          </div>

          <button
            onClick={handleAddToCart}
            disabled={!product.inStock}
            className={`p-2.5 rounded-xl flex items-center justify-center transition shadow-sm ${
              product.inStock
                ? 'bg-blue-600 text-white hover:bg-blue-700 active:scale-95'
                : 'bg-gray-100 text-gray-400 cursor-not-allowed'
            }`}
            title={product.inStock ? 'Add to Cart' : 'Out of Stock'}
          >
            <ShoppingCart className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
