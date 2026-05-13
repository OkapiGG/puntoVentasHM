import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import {
  TrendingUp,
  ShoppingBag,
  DollarSign,
  Clock,
  Lock,
  AlertCircle,
} from 'lucide-react';
import { MainLayout } from '../components/MainLayout';
import { useAuth } from '../context/AuthContext';
import { useShift } from '../context/ShiftContext';
import { apiRequest } from '../lib/api';

type Period = 'DIARIO' | 'SEMANAL' | 'MENSUAL';

interface UserSummaryApi {
  idUsuario: number;
  nombreUsuario: string;
  sesiones: number;
  cierres: number;
  ventasCobradas: number;
  cancelaciones: number;
  retiros: number;
  totalVentas: number;
  efectivoEnCaja: number;
  retirosSeguridad: number;
  saldoEsperado: number;
  diferenciaAcumulada: number;
}

interface AuditItemApi {
  tipoEvento: string;
  fecha: string;
  idUsuario: number;
  nombreUsuario: string;
  referenciaId: number;
  referencia: string;
  monto: number;
  motivo: string;
}

const periodLabels: Record<Period, string> = {
  DIARIO: 'Hoy',
  SEMANAL: 'Semana',
  MENSUAL: 'Mes',
};

export function ReportsPage() {
  const { user } = useAuth();
  const { loadPeriodSummary, periodSummary, shifts } = useShift();
  const navigate = useNavigate();
  const [period, setPeriod] = useState<Period>('DIARIO');
  const [userSummaries, setUserSummaries] = useState<UserSummaryApi[]>([]);
  const [audit, setAudit] = useState<AuditItemApi[]>([]);

  if (user?.role !== 'admin' && user?.role !== 'manager') {
    return (
      <MainLayout>
        <div className="flex h-full items-center justify-center">
          <div className="text-center">
            <Lock className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500 font-medium">Sin acceso a esta sección.</p>
            <button onClick={() => navigate('/dashboard')} className="mt-4 text-orange-500 underline text-sm">
              Volver al Dashboard
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  useEffect(() => {
    const load = async () => {
      if (!user?.idNegocio) return;
      await loadPeriodSummary(period);
      const [users, bitacora] = await Promise.all([
        apiRequest<UserSummaryApi[]>(`/caja/negocios/${user.idNegocio}/resumen-usuarios?periodo=${period}`),
        apiRequest<AuditItemApi[]>(`/caja/negocios/${user.idNegocio}/bitacora?periodo=${period}`),
      ]);
      setUserSummaries(users);
      setAudit(bitacora);
    };
    void load();
  }, [period, user?.idNegocio]);

  return (
    <MainLayout>
      <div className="p-8 max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Reportes</h1>
          <p className="text-gray-500 mt-1">Indicadores operativos y cierre de caja</p>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 mb-6">
          <div className="flex flex-wrap gap-2">
            {(Object.keys(periodLabels) as Period[]).map((key) => (
              <button
                key={key}
                onClick={() => setPeriod(key)}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                  period === key ? 'bg-orange-500 text-white shadow' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {periodLabels[key]}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-2xl p-5 text-white shadow">
            <TrendingUp className="w-6 h-6 mb-3 opacity-80" />
            <p className="text-2xl font-bold">${Number(periodSummary?.totalSales ?? 0).toFixed(2)}</p>
            <p className="text-orange-100 text-sm mt-1">Total ventas</p>
          </div>
          <div className="bg-gradient-to-br from-amber-400 to-amber-500 rounded-2xl p-5 text-white shadow">
            <ShoppingBag className="w-6 h-6 mb-3 opacity-80" />
            <p className="text-2xl font-bold">{periodSummary?.salesCount ?? 0}</p>
            <p className="text-amber-100 text-sm mt-1">Ventas cobradas</p>
          </div>
          <div className="bg-gradient-to-br from-sky-500 to-sky-600 rounded-2xl p-5 text-white shadow">
            <DollarSign className="w-6 h-6 mb-3 opacity-80" />
            <p className="text-2xl font-bold">${Number(periodSummary?.currentCash ?? 0).toFixed(2)}</p>
            <p className="text-sky-100 text-sm mt-1">Efectivo en caja</p>
          </div>
          <div className="bg-gradient-to-br from-emerald-500 to-emerald-600 rounded-2xl p-5 text-white shadow">
            <Clock className="w-6 h-6 mb-3 opacity-80" />
            <p className="text-2xl font-bold">{shifts.length}</p>
            <p className="text-emerald-100 text-sm mt-1">Sesiones del usuario</p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100">
              <h2 className="font-bold text-gray-800">Resumen por usuario</h2>
            </div>
            {userSummaries.length === 0 ? (
              <div className="py-16 text-center text-gray-400">
                <AlertCircle className="w-10 h-10 mx-auto mb-3 opacity-30" />
                <p>Sin movimientos en este período</p>
              </div>
            ) : (
              <div className="divide-y divide-gray-50">
                {userSummaries.map((row) => (
                  <div key={row.idUsuario} className="px-6 py-4 flex items-center justify-between gap-4">
                    <div>
                      <p className="font-semibold text-gray-800">{row.nombreUsuario}</p>
                      <p className="text-xs text-gray-400">
                        Ventas: {row.ventasCobradas} · Cancelaciones: {row.cancelaciones} · Retiros: {row.retiros}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-gray-800">${Number(row.totalVentas ?? 0).toFixed(2)}</p>
                      <p className="text-xs text-gray-400">Diferencia: ${Number(row.diferenciaAcumulada ?? 0).toFixed(2)}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100">
              <h2 className="font-bold text-gray-800">Bitácora</h2>
            </div>
            {audit.length === 0 ? (
              <div className="py-16 text-center text-gray-400">
                <AlertCircle className="w-10 h-10 mx-auto mb-3 opacity-30" />
                <p>Sin eventos en este período</p>
              </div>
            ) : (
              <div className="divide-y divide-gray-50">
                {audit.slice(0, 10).map((item, index) => (
                  <div key={`${item.tipoEvento}-${index}`} className="px-6 py-4">
                    <div className="flex items-center justify-between gap-4">
                      <div>
                        <p className="font-semibold text-gray-800">{item.tipoEvento}</p>
                        <p className="text-xs text-gray-400">
                          {item.nombreUsuario} · {new Date(item.fecha).toLocaleString('es-MX')}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-bold text-gray-800">${Number(item.monto ?? 0).toFixed(2)}</p>
                        <p className="text-xs text-gray-400">{item.referencia}</p>
                      </div>
                    </div>
                    {item.motivo && <p className="text-sm text-gray-500 mt-2">{item.motivo}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
