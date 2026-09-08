import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { customerService } from '@/services/customerService';
import { orderService } from '@/services/orderService';
import { productService } from '@/services/productService';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchCart, applyCoupon, removeCoupon } from '@/store/slices/cartSlice';
import { showToast } from '@/store/slices/uiSlice';
import type { Address, Product } from '@/types';
import {
  MapPin,
  CreditCard,
  Truck,
  ShieldCheck,
  CheckCircle2,
  Plus,
  ArrowRight,
  AlertCircle,
  Banknote,
  Tag,
  Percent,
  Sparkles,
  X,
} from 'lucide-react';

declare global {
  interface Window {
    Razorpay: any;
  }
}

export const CheckoutPage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [searchParams] = useSearchParams();

  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const cart = useAppSelector((state) => state.cart.cart);

  // Direct buy parameters
  const buyNowProductId = searchParams.get('buyNowProductId');
  const buyNowQty = parseInt(searchParams.get('quantity') || '1', 10);
  const [directBuyProduct, setDirectBuyProduct] = useState<Product | null>(null);

  // Checkout form state
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<'RAZORPAY' | 'COD'>('RAZORPAY');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [showAddressModal, setShowAddressModal] = useState(false);

  // Coupon state
  const [couponInput, setCouponInput] = useState('');
  const [couponApplying, setCouponApplying] = useState(false);
  const [directBuyCouponCode, setDirectBuyCouponCode] = useState<string | null>(null);

  const appliedCoupon = buyNowProductId ? directBuyCouponCode : cart?.appliedCouponCode;

  const handleApplyCoupon = async (codeToApply?: string) => {
    const code = (codeToApply || couponInput).trim().toUpperCase();
    if (!code) return;
    setCouponApplying(true);
    try {
      if (buyNowProductId && directBuyProduct) {
        const rawTotal = directBuyProduct.effectivePrice * buyNowQty;
        if (code === 'WELCOME10' && rawTotal < 1000) {
          throw new Error('Minimum order amount of ₹1,000 required for WELCOME10');
        } else if (code === 'FLAT500' && rawTotal < 3000) {
          throw new Error('Minimum order amount of ₹3,000 required for FLAT500');
        } else if (code === 'SUPER20' && rawTotal < 10000) {
          throw new Error('Minimum order amount of ₹10,000 required for SUPER20');
        } else if (code === 'FREESHIP' && rawTotal < 500) {
          throw new Error('Minimum order amount of ₹500 required for FREESHIP');
        } else if (!['WELCOME10', 'FLAT500', 'SUPER20', 'FREESHIP'].includes(code)) {
          throw new Error(`Invalid or expired coupon code: ${code}`);
        }
        setDirectBuyCouponCode(code);
        setCouponInput('');
        dispatch(showToast({ message: `Coupon ${code} applied successfully!`, type: 'success' }));
      } else {
        await dispatch(applyCoupon(code)).unwrap();
        setCouponInput('');
        dispatch(showToast({ message: `Coupon ${code} applied successfully!`, type: 'success' }));
      }
    } catch (err: any) {
      const errMsg = typeof err === 'string' ? err : err?.message || 'Failed to apply coupon';
      dispatch(showToast({ message: errMsg, type: 'error' }));
    } finally {
      setCouponApplying(false);
    }
  };

  const handleRemoveCoupon = async () => {
    setCouponApplying(true);
    try {
      if (buyNowProductId) {
        setDirectBuyCouponCode(null);
        dispatch(showToast({ message: 'Coupon removed', type: 'info' }));
      } else {
        await dispatch(removeCoupon()).unwrap();
        dispatch(showToast({ message: 'Coupon removed', type: 'info' }));
      }
    } catch (err: any) {
      const errMsg = typeof err === 'string' ? err : err?.message || 'Failed to remove coupon';
      dispatch(showToast({ message: errMsg, type: 'error' }));
    } finally {
      setCouponApplying(false);
    }
  };

  // New address form state
  const [newAddr, setNewAddr] = useState({
    fullName: '',
    phone: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'India',
    addressType: 'SHIPPING',
    defaultAddress: true,
  });

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login?redirect=/checkout');
      return;
    }

    // Load Razorpay checkout script
    if (!document.getElementById('razorpay-sdk')) {
      const script = document.createElement('script');
      script.id = 'razorpay-sdk';
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      document.body.appendChild(script);
    }

    // Load addresses
    customerService
      .getAddresses()
      .then((addrs) => {
        setAddresses(addrs);
        const def = addrs.find((a) => a.defaultAddress) || addrs[0];
        if (def) setSelectedAddressId(def.addressId);
      })
      .catch(() => {});

    // If direct buy, fetch target product
    if (buyNowProductId) {
      productService
        .getProduct(buyNowProductId)
        .then(setDirectBuyProduct)
        .catch(() => {})
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [isAuthenticated, buyNowProductId]);

  const handleCreateAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const created = await customerService.addAddress(newAddr);
      setAddresses([...addresses, created]);
      setSelectedAddressId(created.addressId);
      setShowAddressModal(false);
      dispatch(showToast({ message: 'Address saved successfully!', type: 'success' }));
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to save address', type: 'error' }));
    }
  };

  const handlePlaceOrder = async () => {
    if (!selectedAddressId) {
      dispatch(showToast({ message: 'Please select or add a shipping address', type: 'error' }));
      return;
    }

    setPlacingOrder(true);
    try {
      let confirmedOrder;
      const activeCoupon = buyNowProductId ? directBuyCouponCode : cart?.appliedCouponCode;
      if (buyNowProductId && directBuyProduct) {
        confirmedOrder = await orderService.directBuy({
          addressId: selectedAddressId,
          productId: directBuyProduct.productId,
          quantity: buyNowQty,
          paymentMethod: paymentMethod === 'COD' ? 'COD' : 'UPI',
          couponCode: activeCoupon || undefined,
          notes: notes.trim() || undefined,
        });
      } else {
        confirmedOrder = await orderService.checkout({
          addressId: selectedAddressId,
          paymentMethod: paymentMethod === 'COD' ? 'COD' : 'UPI',
          couponCode: activeCoupon || undefined,
          notes: notes.trim() || undefined,
        });
      }

      // If Cash on Delivery, done!
      if (paymentMethod === 'COD') {
        dispatch(fetchCart());
        navigate(`/order-confirmation/${confirmedOrder.orderId}`, { state: { order: confirmedOrder } });
        return;
      }

      // Else Razorpay flow
      const payIntent = await orderService.initiatePayment(confirmedOrder.orderId);

      // Verify Razorpay script is present
      if (typeof window.Razorpay !== 'function') {
        // Fallback for dev environments without external connectivity
        await orderService.verifyPayment({
          orderId: confirmedOrder.orderId,
          transactionReference: 'DEV-SIM-' + Date.now(),
        });
        dispatch(fetchCart());
        navigate(`/order-confirmation/${confirmedOrder.orderId}`, { state: { order: confirmedOrder } });
        return;
      }

      const options = {
        key: (payIntent as any).razorpayKeyId || payIntent.keyId,
        amount: payIntent.amountInPaise,
        currency: payIntent.currency || 'INR',
        name: 'ShopKart E-Commerce',
        description: `Payment for Order #${confirmedOrder.orderNumber}`,
        order_id: payIntent.razorpayOrderId,
        prefill: {
          name: payIntent.customerName || user?.fullName,
          email: payIntent.customerEmail || user?.email,
          contact: payIntent.customerPhone || user?.phone || '',
        },
        theme: {
          color: '#2563eb',
        },
        handler: async (response: any) => {
          try {
            await orderService.verifyPayment({
              orderId: confirmedOrder.orderId,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpayOrderId: response.razorpay_order_id,
              razorpaySignature: response.razorpay_signature,
            });
            dispatch(fetchCart());
            navigate(`/order-confirmation/${confirmedOrder.orderId}`, { state: { order: confirmedOrder } });
          } catch (verErr: any) {
            dispatch(showToast({ message: verErr.message || 'Payment signature mismatch', type: 'error' }));
          }
        },
        modal: {
          ondismiss: async () => {
            await orderService.recordPaymentFailure({
              orderId: confirmedOrder.orderId,
              reason: 'User cancelled or closed the Razorpay payment window',
              razorpayOrderId: payIntent.razorpayOrderId,
            });
            dispatch(showToast({ message: 'Payment cancelled. Order remains pending in My Orders.', type: 'info' }));
            navigate(`/orders`);
          },
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.on('payment.failed', async (resp: any) => {
        await orderService.recordPaymentFailure({
          orderId: confirmedOrder.orderId,
          reason: resp.error?.description || 'Gateway declined payment',
          razorpayOrderId: payIntent.razorpayOrderId,
        });
        dispatch(showToast({ message: resp.error?.description || 'Payment failed', type: 'error' }));
      });
      rzp.open();

    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to place order', type: 'error' }));
    } finally {
      setPlacingOrder(false);
    }
  };

  // Calculation for Direct Buy or Cart
  let totalPayable = 0;
  let itemsCount = 0;
  let couponDiscountAmount = 0;
  let rawSubtotal = 0;

  if (buyNowProductId && directBuyProduct) {
    rawSubtotal = directBuyProduct.effectivePrice * buyNowQty;
    if (directBuyCouponCode === 'WELCOME10') {
      couponDiscountAmount = Math.min(rawSubtotal * 0.10, 1500);
    } else if (directBuyCouponCode === 'FLAT500') {
      couponDiscountAmount = 500;
    } else if (directBuyCouponCode === 'SUPER20') {
      couponDiscountAmount = Math.min(rawSubtotal * 0.20, 4000);
    } else if (directBuyCouponCode === 'FREESHIP') {
      couponDiscountAmount = 100;
    }
    totalPayable = Math.max(0, rawSubtotal - couponDiscountAmount);
    itemsCount = buyNowQty;
  } else if (cart) {
    totalPayable = cart.finalTotal;
    itemsCount = cart.totalQuantity;
    couponDiscountAmount = cart.couponDiscount || 0;
    rawSubtotal = cart.subtotal;
  }

  if (loading) {
    return <div className="p-12 text-center text-sm font-semibold">Preparing secure checkout...</div>;
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div className="border-b border-gray-200 pb-4">
        <h1 className="text-2xl sm:text-3xl font-black text-gray-900">Secure Order Checkout</h1>
        <p className="text-xs text-gray-500 mt-1">Review your delivery address and choose a payment method</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        
        {/* Left Column: Addresses & Payment Methods (8 cols) */}
        <div className="lg:col-span-8 space-y-8">
          
          {/* Step 1: Delivery Address */}
          <div className="bg-white p-6 rounded-3xl border border-gray-200 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3">
              <h2 className="font-bold text-base text-gray-900 flex items-center gap-2">
                <MapPin className="w-5 h-5 text-blue-600" />
                <span>1. Select Delivery Address</span>
              </h2>
              <button
                onClick={() => setShowAddressModal(true)}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-600 rounded-lg text-xs font-bold hover:bg-blue-100 transition"
              >
                <Plus className="w-4 h-4" /> Add Address
              </button>
            </div>

            {addresses.length === 0 ? (
              <div className="text-center py-6 space-y-3">
                <p className="text-xs text-gray-500">No saved shipping addresses found.</p>
                <button
                  onClick={() => setShowAddressModal(true)}
                  className="px-5 py-2 bg-blue-600 text-white rounded-xl text-xs font-bold"
                >
                  Add Your Delivery Address
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {addresses.map((addr) => (
                  <label
                    key={addr.addressId}
                    className={`relative flex flex-col p-4 rounded-2xl border-2 cursor-pointer transition ${
                      selectedAddressId === addr.addressId
                        ? 'border-blue-600 bg-blue-50/40 shadow-sm'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-[10px] font-black uppercase tracking-wider bg-gray-100 px-2 py-0.5 rounded text-gray-700">
                        {addr.addressType}
                      </span>
                      <input
                        type="radio"
                        name="deliveryAddress"
                        checked={selectedAddressId === addr.addressId}
                        onChange={() => setSelectedAddressId(addr.addressId)}
                        className="text-blue-600 focus:ring-blue-500 w-4 h-4"
                      />
                    </div>
                    <p className="font-bold text-sm text-gray-900">{addr.fullName}</p>
                    <p className="text-xs text-gray-600 mt-1 leading-relaxed">
                      {addr.addressLine1}
                      {addr.addressLine2 ? `, ${addr.addressLine2}` : ''}
                      <br />
                      {addr.city}, {addr.state} - {addr.postalCode}
                    </p>
                    <p className="text-xs font-semibold text-gray-700 mt-2">Phone: {addr.phone}</p>
                  </label>
                ))}
              </div>
            )}
          </div>

          {/* Step 2: Payment Method */}
          <div className="bg-white p-6 rounded-3xl border border-gray-200 shadow-xs space-y-4">
            <h2 className="font-bold text-base text-gray-900 flex items-center gap-2 border-b border-gray-100 pb-3">
              <CreditCard className="w-5 h-5 text-blue-600" />
              <span>2. Choose Payment Method</span>
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Razorpay Online */}
              <label
                className={`flex items-start gap-3 p-4 rounded-2xl border-2 cursor-pointer transition ${
                  paymentMethod === 'RAZORPAY'
                    ? 'border-blue-600 bg-blue-50/40 shadow-sm'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <input
                  type="radio"
                  name="paymentMode"
                  checked={paymentMethod === 'RAZORPAY'}
                  onChange={() => setPaymentMethod('RAZORPAY')}
                  className="mt-1 text-blue-600 focus:ring-blue-500 w-4 h-4"
                />
                <div className="space-y-1">
                  <span className="font-bold text-sm text-gray-900 block">
                    Online Payment (Razorpay)
                  </span>
                  <p className="text-xs text-gray-500 leading-relaxed">
                    Pay safely using UPI (GPay, PhonePe, Paytm), Credit/Debit Cards, or Net Banking.
                  </p>
                  <span className="inline-block text-[10px] font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded">
                    Instant & Secure
                  </span>
                </div>
              </label>

              {/* Cash On Delivery */}
              <label
                className={`flex items-start gap-3 p-4 rounded-2xl border-2 cursor-pointer transition ${
                  paymentMethod === 'COD'
                    ? 'border-blue-600 bg-blue-50/40 shadow-sm'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <input
                  type="radio"
                  name="paymentMode"
                  checked={paymentMethod === 'COD'}
                  onChange={() => setPaymentMethod('COD')}
                  className="mt-1 text-blue-600 focus:ring-blue-500 w-4 h-4"
                />
                <div className="space-y-1">
                  <span className="font-bold text-sm text-gray-900 block">
                    Cash On Delivery (COD)
                  </span>
                  <p className="text-xs text-gray-500 leading-relaxed">
                    Pay with cash or UPI QR code at the time of doorstep delivery.
                  </p>
                </div>
              </label>
            </div>
          </div>

          {/* Delivery Instructions / Order Notes */}
          <div className="bg-white p-6 rounded-3xl border border-gray-200 shadow-xs space-y-2">
            <label className="text-xs font-bold text-gray-700 block">
              Order Notes / Delivery Instructions (Optional):
            </label>
            <textarea
              rows={2}
              placeholder="e.g. Please leave package at reception or call before delivery"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full p-3 border border-gray-300 rounded-xl text-xs focus:outline-none focus:border-blue-600"
            />
          </div>
        </div>

        {/* Right Column: Order Summary (4 cols) */}
        <div className="lg:col-span-4 bg-white p-6 rounded-3xl border border-gray-200 shadow-xs space-y-6">
          <h3 className="font-bold text-sm text-gray-900 border-b border-gray-100 pb-3">
            Checkout Summary ({itemsCount} item{itemsCount > 1 ? 's' : ''})
          </h3>

          {/* Items Preview */}
          <div className="space-y-3 max-h-64 overflow-y-auto pr-1">
            {buyNowProductId && directBuyProduct ? (
              <div className="flex items-center gap-3 text-xs">
                <img
                  src={directBuyProduct.primaryImageUrl}
                  alt={directBuyProduct.productName}
                  className="w-12 h-12 object-contain rounded-lg border p-1"
                />
                <div className="flex-1 min-w-0">
                  <p className="font-bold text-gray-900 truncate">{directBuyProduct.productName}</p>
                  <p className="text-gray-500">Qty: {buyNowQty} &times; ₹{directBuyProduct.effectivePrice}</p>
                </div>
                <span className="font-bold text-gray-900">₹{totalPayable.toLocaleString('en-IN')}</span>
              </div>
            ) : (
              cart?.items.map((item) => (
                <div key={item.cartItemId} className="flex items-center gap-3 text-xs">
                  <img
                    src={item.primaryImageUrl}
                    alt={item.productName}
                    className="w-12 h-12 object-contain rounded-lg border p-1"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-bold text-gray-900 truncate">{item.productName}</p>
                    <p className="text-gray-500">Qty: {item.quantity} &times; ₹{item.effectivePrice}</p>
                  </div>
                  <span className="font-bold text-gray-900">₹{item.lineTotal.toLocaleString('en-IN')}</span>
                </div>
              ))
            )}
          </div>

          {/* Promo Coupon Section */}
          <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-200/80 space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-800 flex items-center gap-1.5">
                <Tag className="w-3.5 h-3.5 text-blue-600" />
                <span>Apply Promo Coupon</span>
              </span>
              {appliedCoupon && (
                <span className="text-[10px] font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full">
                  Applied
                </span>
              )}
            </div>

            {appliedCoupon ? (
              <div className="flex items-center justify-between p-2.5 bg-emerald-50 border border-emerald-200 rounded-xl text-xs">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                  <div>
                    <span className="font-mono font-bold text-emerald-900">{appliedCoupon}</span>
                    <p className="text-[11px] text-emerald-700 font-medium">
                      Saved ₹{couponDiscountAmount.toLocaleString('en-IN')} on this order!
                    </p>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={handleRemoveCoupon}
                  disabled={couponApplying}
                  className="p-1 text-slate-400 hover:text-red-600 hover:bg-white rounded-lg transition"
                  title="Remove coupon"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                <div className="flex gap-2">
                  <div className="relative flex-1">
                    <input
                      type="text"
                      placeholder="Coupon code (e.g. WELCOME10)"
                      value={couponInput}
                      onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          handleApplyCoupon();
                        }
                      }}
                      className="w-full pl-8 pr-2.5 py-2 bg-white border border-slate-300 rounded-xl text-xs font-mono uppercase tracking-wider focus:outline-none focus:border-blue-600"
                    />
                    <Percent className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2.5" />
                  </div>
                  <button
                    type="button"
                    onClick={() => handleApplyCoupon()}
                    disabled={!couponInput.trim() || couponApplying}
                    className="px-3.5 py-2 bg-slate-900 hover:bg-blue-600 text-white text-xs font-bold rounded-xl transition disabled:opacity-50 shadow-xs"
                  >
                    {couponApplying ? 'Applying...' : 'Apply'}
                  </button>
                </div>

                {/* Quick select coupons */}
                <div className="flex flex-wrap gap-1 pt-0.5">
                  {[
                    { code: 'WELCOME10', label: '10% OFF' },
                    { code: 'FLAT500', label: '₹500 OFF' },
                    { code: 'SUPER20', label: '20% OFF' },
                  ].map((c) => (
                    <button
                      key={c.code}
                      type="button"
                      onClick={() => {
                        setCouponInput(c.code);
                        handleApplyCoupon(c.code);
                      }}
                      className="px-2 py-0.5 bg-white hover:bg-blue-50 border border-slate-200 hover:border-blue-300 rounded-lg text-[10px] font-mono text-slate-700 transition flex items-center gap-1 shadow-2xs"
                    >
                      <Sparkles className="w-2.5 h-2.5 text-amber-500" />
                      <span className="font-bold">{c.code}</span>
                      <span className="text-slate-400">({c.label})</span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="space-y-2 text-xs text-gray-600 border-t border-gray-100 pt-3">
            <div className="flex justify-between">
              <span>Subtotal</span>
              <span>₹{rawSubtotal.toLocaleString('en-IN')}</span>
            </div>
            {couponDiscountAmount > 0 && (
              <div className="flex justify-between text-emerald-600 font-bold">
                <span className="flex items-center gap-1">
                  <Tag className="w-3 h-3" />
                  <span>Coupon Discount ({appliedCoupon})</span>
                </span>
                <span>-₹{couponDiscountAmount.toLocaleString('en-IN')}</span>
              </div>
            )}
            <div className="flex justify-between">
              <span>Delivery Charges</span>
              <span className="text-emerald-600 font-bold">FREE</span>
            </div>
            <div className="flex justify-between">
              <span>Payment Mode</span>
              <span className="font-bold text-gray-800">{paymentMethod === 'COD' ? 'Cash on Delivery' : 'Online (Razorpay)'}</span>
            </div>
            <div className="border-t border-gray-200 pt-3 flex justify-between text-base font-black text-gray-900">
              <span>Total Payable</span>
              <span className="text-blue-600">₹{totalPayable.toLocaleString('en-IN')}</span>
            </div>
          </div>

          <button
            onClick={handlePlaceOrder}
            disabled={placingOrder || !selectedAddressId}
            className="w-full py-4 bg-blue-600 hover:bg-blue-700 text-white font-black text-sm rounded-2xl shadow-lg transition duration-200 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed active:scale-98"
          >
            {placingOrder ? (
              <span>Processing Order...</span>
            ) : (
              <>
                <span>Place Order &bull; ₹{totalPayable.toLocaleString('en-IN')}</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>

          <div className="flex items-center justify-center gap-2 text-[11px] text-gray-400">
            <ShieldCheck className="w-4 h-4 text-emerald-500" />
            <span>Bank-Grade 256-Bit SSL Encrypted</span>
          </div>
        </div>
      </div>

      {/* Add Address Modal */}
      {showAddressModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-lg w-full space-y-4 shadow-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="font-bold text-lg text-gray-900">Add New Delivery Address</h3>
            <form onSubmit={handleCreateAddress} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-bold text-gray-700 block mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    placeholder="Recipient name"
                    value={newAddr.fullName}
                    onChange={(e) => setNewAddr({ ...newAddr, fullName: e.target.value })}
                    className="w-full p-2.5 border rounded-xl text-xs focus:border-blue-600 outline-none"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-gray-700 block mb-1">Phone Number</label>
                  <input
                    type="tel"
                    required
                    maxLength={10}
                    placeholder="10-digit mobile"
                    value={newAddr.phone}
                    onChange={(e) => setNewAddr({ ...newAddr, phone: e.target.value })}
                    className="w-full p-2.5 border rounded-xl text-xs focus:border-blue-600 outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="text-xs font-bold text-gray-700 block mb-1">Address Line 1</label>
                <input
                  type="text"
                  required
                  placeholder="House/Flat No, Building, Street"
                  value={newAddr.addressLine1}
                  onChange={(e) => setNewAddr({ ...newAddr, addressLine1: e.target.value })}
                  className="w-full p-2.5 border rounded-xl text-xs focus:border-blue-600 outline-none"
                />
              </div>

              <div>
                <label className="text-xs font-bold text-gray-700 block mb-1">Address Line 2 (Optional)</label>
                <input
                  type="text"
                  placeholder="Apartment, Landmark, Area"
                  value={newAddr.addressLine2}
                  onChange={(e) => setNewAddr({ ...newAddr, addressLine2: e.target.value })}
                  className="w-full p-2.5 border rounded-xl text-xs focus:border-blue-600 outline-none"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="text-xs font-bold text-gray-700 block mb-1">City</label>
                  <input
                    type="text"
                    required
                    placeholder="City"
                    value={newAddr.city}
                    onChange={(e) => setNewAddr({ ...newAddr, city: e.target.value })}
                    className="w-full p-2.5 border rounded-xl text-xs focus:border-blue-600 outline-none"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-gray-700 block mb-1">State</label>
                  <input
                    type="text"
                    required
                    placeholder="State"
                    value={newAddr.state}
                    onChange={(e) => setNewAddr({ ...newAddr, state: e.target.value })}
                    className="w-full p-2.5 border rounded-xl text-xs focus:border-blue-600 outline-none"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-gray-700 block mb-1">PIN Code</label>
                  <input
                    type="text"
                    required
                    maxLength={6}
                    placeholder="6-digit"
                    value={newAddr.postalCode}
                    onChange={(e) => setNewAddr({ ...newAddr, postalCode: e.target.value })}
                    className="w-full p-2.5 border rounded-xl text-xs focus:border-blue-600 outline-none"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-4">
                <button
                  type="button"
                  onClick={() => setShowAddressModal(false)}
                  className="px-4 py-2 border rounded-xl text-xs font-bold text-gray-700 hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-blue-600 text-white rounded-xl text-xs font-bold hover:bg-blue-700 transition"
                >
                  Save Address
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
