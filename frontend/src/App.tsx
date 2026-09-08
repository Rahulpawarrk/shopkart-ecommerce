import React, { useEffect } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store, useAppDispatch } from './store';
import { checkAuth } from './store/slices/authSlice';
import { fetchCart } from './store/slices/cartSlice';
import { fetchWishlist } from './store/slices/wishlistSlice';
import { AppRoutes } from './routes/AppRoutes';

const AppInitializer: React.FC = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    // Initial bootstrap: check current session, load cart and wishlist
    dispatch(checkAuth())
      .unwrap()
      .then((user) => {
        if (user) {
          dispatch(fetchCart());
          dispatch(fetchWishlist());
        } else {
          // Still fetch cart for guest session
          dispatch(fetchCart());
        }
      })
      .catch(() => {
        dispatch(fetchCart());
      });
  }, [dispatch]);

  return <AppRoutes />;
};

export function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <AppInitializer />
      </BrowserRouter>
    </Provider>
  );
}

export default App;
