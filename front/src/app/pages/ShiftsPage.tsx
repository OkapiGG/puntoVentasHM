import { useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { useShift } from '../context/ShiftContext';
import { useAuth } from '../context/AuthContext';
import { Clock, User, AlertCircle, X, ChevronDown, ChevronUp, Eye, EyeOff } from 'lucide-react';

function formatDateTime(date: Date) {
  return new Intl.DateTimeFormat('es-MX', { dateStyle: 'short', timeStyle: 'short' }).format(date);
}

function formatElapsed(start: Date) {
  const ms = Date.now() - start.getTime();
  const hours = Math.floor(ms / 3600000);
  const minutes = Math.floor((ms % 3600000) / 60000);
  return `${hours}h ${minutes}m`;
}

const roleLabels: Record<string, string> = {
  admin: 'Administrador',
  manager: 'Gerente',
  cashier: 'Cajero',
  delivery: 'Repartidor',
};

export function ShiftsPage() {
  const { currentShift, shifts, openShift, closeShift } = useShift();
  const { user, validatePassword } = useAuth();

  const [showOpenModal, setShowOpenModal] = useState(false);
  const [showCloseModal, setShowCloseModal] = useState(false);
  const [expandedShift, setExpandedShift] = useState<number | null>(null);

  const [openPassword, setOpenPassword] = useState('');
  const [openPasswordError, setOpenPasswordError] = useState(false);
  const [showOpenPassword, setShowOpenPassword] = useState(false);

  const [closePassword, setClosePassword] = useState('');
  const [closePasswordError, setClosePasswordError] = useState(false);
  const [showClosePassword, setShowClosePassword] = useState(false);
  const [openErrorMessage, setOpenErrorMessage] = useState('');
  const [closeErrorMessage, setCloseErrorMessage] = useState('');

  const [openForm, setOpenForm] = useState({
    employeeName: user?.username ?? '',
    employeeRole: user?.role ?? 'cashier',
    initialAmount: '',
  });

  const [finalAmount, setFinalAmount] = useState('');
  const [closeNotes, setCloseNotes] = useState('');

  const salesCash = currentShift?.salesCash ?? 0;
  const salesCard = currentShift?.salesCard ?? 0;
  const salesTransfer = currentShift?.salesTransfer ?? 0;
  const totalSales = currentShift?.totalSales ?? 0;
  const shiftWithdrawals = (currentShift?.withdrawals ?? 0) + (currentShift?.securityWithdrawals ?? 0);
  const expectedAmount = currentShift?.expectedAmount ?? 0;

  const parsedFinal = parseFloat(finalAmount) || 0;
  const difference = parsedFinal - expectedAmount;
  const isCook = user?.role === 'cook';

  const handleOpenShift = async () => {
    const amount = isCook ? 0 : parseFloat(openForm.initialAmount);
    if (!openForm.employeeName || isNaN(amount) || amount < 0) return;
    setOpenErrorMessage('');
    if (!(await validatePassword(user?.username ?? '', openPassword))) {
      setOpenPasswordError(true);
      return;
    }
    try {
      await openShift(openForm.employeeName, openForm.employeeRole, amount);
      setShowOpenModal(false);
      setOpenForm({ employeeName: user?.username ?? '', employeeRole: user?.role ?? 'cashier', initialAmount: '' });
      setOpenPassword('');
      setOpenPasswordError(false);
    } catch (error) {
      setOpenErrorMessage(error instanceof Error ? error.message : 'No se pudo abrir el turno');
    }
  };

  const handleCloseShift = async () => {
    if (!isCook && !finalAmount) return;
    setCloseErrorMessage('');
    if (!(await validatePassword(user?.username ?? '', closePassword))) {
      setClosePasswordError(true);
      return;
    }
    try {
      await closeShift(parsedFinal, salesCash, salesCard, salesTransfer, shiftWithdrawals, closeNotes);
      setShowCloseModal(false);
      setFinalAmount('');
      setCloseNotes('');
      setClosePassword('');
      setClosePasswordError(false);
    } catch (error) {
      setCloseErrorMessage(error instanceof Error ? error.message : 'No se pudo cerrar el turno');
    }
  };

  const handleDismissOpenModal = () => {
    setShowOpenModal(false);
    setOpenPassword('');
    setOpenPasswordError(false);
    setOpenErrorMessage('');
  };

  const handleDismissCloseModal = () => {
    setShowCloseModal(false);
    setClosePassword('');
    setClosePasswordError(false);
    setCloseErrorMessage('');
  };

  const closedShifts = shifts.filter((s) => s.status === 'closed');

  return (
    <MainLayout>
      <div className="p-8 max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-800 mb-1">Gestión de Turnos</h1>
        <p className="text-gray-500 mb-8">Apertura y cierre de caja por turno de trabajo</p>

        {/* ── TURNO ACTIVO ── */}
        {currentShift ? (
          <div className="bg-gradient-to-r from-green-500 to-emerald-600 rounded-2xl p-6 text-white shadow-lg mb-8">
            <div className="flex justify-between items-start flex-wrap gap-4">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <span className="w-2.5 h-2.5 bg-white rounded-full animate-pulse" />
                  <span className="font-bold text-lg">Turno Activo</span>
                </div>
                <div className="flex items-center gap-2 text-green-100 text-sm">
                  <User className="w-4 h-4" />
                  <span>
                    {currentShift.employeeName} ·{' '}
                    {roleLabels[currentShift.employeeRole] ?? currentShift.employeeRole}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-green-100 text-sm mt-1">
                  <Clock className="w-4 h-4" />
                  <span>
                    Inicio: {formatDateTime(currentShift.startTime)} &nbsp;·&nbsp; Duración:{' '}
                    {formatElapsed(currentShift.startTime)}
                  </span>
                </div>
              </div>
              {!isCook && (
                <div className="text-right">
                  <p className="text-green-100 text-sm">Fondo inicial</p>
                  <p className="text-3xl font-bold">${currentShift.initialAmount.toFixed(2)}</p>
                </div>
              )}
            </div>

            {!isCook && (
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6 pt-5 border-t border-green-400/50">
                <div className="text-center">
                  <p className="text-green-100 text-xs mb-1">Ventas efectivo</p>
                  <p className="text-xl font-bold">${salesCash.toFixed(2)}</p>
                </div>
                <div className="text-center">
                  <p className="text-green-100 text-xs mb-1">Ventas tarjeta</p>
                  <p className="text-xl font-bold">${salesCard.toFixed(2)}</p>
                </div>
                <div className="text-center">
                  <p className="text-green-100 text-xs mb-1">Transferencias</p>
                  <p className="text-xl font-bold">${salesTransfer.toFixed(2)}</p>
                </div>
                <div className="text-center border-l border-green-400/50">
                  <p className="text-green-100 text-xs mb-1">Total ventas</p>
                  <p className="text-2xl font-bold">${totalSales.toFixed(2)}</p>
                </div>
              </div>
            )}

            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowCloseModal(true)}
                className="flex-1 bg-white text-green-600 py-3 rounded-xl font-bold hover:bg-green-50 transition-all shadow"
              >
                Cerrar Turno
              </button>
              <button
                onClick={() => {
                  setShowCloseModal(true);
                }}
                className="px-6 py-3 bg-green-400/40 text-white rounded-xl font-semibold hover:bg-green-400/60 transition-all"
              >
                Relevo de Personal
              </button>
            </div>
          </div>
        ) : (
          /* ── SIN TURNO ── */
          <div className="bg-amber-50 border-2 border-amber-200 rounded-2xl p-10 text-center mb-8">
            <AlertCircle className="w-14 h-14 text-amber-400 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-gray-800 mb-2">Sin turno activo</h2>
            <p className="text-gray-500 mb-6 max-w-sm mx-auto">
              Debes abrir un turno para registrar ventas y controlar el efectivo en caja.
            </p>
            <button
              onClick={() => setShowOpenModal(true)}
              className="bg-gradient-to-r from-orange-500 to-orange-600 text-white px-10 py-3 rounded-xl font-bold hover:from-orange-600 hover:to-orange-700 transition-all shadow-lg"
            >
              Abrir Turno
            </button>
          </div>
        )}

        {/* ── HISTORIAL ── */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
            <h2 className="text-lg font-bold text-gray-800">Historial de Turnos</h2>
            <span className="text-sm text-gray-400">{closedShifts.length} turnos cerrados</span>
          </div>

          {closedShifts.length === 0 ? (
            <div className="py-16 text-center text-gray-400">
              <Clock className="w-10 h-10 mx-auto mb-3 opacity-30" />
              <p>No hay turnos cerrados aún</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-50">
              {closedShifts.map((shift) => {
                const isExpanded = expandedShift === shift.id;
                const diff = shift.difference ?? 0;
                return (
                  <div key={shift.id}>
                    <button
                      onClick={() => {
                        if (isCook) return;
                        setExpandedShift(isExpanded ? null : shift.id);
                      }}
                      className={`w-full text-left px-6 py-4 transition-colors ${isCook ? 'cursor-default' : 'hover:bg-gray-50'}`}
                    >
                      <div className="flex items-center justify-between gap-4">
                        <div className="flex items-center gap-4 min-w-0">
                          <div className="w-9 h-9 rounded-full bg-gray-100 flex items-center justify-center flex-shrink-0">
                            <User className="w-4 h-4 text-gray-500" />
                          </div>
                          <div className="min-w-0">
                            <p className="font-semibold text-gray-800 truncate">{shift.employeeName}</p>
                            <p className="text-xs text-gray-400">
                              {roleLabels[shift.employeeRole] ?? shift.employeeRole} ·{' '}
                              {formatDateTime(shift.startTime)} →{' '}
                              {shift.endTime ? formatDateTime(shift.endTime) : '—'}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-center gap-6 flex-shrink-0">
                          {!isCook && (
                            <>
                              <div className="text-right hidden sm:block">
                                <p className="text-xs text-gray-400">Total ventas</p>
                                <p className="font-semibold text-gray-700">${shift.totalSales.toFixed(2)}</p>
                              </div>
                              <div className="text-right">
                                <p className="text-xs text-gray-400">Diferencia</p>
                                <p
                                  className={`font-bold text-sm ${
                                    diff > 0
                                      ? 'text-green-600'
                                      : diff < 0
                                      ? 'text-red-600'
                                      : 'text-gray-500'
                                  }`}
                                >
                                  {diff >= 0 ? '+' : ''}${diff.toFixed(2)}
                                  <span className="text-xs font-normal ml-1">
                                    {diff > 0 ? 'sobrante' : diff < 0 ? 'faltante' : 'exacto'}
                                  </span>
                                </p>
                              </div>
                            </>
                          )}
                          {!isCook && (isExpanded ? (
                            <ChevronUp className="w-4 h-4 text-gray-400" />
                          ) : (
                            <ChevronDown className="w-4 h-4 text-gray-400" />
                          ))}
                        </div>
                      </div>
                    </button>

                    {isExpanded && !isCook && (
                      <div className="px-6 pb-5 bg-gray-50 border-t border-gray-100">
                        <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mt-4">
                          <div className="bg-white rounded-xl p-4 shadow-sm">
                            <p className="text-xs text-gray-400 mb-1">Fondo inicial</p>
                            <p className="text-lg font-bold text-gray-800">
                              ${shift.initialAmount.toFixed(2)}
                            </p>
                          </div>
                          <div className="bg-white rounded-xl p-4 shadow-sm">
                            <p className="text-xs text-gray-400 mb-1">Ventas efectivo</p>
                            <p className="text-lg font-bold text-green-600">
                              ${shift.salesCash.toFixed(2)}
                            </p>
                          </div>
                          <div className="bg-white rounded-xl p-4 shadow-sm">
                            <p className="text-xs text-gray-400 mb-1">Tarjeta</p>
                            <p className="text-lg font-bold text-blue-600">${shift.salesCard.toFixed(2)}</p>
                          </div>
                          <div className="bg-white rounded-xl p-4 shadow-sm">
                            <p className="text-xs text-gray-400 mb-1">Transferencia</p>
                            <p className="text-lg font-bold text-cyan-600">${shift.salesTransfer.toFixed(2)}</p>
                          </div>
                          <div className="bg-white rounded-xl p-4 shadow-sm">
                            <p className="text-xs text-gray-400 mb-1">Retiros</p>
                            <p className="text-lg font-bold text-red-500">
                              -${shift.withdrawals.toFixed(2)}
                            </p>
                          </div>
                        </div>

                        <div className="grid grid-cols-3 gap-4 mt-3">
                          <div className="bg-blue-50 rounded-xl p-4">
                            <p className="text-xs text-blue-500 mb-1">Efectivo esperado</p>
                            <p className="text-lg font-bold text-blue-700">
                              ${shift.expectedAmount.toFixed(2)}
                            </p>
                          </div>
                          <div className="bg-white rounded-xl p-4 shadow-sm">
                            <p className="text-xs text-gray-400 mb-1">Conteo físico</p>
                            <p className="text-lg font-bold text-gray-800">
                              ${(shift.finalAmount ?? 0).toFixed(2)}
                            </p>
                          </div>
                          <div
                            className={`rounded-xl p-4 ${
                              diff > 0
                                ? 'bg-green-50'
                                : diff < 0
                                ? 'bg-red-50'
                                : 'bg-gray-100'
                            }`}
                          >
                            <p className="text-xs text-gray-500 mb-1">Diferencia</p>
                            <p
                              className={`text-lg font-bold ${
                                diff > 0
                                  ? 'text-green-600'
                                  : diff < 0
                                  ? 'text-red-600'
                                  : 'text-gray-600'
                              }`}
                            >
                              {diff >= 0 ? '+' : ''}${diff.toFixed(2)}
                            </p>
                          </div>
                        </div>

                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* ── MODAL APERTURA ── */}
      {showOpenModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-gray-800">Abrir Turno</h2>
              <button
                onClick={handleDismissOpenModal}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre del empleado
                </label>
                <input
                  type="text"
                  value={openForm.employeeName}
                  onChange={(e) => setOpenForm((f) => ({ ...f, employeeName: e.target.value }))}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  placeholder="Nombre completo"
                />
              </div>

              {!isCook && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Rol / Puesto
                  </label>
                  <select
                    value={openForm.employeeRole}
                    onChange={(e) => setOpenForm((f) => ({ ...f, employeeRole: e.target.value }))}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  >
                    <option value="cashier">Cajero</option>
                    <option value="manager">Gerente</option>
                    <option value="supervisor">Encargado</option>
                    <option value="waiter">Mesero</option>
                    <option value="admin">Administrador</option>
                  </select>
                </div>
              )}

              {!isCook && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Fondo de caja inicial ($)
                  </label>
                  <input
                    type="number"
                    value={openForm.initialAmount}
                    onChange={(e) => setOpenForm((f) => ({ ...f, initialAmount: e.target.value }))}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    placeholder="0.00"
                    min="0"
                  />
                </div>
              )}

              <div className="bg-blue-50 rounded-lg px-4 py-3 text-sm text-blue-700">
                <span className="font-semibold">Fecha y hora de apertura: </span>
                {new Date().toLocaleString('es-MX')}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Contraseña de autorización
                  <span className="text-gray-400 font-normal ml-1">({user?.username})</span>
                </label>
                <div className="relative">
                  <input
                    type={showOpenPassword ? 'text' : 'password'}
                    value={openPassword}
                    onChange={(e) => { setOpenPassword(e.target.value); setOpenPasswordError(false); }}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none ${
                      openPasswordError ? 'border-red-400 bg-red-50' : 'border-gray-300'
                    }`}
                    placeholder="Ingresa tu contraseña"
                  />
                  <button
                    type="button"
                    onClick={() => setShowOpenPassword((v) => !v)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showOpenPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              {openPasswordError && (
                <p className="text-red-500 text-xs mt-1">Contraseña incorrecta. Inténtalo de nuevo.</p>
              )}
              {openErrorMessage && (
                <p className="text-red-500 text-xs mt-1">{openErrorMessage}</p>
              )}
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={handleOpenShift}
                disabled={!openForm.employeeName || (!isCook && !openForm.initialAmount) || !openPassword}
                className="flex-1 bg-gradient-to-r from-orange-500 to-orange-600 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-orange-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Abrir Turno
              </button>
              <button
                onClick={handleDismissOpenModal}
                className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-semibold hover:bg-gray-200 transition-all"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL CIERRE ── */}
      {showCloseModal && currentShift && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full p-6 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-gray-800">Cerrar Turno</h2>
              <button
                onClick={handleDismissCloseModal}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            {/* Datos del turno */}
            <div className="bg-gray-50 rounded-xl p-4 mb-4 space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Empleado:</span>
                <span className="font-medium">{currentShift.employeeName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Inicio de turno:</span>
                <span className="font-medium">{formatDateTime(currentShift.startTime)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Duración:</span>
                <span className="font-medium">{formatElapsed(currentShift.startTime)}</span>
              </div>
              {!isCook && (
                <div className="flex justify-between">
                  <span className="text-gray-500">Fondo inicial:</span>
                  <span className="font-medium">${currentShift.initialAmount.toFixed(2)}</span>
                </div>
              )}
            </div>

            {!isCook && (
              <>
                {/* Desglose de ventas */}
                <div className="rounded-xl border border-gray-200 overflow-hidden mb-4">
                  <div className="bg-green-50 px-4 py-2 border-b border-gray-200">
                    <p className="font-semibold text-green-700 text-sm">
                      Ventas del turno ({currentShift.salesCount} órdenes)
                    </p>
                  </div>
                  <div className="p-4 space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-500">Efectivo:</span>
                      <span className="font-medium text-green-600">${salesCash.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">Tarjeta:</span>
                      <span className="font-medium">${salesCard.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">Transferencia:</span>
                      <span className="font-medium">${salesTransfer.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between border-t border-gray-100 pt-2 font-bold">
                      <span>Total ventas:</span>
                      <span className="text-green-600">${totalSales.toFixed(2)}</span>
                    </div>
                  </div>
                </div>

                {/* Efectivo esperado */}
                <div className="bg-blue-50 rounded-xl p-4 mb-4 text-sm">
                  <div className="flex justify-between mb-1 text-gray-600">
                    <span>Fondo inicial:</span>
                    <span>+${currentShift.initialAmount.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between mb-1 text-gray-600">
                    <span>Ventas en efectivo:</span>
                    <span>+${salesCash.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between mb-2 text-gray-600">
                    <span>Retiros realizados:</span>
                    <span className="text-red-500">-${shiftWithdrawals.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between font-bold text-blue-700 border-t border-blue-200 pt-2">
                    <span>Efectivo esperado en caja:</span>
                    <span>${expectedAmount.toFixed(2)}</span>
                  </div>
                </div>

                {/* Conteo físico */}
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Conteo físico de caja ($)
                  </label>
                  <input
                    type="number"
                    value={finalAmount}
                    onChange={(e) => setFinalAmount(e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none text-lg"
                    placeholder="0.00"
                    min="0"
                    autoFocus
                  />
                </div>

                {/* Diferencia */}
                {finalAmount && (
                  <div
                    className={`rounded-xl p-4 mb-4 text-center border ${
                      difference > 0
                        ? 'bg-green-50 border-green-200'
                        : difference < 0
                        ? 'bg-red-50 border-red-200'
                        : 'bg-gray-50 border-gray-200'
                    }`}
                  >
                    <p className="text-sm text-gray-500 mb-1">Diferencia</p>
                    <p
                      className={`text-4xl font-bold ${
                        difference > 0
                          ? 'text-green-600'
                          : difference < 0
                          ? 'text-red-600'
                          : 'text-gray-600'
                      }`}
                    >
                      {difference >= 0 ? '+' : ''}${difference.toFixed(2)}
                    </p>
                    <p
                      className={`text-sm font-semibold mt-1 ${
                        difference > 0
                          ? 'text-green-500'
                          : difference < 0
                          ? 'text-red-500'
                          : 'text-gray-400'
                      }`}
                    >
                      {difference > 0 ? 'Sobrante' : difference < 0 ? 'Faltante' : 'Exacto'}
                    </p>
                  </div>
                )}
              </>
            )}

            {/* Notas */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Notas del cierre (opcional)
              </label>
              <textarea
                value={closeNotes}
                onChange={(e) => setCloseNotes(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none resize-none"
                rows={2}
                placeholder="Observaciones, incidencias, etc."
              />
            </div>

            {/* Contraseña */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contraseña de autorización
                <span className="text-gray-400 font-normal ml-1">({user?.username})</span>
              </label>
              <div className="relative">
                <input
                  type={showClosePassword ? 'text' : 'password'}
                  value={closePassword}
                  onChange={(e) => { setClosePassword(e.target.value); setClosePasswordError(false); }}
                  className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none ${
                    closePasswordError ? 'border-red-400 bg-red-50' : 'border-gray-300'
                  }`}
                  placeholder="Ingresa tu contraseña"
                />
                <button
                  type="button"
                  onClick={() => setShowClosePassword((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showClosePassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
              {closePasswordError && (
                <p className="text-red-500 text-xs mt-1">Contraseña incorrecta. Inténtalo de nuevo.</p>
              )}
              {closeErrorMessage && (
                <p className="text-red-500 text-xs mt-1">{closeErrorMessage}</p>
              )}
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleCloseShift}
                disabled={(!isCook && !finalAmount) || !closePassword}
                className="flex-1 bg-gradient-to-r from-red-500 to-red-600 text-white py-3 rounded-lg font-semibold hover:from-red-600 hover:to-red-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Confirmar Cierre
              </button>
              <button
                onClick={handleDismissCloseModal}
                className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-semibold hover:bg-gray-200 transition-all"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  );
}
