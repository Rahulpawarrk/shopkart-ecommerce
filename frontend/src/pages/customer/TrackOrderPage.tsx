import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { orderService } from '@/services/orderService';
import type { Order, OrderTracking } from '@/types';
import {
  Truck,
  ArrowLeft,
  Package,
  CheckCircle,
  Clock,
  MapPin,
  AlertCircle,
  Phone,
} from 'lucide-react';

export const TrackOrderPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [tracking, setTracking] = useState<OrderTracking | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.all([
      orderService.getOrderById(Number(id)),
      orderService.getLiveTracking(Number(id)).catch(() => null),
    ])
      .then(([orderData, trackingData]) => {
        setOrder(orderData);
        setTracking(trackingData);
        setError(null);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load tracking data');
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <div className="inline-block w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p className="mt-3 text-slate-500">Tracking your shipment...</p>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <AlertCircle className="w-16 h-16 text-rose-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-slate-900 mb-2">Tracking Unavailable</h2>
        <p className="text-slate-600 mb-6">{error || 'Order tracking not found.'}</p>
        <Link
          to="/orders"
          className="inline-flex items-center gap-2 px-6 py-3 bg-primary text-white font-medium rounded-lg hover:bg-primary/90 transition shadow"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Orders
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      <Link
        to={`/orders/${order.orderId}`}
        className="inline-flex items-center text-sm font-medium text-slate-600 hover:text-primary transition"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" />
        Back to Order Details
      </Link>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 sm:p-8 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b border-slate-100 pb-6 gap-4">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-primary">Live Tracking</span>
            <h1 className="text-2xl font-black text-slate-900 mt-1">Order #{order.orderNumber}</h1>
            <p className="text-xs text-slate-500 mt-1">
              Placed on {new Date(order.createdAt).toLocaleDateString('en-IN', { dateStyle: 'long' })}
            </p>
          </div>
          <div className="sm:text-right">
            <span className="text-xs text-slate-400 block">Current Status</span>
            <span className="inline-block mt-1 px-3 py-1 bg-blue-50 text-blue-700 text-xs font-black uppercase rounded-full border border-blue-200">
              {order.orderStatus.replace(/_/g, ' ')}
            </span>
          </div>
        </div>

        {/* Courier Info Header */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 py-6 border-b border-slate-100 text-sm">
          <div>
            <span className="text-xs text-slate-400 block">Courier Partner</span>
            <strong className="text-slate-800">{order.courierPartner || 'ShopKart Express'}</strong>
          </div>
          <div>
            <span className="text-xs text-slate-400 block">Tracking Number (AWB)</span>
            <strong className="text-slate-800">{order.trackingNumber || 'Awaiting assignment'}</strong>
          </div>
          <div>
            <span className="text-xs text-slate-400 block">Destination</span>
            <strong className="text-slate-800">
              {order.shippingCity ? `${order.shippingCity}, ${order.shippingState}` : 'N/A'}
            </strong>
          </div>
        </div>

        {/* Milestone Stepper */}
        <div className="py-8">
          <h2 className="text-base font-bold text-slate-900 mb-6">Shipment Milestones</h2>
          {tracking?.events && tracking.events.length > 0 ? (
            <div className="relative pl-8 space-y-8 border-l-2 border-slate-200 ml-4">
              {tracking.events.map((ev, idx) => (
                <div key={idx} className="relative">
                  <div className="absolute -left-[37px] top-0 w-6 h-6 rounded-full flex items-center justify-center text-white bg-emerald-500">
                    <CheckCircle className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-slate-900">{ev.status}</h3>
                    <p className="text-xs text-slate-600 mt-1">{ev.description}</p>
                    {ev.location && <p className="text-[11px] text-slate-400 mt-0.5">Location: {ev.location}</p>}
                    {ev.timestamp && (
                      <p className="text-[11px] text-slate-400 mt-1">
                        {new Date(ev.timestamp).toLocaleString('en-IN', {
                          day: 'numeric',
                          month: 'short',
                          year: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 bg-slate-50 rounded-xl text-center text-slate-500 text-sm">
              <Truck className="w-8 h-8 mx-auto mb-2 text-slate-400" />
              Tracking information will be updated once your order is dispatched by our fulfillment center.
            </div>
          )}
        </div>

        {/* Delivery Address & Contact info */}
        {order.shippingFullName && (
          <div className="bg-slate-50 rounded-xl p-5 border border-slate-200 flex flex-col sm:flex-row justify-between gap-4 text-xs">
            <div>
              <span className="font-bold text-slate-700 block mb-1">Delivering to:</span>
              <p className="text-slate-800 font-semibold">{order.shippingFullName}</p>
              <p className="text-slate-600">{order.shippingAddressLine1}</p>
              {order.shippingAddressLine2 && <p className="text-slate-600">{order.shippingAddressLine2}</p>}
              <p className="text-slate-600">
                {order.shippingCity}, {order.shippingState} - {order.shippingPostalCode}
              </p>
            </div>
            {order.shippingPhone && (
              <div className="sm:text-right">
                <span className="font-bold text-slate-700 block mb-1">Contact Details:</span>
                <p className="text-slate-800 font-medium flex sm:justify-end items-center gap-1">
                  <Phone className="w-3.5 h-3.5 text-slate-400" />
                  {order.shippingPhone}
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
