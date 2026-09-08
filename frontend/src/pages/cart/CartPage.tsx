import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { updateCartItem, removeFromCart, applyCoupon, removeCoupon } from '@/store/slices/cartSlice';
import { showToast } from '@/store/slices/uiSlice';
import { ShoppingBag, Trash2, Plus, Minus, Tag, ArrowRight, ShieldCheck } from 'lucide-react';

export const CartPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const cart = useAppSelector((state) => state.cart.cart);
  const isLoading = useAppSelector((state) => state.cart.isLoading);

  const [couponInput, setCouponInput] = useState('');
  const [couponSubmitting, setCouponSubmitting] = useState(false);

  const handleUpdateQuantity = (cartItemId: number, currentQty: number, delta: number) => {
    const newQty = currentQty + delta;
    if (newQty > 0) {
      dispatch(updateCartItem({ cartItemId, quantity: newQty }));
    } else {
      dispatch(removeFromCart(cartItemId));
    }
  };

  const handleApplyCoupon = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!couponInput.trim()) return;

    setCouponSubmitting(true);
    try {
      await dispatch(applyCoupon(couponInput.trim().toUpperCase())).unwrap();
      dispatch(showToast({ message: 'Coupon applied successfully!', type: 'success' }));
      setCouponInput('');
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Invalid coupon code', type: 'error' }));
    } finally {
      setCouponSubmitting(false);
    }
  };

  const handleRemoveCoupon = async () => {
    try {
      await dispatch(removeCoupon()).unwrap();
      dispatch(showToast({ message: 'Coupon removed', type: 'info' }));
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to remove coupon', type: 'error' }));
    }
  };

  const items = cart?.items || [];
  const isEmpty = items.length === 0;

  if (isEmpty) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center space-y-6">
        <div className="w-24 h-24 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center mx-auto shadow-sm">
          <ShoppingBag className="w-12 h-12" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-black text-gray-900">Your Shopping Cart is Empty</h1>
        <p className="text-sm text-gray-500 max-w-md mx-auto">
          Explore our trending catalog to discover high-performance electronics, apparel, and daily essentials.
        </p>
        <Link
          to="/products"
          className="inline-flex items-center gap-2 px-8 py-3.5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm rounded-full shadow-lg transition"
        >
          <span>Continue Shopping</span>
          <ArrowRight className="w-4 h-4" />
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div className="flex items-center justify-between border-b border-gray-200 pb-4">
        <h1 className="text-2xl sm:text-3xl font-black text-gray-900">
          Shopping Cart ({cart?.totalQuantity || 0} items)
        </h1>
        <Link to="/products" className="text-xs font-bold text-blue-600 hover:underline">
          &larr; Continue Shopping
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Cart Line Items (8 cols) */}
        <div className="lg:col-span-8 space-y-4">
          {items.map((item) => (
            <div
              key={item.cartItemId}
              className="p-4 sm:p-6 bg-white rounded-2xl border border-gray-200 shadow-xs flex flex-col sm:flex-row items-center gap-4 sm:gap-6"
            >
              <img
                src={item.primaryImageUrl || '/assets/images/products/placeholder.png'}
                alt={item.productName}
                className="w-24 h-24 object-contain rounded-xl bg-gray-50 p-2 border border-gray-100 flex-shrink-0"
                onError={(e) => {
                  (e.target as HTMLElement).setAttribute('src', '/assets/images/products/placeholder.png');
                }}
              />

              <div className="flex-1 text-center sm:text-left space-y-1">
                {item.brand && <p className="text-[11px] font-bold text-blue-600 uppercase">{item.brand}</p>}
                <Link
                  to={`/products/${item.productSlug}`}
                  className="font-bold text-sm text-gray-900 hover:text-blue-600 line-clamp-2"
                >
                  {item.productName}
                </Link>

                <div className="flex items-center justify-center sm:justify-start gap-2 pt-1">
                  <span className="font-black text-base text-gray-900">
                    ₹{item.effectivePrice.toLocaleString('en-IN')}
                  </span>
                  {item.discountPercentage > 0 && (
                    <span className="text-xs text-gray-400 line-through">
                      ₹{item.unitPrice.toLocaleString('en-IN')}
                    </span>
                  )}
                </div>
              </div>

              {/* Quantity Selector & Line Total */}
              <div className="flex items-center gap-6">
                <div className="flex items-center border border-gray-300 rounded-xl bg-gray-50">
                  <button
                    onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, -1)}
                    className="p-2 hover:bg-gray-200 text-gray-600 rounded-l-xl"
                  >
                    <Minus className="w-3.5 h-3.5" />
                  </button>
                  <span className="px-3 text-xs font-bold text-gray-800">{item.quantity}</span>
                  <button
                    onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, 1)}
                    className="p-2 hover:bg-gray-200 text-gray-600 rounded-r-xl"
                  >
                    <Plus className="w-3.5 h-3.5" />
                  </button>
                </div>

                <div className="text-right min-w-20">
                  <span className="font-black text-base text-gray-900 block">
                    ₹{item.lineTotal.toLocaleString('en-IN')}
                  </span>
                </div>

                <button
                  onClick={() => dispatch(removeFromCart(item.cartItemId))}
                  className="text-gray-400 hover:text-red-600 transition p-2 rounded-lg hover:bg-red-50"
                  title="Remove item"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Order Summary & Coupon (4 cols) */}
        <div className="lg:col-span-4 space-y-6">
          {/* Coupon Box */}
          <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-xs space-y-3">
            <h3 className="font-bold text-sm text-gray-900 flex items-center gap-2">
              <Tag className="w-4 h-4 text-blue-600" /> Apply Coupon Code
            </h3>

            {cart?.appliedCouponCode ? (
              <div className="flex items-center justify-between p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-bold text-emerald-800">
                <span>Code '{cart.appliedCouponCode}' applied (-₹{cart.couponDiscount})</span>
                <button
                  onClick={handleRemoveCoupon}
                  className="text-red-500 hover:text-red-700 underline text-[11px]"
                >
                  Remove
                </button>
              </div>
            ) : (
              <form onSubmit={handleApplyCoupon} className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. WELCOME10"
                  value={couponInput}
                  onChange={(e) => setCouponInput(e.target.value)}
                  className="flex-1 p-2.5 border border-gray-300 rounded-xl text-xs uppercase font-semibold focus:outline-none focus:border-blue-600"
                />
                <button
                  type="submit"
                  disabled={couponSubmitting}
                  className="px-4 py-2.5 bg-gray-900 text-white text-xs font-bold rounded-xl hover:bg-gray-800 transition disabled:opacity-50"
                >
                  {couponSubmitting ? 'Checking...' : 'Apply'}
                </button>
              </form>
            )}
          </div>

          {/* Price Breakdown */}
          {cart && (
            <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-xs space-y-4">
              <h3 className="font-bold text-sm text-gray-900 border-b border-gray-100 pb-3">
                Order Summary
              </h3>

              <div className="space-y-2 text-xs text-gray-600">
                <div className="flex justify-between">
                  <span>Subtotal</span>
                  <span className="font-semibold text-gray-900">₹{cart.subtotal.toLocaleString('en-IN')}</span>
                </div>
                {cart.totalDiscount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-semibold">
                    <span>Product Discounts</span>
                    <span>-₹{cart.totalDiscount.toLocaleString('en-IN')}</span>
                  </div>
                )}
                {cart.couponDiscount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-semibold">
                    <span>Promotional Coupon</span>
                    <span>-₹{cart.couponDiscount.toLocaleString('en-IN')}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span>Estimated Taxes</span>
                  <span className="font-semibold text-gray-900">₹{cart.taxAmount.toLocaleString('en-IN')}</span>
                </div>
                <div className="flex justify-between">
                  <span>Shipping & Delivery</span>
                  <span className="text-emerald-600 font-bold">FREE</span>
                </div>

                <div className="border-t border-gray-200 pt-3 flex justify-between text-base font-black text-gray-900">
                  <span>Total Payable</span>
                  <span className="text-blue-600">₹{cart.finalTotal.toLocaleString('en-IN')}</span>
                </div>
              </div>

              <button
                onClick={() => navigate('/checkout')}
                className="w-full py-3.5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm rounded-xl shadow-md transition duration-200 flex items-center justify-center gap-2"
              >
                <span>Proceed to Checkout</span>
                <ArrowRight className="w-4 h-4" />
              </button>

              <div className="flex items-center justify-center gap-2 text-[11px] text-gray-400 pt-2">
                <ShieldCheck className="w-4 h-4 text-emerald-500" />
                <span>Safe & Secure 256-Bit Encrypted Payments</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
