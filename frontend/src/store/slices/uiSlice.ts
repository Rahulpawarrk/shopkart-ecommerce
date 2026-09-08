import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface ToastMessage {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface UiState {
  cartDrawerOpen: boolean;
  mobileMenuOpen: boolean;
  searchDrawerOpen: boolean;
  toasts: ToastMessage[];
}

const initialState: UiState = {
  cartDrawerOpen: false,
  mobileMenuOpen: false,
  searchDrawerOpen: false,
  toasts: [],
};

export const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    toggleCartDrawer(state, action: PayloadAction<boolean | undefined>) {
      state.cartDrawerOpen = action.payload !== undefined ? action.payload : !state.cartDrawerOpen;
    },
    toggleMobileMenu(state, action: PayloadAction<boolean | undefined>) {
      state.mobileMenuOpen = action.payload !== undefined ? action.payload : !state.mobileMenuOpen;
    },
    toggleSearchDrawer(state, action: PayloadAction<boolean | undefined>) {
      state.searchDrawerOpen = action.payload !== undefined ? action.payload : !state.searchDrawerOpen;
    },
    showToast(state, action: PayloadAction<{ message: string; type?: 'success' | 'error' | 'info' }>) {
      const toast: ToastMessage = {
        id: Date.now(),
        message: action.payload.message,
        type: action.payload.type || 'info',
      };
      state.toasts.push(toast);
    },
    dismissToast(state, action: PayloadAction<number>) {
      state.toasts = state.toasts.filter((t) => t.id !== action.payload);
    },
  },
});

export const { toggleCartDrawer, toggleMobileMenu, toggleSearchDrawer, showToast, dismissToast } =
  uiSlice.actions;
export default uiSlice.reducer;
