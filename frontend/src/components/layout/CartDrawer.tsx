import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { toggleCartDrawer } from '@/store/slices/uiSlice';
import { updateCartItem, removeFromCart } from '@/store/slices/cartSlice';
import {
  X,
  ShoppingBag,
  Trash2,
  Plus,
  Minus,
  ArrowRight,
  ShieldCheck,
  Truck,
} from 'lucide-react';

export const CartDrawer: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const isOpen = useAppSelector((state) => state.ui.cartDrawerOpen);
  const cart = useAppSelector((state) => state.cart.cart);

  if (!isOpen) return null;

  const handleUpdateQuantity = (cartItemId: number, currentQty: number, change: number) => {
    const newQty = currentQty + change;
    if (newQty > 0) {
      dispatch(updateCartItem({ cartItemId, quantity: newQty }));
    } else {
      dispatch(removeFromCart(cartItemId));
    }
  };

  const handleCheckout = () => {
    dispatch(toggleCartDrawer(false));
    navigate('/checkout');
  };

  const items = cart?.items || [];
  const isEmpty = items.length === 0;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div
        onClick={() => dispatch(toggleCartDrawer(false))}
        className="absolute inset-0 bg-slate-950/60 backdrop-blur-xs transition-opacity"
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-6 sm:pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col border-l border-slate-200">
          {/* Header */}
          <div className="p-4 sm:p-5 border-b border-slate-100 flex items-center justify-between bg-white sticky top-0 z-10">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold shadow-2xs">
                <ShoppingBag className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="font-black text-base sm:text-lg text-slate-900">Your Cart</h2>
                  <span className="text-xs font-black bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full">
                    {cart?.totalQuantity || 0}
                  </span>
                </div>
                <p className="text-[11px] text-slate-400 font-medium">Review items before checkout</p>
              </div>
            </div>
            <button
              onClick={() => dispatch(toggleCartDrawer(false))}
              className="p-2 text-slate-400 hover:text-slate-700 rounded-xl hover:bg-slate-100 transition"
              aria-label="Close cart drawer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Delivery Promo Bar */}
          {!isEmpty && (
            <div className="bg-gradient-to-r from-emerald-500 to-teal-600 text-white text-[11px] font-bold py-2 px-4 flex items-center justify-center gap-1.5 shadow-2xs">
              <Truck className="w-3.5 h-3.5 text-emerald-200" />
              <span>⚡ Free Express Delivery unlocked for your order!</span>
            </div>
          )}

          {/* Cart Items List */}
          <div className="flex-1 overflow-y-auto p-4 sm:p-5 space-y-3">
            {isEmpty ? (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 space-y-4">
                <div className="w-20 h-20 rounded-3xl bg-blue-50 text-blue-600 flex items-center justify-center shadow-inner">
                  <ShoppingBag className="w-10 h-10" />
                </div>
                <div>
                  <h3 className="font-black text-slate-900 text-lg">Your cart is empty</h3>
                  <p className="text-xs text-slate-500 max-w-xs mt-1">
                    Discover our bestselling electronics, premium fashion, and home essentials.
                  </p>
                </div>
                <button
                  onClick={() => {
                    dispatch(toggleCartDrawer(false));
                    navigate('/products');
                  }}
                  className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white text-xs font-black rounded-2xl transition shadow-md shadow-blue-600/20 active:scale-98"
                >
                  Start Shopping
                </button>
              </div>
            ) : (
              items.map((item) => (
                <div
                  key={item.cartItemId}
                  className="p-3 bg-white hover:bg-slate-50/50 border border-slate-200 rounded-2xl flex gap-3 items-center transition shadow-2xs group"
                >
                  {/* Strict Fixed-Dimension Image Container */}
                  <div className="w-20 h-20 min-w-[5rem] max-w-[5rem] min-h-[5rem] max-h-[5rem] rounded-xl bg-white border border-slate-100 p-1.5 flex items-center justify-center flex-shrink-0 overflow-hidden shadow-2xs">
                    <img
                      src={item.primaryImageUrl || '/placeholder.svg'}
                      alt={item.productName}
                      className="max-w-full max-h-full object-contain"
                      onError={(e) => {
                        (e.target as HTMLElement).setAttribute('src', '/placeholder.svg');
                      }}
                    />
                  </div>

                  {/* Item Content Details */}
                  <div className="flex-1 min-w-0 flex flex-col justify-between self-stretch">
                    <div>
                      <div className="flex items-start justify-between gap-1.5">
                        <Link
                          to={`/products/${item.productSlug}`}
                          onClick={() => dispatch(toggleCartDrawer(false))}
                          className="text-xs font-bold text-slate-900 hover:text-blue-600 line-clamp-2 leading-snug"
                        >
                          {item.productName}
                        </Link>
                        <button
                          onClick={() => dispatch(removeFromCart(item.cartItemId))}
                          className="text-slate-400 hover:text-red-600 hover:bg-red-50 p-1.5 rounded-lg transition flex-shrink-0"
                          title="Remove item from cart"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                      {item.brand && (
                        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mt-0.5">
                          {item.brand}
                        </span>
                      )}
                    </div>

                    {/* Bottom Row: Price & Quantity Controls */}
                    <div className="flex items-center justify-between mt-2 pt-1 border-t border-slate-100">
                      {/* Quantity Stepper */}
                      <div className="flex items-center border border-slate-200 rounded-lg bg-slate-50 overflow-hidden">
                        <button
                          onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, -1)}
                          className="p-1 hover:bg-white text-slate-600 transition"
                          aria-label="Decrease quantity"
                        >
                          <Minus className="w-3 h-3" />
                        </button>
                        <span className="px-2 text-xs font-black text-slate-900">{item.quantity}</span>
                        <button
                          onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, 1)}
                          className="p-1 hover:bg-white text-slate-600 transition"
                          aria-label="Increase quantity"
                        >
                          <Plus className="w-3 h-3" />
                        </button>
                      </div>

                      {/* Price Display */}
                      <div className="text-right">
                        <span className="font-black text-xs sm:text-sm text-slate-900">
                          ₹{item.effectivePrice.toLocaleString('en-IN')}
                        </span>
                        {item.discountPercentage > 0 && (
                          <span className="text-[10px] text-slate-400 line-through block leading-none">
                            ₹{item.unitPrice.toLocaleString('en-IN')}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Footer / Summary Checkout Area */}
          {!isEmpty && cart && (
            <div className="p-4 sm:p-5 border-t border-slate-200 bg-slate-50/80 space-y-3.5">
              <div className="space-y-1.5 text-xs">
                <div className="flex justify-between text-slate-600 font-medium">
                  <span>Subtotal</span>
                  <span>₹{cart.subtotal.toLocaleString('en-IN')}</span>
                </div>
                {cart.totalDiscount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-bold">
                    <span>Product Discounts</span>
                    <span>-₹{cart.totalDiscount.toLocaleString('en-IN')}</span>
                  </div>
                )}
                {cart.couponDiscount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-bold">
                    <span>Coupon Discount ({cart.appliedCouponCode})</span>
                    <span>-₹{cart.couponDiscount.toLocaleString('en-IN')}</span>
                  </div>
                )}
                <div className="flex justify-between text-slate-600 font-medium">
                  <span>Shipping</span>
                  <span className="text-emerald-700 font-bold bg-emerald-100 px-2 py-0.5 rounded-full text-[10px]">
                    FREE
                  </span>
                </div>
                <div className="border-t border-slate-200 pt-2 flex justify-between text-sm sm:text-base font-black text-slate-900">
                  <span>Total Amount</span>
                  <span className="text-blue-600">₹{cart.finalTotal.toLocaleString('en-IN')}</span>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex gap-2.5 pt-1">
                <Link
                  to="/cart"
                  onClick={() => dispatch(toggleCartDrawer(false))}
                  className="w-1/3 text-center py-3 border border-slate-300 bg-white hover:bg-slate-100 rounded-xl text-xs font-bold text-slate-700 transition shadow-2xs"
                >
                  View Cart
                </Link>
                <button
                  onClick={handleCheckout}
                  className="flex-1 flex items-center justify-center gap-2 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-black transition shadow-md shadow-blue-600/20 active:scale-98"
                >
                  <span>Checkout</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>

              <div className="flex items-center justify-center gap-1.5 text-[10px] text-slate-400 font-medium pt-0.5">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                <span>100% Secure Checkout &bull; 7-Day Free Returns</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
