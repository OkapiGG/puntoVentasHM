import { ReactNode } from 'react';
import { useNavigate, useLocation } from 'react-router';
import { useAuth } from '../context/AuthContext';
import { useSettings } from '../context/SettingsContext';
import { useShift } from '../context/ShiftContext';
import {
  LayoutDashboard,
  ShoppingCart,
  Table2,
  Package,
  Layers,
  Tag,
  DollarSign,
  ClipboardList,
  FileText,
  History,
  Settings,
  LogOut,
  Clock,
  ChefHat,
} from 'lucide-react';

interface MainLayoutProps {
  children: ReactNode;
}

export function MainLayout({ children }: MainLayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const { settings } = useSettings();
  const { currentShift } = useShift();

  const menuItems = [
    { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard', roles: ['admin', 'manager', 'cashier', 'delivery'] },
    { icon: ShoppingCart, label: 'Nuevo Pedido', path: '/pos', roles: ['admin', 'manager', 'cashier'] },
    { icon: Table2, label: 'Mesas', path: '/tables', roles: ['admin', 'manager', 'cashier'] },
    { icon: ClipboardList, label: 'Órdenes', path: '/orders', roles: ['admin', 'manager', 'cashier'] },
    { icon: ChefHat, label: 'Cocina', path: '/kitchen', roles: ['admin', 'manager', 'cook'] },
    { icon: Package, label: 'Productos', path: '/products', roles: ['admin', 'manager'] },
    { icon: Layers, label: 'Combos', path: '/combos', roles: ['admin', 'manager'] },
    { icon: Tag, label: 'Promociones', path: '/promotions', roles: ['admin', 'manager'] },
    { icon: Clock, label: 'Turnos', path: '/shifts', roles: ['admin', 'manager', 'cashier', 'delivery', 'cook'] },
    { icon: DollarSign, label: 'Caja', path: '/cash-register', roles: ['admin', 'manager', 'cashier'] },
    { icon: History, label: 'Historial', path: '/sales-history', roles: ['admin', 'manager', 'cashier'] },
    { icon: FileText, label: 'Reportes', path: '/reports', roles: ['admin', 'manager'] },
    { icon: Settings, label: 'Configuración', path: '/settings', roles: ['admin', 'manager'] },
  ].filter((item) => !item.roles || (user?.role && item.roles.includes(user.role)));

  const handleLogout = () => {
    if (currentShift?.status === 'open') {
      const confirmar = window.confirm(
        'Tu turno sigue abierto. Podrás continuar cerrándolo al volver a iniciar sesión. ¿Cerrar sesión de todos modos?'
      );
      if (!confirmar) return;
    }
    logout();
    navigate('/');
  };

  return (
    <div className="flex h-screen bg-gray-100">
      <aside className="w-64 bg-gradient-to-b from-orange-500 to-orange-600 text-white flex flex-col">
        <div className="p-6 border-b border-orange-400">
          <h1 className="text-2xl font-bold">{settings.restaurantName}</h1>
          <p className="text-sm text-orange-100 mt-1">
            {user?.username} ({user?.role})
          </p>
        </div>

        <nav className="flex-1 py-4 overflow-y-auto">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`w-full flex items-center gap-3 px-6 py-3 transition-colors ${
                  isActive
                    ? 'bg-white text-orange-600 font-semibold'
                    : 'text-white hover:bg-orange-400'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        <button
          onClick={handleLogout}
          className="flex items-center gap-3 px-6 py-4 text-white hover:bg-orange-400 transition-colors border-t border-orange-400"
        >
          <LogOut className="w-5 h-5" />
          <span>Cerrar Sesión</span>
        </button>
      </aside>

      <main className="flex-1 overflow-y-auto">{children}</main>
    </div>
  );
}
