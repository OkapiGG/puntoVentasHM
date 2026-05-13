import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { MainLayout } from '../components/MainLayout';
import { usePOS, CartItem } from '../context/POSContext';
import { useShift } from '../context/ShiftContext';
import { useAuth } from '../context/AuthContext';
import { useSettings } from '../context/SettingsContext';
import { apiRequest } from '../lib/api';
import {
  Home,
  ShoppingBag,
  Truck,
  DollarSign,
  CreditCard,
  Smartphone,
  ArrowLeft,
  Printer,
  CheckCircle,
} from 'lucide-react';

type OrderType = 'dine-in' | 'takeout' | 'delivery';
type PaymentMethod = 'cash' | 'card' | 'transfer';
type Currency = 'MXN' | 'USD' | 'GTQ';

interface OrderResponseApi {
  idOrden: number;
  folio: string;
  tipoOrden: string;
  estado: string;
  pagada: boolean;
  totalDescuentoPromocional: number;
  total: number;
  fecha: string;
}

interface PaymentResponseApi {
  idPago: number;
  idOrden: number;
  metodo: string;
  monto: number;
  efectivoRecibido?: number | null;
  cambio?: number | null;
  estadoOrden: string;
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
    idRepartidor?: number | null;
    nombreRepartidor?: string | null;
  } | null;
  items: TicketItemApi[];
}

interface CompletedOrder {
  id: number;
  folio: string;
  items: TicketItemApi[];
  type: OrderType;
  totalDiscount: number;
  total: number;
  paymentMethod: PaymentMethod;
  currency: Currency;
  amountReceived: number;
  changeAmount: number;
  timestamp: Date;
  cashierName: string;
  businessName: string;
  domicilio?: TicketResponseApi['domicilio'];
}

const orderTypeLabel: Record<OrderType, string> = {
  'dine-in': 'Consumir en Local',
  takeout: 'Para Llevar',
  delivery: 'A Domicilio',
};

const paymentLabel: Record<PaymentMethod, string> = {
  cash: 'Efectivo',
  card: 'Tarjeta',
  transfer: 'Transferencia',
};

const currencySymbol: Record<Currency, string> = { MXN: '$', USD: '$', GTQ: 'Q' };
const exchangeRates: Record<Currency, number> = { MXN: 1, USD: 17.5, GTQ: 2.2 };

function mapOrderType(type: OrderType): 'MOSTRADOR' | 'DOMICILIO' {
  return type === 'delivery' ? 'DOMICILIO' : 'MOSTRADOR';
}

