export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  errorCode?: string;
  errors?: string[];
  timestamp: string;
}

export interface User {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phone?: string;
  roles: string[];
  admin: boolean;
}

export interface Category {
  categoryId: number;
  parentCategoryId?: number | null;
  categoryName: string;
  slug: string;
  description?: string;
  active: boolean;
  subCategories?: Category[];
}

export interface ProductImage {
  imageId?: number;
  productId?: number;
  imageUrl: string;
  altText?: string;
  displayOrder?: number;
  primary?: boolean;
}

export interface Product {
  productId: number;
  categoryId: number;
  categoryName?: string;
  sku: string;
  productName: string;
  slug: string;
  description?: string;
  brand?: string;
  price: number;
  discountPercentage: number;
  effectivePrice: number;
  taxPercentage: number;
  stockQuantity: number;
  inStock: boolean;
  status: string;
  primaryImageUrl: string;
  ratingAverage: number;
  ratingCount: number;
  images?: ProductImage[];
}

export interface CatalogPageResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface HomeShowcase {
  categories: Category[];
  featuredProducts: Product[];
  dealsOfTheDay: Product[];
  newArrivals: Product[];
  bestsellers: Product[];
}

export interface CartItem {
  cartItemId: number;
  productId: number;
  productName: string;
  productSlug: string;
  brand?: string;
  primaryImageUrl: string;
  quantity: number;
  unitPrice: number;
  discountPercentage: number;
  effectivePrice: number;
  lineTotal: number;
  availableStock: number;
  inStock: boolean;
}

export interface Cart {
  cartId: number;
  userId?: number;
  totalQuantity: number;
  subtotal: number;
  totalDiscount: number;
  couponDiscount: number;
  appliedCouponCode?: string | null;
  taxAmount: number;
  shippingAmount: number;
  finalTotal: number;
  empty: boolean;
  items: CartItem[];
}

export interface WishlistItem {
  wishlistItemId: number;
  productId: number;
  productName: string;
  productSlug: string;
  brand?: string;
  primaryImageUrl: string;
  unitPrice: number;
  discountPercentage: number;
  effectivePrice: number;
  inStock: boolean;
}

export interface Wishlist {
  wishlistId: number;
  userId: number;
  items: WishlistItem[];
}

export interface Address {
  addressId: number;
  userId?: number;
  addressType: string;
  fullName: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  defaultAddress: boolean;
}

export interface OrderItem {
  orderItemId: number;
  orderId: number;
  productId: number;
  productName: string;
  sku: string;
  primaryImageUrl?: string;
  quantity: number;
  unitPrice: number;
  discountPercentage: number;
  effectivePrice: number;
  lineTotal: number;
}

export interface Order {
  orderId: number;
  orderNumber: string;
  userId: number;
  orderStatus: string;
  paymentStatus: string;
  paymentMethod: string;
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  shippingAmount: number;
  totalAmount: number;
  shippingFullName?: string;
  shippingPhone?: string;
  shippingAddressLine1?: string;
  shippingAddressLine2?: string;
  shippingCity?: string;
  shippingState?: string;
  shippingPostalCode?: string;
  shippingCountry?: string;
  formattedShippingAddress?: string;
  courierPartner?: string;
  trackingNumber?: string;
  trackingUrl?: string;
  canCancel: boolean;
  canReturn: boolean;
  createdAt: string;
  updatedAt?: string;
  items: OrderItem[];
}

export interface TrackingEvent {
  timestamp: string;
  status: string;
  location: string;
  description: string;
}

export interface OrderTracking {
  orderNumber: string;
  status: string;
  courierPartner?: string;
  trackingNumber?: string;
  trackingUrl?: string;
  estimatedDelivery?: string;
  events: TrackingEvent[];
}

export interface RatingSummary {
  productId: number;
  averageRating: number;
  totalReviews: number;
  fiveStarCount: number;
  fourStarCount: number;
  threeStarCount: number;
  twoStarCount: number;
  oneStarCount: number;
}

export interface Review {
  reviewId: number;
  productId: number;
  userId: number;
  authorName?: string;
  rating: number;
  title: string;
  comment: string;
  imageUrl?: string;
  imageUrls?: string[];
  verifiedPurchase: boolean;
  formattedDate?: string;
  createdAt: string;
}

export interface Coupon {
  couponId: number;
  code: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  minSpend?: number;
  maxDiscount?: number;
  usageLimit?: number;
  startDate?: string;
  endDate?: string;
  active: boolean;
}

export interface InventoryItem {
  inventoryId: number;
  productId: number;
  productName: string;
  sku: string;
  quantityAvailable: number;
  quantityReserved: number;
  lowStockThreshold: number;
  isLowStock: boolean;
  isOutOfStock: boolean;
}

export interface AuditLog {
  logId: number;
  userId?: number;
  action: string;
  entityName: string;
  entityId?: number;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  createdAt: string;
}

export interface AdminDashboardStats {
  totalRevenue: number;
  totalOrders: number;
  totalProducts: number;
  totalCustomers: number;
  pendingOrdersCount: number;
  lowStockCount: number;
  recentOrders: Order[];
  topSellingProducts: any[];
}
