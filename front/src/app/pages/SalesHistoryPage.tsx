import { useEffect, useMemo, useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';
import { Eye, Printer, Ban, History, Lock, Search, X } from 'lucide-react';

interface HistorialVentaApi {
  idOrden: number;
  folio: string;
  fecha: string;
  nombreCajero: string;
  tipoOrden: string;
  metodoPago: string;
  total: number;
  estado: string;
  pagada: boolean;
  motivoCancelacion?: string | null;
}

interface TicketItemApi {
  idProducto: number;
  nombre: string;
  cantidad: number;
  precioListaUnitario: number;
  precioUnitario: number;
  descuentoPromocional: number;
  subtotal: number;
  modificadores: Array<{
    idModificador: number;
    nombreSnapshot: string;
    precioExtraSnapshot: number;
  }>;
}

interface TicketResponseApi {
  idOrden: number;
  folio: string;
  tipoOrden: string;
  negocio: string;
  cajero: string;
  fecha: string;
  metodoPago: string;
  totalDescuentoPromocional: number;
  total: number;
  efectivoRecibido?: number | null;
  cambio?: number | null;
  domicilio?: {
    nombreCliente: string;
    direccion: string;
    telefono: string;
    estadoEntrega: string;
  } | null;
  items: TicketItemApi[];
}

const today = new Date().toISOString().slice(0, 10);

function printTicket(ticket: TicketResponseApi) {
  const itemsHtml = ticket.items
    .map((item) => {
      const modifiers = (item.modificadores ?? [])
        .map(
          (modifier) =>
            `<div style="color:#555;padding-left:8px">+ ${modifier.nombreSnapshot} $${Number(modifier.precioExtraSnapshot ?? 0).toFixed(2)}</div>`
        )
        .join('');

      return `
        <div style="display:flex;justify-content:space-between">
          <span>${item.nombre} x${item.cantidad}</span>
          <span>$${Number(item.subtotal ?? 0).toFixed(2)}</span>
        </div>
        ${modifiers}
      `;
    })
    .join('');

  const domicilioHtml = ticket.domicilio
    ? `
      <div><b>Cliente:</b> ${ticket.domicilio.nombreCliente}</div>
      <div><b>Tel:</b> ${ticket.domicilio.telefono}</div>
      <div><b>Dir:</b> ${ticket.domicilio.direccion}</div>
    `
    : '';

  const html = `
    <html><head><title>Ticket ${ticket.folio}</title>
    <style>
      * { box-sizing: border-box; }
      body { font-family: 'Courier New', monospace; width: 280px; margin: 0 auto; font-size: 13px; color: #111; }
      h2 { text-align: center; margin: 4px 0; font-size: 16px; }
      .center { text-align: center; }
      .divider { border-top: 1px dashed #999; margin: 6px 0; }
      .total { display:flex; justify-content:space-between; font-weight:bold; font-size:15px; }
    </style></head>
    <body>
      <h2>${ticket.negocio}</h2>
      <div class="center" style="font-size:11px;color:#666">Ticket de venta</div>
      <div class="divider"></div>
      <div><b>Ticket:</b> ${ticket.folio}</div>
      <div><b>Fecha:</b> ${new Date(ticket.fecha).toLocaleString('es-MX')}</div>
      <div><b>Cajero:</b> ${ticket.cajero}</div>
      <div><b>Tipo:</b> ${ticket.tipoOrden}</div>
      ${domicilioHtml}
      <div class="divider"></div>
      ${itemsHtml}
      <div class="divider"></div>
      ${
        Number(ticket.totalDescuentoPromocional ?? 0) > 0
          ? `<div style="display:flex;justify-content:space-between"><span>Descuento:</span><span>-$${Number(ticket.totalDescuentoPromocional).toFixed(2)}</span></div>`
          : ''
      }
      <div class="total"><span>TOTAL:</span><span>$${Number(ticket.total ?? 0).toFixed(2)} MXN</span></div>
      <div class="divider"></div>
      <div><b>Pago:</b> ${ticket.metodoPago}</div>
    </body></html>`;

  const w = window.open('', '_blank', 'width=340,height=620');
  if (!w) return;
  w.document.write(html);
  w.document.close();
  w.focus();
  setTimeout(() => {
    w.print();
    w.close();
  }, 250);
}

export function SalesHistoryPage() {
  const { user } = useAuth();
  const [sales, setSales] = useState<HistorialVentaApi[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [ticket, setTicket] = useState<TicketResponseApi | null>(null);
  const [ticketLoading, setTicketLoading] = useState(false);
  const [cancelTarget, setCancelTarget] = useState<HistorialVentaApi | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const [filters, setFilters] = useState({
    fechaInicio: today,
    fechaFin: today,
    estado: 'TODOS',
    metodoPago: 'TODOS',
  });

  const canUseHistory = user?.role === 'admin' || user?.role === 'manager' || user?.role === 'cashier';

  const totalVisible = useMemo(
    () => sales.reduce((sum, sale) => sum + Number(sale.total ?? 0), 0),
    [sales]
  );

  const loadHistory = async () => {
    if (!user?.idNegocio || !canUseHistory) return;
    setLoading(true);
    setError('');
    try {
      const params = new URLSearchParams();
      if (filters.fechaInicio) params.set('fechaInicio', filters.fechaInicio);
      if (filters.fechaFin) params.set('fechaFin', filters.fechaFin);
      if (filters.estado && filters.estado !== 'TODOS') params.set('estado', filters.estado);
      if (filters.metodoPago && filters.metodoPago !== 'TODOS') params.set('metodoPago', filters.metodoPago);
      const response = await apiRequest<HistorialVentaApi[]>(
        `/ventas/negocios/${user.idNegocio}/historial?${params.toString()}`
      );
      setSales(response);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo cargar el historial');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadHistory();
  }, [user?.idNegocio]);

  const handleViewTicket = async (idOrden: number) => {
    setTicketLoading(true);
    setError('');
    try {
      const response = await apiRequest<TicketResponseApi>(`/ventas/ordenes/${idOrden}/ticket`);
      setTicket(response);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo cargar el ticket');
    } finally {
      setTicketLoading(false);
    }
  };

  const handleReprint = async (idOrden: number) => {
    setTicketLoading(true);
    setError('');
    try {
      const response = await apiRequest<TicketResponseApi>(`/ventas/ordenes/${idOrden}/ticket`);
      printTicket(response);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo reimprimir el ticket');
    } finally {
      setTicketLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!cancelTarget) return;
    if (!cancelReason.trim()) {
      setError('Debes indicar el motivo de cancelación.');
      return;
    }
    setError('');
    try {
      await apiRequest(`/ventas/ordenes/${cancelTarget.idOrden}/cancelacion`, {
        method: 'POST',
        body: JSON.stringify({ motivo: cancelReason.trim() }),
      });
      setCancelTarget(null);
      setCancelReason('');
      await loadHistory();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo cancelar la orden');
    }
  };

  if (!canUseHistory) {
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

  return (
    <MainLayout>
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Historial de ventas</h1>
            <p className="text-gray-600 mt-1">Consulta, ticket y cancelación de órdenes</p>
          </div>
          <div className="text-right">
            <p className="text-sm text-gray-500">Resultados</p>
            <p className="text-2xl font-bold text-gray-800">{sales.length}</p>
            <p className="text-sm text-orange-600 font-semibold">${totalVisible.toFixed(2)}</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Fecha inicio</label>
              <input
                type="date"
                value={filters.fechaInicio}
                onChange={(e) => setFilters((current) => ({ ...current, fechaInicio: e.target.value }))}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Fecha fin</label>
              <input
                type="date"
                value={filters.fechaFin}
                onChange={(e) => setFilters((current) => ({ ...current, fechaFin: e.target.value }))}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Estado</label>
              <select
                value={filters.estado}
                onChange={(e) => setFilters((current) => ({ ...current, estado: e.target.value }))}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
              >
                <option value="TODOS">Todos</option>
                <option value="PREPARANDO">Preparando</option>
                <option value="LISTO">Listo</option>
                <option value="EN_RUTA">En ruta</option>
                <option value="ENTREGADO">Entregado</option>
                <option value="CANCELADO">Cancelado</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Método de pago</label>
              <select
                value={filters.metodoPago}
                onChange={(e) => setFilters((current) => ({ ...current, metodoPago: e.target.value }))}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
              >
                <option value="TODOS">Todos</option>
                <option value="EFECTIVO">Efectivo</option>
                <option value="TARJETA">Tarjeta</option>
                <option value="TRANSFERENCIA">Transferencia</option>
                <option value="SIN_PAGO">Sin pago</option>
              </select>
            </div>
            <div className="flex items-end">
              <button
                onClick={() => void loadHistory()}
                className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-orange-500 to-yellow-500 text-white px-6 py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
              >
                <Search className="w-4 h-4" />
                Filtrar
              </button>
            </div>
          </div>
        </div>

        {error && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 flex items-center gap-2">
            <History className="w-5 h-5 text-orange-500" />
            <h2 className="font-bold text-gray-800">Órdenes registradas</h2>
          </div>

          {loading ? (
            <div className="py-16 text-center text-gray-400">Cargando historial...</div>
          ) : sales.length === 0 ? (
            <div className="py-16 text-center text-gray-400">No hay órdenes en ese filtro.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead className="bg-gray-50 text-gray-500">
                  <tr>
                    <th className="px-4 py-3 text-left">Folio</th>
                    <th className="px-4 py-3 text-left">Fecha</th>
                    <th className="px-4 py-3 text-left">Cajero</th>
                    <th className="px-4 py-3 text-left">Tipo</th>
                    <th className="px-4 py-3 text-left">Pago</th>
                    <th className="px-4 py-3 text-left">Estado</th>
                    <th className="px-4 py-3 text-right">Total</th>
                    <th className="px-4 py-3 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {sales.map((sale) => {
                    const canCancel = !sale.pagada && sale.estado !== 'CANCELADO';
                    return (
                      <tr key={sale.idOrden} className="hover:bg-gray-50">
                        <td className="px-4 py-3 font-semibold text-gray-800">{sale.folio}</td>
                        <td className="px-4 py-3 text-gray-600">{new Date(sale.fecha).toLocaleString('es-MX')}</td>
                        <td className="px-4 py-3 text-gray-600">{sale.nombreCajero}</td>
                        <td className="px-4 py-3 text-gray-600">{sale.tipoOrden}</td>
                        <td className="px-4 py-3 text-gray-600">{sale.metodoPago}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
                            sale.estado === 'CANCELADO'
                              ? 'bg-red-100 text-red-700'
                              : sale.pagada
                              ? 'bg-green-100 text-green-700'
                              : 'bg-amber-100 text-amber-700'
                          }`}>
                            {sale.estado}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right font-semibold text-gray-800">${Number(sale.total ?? 0).toFixed(2)}</td>
                        <td className="px-4 py-3">
                          <div className="flex items-center justify-end gap-2">
                            {sale.pagada && (
                              <>
                                <button
                                  onClick={() => void handleViewTicket(sale.idOrden)}
                                  className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                  title="Ver ticket"
                                >
                                  <Eye className="w-4 h-4" />
                                </button>
                                <button
                                  onClick={() => void handleReprint(sale.idOrden)}
                                  className="p-2 text-orange-600 hover:bg-orange-50 rounded-lg transition-colors"
                                  title="Reimprimir ticket"
                                >
                                  <Printer className="w-4 h-4" />
                                </button>
                              </>
                            )}
                            {canCancel && (
                              <button
                                onClick={() => {
                                  setCancelTarget(sale);
                                  setCancelReason('');
                                  setError('');
                                }}
                                className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                title="Cancelar orden"
                              >
                                <Ban className="w-4 h-4" />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {ticket && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full p-6 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold text-gray-800">Ticket {ticket.folio}</h2>
              <button onClick={() => setTicket(null)} className="text-gray-400 hover:text-gray-600">
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="space-y-2 text-sm">
              <div className="flex justify-between"><span className="text-gray-500">Negocio</span><span>{ticket.negocio}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Cajero</span><span>{ticket.cajero}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Fecha</span><span>{new Date(ticket.fecha).toLocaleString('es-MX')}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Pago</span><span>{ticket.metodoPago}</span></div>
            </div>

            <div className="border-t border-dashed border-gray-200 my-4" />
            <div className="space-y-3 text-sm">
              {ticket.items.map((item, index) => (
                <div key={`${item.idProducto}-${index}`}>
                  <div className="flex justify-between">
                    <span>{item.nombre} x{item.cantidad}</span>
                    <span className="font-semibold">${Number(item.subtotal ?? 0).toFixed(2)}</span>
                  </div>
                  {(item.modificadores ?? []).map((modifier, modifierIndex) => (
                    <div key={`${modifier.idModificador}-${modifierIndex}`} className="pl-3 text-xs text-orange-500">
                      + {modifier.nombreSnapshot} ${Number(modifier.precioExtraSnapshot ?? 0).toFixed(2)}
                    </div>
                  ))}
                </div>
              ))}
            </div>

            <div className="border-t border-dashed border-gray-200 my-4" />
            <div className="space-y-2 text-sm">
              {Number(ticket.totalDescuentoPromocional ?? 0) > 0 && (
                <div className="flex justify-between text-gray-600">
                  <span>Descuento</span>
                  <span>-${Number(ticket.totalDescuentoPromocional ?? 0).toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between text-base font-bold">
                <span>Total</span>
                <span>${Number(ticket.total ?? 0).toFixed(2)}</span>
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={() => printTicket(ticket)}
                className="flex-1 flex items-center justify-center gap-2 bg-gradient-to-r from-orange-500 to-orange-600 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-orange-700 transition-all"
              >
                <Printer className="w-4 h-4" />
                Reimprimir
              </button>
              <button
                onClick={() => setTicket(null)}
                className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition-all"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}

      {cancelTarget && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Cancelar orden</h2>
            <p className="text-gray-500 text-sm mb-4">
              Folio {cancelTarget.folio}. Esta acción solo aplica a órdenes sin pago registrado.
            </p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none resize-none"
              rows={4}
              placeholder="Motivo de cancelación"
            />
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => void handleCancel()}
                className="flex-1 bg-gradient-to-r from-red-500 to-red-600 text-white py-3 rounded-lg font-semibold hover:from-red-600 hover:to-red-700 transition-all"
              >
                Confirmar cancelación
              </button>
              <button
                onClick={() => {
                  setCancelTarget(null);
                  setCancelReason('');
                }}
                className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition-all"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}

      {ticketLoading && (
        <div className="fixed bottom-4 right-4 rounded-lg bg-gray-900 text-white px-4 py-2 text-sm shadow-lg">
          Cargando ticket...
        </div>
      )}
    </MainLayout>
  );
}
