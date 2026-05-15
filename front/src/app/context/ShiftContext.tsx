import { createContext, useContext, useEffect, useMemo, useState, ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { apiRequest } from '../lib/api';

export interface CashMovement {
  id: number;
  type: string;
  amount: number;
  date: Date;
  reason: string;
}

export interface Shift {
  id: number;
  employeeName: string;
  employeeRole: string;
  startTime: Date;
  endTime?: Date;
  initialAmount: number;
  finalAmount?: number;
  salesCash: number;
  salesCard: number;
  salesTransfer: number;
  totalSales: number;
  withdrawals: number;
  securityWithdrawals: number;
  manualIncome: number;
  currentCash: number;
  expectedAmount: number;
  difference?: number;
  salesCount: number;
  cancellationsCount: number;
  movementsCount: number;
  status: 'open' | 'closed';
  movements: CashMovement[];
}

export interface CashSummaryByPeriod {
  period: string;
  totalSales: number;
  totalCashSales: number;
  totalCardSales: number;
  totalTransferSales: number;
  totalManualIncome: number;
  totalWithdrawals: number;
  totalSecurityWithdrawals: number;
  currentCash: number;
  expectedBalance: number;
  salesCount: number;
  cancellationsCount: number;
  movementsCount: number;
}

interface ShiftContextType {
  currentShift: Shift | null;
  shifts: Shift[];
  loading: boolean;
  periodSummary: CashSummaryByPeriod | null;
  refreshShiftData: () => Promise<void>;
  loadPeriodSummary: (period?: string) => Promise<void>;
  openShift: (employeeName: string, employeeRole: string, initialAmount: number) => Promise<void>;
  closeShift: (
    finalAmount: number,
    salesCash: number,
    salesCard: number,
    salesTransfer: number,
    withdrawals: number,
    notes: string
  ) => Promise<void>;
  registerMovement: (amount: number, type: 'withdrawal' | 'income' | 'security_withdrawal', reason: string) => Promise<void>;
}

interface SessionApi {
  idSesionCaja: number;
  idUsuario: number;
  nombreUsuario: string;
  estado: string;
  fondoInicial: number;
  apertura: string;
  cierre?: string | null;
  totalVentas: number;
  totalVentasEfectivo: number;
  totalVentasTarjeta: number;
  totalVentasTransferencia: number;
  totalIngresosManuales: number;
  totalRetiros: number;
  totalRetirosSeguridad: number;
  efectivoEnCaja: number;
  saldoEsperado: number;
  montoDeclarado?: number | null;
  diferencia?: number | null;
  cantidadVentas: number;
  cantidadCancelaciones: number;
  cantidadMovimientos: number;
  movimientos: MovementApi[];
}

interface SessionSummaryApi {
  idSesionCaja: number;
  idUsuario: number;
  nombreUsuario: string;
  estado: string;
  apertura: string;
  cierre?: string | null;
  fondoInicial: number;
  totalVentas: number;
  totalVentasEfectivo: number;
  totalVentasTarjeta: number;
  totalVentasTransferencia: number;
  totalIngresosManuales: number;
  totalRetiros: number;
  totalRetirosSeguridad: number;
  efectivoEnCaja: number;
  saldoEsperado: number;
  montoDeclarado?: number | null;
  diferencia?: number | null;
  cantidadVentas: number;
  cantidadCancelaciones: number;
  cantidadMovimientos: number;
}

interface MovementApi {
  idMovimientoCaja: number;
  tipo: string;
  monto: number;
  fecha: string;
  motivo: string;
}

interface PeriodSummaryApi {
  periodo: string;
  totalVentas: number;
  totalVentasEfectivo: number;
  totalVentasTarjeta: number;
  totalVentasTransferencia: number;
  totalIngresosManuales: number;
  totalRetiros: number;
  totalRetirosSeguridad: number;
  efectivoEnCaja: number;
  saldoEsperado: number;
  cantidadVentas: number;
  cantidadCancelaciones: number;
  cantidadMovimientos: number;
}

const ShiftContext = createContext<ShiftContextType | undefined>(undefined);

function toNumber(value: number | null | undefined) {
  return Number(value ?? 0);
}

function mapMovement(movement: MovementApi): CashMovement {
  return {
    id: movement.idMovimientoCaja,
    type: movement.tipo,
    amount: toNumber(movement.monto),
    date: new Date(movement.fecha),
    reason: movement.motivo,
  };
}

function mapSession(session: SessionApi | SessionSummaryApi, movements: CashMovement[] = []): Shift {
  const isOpen = (session.estado || '').toUpperCase() === 'ABIERTA';
  return {
    id: session.idSesionCaja,
    employeeName: session.nombreUsuario,
    employeeRole: '',
    startTime: new Date(session.apertura),
    endTime: session.cierre ? new Date(session.cierre) : undefined,
    initialAmount: toNumber(session.fondoInicial),
    finalAmount: session.montoDeclarado == null ? undefined : toNumber(session.montoDeclarado),
    salesCash: toNumber(session.totalVentasEfectivo),
    salesCard: toNumber(session.totalVentasTarjeta),
    salesTransfer: toNumber(session.totalVentasTransferencia),
    totalSales: toNumber(session.totalVentas),
    withdrawals: toNumber(session.totalRetiros),
    securityWithdrawals: toNumber(session.totalRetirosSeguridad),
    manualIncome: toNumber(session.totalIngresosManuales),
    currentCash: toNumber(session.efectivoEnCaja),
    expectedAmount: toNumber(session.efectivoEnCaja),
    difference: session.diferencia == null ? undefined : toNumber(session.diferencia),
    salesCount: session.cantidadVentas ?? 0,
    cancellationsCount: session.cantidadCancelaciones ?? 0,
    movementsCount: session.cantidadMovimientos ?? 0,
    status: isOpen ? 'open' : 'closed',
    movements,
  };
}

function mapPeriodSummary(summary: PeriodSummaryApi): CashSummaryByPeriod {
  return {
    period: summary.periodo,
    totalSales: toNumber(summary.totalVentas),
    totalCashSales: toNumber(summary.totalVentasEfectivo),
    totalCardSales: toNumber(summary.totalVentasTarjeta),
    totalTransferSales: toNumber(summary.totalVentasTransferencia),
    totalManualIncome: toNumber(summary.totalIngresosManuales),
    totalWithdrawals: toNumber(summary.totalRetiros),
    totalSecurityWithdrawals: toNumber(summary.totalRetirosSeguridad),
    currentCash: toNumber(summary.efectivoEnCaja),
    expectedBalance: toNumber(summary.saldoEsperado),
    salesCount: summary.cantidadVentas ?? 0,
    cancellationsCount: summary.cantidadCancelaciones ?? 0,
    movementsCount: summary.cantidadMovimientos ?? 0,
  };
}

export function ShiftProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [currentShift, setCurrentShift] = useState<Shift | null>(null);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [periodSummary, setPeriodSummary] = useState<CashSummaryByPeriod | null>(null);
  const [loading, setLoading] = useState(false);

  const refreshShiftData = async () => {
    if (!user?.idUsuario) {
      setCurrentShift(null);
      setShifts([]);
      setPeriodSummary(null);
      return;
    }

    setLoading(true);
    try {
      const current = await apiRequest<SessionApi | null>(
        `/caja/usuarios/${user.idUsuario}/sesion-actual`
      );
      const history = await apiRequest<SessionSummaryApi[]>(
        `/caja/usuarios/${user.idUsuario}/historial`
      );

      setCurrentShift(current ? mapSession(current, (current.movimientos ?? []).map(mapMovement)) : null);
      setShifts(history.map((item) => mapSession(item)).reverse());
    } finally {
      setLoading(false);
    }
  };

  const loadPeriodSummary = async (period = 'DIARIO') => {
    if (!user?.idNegocio) {
      setPeriodSummary(null);
      return;
    }

    const summary = await apiRequest<PeriodSummaryApi>(
      `/caja/negocios/${user.idNegocio}/resumen-periodo?periodo=${period}`
    );
    setPeriodSummary(mapPeriodSummary(summary));
  };

  useEffect(() => {
    void refreshShiftData();
  }, [user?.idUsuario]);

  const openShift = async (_employeeName: string, _employeeRole: string, initialAmount: number) => {
    if (!user?.idUsuario) {
      return;
    }

    await apiRequest(`/caja/usuarios/${user.idUsuario}/apertura`, {
      method: 'POST',
      body: JSON.stringify({ fondoInicial: initialAmount }),
    });
    await refreshShiftData();
    await loadPeriodSummary();
  };

  const closeShift = async (
    finalAmount: number,
    _salesCash: number,
    _salesCard: number,
    _salesTransfer: number,
    _withdrawals: number,
    _notes: string
  ) => {
    if (!user?.idUsuario) {
      return;
    }

    await apiRequest(`/caja/usuarios/${user.idUsuario}/cierre`, {
      method: 'POST',
      body: JSON.stringify({ montoDeclarado: finalAmount }),
    });
    await refreshShiftData();
    await loadPeriodSummary();
  };

  const registerMovement = async (
    amount: number,
    type: 'withdrawal' | 'income' | 'security_withdrawal',
    reason: string
  ) => {
    if (!user?.idUsuario) {
      return;
    }

    const backendType =
      type === 'income'
        ? 'INGRESO_MANUAL'
        : type === 'security_withdrawal'
        ? 'RETIRO_SEGURIDAD'
        : 'RETIRO';

    await apiRequest(`/caja/usuarios/${user.idUsuario}/movimientos`, {
      method: 'POST',
      body: JSON.stringify({
        tipo: backendType,
        monto: amount,
        motivo: reason,
      }),
    });
    await refreshShiftData();
    await loadPeriodSummary();
  };

  const value = useMemo(
    () => ({
      currentShift,
      shifts,
      loading,
      periodSummary,
      refreshShiftData,
      loadPeriodSummary,
      openShift,
      closeShift,
      registerMovement,
    }),
    [currentShift, shifts, loading, periodSummary]
  );

  return <ShiftContext.Provider value={value}>{children}</ShiftContext.Provider>;
}

export function useShift() {
  const context = useContext(ShiftContext);
  if (!context) throw new Error('useShift must be used within ShiftProvider');
  return context;
}
