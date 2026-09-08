import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import { cartService } from '@/services/cartService';
import type { Cart } from '@/types';

interface CartState {
  cart: Cart | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: CartState = {
  cart: null,
  isLoading: false,
  error: null,
};

export const fetchCart = createAsyncThunk('cart/fetchCart', async () => {
  return await cartService.getCart();
});

export const addToCart = createAsyncThunk(
  'cart/addToCart',
  async ({ productId, quantity = 1 }: { productId: number; quantity?: number }) => {
    return await cartService.addToCart(productId, quantity);
  }
);

export const updateCartItem = createAsyncThunk(
  'cart/updateCartItem',
  async ({ cartItemId, quantity }: { cartItemId: number; quantity: number }) => {
    return await cartService.updateCartItem(cartItemId, quantity);
  }
);

export const removeFromCart = createAsyncThunk(
  'cart/removeFromCart',
  async (cartItemId: number) => {
    return await cartService.removeFromCart(cartItemId);
  }
);

export const applyCoupon = createAsyncThunk(
  'cart/applyCoupon',
  async (code: string) => {
    return await cartService.applyCoupon(code);
  }
);

export const removeCoupon = createAsyncThunk('cart/removeCoupon', async () => {
  return await cartService.removeCoupon();
});

export const clearCart = createAsyncThunk('cart/clearCart', async () => {
  return await cartService.clearCart();
});

export const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    setCart(state, action: PayloadAction<Cart | null>) {
      state.cart = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCart.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(fetchCart.fulfilled, (state, action) => {
        state.cart = action.payload;
        state.isLoading = false;
      })
      .addCase(fetchCart.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to load cart';
      })
      .addCase(addToCart.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      .addCase(updateCartItem.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      .addCase(removeFromCart.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      .addCase(applyCoupon.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      .addCase(removeCoupon.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      .addCase(clearCart.fulfilled, (state, action) => {
        state.cart = action.payload;
      });
  },
});

export const { setCart } = cartSlice.actions;
export default cartSlice.reducer;
