import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { MainLayout } from '../components/MainLayout';
import { ReembolsoModal } from '../components/ReembolsoModal';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';
import { Clock3, Eye, Printer, Ban, Lock, RefreshCw, ChevronRight, X, Undo2, DollarSign, PlusSquare } from 'lucide-react';

interface OrderTrackingApi {
  idOrden: number;
  folio: string;
  fecha: string;
  nombreCajero: string;
  tipoOrden: string;
  total: number;
  cantidadItems: number;
  estado: string;
  pagada: boolean;
  motivoCancelacion?: string | null;
  numeroMesa?: number | null;
  domicilio?: {
    nombreCliente: string;
    direccion: string;
    telefono: string;
    estadoEntrega: string;
    nombreRepartidor?: string | null;
  } | null;
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
  domicilio?: {
    nombreCliente: string;
    direccion: string;
    telefono: string;
    estadoEntrega: string;
  } | null;
  items: TicketItemApi[];
}

type OrdersView = 'ACTIVAS' | 'ENTREGADAS' | 'CANCELADAS' | 'REEMBOLSADAS' | 'TODAS';

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

  const html = `
    <html><head><title>Ticket ${ticket.folio}</title>
    <style>
      * { box-sizing: border-box; }
      body { font-family: 'Courier New', monospace; width: 280px; margin: 0 auto; font-size: 13px; color: #111; }
      h2 { text-align: center; margin: 4px 0; font-size: 16px; }
      .divider { border-top: 1px dashed #999; margin: 6px 0; }
      .total { display:flex; justify-content:space-between; font-weight:bold; font-size:15px; }
    </style></head>
    <body>
      <h2>${ticket.negocio}</h2>
      <div class="divider"></div>
      <div><b>Ticket:</b> ${ticket.folio}</div>
      <div><b>Fecha:</b> ${new Date(ticket.fecha).toLocaleString('es-MX')}</div>
      <div><b>Cajero:</b> ${ticket.cajero}</div>
      <div><b>Tipo:</b> ${ticket.tipoOrden}</div>
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

export function OrdersPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OrderTrackingApi[]>([]);
  const [view, setView] = useState<OrdersView>('ACTIVAS');
  const [typeFilter, setTypeFilter] = useState('TODOS');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [ticket, setTicket] = useState<TicketResponseApi | null>(null);
  const [cancelTarget, setCancelTarget] = useState<OrderTrackingApi | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const [refundTarget, setRefundTarget] = useState<OrderTrackingApi | null>(null);
  const [refundLoading, setRefundLoading] = useState(false);
  const [refundError, setRefundError] = useState('');

  const canUseOrders = user?.role === 'admin' || user?.role === 'manager' || user?.role === 'cashier';

  const loadOrders = async () => {
    if (!user?.idNegocio || !canUseOrders) return;
    setLoading(true);
    setError('');
    try {
      const response = await apiRequest<OrderTrackingApi[]>(`/ventas/negocios/${user.idNegocio}/ordenes`);
      setOrders(response);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudieron cargar las órdenes');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOrders();
  }, [user?.idNegocio]);

  const filteredOrders = useMemo(() => {
    return orders.filter((order) => {
      const matchesView =
        view === 'TODAS'
          ? true
          : view === 'ACTIVAS'
          ? ['POR_ACEPTAR', 'PREPARANDO', 'LISTO', 'EN_RUTA'].includes(order.estado)
          : view === 'ENTREGADAS'
          ? order.estado === 'ENTREGADO'
          : view === 'REEMBOLSADAS'
          ? order.estado === 'REEMBOLSADO'
          : order.estado === 'CANCELADO';

      const matchesType = typeFilter === 'TODOS' || order.tipoOrden === typeFilter;
      return matchesView && matchesType;
    });
  }, [orders, view, typeFilter]);

  const handleChangeStatus = async (order: OrderTrackingApi, nextStatus: string) => {
    setError('');
    try {
      await apiRequest(`/ventas/ordenes/${order.idOrden}/estado`, {
        method: 'POST',
        body: JSON.stringify({ estado: nextStatus }),
      });
      await loadOrders();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo actualizar el estado');
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
      await loadOrders();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo cancelar la orden');
    }
  };

  const handleViewTicket = async (idOrden: number, print = false) => {
    setError('');
    try {
      const response = await apiRequest<TicketResponseApi>(`/ventas/ordenes/${idOrden}/ticket`);
      if (print) {
        printTicket(response);
        return;
      }
      setTicket(response);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo obtener el ticket');
    }
  };

  const handleRefund = async (motivo: string, pinGerente: string) => {
    if (!refundTarget) return;
    setRefundLoading(true);
    setRefundError('');
    setError('');
    try {
      await apiRequest(`/ventas/ordenes/${refundTarget.idOrden}/reembolso`, {
        method: 'POST',
        body: JSON.stringify({ motivo, pinGerente }),
      });
      setRefundTarget(null);
      await loadOrders();
    } catch (requestError) {
      setRefundError(requestError instanceof Error ? requestError.message : 'No se pudo reembolsar la orden');
    } finally {
      setRefundLoading(false);
    }
  };

  const getNextStatuses = (order: OrderTrackingApi) => {
    if (order.estado === 'POR_ACEPTAR') return ['PREPARANDO'];
    if (order.estado === 'PREPARANDO') return ['LISTO'];
    if (order.estado === 'LISTO') return [order.tipoOrden === 'DOMICILIO' ? 'EN_RUTA' : 'ENTREGADO'];
    if (order.estado === 'EN_RUTA') return ['ENTREGADO'];
    return [];
  };

  const handleCobrar = (order: OrderTrackingApi) => {
    navigate('/checkout', {
      state: {
        existingOrder: {
          idOrden: order.idOrden,
          folio: order.folio,
          tipoOrden: order.tipoOrden,
          total: Number(order.total ?? 0),
        },
      },
    });
  };

  const handleAgregarItems = (order: OrderTrackingApi) => {
    navigate('/pos', {
      state: {
        addToOrder: {
          idOrden: order.idOrden,
          folio: order.folio,
        },
      },
    });
  };

  const canAddItems = (order: OrderTrackingApi) =>
    !order.pagada && ['POR_ACEPTAR', 'PREPARANDO', 'LISTO'].includes(order.estado);

  if (!canUseOrders) {
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
            <h1 className="text-3xl font-bold text-gray-800">Órdenes</h1>
            <p className="text-gray-600 mt-1">Seguimiento operativo por estado</p>
          </div>
          <button
            onClick={() => void loadOrders()}
            className="flex items-center gap-2 bg-white border border-gray-200 px-4 py-3 rounded-lg font-semibold text-gray-700 hover:bg-gray-50 transition-all shadow-sm"
          >
            <RefreshCw className="w-4 h-4" />
            Actualizar
          </button>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 mb-6">
          <div className="flex flex-wrap gap-2 mb-4">
            {[
              ['ACTIVAS', 'Activas'],
              ['ENTREGADAS', 'Entregadas'],
              ['CANCELADAS', 'Canceladas'],
              ['REEMBOLSADAS', 'Reembolsadas'],
              ['TODAS', 'Todas'],
            ].map(([value, label]) => (
              <button
                key={value}
                onClick={() => setView(value as OrdersView)}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                  view === value ? 'bg-orange-500 text-white shadow' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {label}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Tipo de orden</label>
              <select
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
              >
                <option value="TODOS">Todos</option>
                <option value="MOSTRADOR">Mostrador</option>
                <option value="DOMICILIO">Domicilio</option>
              </select>
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
            <Clock3 className="w-5 h-5 text-orange-500" />
            <h2 className="font-bold text-gray-800">Listado de órdenes</h2>
          </div>

          {loading ? (
            <div className="py-16 text-center text-gray-400">Cargando órdenes...</div>
          ) : filteredOrders.length === 0 ? (
            <div className="py-16 text-center text-gray-400">No hay órdenes para ese filtro.</div>
          ) : (
            <div className="divide-y divide-gray-100">
              {filteredOrders.map((order) => {
                const nextStatuses = getNextStatuses(order);
                return (
                  <div key={order.idOrden} className="px-6 py-5">
                    <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
                      <div className="min-w-0">
                        <div className="flex items-center gap-3 flex-wrap">
                          <p className="font-bold text-gray-800">{order.folio}</p>
                          <span className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
                            order.estado === 'CANCELADO'
                              ? 'bg-red-100 text-red-700'
                              : order.estado === 'REEMBOLSADO'
                              ? 'bg-rose-100 text-rose-700'
                              : order.estado === 'ENTREGADO'
                              ? 'bg-green-100 text-green-700'
                              : 'bg-amber-100 text-amber-700'
                          }`}>
                            {order.estado}
                          </span>
                          <span className="text-xs text-gray-500">{order.pagada ? 'Pagada' : 'Sin pago'}</span>
                        </div>
                        <p className="text-sm text-gray-500 mt-1">{new Date(order.fecha).toLocaleString('es-MX')}</p>
                        <p className="text-sm text-gray-600 mt-1">
                          {order.tipoOrden} · {order.cantidadItems} items · Cajero: {order.nombreCajero}
                          {order.numeroMesa != null ? ` · Mesa ${order.numeroMesa}` : ''}
                        </p>
                        {order.domicilio?.nombreCliente && (
                          <p className="text-sm text-orange-600 mt-1">
                            {order.domicilio.nombreCliente}
                            {order.domicilio.nombreRepartidor ? ` · Repartidor: ${order.domicilio.nombreRepartidor}` : ''}
                          </p>
                        )}
                        {order.motivoCancelacion && (
                          <p className="text-sm text-red-600 mt-2">Motivo: {order.motivoCancelacion}</p>
                        )}
                      </div>

                      <div className="flex flex-col items-start lg:items-end gap-3">
                        <p className="text-xl font-bold text-orange-600">${Number(order.total ?? 0).toFixed(2)}</p>

                        <div className="flex flex-wrap gap-2">
                          {nextStatuses.map((status) => (
                            <button
                              key={status}
                              onClick={() => void handleChangeStatus(order, status)}
                              className="inline-flex items-center gap-2 rounded-lg bg-blue-50 px-3 py-2 text-sm font-semibold text-blue-700 hover:bg-blue-100 transition-colors"
                            >
                              {status}
                              <ChevronRight className="w-4 h-4" />
                            </button>
                          ))}

                          {canAddItems(order) && (
                            <button
                              onClick={() => handleAgregarItems(order)}
                              className="inline-flex items-center gap-2 rounded-lg bg-amber-50 px-3 py-2 text-sm font-semibold text-amber-700 hover:bg-amber-100 transition-colors"
                            >
                              <PlusSquare className="w-4 h-4" />
                              Agregar items
                            </button>
                          )}

                          {!order.pagada && order.estado !== 'CANCELADO' && order.estado !== 'REEMBOLSADO' && (
                            <button
                              onClick={() => handleCobrar(order)}
                              className="inline-flex items-center gap-2 rounded-lg bg-green-50 px-3 py-2 text-sm font-semibold text-green-700 hover:bg-green-100 transition-colors"
                            >
                              <DollarSign className="w-4 h-4" />
                              Cobrar
                            </button>
                          )}

                          {order.estado === 'POR_ACEPTAR' && (
                            <button
                              onClick={() => {
                                setCancelTarget(order);
                                setCancelReason('');
                                setError('');
                              }}
                              className="inline-flex items-center gap-2 rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-700 hover:bg-red-100 transition-colors"
                            >
                              <Ban className="w-4 h-4" />
                              Cancelar
                            </button>
                          )}

                          {order.pagada && (
                            <>
                              <button
                                onClick={() => void handleViewTicket(order.idOrden)}
                                className="inline-flex items-center gap-2 rounded-lg bg-gray-100 px-3 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-200 transition-colors"
                              >
                                <Eye className="w-4 h-4" />
                                Ticket
                              </button>
                              <button
                                onClick={() => void handleViewTicket(order.idOrden, true)}
                                className="inline-flex items-center gap-2 rounded-lg bg-orange-50 px-3 py-2 text-sm font-semibold text-orange-700 hover:bg-orange-100 transition-colors"
                              >
                                <Printer className="w-4 h-4" />
                                Reimprimir
                              </button>
                              {order.estado !== 'REEMBOLSADO' && order.estado !== 'CANCELADO' && (
                                <button
                                  onClick={() => {
                                    setRefundTarget(order);
                                    setRefundError('');
                                    setError('');
                                  }}
                                  className="inline-flex items-center gap-2 rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-700 hover:bg-red-100 transition-colors"
                                >
                                  <Undo2 className="w-4 h-4" />
                                  Reembolsar
                                </button>
                              )}
                            </>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {cancelTarget && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold text-gray-800">Cancelar orden</h2>
              <button onClick={() => setCancelTarget(null)} className="text-gray-400 hover:text-gray-600">
                <X className="w-6 h-6" />
              </button>
            </div>
            <p className="text-sm text-gray-500 mb-4">Folio {cancelTarget.folio}. Debes dejar trazabilidad del motivo.</p>
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
              <div className="flex justify-between"><span className="text-gray-500">Método</span><span>{ticket.metodoPago}</span></div>
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
            <div className="flex justify-between text-base font-bold">
              <span>Total</span>
              <span>${Number(ticket.total ?? 0).toFixed(2)}</span>
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

      {refundTarget && (
        <ReembolsoModal
          folio={refundTarget.folio}
          total={refundTarget.total}
          loading={refundLoading}
          error={refundError}
          onClose={() => {
            if (refundLoading) return;
            setRefundTarget(null);
            setRefundError('');
          }}
          onSubmit={handleRefund}
        />
      )}
    </MainLayout>
  );
}
