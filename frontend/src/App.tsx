import React, { useEffect } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store, useAppDispatch } from './store';
import { checkAuth } from './store/slices/authSlice';
import { fetchCart } from './store/slices/cartSlice';
import { fetchWishlist } from './store/slices/wishlistSlice';
import { AppRoutes } from './routes/AppRoutes';
import { ErrorBoundary } from './components/common/ErrorBoundary';

const AppInitializer: React.FC = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    // Initial bootstrap: check current session, load cart and wishlist
    dispatch(checkAuth())
      .unwrap()
      .then((user) => {
        if (user) {
          dispatch(fetchCart());
          if ('requestIdleCallback' in window) {
            window.requestIdleCallback(() => dispatch(fetchWishlist()));
          } else {
            setTimeout(() => dispatch(fetchWishlist()), 100);
          }
        } else {
          // Still fetch cart for guest session
          dispatch(fetchCart());
        }
      })
      .catch(() => {
        dispatch(fetchCart());
      });
  }, [dispatch]);

  return (
    <ErrorBoundary>
      <AppRoutes />
    </ErrorBoundary>
  );
};

export function App() {
  return (
    <ErrorBoundary>
      <Provider store={store}>
        <BrowserRouter>
          <AppInitializer />
        </BrowserRouter>
      </Provider>
    </ErrorBoundary>
  );
}

export default App;
