import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { wishlistService } from '@/services/wishlistService';
import type { WishlistItem } from '@/types';

interface WishlistState {
  items: WishlistItem[];
  productIdMap: Record<number, boolean>;
  isLoading: boolean;
  error: string | null;
}

const initialState: WishlistState = {
  items: [],
  productIdMap: {},
  isLoading: false,
  error: null,
};

export const fetchWishlist = createAsyncThunk('wishlist/fetchWishlist', async () => {
  const res = await wishlistService.getWishlist();
  return res.items || [];
});

export const toggleWishlist = createAsyncThunk(
  'wishlist/toggleWishlist',
  async (productId: number, { dispatch }) => {
    const res = await wishlistService.toggleWishlist(productId);
    dispatch(fetchWishlist());
    return { productId, inWishlist: res.inWishlist };
  }
);

export const moveToCart = createAsyncThunk(
  'wishlist/moveToCart',
  async (productId: number, { dispatch }) => {
    await wishlistService.moveToCart(productId);
    dispatch(fetchWishlist());
  }
);

export const wishlistSlice = createSlice({
  name: 'wishlist',
  initialState,
  reducers: {},
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
      });
  },
});

export default wishlistSlice.reducer;