function printTicket(order: CompletedOrder) {
  const itemsHtml = order.items
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

  const domicilioHtml = order.domicilio
    ? `
      <div><b>Cliente:</b> ${order.domicilio.nombreCliente}</div>
      <div><b>Tel:</b> ${order.domicilio.telefono}</div>
      <div><b>Dir:</b> ${order.domicilio.direccion}</div>
    `
    : '';

  const changeRow =
    order.paymentMethod === 'cash'
      ? `<div style="display:flex;justify-content:space-between">
           <span>Recibido:</span>
           <span>${currencySymbol[order.currency]}${order.amountReceived.toFixed(2)}${order.currency !== 'MXN' ? ' ' + order.currency : ''}</span>
         </div>
         <div style="display:flex;justify-content:space-between">
           <span>Cambio:</span><span>$${order.changeAmount.toFixed(2)} MXN</span>
         </div>`
      : '';

  const html = `
    <html><head><title>Ticket ${order.folio}</title>
    <style>
      * { box-sizing: border-box; }
      body { font-family: 'Courier New', monospace; width: 280px; margin: 0 auto; font-size: 13px; color: #111; }
      h2 { text-align: center; margin: 4px 0; font-size: 16px; }
      .center { text-align: center; }
      .divider { border-top: 1px dashed #999; margin: 6px 0; }
      .total { display:flex; justify-content:space-between; font-weight:bold; font-size:15px; }
      .footer { text-align:center; margin-top:8px; font-size:12px; color:#555; }
    </style></head>
    <body>
      <h2>${order.businessName}</h2>
      <div class="center" style="font-size:11px;color:#666">Sistema Punto de Venta</div>
      <div class="divider"></div>
      <div><b>Ticket:</b> ${order.folio}</div>
      <div><b>Fecha:</b> ${order.timestamp.toLocaleString('es-MX')}</div>
      <div><b>Cajero:</b> ${order.cashierName}</div>
      <div><b>Tipo:</b> ${orderTypeLabel[order.type]}</div>
      ${domicilioHtml}
      <div class="divider"></div>
      ${itemsHtml}
      <div class="divider"></div>
      ${
        order.totalDiscount > 0
          ? `<div style="display:flex;justify-content:space-between"><span>Descuento:</span><span>-$${order.totalDiscount.toFixed(2)}</span></div>`
          : ''
      }
      <div class="total"><span>TOTAL:</span><span>$${order.total.toFixed(2)} MXN</span></div>
      <div class="divider"></div>
      <div><b>Pago:</b> ${paymentLabel[order.paymentMethod]}</div>
      ${changeRow}
      <div class="divider"></div>
      <div class="footer">Gracias por su visita!<br>Vuelva pronto</div>
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

export function CheckoutPage() {
  const { cart, clearCart } = usePOS();
  const { currentShift } = useShift();
  const { user } = useAuth();
  const { settings } = useSettings();
  const navigate = useNavigate();

  const [step, setStep] = useState<'type' | 'payment' | 'ticket'>('type');
  const [completedOrder, setCompletedOrder] = useState<CompletedOrder | null>(null);
  const [orderType, setOrderType] = useState<OrderType | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod | null>(null);
  const [currency, setCurrency] = useState<Currency>('MXN');
  const [amountReceived, setAmountReceived] = useState<string>('');
  const [deliveryData, setDeliveryData] = useState({
    nombreCliente: '',
    direccion: '',
    telefono: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!currentShift) navigate('/shifts', { replace: true });
  }, [currentShift, navigate]);

  const subtotal = useMemo(
    () =>
      cart.reduce((sum, item) => {
        const productPrice = item.product.price * item.quantity;
        const extrasPrice = item.extras.reduce((s, e) => s + e.price, 0) * item.quantity;
        return sum + productPrice + extrasPrice;
      }, 0),
    [cart]
  );

  const deliveryFee = orderType === 'delivery' ? settings.deliveryFee : 0;
  const visualTotal = subtotal + deliveryFee;
  const received = parseFloat(amountReceived) || 0;
  const totalInCurrency = visualTotal / exchangeRates[currency];
  const receivedInMXN = received * exchangeRates[currency];
  const changeInMXN = receivedInMXN - visualTotal;

  const handleCompleteOrder = async () => {
    if (!orderType || !paymentMethod || !user) {
      return;
    }

    if (orderType === 'delivery') {
      if (!deliveryData.nombreCliente || !deliveryData.direccion || !deliveryData.telefono) {
        setError('Completa los datos del domicilio.');
        return;
      }
    }

    setSubmitting(true);
    setError('');

    try {
      const orderPayload = {
        idUsuario: user.idUsuario,
        tipoOrden: mapOrderType(orderType),
        domicilio:
          orderType === 'delivery'
            ? {
                ...deliveryData,
                costoEnvio: deliveryFee,
              }
            : null,
        items: cart.map((item: CartItem) => ({
          idProducto: item.product.id,
          cantidad: item.quantity,
          modificadores: item.extras.map((extra) => ({
            idModificador: extra.id,
          })),
        })),
      };

      const order = await apiRequest<OrderResponseApi>('/ventas/ordenes', {
        method: 'POST',
        body: JSON.stringify(orderPayload),
      });

      const payment = await apiRequest<PaymentResponseApi>(`/ventas/ordenes/${order.idOrden}/pago`, {
        method: 'POST',
        body: JSON.stringify({
          metodo:
            paymentMethod === 'cash'
              ? 'EFECTIVO'
              : paymentMethod === 'card'
              ? 'TARJETA'
              : 'TRANSFERENCIA',
          efectivoRecibido: paymentMethod === 'cash' ? receivedInMXN : null,
        }),
      });

      const ticket = await apiRequest<TicketResponseApi>(`/ventas/ordenes/${order.idOrden}/ticket`);

      setCompletedOrder({
        id: ticket.idOrden,
        folio: ticket.folio,
        items: ticket.items,
        type: orderType,
        totalDiscount: Number(ticket.totalDescuentoPromocional ?? 0),
        total: Number(ticket.total ?? payment.monto ?? 0),
        paymentMethod,
        currency,
        amountReceived: paymentMethod === 'cash' ? received : 0,
        changeAmount: Number(ticket.cambio ?? payment.cambio ?? 0),
        timestamp: new Date(ticket.fecha),
        cashierName: ticket.cajero,
        businessName: ticket.negocio,
        domicilio: ticket.domicilio ?? undefined,
      });

      clearCart();
      setStep('ticket');
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No fue posible completar la venta');
    } finally {
      setSubmitting(false);
    }
  };

  if (step === 'ticket' && completedOrder) {
    return (
      <MainLayout>
        <div className="p-8 max-w-lg mx-auto">
          <div className="text-center mb-8">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <CheckCircle className="w-9 h-9 text-green-500" />
            </div>
            <h1 className="text-2xl font-bold text-gray-800">Venta completada</h1>
            <p className="text-gray-500 mt-1">{completedOrder.folio}</p>
          </div>

          <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden mb-6">
            <div className="bg-gradient-to-r from-orange-500 to-orange-600 text-white text-center py-5 px-6">
              <p className="font-bold text-xl">{completedOrder.businessName}</p>
              <p className="text-orange-100 text-sm mt-1">Ticket de venta</p>
            </div>

            <div className="px-6 py-5 font-mono text-sm space-y-1">
              <div className="flex justify-between text-gray-500">
                <span>Folio:</span>
                <span className="font-semibold text-gray-800">{completedOrder.folio}</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>Fecha:</span>
                <span>{completedOrder.timestamp.toLocaleString('es-MX')}</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>Cajero:</span>
                <span>{completedOrder.cashierName}</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>Tipo:</span>
                <span>{orderTypeLabel[completedOrder.type]}</span>
              </div>
              {completedOrder.domicilio && (
                <>
                  <div className="flex justify-between text-gray-500 gap-4">
                    <span>Cliente:</span>
                    <span className="text-right">{completedOrder.domicilio.nombreCliente}</span>
                  </div>
                  <div className="flex justify-between text-gray-500 gap-4">
                    <span>Teléfono:</span>
                    <span className="text-right">{completedOrder.domicilio.telefono}</span>
                  </div>
                </>
              )}

              <div className="border-t border-dashed border-gray-200 my-3" />

              <div className="space-y-2">
                {completedOrder.items.map((item, i) => (
                  <div key={`${item.idProducto}-${i}`}>
                    <div className="flex justify-between">
                      <span className="text-gray-800">
                        {item.nombre} <span className="text-gray-400">x{item.cantidad}</span>
                      </span>
                      <span className="font-semibold">${Number(item.subtotal ?? 0).toFixed(2)}</span>
                    </div>
                    {(item.modificadores ?? []).map((modifier, j) => (
                      <div key={`${modifier.idModificador}-${j}`} className="text-xs text-orange-500 pl-3">
                        + {modifier.nombreSnapshot} ${Number(modifier.precioExtraSnapshot ?? 0).toFixed(2)}
                      </div>
                    ))}
                  </div>
                ))}
              </div>

              <div className="border-t border-dashed border-gray-200 my-3" />
              {completedOrder.totalDiscount > 0 && (
                <div className="flex justify-between text-gray-500">
                  <span>Descuento:</span>
                  <span>-${completedOrder.totalDiscount.toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between text-base font-bold text-gray-900 pt-1">
                <span>TOTAL:</span>
                <span>${completedOrder.total.toFixed(2)} MXN</span>
              </div>

              <div className="border-t border-dashed border-gray-200 my-3" />
              <div className="flex justify-between text-gray-500">
                <span>Método:</span>
                <span>{paymentLabel[completedOrder.paymentMethod]}</span>
              </div>
              {completedOrder.paymentMethod === 'cash' && (
                <>
                  <div className="flex justify-between text-gray-500">
                    <span>Recibido:</span>
                    <span>
                      {currencySymbol[completedOrder.currency]}
                      {completedOrder.amountReceived.toFixed(2)}
                      {completedOrder.currency !== 'MXN' ? ` ${completedOrder.currency}` : ''}
                    </span>
                  </div>
                  <div className="flex justify-between text-green-600 font-semibold">
                    <span>Cambio:</span>
                    <span>${completedOrder.changeAmount.toFixed(2)} MXN</span>
                  </div>
                </>
              )}
            </div>
          </div>

          <div className="flex flex-col gap-3">
            <button
              onClick={() => printTicket(completedOrder)}
              className="flex items-center justify-center gap-2 w-full bg-gradient-to-r from-orange-500 to-orange-600 text-white py-3 rounded-xl font-semibold hover:from-orange-600 hover:to-orange-700 transition-all shadow-lg"
            >
              <Printer className="w-5 h-5" />
              Imprimir Ticket
            </button>
            <button
              onClick={() => navigate('/pos')}
              className="w-full bg-gray-100 text-gray-700 py-3 rounded-xl font-semibold hover:bg-gray-200 transition-all"
            >
              Nuevo Pedido
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  if (step === 'type') {
    return (
      <MainLayout>
        <div className="p-8 max-w-4xl mx-auto">
          <button onClick={() => navigate('/pos')} className="flex items-center gap-2 text-gray-600 hover:text-gray-800 mb-6">
            <ArrowLeft className="w-5 h-5" />
            Volver
          </button>

          <h1 className="text-3xl font-bold text-gray-800 mb-2">Tipo de Pedido</h1>
          <p className="text-gray-600 mb-8">Selecciona cómo será entregado el pedido</p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {([
              { id: 'dine-in', label: 'Consumir en Local', sub: 'Venta en mostrador', Icon: Home },
              { id: 'takeout', label: 'Para Llevar', sub: 'Entrega rápida', Icon: ShoppingBag },
              { id: 'delivery', label: 'A Domicilio', sub: `+$${deliveryFee} envío`, Icon: Truck },
            ] as const).map(({ id, label, sub, Icon }) => (
              <button
                key={id}
                onClick={() => setOrderType(id)}
                className={`p-8 rounded-2xl border-2 transition-all ${
                  orderType === id ? 'border-orange-500 bg-orange-50 shadow-lg' : 'border-gray-200 bg-white hover:border-gray-300'
                }`}
              >
                <Icon className={`w-16 h-16 mx-auto mb-4 ${orderType === id ? 'text-orange-500' : 'text-gray-400'}`} />
                <h3 className="text-xl font-bold text-gray-800 mb-2">{label}</h3>
                <p className="text-sm text-gray-600">{sub}</p>
              </button>
            ))}
          </div>

          {orderType === 'delivery' && (
            <div className="mb-8 p-6 bg-white rounded-xl shadow-md space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Cliente</label>
                <input
                  type="text"
                  value={deliveryData.nombreCliente}
                  onChange={(e) => setDeliveryData((prev) => ({ ...prev, nombreCliente: e.target.value }))}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Teléfono</label>
                <input
                  type="text"
                  value={deliveryData.telefono}
                  onChange={(e) => setDeliveryData((prev) => ({ ...prev, telefono: e.target.value }))}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Dirección</label>
                <textarea
                  value={deliveryData.direccion}
                  onChange={(e) => setDeliveryData((prev) => ({ ...prev, direccion: e.target.value }))}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none min-h-24"
                />
              </div>
            </div>
          )}

          <div className="bg-white rounded-xl shadow-md p-6 mb-6">
            <h3 className="font-semibold text-gray-800 mb-4">Resumen del Pedido</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Subtotal:</span>
                <span className="font-semibold">${subtotal.toFixed(2)}</span>
              </div>
              {deliveryFee > 0 && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Envío:</span>
                  <span className="font-semibold">${deliveryFee.toFixed(2)}</span>
                </div>
              )}
              <div className="border-t pt-2 flex justify-between text-xl font-bold">
                <span>Total visual:</span>
                <span className="text-orange-600">${visualTotal.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {error && <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

          <button
            onClick={() => orderType && setStep('payment')}
            disabled={!orderType}
            className="w-full bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-4 rounded-xl font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Continuar a Pago
          </button>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="p-8 max-w-4xl mx-auto">
        <button onClick={() => setStep('type')} className="flex items-center gap-2 text-gray-600 hover:text-gray-800 mb-6">
          <ArrowLeft className="w-5 h-5" />
          Volver
        </button>

        <h1 className="text-3xl font-bold text-gray-800 mb-2">Método de Pago</h1>
        <p className="text-gray-600 mb-8">Selecciona cómo se realizará el pago</p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          {([
            { id: 'cash', label: 'Efectivo', Icon: DollarSign },
            { id: 'card', label: 'Tarjeta', Icon: CreditCard },
            { id: 'transfer', label: 'Transferencia', Icon: Smartphone },
          ] as const).map(({ id, label, Icon }) => (
            <button
              key={id}
              onClick={() => setPaymentMethod(id)}
              className={`p-8 rounded-2xl border-2 transition-all ${
                paymentMethod === id ? 'border-orange-500 bg-orange-50 shadow-lg' : 'border-gray-200 bg-white hover:border-gray-300'
              }`}
            >
              <Icon className={`w-16 h-16 mx-auto mb-4 ${paymentMethod === id ? 'text-orange-500' : 'text-gray-400'}`} />
              <h3 className="text-xl font-bold text-gray-800">{label}</h3>
            </button>
          ))}
        </div>

        {paymentMethod === 'cash' && (
          <div className="mb-8 p-6 bg-white rounded-xl shadow-md">
            <label className="block text-sm font-medium text-gray-700 mb-2">Moneda</label>
            <div className="grid grid-cols-3 gap-3 mb-4">
              {(['MXN', 'USD', 'GTQ'] as Currency[]).map((curr) => (
                <button
                  key={curr}
                  onClick={() => setCurrency(curr)}
                  className={`px-4 py-2 rounded-lg font-medium transition-all ${
                    currency === curr ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  {curr}
                </button>
              ))}
            </div>

            <label className="block text-sm font-medium text-gray-700 mb-2">Total a pagar ({currency})</label>
            <div className="text-3xl font-bold text-orange-600 mb-4">
              {currencySymbol[currency]}
              {totalInCurrency.toFixed(2)}
            </div>

            <label className="block text-sm font-medium text-gray-700 mb-2">Dinero recibido</label>
            <input
              type="number"
              value={amountReceived}
              onChange={(e) => setAmountReceived(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none mb-4"
              placeholder="0.00"
            />

            {received > 0 && (
              <div className="p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-semibold text-gray-700">Cambio estimado (MXN):</span>
                  <span className="text-2xl font-bold text-green-600">${changeInMXN.toFixed(2)}</span>
                </div>
              </div>
            )}
          </div>
        )}

        <div className="bg-white rounded-xl shadow-md p-6 mb-6">
          <h3 className="font-semibold text-gray-800 mb-4">Resumen del Pedido</h3>
          <div className="space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-600">Subtotal:</span>
              <span className="font-semibold">${subtotal.toFixed(2)}</span>
            </div>
            {deliveryFee > 0 && (
              <div className="flex justify-between">
                <span className="text-gray-600">Envío:</span>
                <span className="font-semibold">${deliveryFee.toFixed(2)}</span>
              </div>
            )}
            <div className="border-t pt-2 flex justify-between text-xl font-bold">
              <span>Total visual:</span>
              <span className="text-orange-600">${visualTotal.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {error && <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

        <button
          onClick={handleCompleteOrder}
          disabled={submitting || !paymentMethod || (paymentMethod === 'cash' && receivedInMXN < visualTotal)}
          className="w-full bg-gradient-to-r from-green-500 to-emerald-500 text-white py-4 rounded-xl font-semibold hover:from-green-600 hover:to-emerald-600 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {submitting ? 'Procesando...' : 'Completar Pedido'}
        </button>
      </div>
    </MainLayout>
  );
}
