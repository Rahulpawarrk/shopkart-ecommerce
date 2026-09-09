import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { ShieldCheck, Truck, RotateCcw, FileText, ArrowLeft, Mail, Phone, MapPin } from 'lucide-react';

export const PolicyPage: React.FC = () => {
  const location = useLocation();
  const path = location.pathname.toLowerCase();

  let title = 'ShopKart Information';
  let icon = <FileText className="w-8 h-8 text-[#2874F0]" />;
  let content = null;

  if (path.includes('return')) {
    title = 'Cancellation & Return Policy';
    icon = <RotateCcw className="w-8 h-8 text-blue-600" />;
    content = (
      <div className="space-y-4 text-sm text-slate-700 leading-relaxed">
        <p>At ShopKart, we want you to be completely satisfied with every purchase. If you receive a damaged, defective, or incorrect item, you can request an easy return or replacement within <strong>7 days</strong> of delivery.</p>
        <h3 className="text-base font-bold text-slate-900 mt-4">Return Eligibility</h3>
        <ul className="list-disc pl-5 space-y-1.5">
          <li>Items must be in original condition with intact packaging, tags, and accessories.</li>
          <li>Electronics and mobiles must be unboxed and reported within 7 days for authorized technician inspection.</li>
          <li>Personal care, hygiene products, and innerwear are non-returnable once opened.</li>
        </ul>
        <h3 className="text-base font-bold text-slate-900 mt-4">Refund Timeline</h3>
        <p>Refunds are initiated immediately upon courier pickup and inspection. UPI &amp; card refunds reflect within 3-5 banking business days.</p>
      </div>
    );
  } else if (path.includes('shipping')) {
    title = 'Shipping & Delivery Policy';
    icon = <Truck className="w-8 h-8 text-emerald-600" />;
    content = (
      <div className="space-y-4 text-sm text-slate-700 leading-relaxed">
        <p>We deliver to 19,000+ pin codes across India with top logistics partners including Blue Dart, Delhivery, and India Post.</p>
        <h3 className="text-base font-bold text-slate-900 mt-4">Delivery Speed &amp; Charges</h3>
        <ul className="list-disc pl-5 space-y-1.5">
          <li><strong>Free Delivery:</strong> All orders above ₹499 qualify for Free Standard Delivery across India.</li>
          <li><strong>Standard Shipping:</strong> ₹49 flat fee applies for orders below ₹499.</li>
          <li><strong>Estimated Transit Time:</strong> 2-4 business days for metro cities; 3-6 business days for rest of India.</li>
        </ul>
        <p>Real-time tracking links and SMS updates are sent as soon as your parcel is dispatched from our fulfillment hub.</p>
      </div>
    );
  } else if (path.includes('privacy') || path.includes('security')) {
    title = 'Privacy & Security Policy';
    icon = <ShieldCheck className="w-8 h-8 text-blue-600" />;
    content = (
      <div className="space-y-4 text-sm text-slate-700 leading-relaxed">
        <p>ShopKart is committed to protecting your privacy. All transactions and personal data are safeguarded with bank-grade 256-bit SSL encryption and PCI-DSS compliance.</p>
        <h3 className="text-base font-bold text-slate-900 mt-4">Data Protection</h3>
        <ul className="list-disc pl-5 space-y-1.5">
          <li>We never sell or rent your personal contact information to third-party advertisers.</li>
          <li>Payment information is processed securely through RBI-licensed payment gateways (Razorpay). ShopKart does not store raw credit/debit card CVV numbers.</li>
          <li>You can request full data deletion or address removal anytime via your profile settings.</li>
        </ul>
      </div>
    );
  } else if (path.includes('terms')) {
    title = 'Terms of Use & Payments';
    icon = <FileText className="w-8 h-8 text-slate-700" />;
    content = (
      <div className="space-y-4 text-sm text-slate-700 leading-relaxed">
        <p>Welcome to ShopKart. By using our website and services, you agree to comply with our Terms of Use and applicable Indian e-commerce consumer protection laws.</p>
        <h3 className="text-base font-bold text-slate-900 mt-4">Accepted Payment Methods</h3>
        <p>We support UPI (GPay, PhonePe, Paytm, BHIM), Debit &amp; Credit Cards (Visa, Mastercard, RuPay), NetBanking from 50+ banks, and Cash on Delivery (COD) on select pin codes.</p>
        <h3 className="text-base font-bold text-slate-900 mt-4">Pricing &amp; Availability</h3>
        <p>All prices listed on ShopKart are inclusive of GST. In the rare event of a technical pricing error, we reserve the right to cancel affected unfulfilled orders with a prompt 100% refund.</p>
      </div>
    );
  } else if (path.includes('contact')) {
    title = 'Contact Us & FAQ';
    icon = <Mail className="w-8 h-8 text-amber-500" />;
    content = (
      <div className="space-y-4 text-sm text-slate-700 leading-relaxed">
        <p>Need assistance with an order, return, or account? Our support team is available 24x7 to help you.</p>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 my-6">
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200">
            <Phone className="w-5 h-5 text-blue-600 mb-2" />
            <h4 className="font-bold text-slate-900 text-sm">Toll-Free Helpline</h4>
            <p className="text-xs text-slate-600 mt-1">1800-SHOPKART (9am - 9pm IST)</p>
          </div>
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200">
            <Mail className="w-5 h-5 text-emerald-600 mb-2" />
            <h4 className="font-bold text-slate-900 text-sm">Email Support</h4>
            <p className="text-xs text-slate-600 mt-1">support@shopkart11.in</p>
          </div>
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200">
            <MapPin className="w-5 h-5 text-purple-600 mb-2" />
            <h4 className="font-bold text-slate-900 text-sm">Corporate Office</h4>
            <p className="text-xs text-slate-600 mt-1">ShopKart Towers, Bengaluru, India</p>
          </div>
        </div>
      </div>
    );
  } else {
    title = 'About ShopKart';
    icon = <ShieldCheck className="w-8 h-8 text-blue-600" />;
    content = (
      <div className="space-y-4 text-sm text-slate-700 leading-relaxed">
        <p>ShopKart is India's trusted online shopping destination, delivering high-grade consumer electronics, trendy apparel, home essentials, and lifestyle products at honest prices.</p>
        <h3 className="text-base font-bold text-slate-900 mt-4">The ShopKart Promise</h3>
        <ul className="list-disc pl-5 space-y-1.5">
          <li><strong>100% Genuine Products:</strong> Sourced directly from verified brands and authorized distributors.</li>
          <li><strong>Best Deals, Always:</strong> Transparent daily flash sales and price drop guarantees.</li>
          <li><strong>Nationwide Fast Delivery:</strong> Seamless courier fulfillment covering 19,000+ pin codes.</li>
        </ul>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10 sm:py-14">
      <Link to="/" className="inline-flex items-center gap-1.5 text-xs font-bold text-[#2874F0] hover:underline mb-6">
        <ArrowLeft className="w-4 h-4" />
        Back to Home
      </Link>

      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-10 space-y-6">
        <div className="flex items-center gap-4 border-b border-slate-100 pb-6">
          <div className="p-3 bg-slate-50 rounded-2xl border border-slate-100">{icon}</div>
          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">{title}</h1>
            <p className="text-xs text-slate-500 mt-0.5">ShopKart E-Commerce Customer Support &amp; Legal Center</p>
          </div>
        </div>

        {content}
      </div>
    </div>
  );
};
