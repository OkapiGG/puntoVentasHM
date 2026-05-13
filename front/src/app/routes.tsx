import { createBrowserRouter, Navigate } from 'react-router';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { POSPage } from './pages/POSPage';
import { CheckoutPage } from './pages/CheckoutPage';
import { TablesPage } from './pages/TablesPage';
import { ProductsPage } from './pages/ProductsPage';
import { CombosPage } from './pages/CombosPage';
import { PromotionsPage } from './pages/PromotionsPage';
import { CashRegisterPage } from './pages/CashRegisterPage';
import { ReportsPage } from './pages/ReportsPage';
import { SettingsPage } from './pages/SettingsPage';
import { ShiftsPage } from './pages/ShiftsPage';
import { useAuth } from './context/AuthContext';

function PublicRoute({ children }: { children: JSX.Element }) {
  const { user } = useAuth();
  return user ? <Navigate to="/dashboard" replace /> : children;
}

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/" replace />;
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <PublicRoute>
        <LoginPage />
      </PublicRoute>
    ),
  },
  {
    path: '/dashboard',
    element: (
      <ProtectedRoute>
        <DashboardPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/pos',
    element: (
      <ProtectedRoute>
        <POSPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/checkout',
    element: (
      <ProtectedRoute>
        <CheckoutPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/tables',
    element: (
      <ProtectedRoute>
        <TablesPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/products',
    element: (
      <ProtectedRoute>
        <ProductsPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/combos',
    element: (
      <ProtectedRoute>
        <CombosPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/promotions',
    element: (
      <ProtectedRoute>
        <PromotionsPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/cash-register',
    element: (
      <ProtectedRoute>
        <CashRegisterPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/shifts',
    element: (
      <ProtectedRoute>
        <ShiftsPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/reports',
    element: (
      <ProtectedRoute>
        <ReportsPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/settings',
    element: (
      <ProtectedRoute>
        <SettingsPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
]);
