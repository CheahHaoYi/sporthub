import { BrowserRouter, Link, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { AppLayout } from './layouts/AppLayout';
import { AdminGuard, CustomerGuard } from './routes/Guards';
import {
  Account,
  AdminPage,
  Bookings,
  CartPage,
  Checkout,
  Confirmation,
  Facilities,
  FacilityDetail,
  Home,
  Login,
  Orders,
  ProductDetail,
  Products,
  Register,
} from './pages/Pages';
import './styles.css';

// Vite sets BASE_URL to /sporthub/ for GitHub Pages builds and / locally.
// React Router needs that prefix so every client-side URL stays inside the
// project site instead of taking over the account's root portfolio site.
const routerBasename = import.meta.env.BASE_URL.replace(/\/+$/, '') || '/';

export default function App() {
  return (
    <BrowserRouter basename={routerBasename}>
      <AuthProvider>
        <Routes>
          <Route element={<AppLayout />}>
            <Route index element={<Home />} />
            <Route path="facilities" element={<Facilities />} />
            <Route path="facilities/:id" element={<FacilityDetail />} />
            <Route path="products" element={<Products />} />
            <Route path="products/:id" element={<ProductDetail />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
            <Route path="cart" element={<CartPage />} />
            <Route element={<CustomerGuard />}>
              <Route path="checkout" element={<Checkout />} />
              <Route path="order-confirmation" element={<Confirmation />} />
              <Route path="bookings" element={<Bookings />} />
              <Route path="orders" element={<Orders />} />
              <Route path="account" element={<Account />} />
            </Route>
            <Route path="admin" element={<AdminGuard />}>
              <Route index element={<AdminPage />} />
              <Route path=":section" element={<AdminRoute />} />
            </Route>
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

function AdminRoute() {
  const section = window.location.pathname.split('/').pop() || '';
  return <AdminPage section={section} />;
}

function NotFound() {
  return (
    <div className="container page not-found">
      <span className="eyebrow">404 · OUT OF BOUNDS</span>
      <h1>That page missed the court.</h1>
      <Link to="/" className="button button-dark">Back to SportHub</Link>
    </div>
  );
}
