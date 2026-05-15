import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { MainLayout } from '../components/MainLayout';
import { useAuth } from '../context/AuthContext';
import { useShift } from '../context/ShiftContext';
import { apiRequest } from '../lib/api';
import { ChefHat, CheckCircle2, Clock, Lock, RefreshCw, Utensils } from 'lucide-react';

interface KitchenItemApi {
  nombre: string;
  cantidad: number;
  modificadores: string[];
}

interface KitchenOrderApi {
  idOrden: number;
  folio: string;
  fecha: string;
  tipoOrden: string;
  cantidadItems: number;
  estado: string;
  numeroMesa?: number | null;
  nombreCliente?: string | null;
  items: KitchenItemApi[];
}

const REFRESH_INTERVAL_MS = 10_000;

export function KitchenPage() {
  const { user } = useAuth();
  const { currentShift } = useShift();
  const navigate = useNavigate();
  const [orders, setOrders] = useState<KitchenOrderApi[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [actionId, setActionId] = useState<number | null>(null);

  const canUseKitchen = user?.role === 'admin' || user?.role === 'manager' || user?.role === 'cook';
  const requiresShift = user?.role === 'cook';
  const shiftMissing = requiresShift && !currentShift;

  const loadOrders = async () => {
    if (!user?.idNegocio || !canUseKitchen || shiftMissing) return;
    setLoading(true);
    setError('');
    try {
      const response = await apiRequest<KitchenOrderApi[]>(`/ventas/negocios/${user.idNegocio}/cocina`);
      setOrders(response);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo cargar la cola de cocina');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOrders();
    if (!canUseKitchen || shiftMissing) return;
    const id = setInterval(() => void loadOrders(), REFRESH_INTERVAL_MS);
    return () => clearInterval(id);
  }, [user?.idNegocio, canUseKitchen, shiftMissing]);

  const changeStatus = async (idOrden: number, estado: string) => {
    setActionId(idOrden);
    setError('');
    try {
      await apiRequest(`/ventas/ordenes/${idOrden}/estado`, {
        method: 'POST',
        body: JSON.stringify({ estado }),
      });
      await loadOrders();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo actualizar la orden');
    } finally {
      setActionId(null);
    }
  };

  if (!canUseKitchen) {
    return (
      <MainLayout>
        <div className="flex h-full items-center justify-center">
          <div className="text-center">
            <Lock className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500 font-medium">Sin acceso a esta sección.</p>
          </div>
        </div>
      </MainLayout>
    );
  }

  if (shiftMissing) {
    return (
      <MainLayout>
        <div className="flex h-full items-center justify-center">
          <div className="text-center max-w-sm">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-5">
              <Lock className="w-9 h-9 text-gray-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Sin turno activo</h2>
            <p className="text-gray-500 mb-6">
              Debes abrir un turno antes de aceptar órdenes en cocina.
            </p>
            <button
              onClick={() => navigate('/shifts')}
              className="bg-gradient-to-r from-orange-500 to-orange-600 text-white px-8 py-3 rounded-xl font-bold hover:from-orange-600 hover:to-orange-700 transition-all shadow-lg"
            >
              Ir a Turnos
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  const recibidas = orders.filter((order) => order.estado === 'POR_ACEPTAR');
  const preparando = orders.filter((order) => order.estado === 'PREPARANDO');

  const renderCard = (order: KitchenOrderApi) => {
    const isRecibida = order.estado === 'POR_ACEPTAR';
    const minutos = Math.max(0, Math.floor((Date.now() - new Date(order.fecha).getTime()) / 60000));
    const accent = isRecibida ? 'border-amber-300 bg-amber-50' : 'border-blue-300 bg-blue-50';
    const chip = isRecibida ? 'bg-amber-200 text-amber-900' : 'bg-blue-200 text-blue-900';

    return (
      <div key={order.idOrden} className={`rounded-2xl border-2 ${accent} p-5 shadow-sm`}>
        <div className="flex items-start justify-between mb-3">
          <div>
            <p className="font-bold text-gray-900 text-lg">{order.folio}</p>
            <p className="text-xs text-gray-600">
              {order.tipoOrden}
              {order.numeroMesa != null ? ` · Mesa ${order.numeroMesa}` : ''}
              {order.nombreCliente ? ` · ${order.nombreCliente}` : ''}
            </p>
          </div>
          <span className={`inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-semibold ${chip}`}>
            <Clock className="w-3 h-3" />
            {minutos} min
          </span>
        </div>

        <div className="space-y-2 mb-4">
          {order.items.map((item, index) => (
            <div key={`${order.idOrden}-${index}`} className="bg-white rounded-lg px-3 py-2 border border-gray-200">
              <div className="flex items-center justify-between">
                <span className="font-semibold text-gray-800">{item.nombre}</span>
                <span className="text-orange-600 font-bold">x{item.cantidad}</span>
              </div>
              {item.modificadores.length > 0 && (
                <div className="mt-1 pl-2 text-xs text-gray-600">
                  {item.modificadores.map((mod, modIndex) => (
                    <div key={modIndex}>+ {mod}</div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>

        {isRecibida ? (
          <button
            onClick={() => void changeStatus(order.idOrden, 'PREPARANDO')}
            disabled={actionId === order.idOrden}
            className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-amber-500 to-orange-500 text-white py-3 rounded-xl font-semibold hover:from-amber-600 hover:to-orange-600 transition-all shadow disabled:opacity-50"
          >
            <Utensils className="w-5 h-5" />
            {actionId === order.idOrden ? 'Procesando...' : 'Aceptar y preparar'}
          </button>
        ) : (
          <button
            onClick={() => void changeStatus(order.idOrden, 'LISTO')}
            disabled={actionId === order.idOrden}
            className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-green-500 to-emerald-500 text-white py-3 rounded-xl font-semibold hover:from-green-600 hover:to-emerald-600 transition-all shadow disabled:opacity-50"
          >
            <CheckCircle2 className="w-5 h-5" />
            {actionId === order.idOrden ? 'Procesando...' : 'Marcar como listo'}
          </button>
        )}
      </div>
    );
  };

  return (
    <MainLayout>
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <div className="flex items-center gap-3">
            <ChefHat className="w-9 h-9 text-orange-500" />
            <div>
              <h1 className="text-3xl font-bold text-gray-800">Cocina</h1>
              <p className="text-gray-600 mt-1">Acepta y prepara las órdenes en cola</p>
            </div>
          </div>
          <button
            onClick={() => void loadOrders()}
            className="flex items-center gap-2 bg-white border border-gray-200 px-4 py-3 rounded-lg font-semibold text-gray-700 hover:bg-gray-50 transition-all shadow-sm"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            Actualizar
          </button>
        </div>

        {error && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-amber-700">Por aceptar</h2>
              <span className="inline-flex rounded-full bg-amber-100 px-3 py-1 text-sm font-semibold text-amber-700">
                {recibidas.length}
              </span>
            </div>
            <div className="space-y-4">
              {recibidas.length === 0 ? (
                <div className="py-10 text-center text-gray-400 bg-white rounded-2xl border border-dashed border-gray-200">
                  Nada por aceptar.
                </div>
              ) : (
                recibidas.map(renderCard)
              )}
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-blue-700">En preparación</h2>
              <span className="inline-flex rounded-full bg-blue-100 px-3 py-1 text-sm font-semibold text-blue-700">
                {preparando.length}
              </span>
            </div>
            <div className="space-y-4">
              {preparando.length === 0 ? (
                <div className="py-10 text-center text-gray-400 bg-white rounded-2xl border border-dashed border-gray-200">
                  Nada en preparación.
                </div>
              ) : (
                preparando.map(renderCard)
              )}
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
