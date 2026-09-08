import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { wishlistService } from '@/services/wishlistService';
import type { WishlistItem } from '@/types';

interface WishlistState {
  items: WishlistItem[];
  productIdMap: Record<number, boolean>;
  isLoading: boolean;
  error: string | null;
}

const GUEST_WISHLIST_KEY = 'shopkart_guest_wishlist';

function loadGuestWishlist(): WishlistItem[] {
  try {
    const raw = localStorage.getItem(GUEST_WISHLIST_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveGuestWishlist(items: WishlistItem[]) {
  try {
    localStorage.setItem(GUEST_WISHLIST_KEY, JSON.stringify(items));
  } catch {}
}

const initialGuestItems = loadGuestWishlist();

const initialState: WishlistState = {
  items: initialGuestItems,
  productIdMap: initialGuestItems.reduce((acc, item) => {
    acc[item.productId] = true;
    return acc;
  }, {} as Record<number, boolean>),
  isLoading: false,
  error: null,
};

export const fetchWishlist = createAsyncThunk('wishlist/fetchWishlist', async (_, { getState }) => {
  const state: any = getState();
  if (!state.auth?.isAuthenticated) {
    return loadGuestWishlist();
  }
  try {
    const res = await wishlistService.getWishlist();
    return res.items || [];
  } catch {
    return loadGuestWishlist();
  }
});

export const toggleWishlist = createAsyncThunk(
  'wishlist/toggleWishlist',
  async (product: number | any, { dispatch, getState }) => {
    const state: any = getState();
    const productId = typeof product === 'number' ? product : product.productId;

    if (!state.auth?.isAuthenticated) {
      // Guest local storage toggle
      const current = loadGuestWishlist();
      const exists = current.some((it) => it.productId === productId);
      let updated: WishlistItem[];
      if (exists) {
        updated = current.filter((it) => it.productId !== productId);
      } else {
        const newItem: WishlistItem = {
          wishlistItemId: Date.now(),
          wishlistId: 0,
          productId: productId,
          productName: typeof product === 'object' ? product.productName : `Product #${productId}`,
          productSlug: typeof product === 'object' ? (product.slug || `${productId}`) : `${productId}`,
          sku: typeof product === 'object' ? product.sku : '',
          brand: typeof product === 'object' ? product.brand : 'ShopKart',
          primaryImageUrl: typeof product === 'object' ? product.primaryImageUrl : undefined,
          unitPrice: typeof product === 'object' ? (product.price || 0) : 0,
          discountPercentage: typeof product === 'object' ? (product.discountPercentage || 0) : 0,
          effectivePrice: typeof product === 'object' ? (product.effectivePrice || product.price || 0) : 0,
          inStock: typeof product === 'object' ? (product.inStock !== false) : true,
          addedAt: new Date().toISOString(),
        };
        updated = [newItem, ...current];
      }
      saveGuestWishlist(updated);
      return { productId, inWishlist: !exists, items: updated };
    }

    const res = await wishlistService.toggleWishlist(productId);
    dispatch(fetchWishlist());
    return { productId, inWishlist: res.inWishlist };
  }
);

export const moveToCart = createAsyncThunk(
  'wishlist/moveToCart',
  async (productId: number, { dispatch, getState }) => {
    const state: any = getState();
    if (!state.auth?.isAuthenticated) {
      const current = loadGuestWishlist().filter((it) => it.productId !== productId);
      saveGuestWishlist(current);
      return;
    }
    await wishlistService.moveToCart(productId);
    dispatch(fetchWishlist());
  }
);

export const wishlistSlice = createSlice({
  name: 'wishlist',
  initialState,
  reducers: {
    clearGuestWishlist: (state) => {
      localStorage.removeItem(GUEST_WISHLIST_KEY);
      state.items = [];
      state.productIdMap = {};
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchWishlist.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(fetchWishlist.fulfilled, (state, action) => {
        state.items = action.payload;
        state.productIdMap = action.payload.reduce((acc, item) => {
          acc[item.productId] = true;
          return acc;
        }, {} as Record<number, boolean>);
        state.isLoading = false;
      })
      .addCase(fetchWishlist.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to load wishlist';
      })
      .addCase(toggleWishlist.fulfilled, (state, action) => {
        if (action.payload.items) {
          state.items = action.payload.items;
          state.productIdMap = action.payload.items.reduce((acc, item) => {
            acc[item.productId] = true;
            return acc;
          }, {} as Record<number, boolean>);
        }
      });
  },
});

export const { clearGuestWishlist } = wishlistSlice.actions;
export default wishlistSlice.reducer;
