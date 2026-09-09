import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { productService } from '@/services/productService';
import { useAppDispatch, useAppSelector } from '@/store';
import { addToCart } from '@/store/slices/cartSlice';
import { toggleWishlist } from '@/store/slices/wishlistSlice';
import { showToast, toggleCartDrawer } from '@/store/slices/uiSlice';
import { RatingStars } from '@/components/common/RatingStars';
import type { Product, Review, RatingSummary } from '@/types';
import {
  Heart,
  ShoppingCart,
  Zap,
  Truck,
  ShieldCheck,
  RotateCcw,
  CheckCircle,
  AlertCircle,
  Star,
  MapPin,
} from 'lucide-react';

export const ProductDetailPage: React.FC = () => {
  const params = useParams<{ id?: string; slugOrId?: string }>();
  const productIdentifier = params.slugOrId || params.id;
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useAppDispatch();

  const { isAuthenticated } = useAppSelector((state) => state.auth);
  const [product, setProduct] = useState<Product | null>(null);
  const isInWishlist = useAppSelector(
    (state) => (product ? !!state.wishlist.productIdMap[product.productId] : false)
  );
  const [selectedImage, setSelectedImage] = useState<string>('');
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);

  // Reviews state
  const [reviewSummary, setReviewSummary] = useState<RatingSummary | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [newRating, setNewRating] = useState(5);
  const [newTitle, setNewTitle] = useState('');
  const [newComment, setNewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  // Pincode checker state
  const [pincode, setPincode] = useState('');
  const [pincodeStatus, setPincodeStatus] = useState<string | null>(null);

  useEffect(() => {
    if (!productIdentifier) return;
    setLoading(true);
    productService
      .getProduct(productIdentifier)
      .then((data) => {
        setProduct(data);
        setSelectedImage(data.primaryImageUrl || '/placeholder.svg');
        return productService.getProductReviews(data.productId);
      })
      .then((revData) => {
        setReviewSummary(revData.summary);
        setReviews(revData.reviews);
      })
      .catch((err) => {
        dispatch(showToast({ message: err.message || 'Product not found', type: 'error' }));
      })
      .finally(() => setLoading(false));
  }, [productIdentifier, dispatch]);

  const handleAddToCart = async () => {
    if (!product || !product.inStock) return;
    if (!isAuthenticated) {
      dispatch(showToast({ message: 'Please sign in to add items to your cart', type: 'info' }));
      navigate(`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`);
      return;
    }
    try {
      await dispatch(addToCart({ productId: product.productId, quantity })).unwrap();
      dispatch(showToast({ message: `Added ${product.productName} to cart!`, type: 'success' }));
      dispatch(toggleCartDrawer(true));
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to add item', type: 'error' }));
    }
  };

  const handleBuyNow = () => {
    if (!product || !product.inStock) return;
    const checkoutUrl = `/checkout?buyNowProductId=${product.productId}&quantity=${quantity}`;
    if (!isAuthenticated) {
      dispatch(showToast({ message: 'Please login to checkout directly', type: 'info' }));
      navigate(`/login?redirect=${encodeURIComponent(checkoutUrl)}`);
      return;
    }
    // Navigate to checkout with direct buy query params
    navigate(checkoutUrl);
  };

  const handleToggleWishlist = async () => {
    if (!product) return;
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

  const handleCheckPincode = (e: React.FormEvent) => {
    e.preventDefault();
    if (/^\d{6}$/.test(pincode.trim())) {
      setPincodeStatus('Available for Delivery in 2-3 business days. Cash on Delivery supported.');
    } else {
      setPincodeStatus('Please enter a valid 6-digit Indian PIN code.');
    }
  };

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!product) return;
    if (!isAuthenticated) {
      dispatch(showToast({ message: 'Please login to submit a review', type: 'info' }));
      return;
    }

    setSubmittingReview(true);
    try {
      await productService.submitReview({
        productId: product.productId,
        rating: newRating,
        title: newTitle,
        comment: newComment,
      });
      dispatch(showToast({ message: 'Review submitted successfully!', type: 'success' }));
      setShowReviewModal(false);
      // Reload reviews
      const updated = await productService.getProductReviews(product.productId);
      setReviewSummary(updated.summary);
      setReviews(updated.reviews);
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to submit review', type: 'error' }));
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 animate-pulse space-y-8">
        <div className="h-6 w-48 bg-gray-200 rounded"></div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
          <div className="h-96 bg-gray-200 rounded-3xl"></div>
          <div className="space-y-4">
            <div className="h-8 bg-gray-200 rounded w-3/4"></div>
            <div className="h-6 bg-gray-200 rounded w-1/4"></div>
            <div className="h-24 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-20 text-center space-y-4">
        <h2 className="text-2xl font-bold text-gray-800">Product Not Found</h2>
        <p className="text-gray-500 text-sm">The product you requested might have been removed or is temporarily unavailable.</p>
        <Link to="/products" className="px-6 py-2.5 bg-blue-600 text-white rounded-full font-bold text-sm inline-block">
          Return to Catalog
        </Link>
      </div>
    );
  }

  const galleryImages = [
    product.primaryImageUrl,
    ...(product.images ? product.images.map((img) => img.imageUrl) : []),
  ].filter(Boolean);

  return (
    <div className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 py-4 sm:py-8 space-y-8 sm:space-y-12 pb-24 lg:pb-8">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-xs text-gray-500 font-medium">
        <Link to="/" className="hover:text-blue-600">Home</Link>
        <span>/</span>
        <Link to="/products" className="hover:text-blue-600">Products</Link>
        {product.categoryName && (
          <>
            <span>/</span>
            <Link to={`/products?categorySlug=${product.categoryName.toLowerCase()}`} className="hover:text-blue-600">
              {product.categoryName}
            </Link>
          </>
        )}
        <span>/</span>
        <span className="text-gray-900 font-semibold truncate max-w-xs">{product.productName}</span>
      </nav>

      {/* Main Grid: Gallery & Purchase Info */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
        
        {/* Gallery Column (5 cols) */}
        <div className="lg:col-span-5 space-y-4">
          <div className="aspect-square bg-white rounded-3xl border border-gray-200 p-6 flex items-center justify-center overflow-hidden relative shadow-sm">
            <img
              src={selectedImage}
              alt={product.productName}
              className="max-h-full max-w-full object-contain transition-transform duration-300 hover:scale-105"
              onError={(e) => {
                (e.target as HTMLElement).setAttribute('src', '/placeholder.svg');
              }}
            />
            {product.discountPercentage > 0 && (
              <span className="absolute top-4 left-4 bg-red-500 text-white font-black text-xs px-2.5 py-1 rounded-full shadow-md">
                {product.discountPercentage}% OFF
              </span>
            )}
          </div>

          {/* Thumbnails */}
          {galleryImages.length > 1 && (
            <div className="flex items-center gap-3 overflow-x-auto pb-2">
              {galleryImages.map((img, idx) => (
                <button
                  key={idx}
                  onClick={() => setSelectedImage(img)}
                  className={`w-16 h-16 rounded-xl border-2 p-1.5 bg-white flex-shrink-0 transition ${
                    selectedImage === img ? 'border-blue-600 shadow-md' : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <img src={img} alt="thumbnail" className="w-full h-full object-contain" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Product Details & Actions Column (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          <div>
            {product.brand && (
              <span className="text-xs font-black tracking-widest text-blue-600 uppercase bg-blue-50 px-2.5 py-1 rounded-md">
                {product.brand}
              </span>
            )}
            <h1 className="text-2xl sm:text-3xl font-black text-gray-900 mt-2 leading-tight">
              {product.productName}
            </h1>
            <p className="text-xs text-gray-400 mt-1">SKU: {product.sku}</p>
          </div>

          {/* Ratings Overview */}
          <div className="flex items-center gap-3">
            <RatingStars rating={product.ratingAverage || 0} count={product.ratingCount || 0} size="md" />
            <span className="text-xs text-blue-600 font-semibold cursor-pointer hover:underline">
              {product.ratingCount || 0} Customer Reviews
            </span>
          </div>

          {/* Price Block */}
          <div className="bg-gray-50 p-4 rounded-2xl border border-gray-200 flex flex-wrap items-baseline gap-3">
            <span className="text-3xl sm:text-4xl font-black text-gray-900">
              ₹{product.effectivePrice ? product.effectivePrice.toLocaleString('en-IN') : product.price.toLocaleString('en-IN')}
            </span>
            {product.discountPercentage > 0 && (
              <>
                <span className="text-base text-gray-400 line-through">
                  ₹{product.price.toLocaleString('en-IN')}
                </span>
                <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                  Save ₹{(product.price - product.effectivePrice).toLocaleString('en-IN')}
                </span>
              </>
            )}
            <p className="w-full text-[11px] text-gray-500 mt-1">
              Inclusive of all taxes. Free delivery on orders above ₹499.
            </p>
          </div>

          {/* Stock Status */}
          <div className="flex items-center gap-2 text-sm font-semibold">
            {product.inStock ? (
              <span className="flex items-center gap-1.5 text-emerald-600">
                <CheckCircle className="w-4 h-4" /> In Stock ({product.stockQuantity} units available)
              </span>
            ) : (
              <span className="flex items-center gap-1.5 text-red-600">
                <AlertCircle className="w-4 h-4" /> Currently Out of Stock
              </span>
            )}
          </div>

          {/* Quantity & CTA Buttons */}
          <div className="space-y-4 pt-2">
            <div className="flex items-center gap-4">
              <label className="text-xs font-bold text-gray-700">Quantity:</label>
              <div className="flex items-center border border-gray-300 rounded-xl bg-white shadow-xs">
                <button
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="px-3 py-1.5 text-gray-600 hover:bg-gray-100 rounded-l-xl font-bold"
                >
                  -
                </button>
                <span className="px-4 text-sm font-bold">{quantity}</span>
                <button
                  onClick={() => setQuantity(Math.min(product.stockQuantity || 10, quantity + 1))}
                  className="px-3 py-1.5 text-gray-600 hover:bg-gray-100 rounded-r-xl font-bold"
                >
                  +
                </button>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row gap-3 pt-2">
              <button
                onClick={handleAddToCart}
                disabled={!product.inStock}
                className="flex-1 flex items-center justify-center gap-2 py-3.5 px-6 rounded-xl bg-blue-600 text-white font-bold text-sm hover:bg-blue-700 transition shadow-md disabled:opacity-50 disabled:cursor-not-allowed active:scale-98"
              >
                <ShoppingCart className="w-5 h-5" />
                <span>Add to Cart</span>
              </button>

              <button
                onClick={handleBuyNow}
                disabled={!product.inStock}
                className="flex-1 flex items-center justify-center gap-2 py-3.5 px-6 rounded-xl bg-amber-500 text-white font-bold text-sm hover:bg-amber-600 transition shadow-md disabled:opacity-50 disabled:cursor-not-allowed active:scale-98"
              >
                <Zap className="w-5 h-5 fill-white" />
                <span>Buy Now</span>
              </button>

              <button
                onClick={handleToggleWishlist}
                className={`p-3.5 rounded-xl border transition shadow-xs flex items-center justify-center ${
                  isInWishlist
                    ? 'border-red-200 bg-red-50 text-red-600'
                    : 'border-gray-300 bg-white text-gray-600 hover:bg-gray-50'
                }`}
                title="Save to Wishlist"
              >
                <Heart className={`w-5 h-5 ${isInWishlist ? 'fill-red-600' : ''}`} />
              </button>
            </div>
          </div>

          {/* Pincode & Delivery Checker */}
          <div className="border-t border-gray-200 pt-6 space-y-3">
            <h3 className="text-xs font-bold text-gray-800 uppercase tracking-wider flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-blue-600" /> Delivery Options
            </h3>
            <form onSubmit={handleCheckPincode} className="flex gap-2 max-w-sm">
              <input
                type="text"
                placeholder="Enter 6-digit pincode"
                maxLength={6}
                value={pincode}
                onChange={(e) => setPincode(e.target.value)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-xl text-xs focus:outline-none focus:border-blue-600"
              />
              <button
                type="submit"
                className="px-4 py-2 bg-gray-900 text-white text-xs font-bold rounded-xl hover:bg-gray-800 transition"
              >
                Check
              </button>
            </form>
            {pincodeStatus && (
              <p className={`text-xs font-medium ${pincodeStatus.includes('Available') ? 'text-emerald-600' : 'text-red-500'}`}>
                {pincodeStatus}
              </p>
            )}
          </div>

          {/* Trust Highlights */}
          <div className="grid grid-cols-3 gap-3 border-t border-gray-200 pt-6 text-center text-xs">
            <div className="p-3 bg-gray-50 rounded-xl">
              <Truck className="w-5 h-5 text-blue-600 mx-auto mb-1" />
              <span className="font-bold text-gray-800 block">Fast Dispatch</span>
              <span className="text-[10px] text-gray-500">Ships within 24 hours</span>
            </div>
            <div className="p-3 bg-gray-50 rounded-xl">
              <ShieldCheck className="w-5 h-5 text-emerald-600 mx-auto mb-1" />
              <span className="font-bold text-gray-800 block">Original Quality</span>
              <span className="text-[10px] text-gray-500">Verified manufacturer</span>
            </div>
            <div className="p-3 bg-gray-50 rounded-xl">
              <RotateCcw className="w-5 h-5 text-purple-600 mx-auto mb-1" />
              <span className="font-bold text-gray-800 block">7-Day Returns</span>
              <span className="text-[10px] text-gray-500">Easy replacement</span>
            </div>
          </div>
        </div>
      </div>

      {/* Description Section */}
      {product.description && (
        <div className="bg-white rounded-3xl border border-gray-200 p-8 space-y-4">
          <h2 className="text-xl font-bold text-gray-900">Product Description & Specifications</h2>
          <div className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
            {product.description}
          </div>
        </div>
      )}

      {/* Customer Reviews Section */}
      <div className="bg-white rounded-3xl border border-gray-200 p-8 space-y-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-gray-200 pb-6">
          <div>
            <h2 className="text-xl font-bold text-gray-900">Customer Ratings & Reviews</h2>
            <p className="text-xs text-gray-500 mt-0.5">Verified purchaser experiences</p>
          </div>
          <button
            onClick={() => setShowReviewModal(true)}
            className="px-5 py-2.5 bg-blue-600 text-white rounded-xl text-xs font-bold hover:bg-blue-700 transition shadow-sm"
          >
            Write a Product Review
          </button>
        </div>

        {/* Rating Breakdown */}
        {reviewSummary && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-center bg-gray-50 p-6 rounded-2xl border border-gray-100">
            <div className="text-center space-y-2">
              <span className="text-5xl font-black text-gray-900">{reviewSummary.averageRating.toFixed(1)}</span>
              <div className="flex justify-center">
                <RatingStars rating={reviewSummary.averageRating} showCount={false} size="lg" />
              </div>
              <p className="text-xs text-gray-500">{reviewSummary.totalReviews} verified reviews</p>
            </div>

            <div className="md:col-span-2 space-y-2 text-xs">
              {[
                { stars: 5, count: reviewSummary.fiveStarCount },
                { stars: 4, count: reviewSummary.fourStarCount },
                { stars: 3, count: reviewSummary.threeStarCount },
                { stars: 2, count: reviewSummary.twoStarCount },
                { stars: 1, count: reviewSummary.oneStarCount },
              ].map((row) => {
                const pct = reviewSummary.totalReviews > 0 ? (row.count / reviewSummary.totalReviews) * 100 : 0;
                return (
                  <div key={row.stars} className="flex items-center gap-3">
                    <span className="w-8 font-bold text-gray-700">{row.stars} ★</span>
                    <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                      <div className="h-full bg-amber-400 rounded-full" style={{ width: `${pct}%` }}></div>
                    </div>
                    <span className="w-8 text-right text-gray-500">{row.count}</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Reviews List */}
        <div className="space-y-6">
          {reviews.length === 0 ? (
            <p className="text-sm text-gray-500 italic text-center py-6">
              No reviews yet for this product. Be the first to share your thoughts!
            </p>
          ) : (
            reviews.map((rev) => (
              <div key={rev.reviewId} className="border-b border-gray-100 pb-6 space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-sm text-gray-800">{rev.authorName || 'Customer'}</span>
                    {rev.verifiedPurchase && (
                      <span className="text-[10px] font-bold bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full flex items-center gap-1">
                        <CheckCircle className="w-3 h-3" /> Verified Purchase
                      </span>
                    )}
                  </div>
                  <span className="text-xs text-gray-400">{rev.formattedDate || rev.createdAt?.slice(0, 10)}</span>
                </div>
                <RatingStars rating={rev.rating} showCount={false} />
                <h4 className="font-bold text-sm text-gray-900">{rev.title}</h4>
                <p className="text-xs text-gray-700 leading-relaxed">{rev.comment}</p>
                {rev.imageUrl && (
                  <img src={rev.imageUrl} alt="Review attachment" className="w-20 h-20 object-cover rounded-xl border mt-2" />
                )}
              </div>
            ))
          )}
        </div>
      </div>

      {/* Review Modal */}
      {showReviewModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-md w-full space-y-4 shadow-2xl">
            <h3 className="font-bold text-lg text-gray-900">Write a Product Review</h3>
            <form onSubmit={handleSubmitReview} className="space-y-4">
              <div>
                <label className="text-xs font-bold text-gray-700 block mb-1">Your Rating</label>
                <div className="flex items-center gap-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setNewRating(star)}
                      className="p-1 text-amber-400 hover:scale-110 transition"
                    >
                      <Star className={`w-6 h-6 ${newRating >= star ? 'fill-amber-400' : 'text-gray-300'}`} />
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-xs font-bold text-gray-700 block mb-1">Review Headline</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Excellent battery life and performance"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  className="w-full p-2.5 border border-gray-300 rounded-xl text-xs focus:outline-none focus:border-blue-600"
                />
              </div>

              <div>
                <label className="text-xs font-bold text-gray-700 block mb-1">Detailed Feedback</label>
                <textarea
                  required
                  rows={4}
                  placeholder="Describe what you liked or disliked about this product..."
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  className="w-full p-2.5 border border-gray-300 rounded-xl text-xs focus:outline-none focus:border-blue-600"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowReviewModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-xl text-xs font-bold text-gray-700 hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingReview}
                  className="px-5 py-2 bg-blue-600 text-white rounded-xl text-xs font-bold hover:bg-blue-700 transition disabled:opacity-50"
                >
                  {submittingReview ? 'Submitting...' : 'Submit Review'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Mobile Sticky Bottom Action Bar (< lg) */}
      <div className="fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-slate-200 p-2.5 px-3 flex items-center justify-between gap-2.5 lg:hidden shadow-[0_-4px_16px_rgba(0,0,0,0.1)]">
        {/* Wishlist Icon Button */}
        <button
          onClick={handleToggleWishlist}
          className={`w-10 h-10 rounded-xl border flex items-center justify-center flex-shrink-0 transition active:scale-95 ${
            isInWishlist
              ? 'border-red-200 bg-red-50 text-red-600'
              : 'border-slate-200 bg-slate-50 text-slate-600'
          }`}
          title="Wishlist"
          aria-label="Wishlist"
        >
          <Heart className={`w-4 h-4 ${isInWishlist ? 'fill-red-600' : ''}`} />
        </button>

        {/* Add to Cart */}
        <button
          onClick={handleAddToCart}
          disabled={!product.inStock}
          className="flex-1 py-2.5 px-2 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-xs flex items-center justify-center gap-1.5 transition active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed shadow-xs"
        >
          <ShoppingCart className="w-4 h-4" />
          <span>Add to Cart</span>
        </button>

        {/* Buy Now */}
        <button
          onClick={handleBuyNow}
          disabled={!product.inStock}
          className="flex-1 py-2.5 px-2 rounded-xl bg-[#2874F0] hover:bg-blue-600 text-white font-bold text-xs flex items-center justify-center gap-1.5 transition active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed shadow-md"
        >
          <Zap className="w-4 h-4 fill-white" />
          <span>Buy Now</span>
        </button>
      </div>
    </div>
  );
};
