import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { MainLayout } from '../components/MainLayout';
import { useShift } from '../context/ShiftContext';
import { useSettings } from '../context/SettingsContext';
import { DollarSign, TrendingUp, TrendingDown, AlertCircle, Clock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export function CashRegisterPage() {
  const { currentShift, registerMovement, periodSummary, loadPeriodSummary } = useShift();
  const { settings } = useSettings();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [withdrawReason, setWithdrawReason] = useState('');

  useEffect(() => {
    void loadPeriodSummary();
  }, []);

  const maxCashLimit = settings.cashLimit;
  const initialAmount = currentShift?.initialAmount ?? 0;
  const salesAmount = currentShift?.totalSales ?? periodSummary?.totalSales ?? 0;
  const withdrawalsAmount =
    (currentShift?.withdrawals ?? 0) + (currentShift?.securityWithdrawals ?? 0);
  const currentCash = currentShift?.currentCash ?? periodSummary?.currentCash ?? 0;

  const handleWithdraw = async () => {
    const amount = parseFloat(withdrawAmount);
    if (amount > 0 && amount <= currentCash && (user?.role === 'admin' || user?.role === 'manager')) {
      await registerMovement(
        amount,
        withdrawReason === 'security' ? 'security_withdrawal' : 'withdrawal',
        withdrawReason
      );
      setShowWithdrawModal(false);
      setWithdrawAmount('');
      setWithdrawReason('');
    }
  };

  const isNearLimit = currentCash > maxCashLimit * 0.8;
  const isOverLimit = currentCash > maxCashLimit;

  return (
    <MainLayout>
      <div className="p-8 max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-800 mb-2">Control de Caja</h1>
        <p className="text-gray-600 mb-6">Gestión del efectivo en caja</p>

        {currentShift ? (
          <div className="flex items-center justify-between bg-green-50 border border-green-200 rounded-xl px-5 py-3 mb-6">
            <div className="flex items-center gap-3 text-green-700">
              <span className="w-2.5 h-2.5 bg-green-500 rounded-full animate-pulse" />
              <Clock className="w-4 h-4" />
              <span className="text-sm font-medium">
                Turno activo: <strong>{currentShift.employeeName}</strong> · desde{' '}
                {new Intl.DateTimeFormat('es-MX', { timeStyle: 'short' }).format(currentShift.startTime)}
              </span>
            </div>
            <button
              onClick={() => navigate('/shifts')}
              className="text-sm text-green-600 hover:text-green-800 font-semibold underline"
            >
              Ver turno
            </button>
          </div>
        ) : (
          <div className="flex items-center justify-between bg-amber-50 border border-amber-200 rounded-xl px-5 py-3 mb-6">
            <div className="flex items-center gap-3 text-amber-700">
              <AlertCircle className="w-4 h-4" />
              <span className="text-sm font-medium">Sin turno activo — los movimientos no se asociarán a ningún turno.</span>
            </div>
            <button
              onClick={() => navigate('/shifts')}
              className="text-sm text-amber-600 hover:text-amber-800 font-semibold underline"
            >
              Abrir turno
            </button>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <DollarSign className="w-8 h-8" />
              <span className="text-sm opacity-90">Apertura</span>
            </div>
            <p className="text-3xl font-bold">${initialAmount.toFixed(2)}</p>
            <p className="text-sm opacity-90 mt-1">Dinero inicial</p>
          </div>

          <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <TrendingUp className="w-8 h-8" />
              <span className="text-sm opacity-90">Ingresos</span>
            </div>
            <p className="text-3xl font-bold">${salesAmount.toFixed(2)}</p>
            <p className="text-sm opacity-90 mt-1">Por ventas</p>
          </div>

          <div className="bg-gradient-to-br from-red-500 to-red-600 rounded-xl p-6 text-white shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <TrendingDown className="w-8 h-8" />
              <span className="text-sm opacity-90">Retiros</span>
            </div>
            <p className="text-3xl font-bold">${withdrawalsAmount.toFixed(2)}</p>
            <p className="text-sm opacity-90 mt-1">Retirado</p>
          </div>
        </div>

        <div className="bg-white rounded-xl p-8 shadow-lg mb-8">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h2 className="text-2xl font-bold text-gray-800">Total en Caja</h2>
              <p className="text-gray-600 mt-1">Dinero disponible actualmente</p>
            </div>
            <div className="text-right">
              <p className="text-5xl font-bold text-orange-600">${currentCash.toFixed(2)}</p>
              <p className="text-sm text-gray-600 mt-2">
                Límite máximo: ${maxCashLimit.toFixed(2)}
              </p>
            </div>
          </div>

          {isOverLimit && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center gap-3 mb-4">
              <AlertCircle className="w-6 h-6 text-red-500 flex-shrink-0" />
              <div>
                <p className="font-semibold text-red-700">Límite excedido</p>
                <p className="text-sm text-red-600">
                  La caja ha superado el límite máximo permitido. Se recomienda realizar un retiro.
                </p>
              </div>
            </div>
          )}

          {isNearLimit && !isOverLimit && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-center gap-3 mb-4">
              <AlertCircle className="w-6 h-6 text-yellow-500 flex-shrink-0" />
              <div>
                <p className="font-semibold text-yellow-700">Cerca del límite</p>
                <p className="text-sm text-yellow-600">
                  La caja está cerca del límite máximo. Considere realizar un retiro pronto.
                </p>
              </div>
            </div>
          )}

          <div className="w-full bg-gray-200 rounded-full h-3">
            <div
              className={`h-3 rounded-full transition-all ${
                isOverLimit ? 'bg-red-500' : isNearLimit ? 'bg-yellow-500' : 'bg-green-500'
              }`}
              style={{ width: `${Math.min((currentCash / maxCashLimit) * 100, 100)}%` }}
            />
          </div>
        </div>

        <div className="bg-white rounded-xl p-8 shadow-lg mb-8">
          <h2 className="text-xl font-bold text-gray-800 mb-6">Desglose del Día</h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
              <span className="text-gray-700">Dinero inicial:</span>
              <span className="font-semibold text-gray-800">${initialAmount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center p-4 bg-green-50 rounded-lg">
              <span className="text-gray-700">Total de ventas:</span>
              <span className="font-semibold text-green-600">+${salesAmount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center p-4 bg-red-50 rounded-lg">
              <span className="text-gray-700">Retiros realizados:</span>
              <span className="font-semibold text-red-600">-${withdrawalsAmount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center p-4 bg-orange-50 border-2 border-orange-200 rounded-lg">
              <span className="font-bold text-gray-800">Total en caja:</span>
              <span className="text-2xl font-bold text-orange-600">${currentCash.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {(user?.role === 'admin' || user?.role === 'manager') && (
          <button
            onClick={() => setShowWithdrawModal(true)}
            className="w-full bg-gradient-to-r from-red-500 to-red-600 text-white py-4 rounded-xl font-semibold hover:from-red-600 hover:to-red-700 transition-all shadow-lg"
          >
            Realizar Retiro de Efectivo
          </button>
        )}

        {showWithdrawModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-6">Retiro de Efectivo</h2>

              <div className="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                <p className="text-sm text-yellow-700">
                  Solo el gerente puede autorizar retiros de efectivo.
                </p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Cantidad a retirar
                  </label>
                  <input
                    type="number"
                    value={withdrawAmount}
                    onChange={(e) => setWithdrawAmount(e.target.value)}
                    max={currentCash}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    placeholder="0.00"
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    Disponible: ${currentCash.toFixed(2)}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Motivo del retiro
                  </label>
                  <select
                    value={withdrawReason}
                    onChange={(e) => setWithdrawReason(e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  >
                    <option value="">Seleccionar motivo</option>
                    <option value="security">Seguridad - Exceso de efectivo</option>
                    <option value="bank">Depósito bancario</option>
                    <option value="expenses">Gastos operativos</option>
                    <option value="other">Otro</option>
                  </select>
                </div>

                <div className="p-4 bg-gray-50 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">Autorizado por:</p>
                  <p className="font-semibold text-gray-800">
                    {user?.username} ({user?.role})
                  </p>
                </div>
              </div>

              <div className="flex gap-3 mt-6">
                <button
                  onClick={handleWithdraw}
                  disabled={!withdrawAmount || !withdrawReason || parseFloat(withdrawAmount) <= 0}
                  className="flex-1 bg-gradient-to-r from-red-500 to-red-600 text-white py-3 rounded-lg font-semibold hover:from-red-600 hover:to-red-700 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Confirmar Retiro
                </button>
                <button
                  onClick={() => {
                    setShowWithdrawModal(false);
                    setWithdrawAmount('');
                    setWithdrawReason('');
                  }}
                  className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition-all"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </MainLayout>
  );
}
