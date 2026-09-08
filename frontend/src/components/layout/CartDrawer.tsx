import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { toggleCartDrawer } from '@/store/slices/uiSlice';
import { updateCartItem, removeFromCart } from '@/store/slices/cartSlice';
import { X, ShoppingBag, Trash2, Plus, Minus, ArrowRight } from 'lucide-react';

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
        className="absolute inset-0 bg-gray-900/60 backdrop-blur-sm transition-opacity"
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col">
          {/* Header */}
          <div className="p-4 border-b border-gray-100 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <ShoppingBag className="w-5 h-5 text-blue-600" />
              <h2 className="font-bold text-lg text-gray-900">
                Your Cart ({cart?.totalQuantity || 0})
              </h2>
            </div>
            <button
              onClick={() => dispatch(toggleCartDrawer(false))}
              className="p-1.5 text-gray-400 hover:text-gray-600 rounded-full hover:bg-gray-100"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Cart Items List */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {isEmpty ? (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 space-y-4">
                <div className="w-20 h-20 rounded-full bg-blue-50 text-blue-500 flex items-center justify-center">
                  <ShoppingBag className="w-10 h-10" />
                </div>
                <h3 className="font-bold text-gray-800 text-lg">Your cart is empty</h3>
                <p className="text-xs text-gray-500 max-w-xs">
                  Looks like you haven't added anything to your cart yet. Discover trending deals today!
                </p>
                <button
                  onClick={() => {
                    dispatch(toggleCartDrawer(false));
                    navigate('/products');
                  }}
                  className="px-6 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-full hover:bg-blue-700 transition shadow-md"
                >
                  Explore Products
                </button>
              </div>
            ) : (
              items.map((item) => (
                <div
                  key={item.cartItemId}
                  className="flex gap-3 p-3 bg-gray-50 rounded-xl border border-gray-100 relative group"
                >
                  <img
                    src={item.primaryImageUrl || '/placeholder.svg'}
                    alt={item.productName}
                    className="w-18 h-18 object-cover rounded-lg bg-white border border-gray-200 flex-shrink-0"
                    onError={(e) => {
                      (e.target as HTMLElement).setAttribute('src', '/placeholder.svg');
                    }}
                  />

                  <div className="flex-1 min-w-0">
                    <Link
                      to={`/products/${item.productSlug}`}
                      onClick={() => dispatch(toggleCartDrawer(false))}
                      className="text-xs font-bold text-gray-900 hover:text-blue-600 line-clamp-2"
                    >
                      {item.productName}
                    </Link>
                    {item.brand && <p className="text-[11px] text-gray-400 mt-0.5">{item.brand}</p>}

                    <div className="flex items-center gap-2 mt-2">
                      <span className="font-bold text-sm text-gray-900">
                        ₹{item.effectivePrice.toLocaleString('en-IN')}
                      </span>
                      {item.discountPercentage > 0 && (
                        <span className="text-[11px] text-gray-400 line-through">
                          ₹{item.unitPrice.toLocaleString('en-IN')}
                        </span>
                      )}
                    </div>

                    {/* Quantity Controls */}
                    <div className="flex items-center justify-between mt-3">
                      <div className="flex items-center border border-gray-300 rounded-lg bg-white">
                        <button
                          onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, -1)}
                          className="p-1 hover:bg-gray-100 text-gray-500 rounded-l-lg"
                        >
                          <Minus className="w-3.5 h-3.5" />
                        </button>
                        <span className="px-2.5 text-xs font-semibold">{item.quantity}</span>
                        <button
                          onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, 1)}
                          className="p-1 hover:bg-gray-100 text-gray-500 rounded-r-lg"
                        >
                          <Plus className="w-3.5 h-3.5" />
                        </button>
                      </div>

                      <button
                        onClick={() => dispatch(removeFromCart(item.cartItemId))}
                        className="text-gray-400 hover:text-red-600 transition p-1"
                        title="Remove item"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Footer / Checkout */}
          {!isEmpty && cart && (
            <div className="p-4 border-t border-gray-200 bg-gray-50 space-y-3">
              <div className="space-y-1.5 text-xs">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal</span>
                  <span>₹{cart.subtotal.toLocaleString('en-IN')}</span>
                </div>
                {cart.totalDiscount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-medium">
                    <span>Product Discounts</span>
                    <span>-₹{cart.totalDiscount.toLocaleString('en-IN')}</span>
                  </div>
                )}
                {cart.couponDiscount > 0 && (
                  <div className="flex justify-between text-emerald-600 font-medium">
                    <span>Coupon ({cart.appliedCouponCode})</span>
                    <span>-₹{cart.couponDiscount.toLocaleString('en-IN')}</span>
                  </div>
                )}
                <div className="flex justify-between text-gray-600">
                  <span>Shipping</span>
                  <span className="text-emerald-600 font-semibold">FREE</span>
                </div>
                <div className="border-t border-gray-200 pt-2 flex justify-between text-sm font-black text-gray-900">
                  <span>Total Amount</span>
                  <span>₹{cart.finalTotal.toLocaleString('en-IN')}</span>
                </div>
              </div>

              <div className="flex gap-2">
                <Link
                  to="/cart"
                  onClick={() => dispatch(toggleCartDrawer(false))}
                  className="flex-1 text-center py-2.5 border border-gray-300 rounded-xl text-xs font-bold text-gray-700 hover:bg-gray-100 transition"
                >
                  View Cart
                </Link>
                <button
                  onClick={handleCheckout}
                  className="flex-2 flex items-center justify-center gap-1.5 py-2.5 bg-blue-600 text-white rounded-xl text-xs font-bold hover:bg-blue-700 transition shadow-md"
                >
                  <span>Checkout</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
