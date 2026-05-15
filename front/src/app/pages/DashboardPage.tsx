import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { DollarSign, ShoppingCart, CreditCard, TrendingUp, Plus, FileText } from 'lucide-react';
import { MainLayout } from '../components/MainLayout';
import { usePOS } from '../context/POSContext';
import { useAuth } from '../context/AuthContext';
import { useShift } from '../context/ShiftContext';
import { apiRequest } from '../lib/api';

interface SalesTodayApi {
  fecha: string;
  totalVentas: number;
  cantidadOperaciones: number;
  cantidadVentas: number;
  cantidadCanceladas: number;
  cantidadActivas: number;
  ventasPagadas: Array<{
    idOrden: number;
    folio: string;
    fecha: string;
    tipoOrden: string;
    metodoPago: string;
    total: number;
    estado: string;
  }>;
}

export function DashboardPage() {
  const { products, categories } = usePOS();
  const { user } = useAuth();
  const { currentShift, loadPeriodSummary, periodSummary } = useShift();
  const navigate = useNavigate();
  const [summary, setSummary] = useState<SalesTodayApi | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!user?.idUsuario) return;
      const response = await apiRequest<SalesTodayApi>(`/ventas/usuarios/${user.idUsuario}/resumen-hoy`);
      setSummary(response);
      await loadPeriodSummary();
    };
    void load();
  }, [user?.idUsuario]);

  const topProducts = [...products].slice(0, 5);
  const cashSales = currentShift?.salesCash ?? periodSummary?.totalCashSales ?? 0;
  const cardSales = currentShift?.salesCard ?? periodSummary?.totalCardSales ?? 0;
  const transferSales = currentShift?.salesTransfer ?? periodSummary?.totalTransferSales ?? 0;

  return (
    <MainLayout>
      <div className="p-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Panel Principal</h1>
          <p className="text-gray-600 mt-1">Resumen del día</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <DollarSign className="w-8 h-8" />
              <span className="text-sm opacity-90">Hoy</span>
            </div>
            <p className="text-3xl font-bold">${Number(summary?.totalVentas ?? 0).toFixed(2)}</p>
            <p className="text-sm opacity-90 mt-1">Ventas del día</p>
          </div>

          <div className="bg-gradient-to-br from-yellow-500 to-yellow-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <ShoppingCart className="w-8 h-8" />
              <span className="text-sm opacity-90">Total</span>
            </div>
            <p className="text-3xl font-bold">{summary?.cantidadOperaciones ?? 0}</p>
            <p className="text-sm opacity-90 mt-1">Operaciones registradas</p>
          </div>

            <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <CreditCard className="w-8 h-8" />
              <span className="text-sm opacity-90">Pagos</span>
            </div>
            <p className="text-lg font-bold">Efectivo: ${cashSales.toFixed(2)}</p>
            <p className="text-lg font-bold">Tarjeta: ${cardSales.toFixed(2)}</p>
            <p className="text-lg font-bold">Transfer.: ${transferSales.toFixed(2)}</p>
          </div>

          <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <TrendingUp className="w-8 h-8" />
              <span className="text-sm opacity-90">Caja</span>
            </div>
            <p className="text-3xl font-bold">${Number(currentShift?.currentCash ?? periodSummary?.currentCash ?? 0).toFixed(2)}</p>
            <p className="text-sm opacity-90 mt-1">Estado actual</p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <div className="bg-white rounded-xl p-6 shadow-lg">
            <h2 className="text-xl font-bold text-gray-800 mb-4">Catálogo activo</h2>
            <div className="space-y-3">
              {topProducts.length > 0 ? (
                topProducts.map((product, index) => (
                  <div key={product.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <div className="flex items-center gap-3">
                      <span className="flex items-center justify-center w-8 h-8 bg-orange-500 text-white rounded-full font-bold">
                        {index + 1}
                      </span>
                      <div>
                        <span className="font-medium text-gray-800 block">{product.name}</span>
                        <span className="text-xs text-gray-400">{product.category}</span>
                      </div>
                    </div>
                    <span className="text-gray-600 font-semibold">${product.price.toFixed(2)}</span>
                  </div>
                ))
              ) : (
                <p className="text-gray-500 text-center py-8">No hay productos cargados</p>
              )}
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-lg">
            <h2 className="text-xl font-bold text-gray-800 mb-4">Indicadores</h2>
            <div className="space-y-3">
              <div className="p-4 bg-gradient-to-r from-orange-50 to-yellow-50 border border-orange-200 rounded-lg">
                <h3 className="font-semibold text-orange-700">Categorías</h3>
                <p className="text-lg font-bold text-orange-600 mt-2">{categories.length}</p>
              </div>
              <div className="p-4 bg-gradient-to-r from-orange-50 to-yellow-50 border border-orange-200 rounded-lg">
                <h3 className="font-semibold text-orange-700">Ventas pagadas</h3>
                <p className="text-lg font-bold text-orange-600 mt-2">{summary?.cantidadVentas ?? 0}</p>
              </div>
              <div className="p-4 bg-gradient-to-r from-orange-50 to-yellow-50 border border-orange-200 rounded-lg">
                <h3 className="font-semibold text-orange-700">Pedidos activos</h3>
                <p className="text-lg font-bold text-orange-600 mt-2">{summary?.cantidadActivas ?? 0}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <button
            onClick={() => navigate('/pos')}
            className="bg-gradient-to-r from-orange-500 to-yellow-500 text-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-3"
          >
            <Plus className="w-6 h-6" />
            <span className="text-lg font-semibold">Nuevo Pedido</span>
          </button>

          <button
            onClick={() => navigate('/cash-register')}
            className="bg-gradient-to-r from-green-500 to-emerald-500 text-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-3"
          >
            <DollarSign className="w-6 h-6" />
            <span className="text-lg font-semibold">Caja</span>
          </button>

          <button
            onClick={() => navigate('/reports')}
            className="bg-gradient-to-r from-blue-500 to-cyan-500 text-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-3"
          >
            <FileText className="w-6 h-6" />
            <span className="text-lg font-semibold">Reportes</span>
          </button>
        </div>
      </div>
    </MainLayout>
  );
}
