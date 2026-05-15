import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { MainLayout } from '../components/MainLayout';
import { usePOS, Product, ExtraIngredient } from '../context/POSContext';
import { useShift } from '../context/ShiftContext';
import { Plus, Trash2, ShoppingCart, Lock, Ban, RefreshCw, PlusSquare, X } from 'lucide-react';
import { ExtraIngredientsModal } from '../components/ExtraIngredientsModal';
import { apiRequest } from '../lib/api';
import { useAuth } from '../context/AuthContext';

interface AddToOrderState {
  idOrden: number;
  folio: string;
}

interface OrdenActivaApi {
  idOrden: number;
  folio: string;
  fecha: string;
  tipoOrden: string;
  total: number;
  cantidadItems: number;
  estado: string;
  pagada: boolean;
  domicilio?: {
    nombreCliente: string;
    direccion: string;
    telefono: string;
    estadoEntrega: string;
  } | null;
}

export function POSPage() {
  const { categories, products, cart, addToCart, removeFromCart, loadingProducts, clearCart } = usePOS();
  const { currentShift } = useShift();
  const { user } = useAuth();
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [showExtrasModal, setShowExtrasModal] = useState(false);
  const [activeOrders, setActiveOrders] = useState<OrdenActivaApi[]>([]);
  const [loadingActiveOrders, setLoadingActiveOrders] = useState(false);
  const [cancelTarget, setCancelTarget] = useState<OrdenActivaApi | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const [activeOrdersError, setActiveOrdersError] = useState('');
  const [submittingAppend, setSubmittingAppend] = useState(false);
  const [appendError, setAppendError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const addToOrder = (location.state as { addToOrder?: AddToOrderState } | null)?.addToOrder ?? null;

  useEffect(() => {
    if (addToOrder) {
      clearCart();
    }
  }, [addToOrder?.idOrden]);

  const categoryOptions = [{ id: 'all', label: 'Todos' }, ...categories.map((category) => ({
    id: category.slug,
    label: category.label,
  }))];

  const filteredProducts =
    selectedCategory === 'all'
      ? products
      : products.filter((p) => p.categorySlug === selectedCategory);

  const handleProductClick = (product: Product) => {
    if (product.modifiers.length > 0) {
      setSelectedProduct(product);
      setShowExtrasModal(true);
    } else {
      addToCart(product);
    }
  };

  const handleAddWithExtras = (extras: ExtraIngredient[]) => {
    if (selectedProduct) {
      addToCart(selectedProduct, extras);
    }
    setShowExtrasModal(false);
    setSelectedProduct(null);
  };

  const subtotal = cart.reduce((sum, item) => {
    const productPrice = item.product.price * item.quantity;
    const extrasPrice = item.extras.reduce((s, e) => s + e.price, 0) * item.quantity;
    return sum + productPrice + extrasPrice;
  }, 0);

  const handleCheckout = () => {
    if (cart.length > 0) {
      navigate('/checkout');
    }
  };

  const handleAppendToOrder = async () => {
    if (!addToOrder || cart.length === 0) return;
    setSubmittingAppend(true);
    setAppendError('');
    try {
      const payload = {
        items: cart.map((item) => ({
          idProducto: item.product.id,
          cantidad: item.quantity,
          modificadores: item.extras.map((extra) => ({ idModificador: extra.id })),
        })),
      };
      await apiRequest(`/ventas/ordenes/${addToOrder.idOrden}/items`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      clearCart();
      navigate('/orders');
    } catch (requestError) {
      setAppendError(requestError instanceof Error ? requestError.message : 'No se pudieron agregar los productos');
    } finally {
      setSubmittingAppend(false);
    }
  };

  const handleCancelAppend = () => {
    clearCart();
    navigate('/orders');
  };

  const loadActiveOrders = async () => {
    if (!user?.idUsuario || !currentShift) {
      return;
    }
    setLoadingActiveOrders(true);
    setActiveOrdersError('');
    try {
      const response = await apiRequest<OrdenActivaApi[]>(`/ventas/usuarios/${user.idUsuario}/activas`);
      setActiveOrders(response);
    } catch (requestError) {
      setActiveOrdersError(requestError instanceof Error ? requestError.message : 'No se pudieron cargar los pedidos activos');
    } finally {
      setLoadingActiveOrders(false);
    }
  };

  useEffect(() => {
    void loadActiveOrders();
  }, [user?.idUsuario, currentShift?.id]);

  const handleCancelActiveOrder = async () => {
    if (!cancelTarget || !cancelReason.trim()) {
      setActiveOrdersError('Debes indicar el motivo de cancelación.');
      return;
    }
    setActiveOrdersError('');
    try {
      await apiRequest(`/ventas/ordenes/${cancelTarget.idOrden}/cancelacion`, {
        method: 'POST',
        body: JSON.stringify({ motivo: cancelReason.trim() }),
      });
      setCancelTarget(null);
      setCancelReason('');
      await loadActiveOrders();
    } catch (requestError) {
      setActiveOrdersError(requestError instanceof Error ? requestError.message : 'No se pudo cancelar la orden');
    }
  };

  if (!currentShift) {
    return (
      <MainLayout>
        <div className="flex h-full items-center justify-center">
          <div className="text-center max-w-sm">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-5">
              <Lock className="w-9 h-9 text-gray-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Sin turno activo</h2>
            <p className="text-gray-500 mb-6">
              Debes abrir un turno antes de poder registrar ventas.
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

  return (
    <MainLayout>
      <div className="flex h-full">
        <div className="flex-1 p-6 overflow-y-auto">
          {addToOrder ? (
            <div className="mb-6 flex items-center justify-between gap-3 rounded-xl border-2 border-amber-300 bg-amber-50 px-5 py-4">
              <div>
                <h1 className="text-xl font-bold text-amber-900">Agregando a orden {addToOrder.folio}</h1>
                <p className="text-sm text-amber-700">Los productos seleccionados se anexarán a la cuenta existente.</p>
              </div>
              <button
                onClick={handleCancelAppend}
                className="inline-flex items-center gap-2 rounded-lg bg-white border border-amber-300 px-3 py-2 text-sm font-semibold text-amber-700 hover:bg-amber-100 transition-colors"
              >
                <X className="w-4 h-4" />
                Cancelar
              </button>
            </div>
          ) : (
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Nuevo Pedido</h1>
          )}

          <div className="flex gap-2 mb-6 overflow-x-auto pb-2">
            {categoryOptions.map((category) => (
              <button
                key={category.id}
                onClick={() => setSelectedCategory(category.id)}
                className={`px-6 py-2 rounded-lg font-medium whitespace-nowrap transition-all ${
                  selectedCategory === category.id
                    ? 'bg-gradient-to-r from-orange-500 to-yellow-500 text-white shadow-lg'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                }`}
              >
                {category.label}
              </button>
            ))}
          </div>

          {loadingProducts ? (
            <div className="bg-white rounded-xl p-8 text-center text-gray-500 shadow-sm">
              Cargando productos...
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {filteredProducts.map((product) => (
                <button
                  key={product.id}
                  onClick={() => handleProductClick(product)}
                  className="bg-white rounded-xl p-4 shadow-md hover:shadow-xl transition-all text-left group"
                >
                  <div className="aspect-square bg-gradient-to-br from-orange-100 to-yellow-100 rounded-lg mb-3 flex items-center justify-center">
                    <ShoppingCart className="w-12 h-12 text-orange-500" />
                  </div>
                  <h3 className="font-semibold text-gray-800 mb-1">{product.name}</h3>
                  <p className="text-sm text-gray-500 mb-1">{product.category}</p>
                  <p className="text-lg font-bold text-orange-600">${product.price.toFixed(2)}</p>
                  <div className="mt-3 flex items-center justify-center gap-2 bg-orange-500 text-white py-2 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity">
                    <Plus className="w-4 h-4" />
                    <span className="text-sm font-medium">Agregar</span>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="w-96 bg-white border-l border-gray-200 p-6 flex flex-col">
          <h2 className="text-xl font-bold text-gray-800 mb-4">Resumen del Pedido</h2>

          <div className="overflow-y-auto mb-4">
            {cart.length === 0 ? (
              <div className="text-center py-12 text-gray-400">
                <ShoppingCart className="w-16 h-16 mx-auto mb-3 opacity-50" />
                <p>No hay productos en el pedido</p>
              </div>
            ) : (
              <div className="space-y-3">
                {cart.map((item, index) => {
                  const extrasTotal = item.extras.reduce((s, e) => s + e.price, 0);
                  const itemTotal = (item.product.price + extrasTotal) * item.quantity;

                  return (
                    <div key={index} className="bg-gray-50 rounded-lg p-3">
                      <div className="flex justify-between items-start mb-2">
                        <div className="flex-1">
                          <h3 className="font-semibold text-gray-800">{item.product.name}</h3>
                          <p className="text-sm text-gray-600">${item.product.price}</p>
                          {item.extras.length > 0 && (
                            <div className="mt-1">
                              {item.extras.map((extra, i) => (
                                <p key={i} className="text-xs text-orange-600">
                                  + {extra.name} (${extra.price})
                                </p>
                              ))}
                            </div>
                          )}
                        </div>
                        <button
                          onClick={() => removeFromCart(index)}
                          className="text-red-500 hover:bg-red-50 p-2 rounded-lg transition-colors"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-600">Cantidad: {item.quantity}</span>
                        <span className="font-bold text-orange-600">${itemTotal.toFixed(2)}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="border-t pt-4 space-y-3">
            <div className="flex justify-between text-gray-700">
              <span>Subtotal:</span>
              <span className="font-semibold">${subtotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-xl font-bold text-gray-800">
              <span>Total:</span>
              <span>${subtotal.toFixed(2)}</span>
            </div>
            {appendError && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-700">
                {appendError}
              </div>
            )}
            {addToOrder ? (
              <button
                onClick={() => void handleAppendToOrder()}
                disabled={cart.length === 0 || submittingAppend}
                className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-amber-500 to-orange-500 text-white py-3 rounded-lg font-semibold hover:from-amber-600 hover:to-orange-600 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <PlusSquare className="w-5 h-5" />
                {submittingAppend ? 'Agregando...' : `Agregar a orden ${addToOrder.folio}`}
              </button>
            ) : (
              <button
                onClick={handleCheckout}
                disabled={cart.length === 0}
                className="w-full bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Proceder a Pago
              </button>
            )}
          </div>

          <div className="border-t mt-6 pt-4 flex-1 min-h-0 flex flex-col">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-lg font-bold text-gray-800">Pedidos activos</h3>
              <button
                onClick={() => void loadActiveOrders()}
                className="p-2 text-gray-500 hover:bg-gray-100 rounded-lg transition-colors"
                title="Actualizar"
              >
                <RefreshCw className="w-4 h-4" />
              </button>
            </div>

            {activeOrdersError && (
              <div className="mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-700">
                {activeOrdersError}
              </div>
            )}

            <div className="flex-1 overflow-y-auto space-y-3 pr-1">
              {loadingActiveOrders ? (
                <div className="text-center py-8 text-sm text-gray-400">Cargando pedidos activos...</div>
              ) : activeOrders.length === 0 ? (
                <div className="text-center py-8 text-sm text-gray-400">No hay pedidos activos.</div>
              ) : (
                activeOrders.map((order) => (
                  <div key={order.idOrden} className="rounded-xl border border-gray-200 bg-gray-50 p-3">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="font-semibold text-gray-800">{order.folio}</p>
                        <p className="text-xs text-gray-500">{new Date(order.fecha).toLocaleString('es-MX')}</p>
                        <p className="text-xs text-gray-500">
                          {order.tipoOrden} · {order.cantidadItems} items
                        </p>
                        {order.domicilio?.nombreCliente && (
                          <p className="text-xs text-orange-600">{order.domicilio.nombreCliente}</p>
                        )}
                      </div>
                      <div className="text-right">
                        <p className="font-bold text-orange-600">${Number(order.total ?? 0).toFixed(2)}</p>
                        <span className="inline-flex rounded-full bg-amber-100 px-2 py-1 text-[11px] font-semibold text-amber-700">
                          {order.estado}
                        </span>
                      </div>
                    </div>

                    {order.estado === 'POR_ACEPTAR' && (
                      <button
                        onClick={() => {
                          setCancelTarget(order);
                          setCancelReason('');
                          setActiveOrdersError('');
                        }}
                        className="mt-3 w-full flex items-center justify-center gap-2 rounded-lg bg-red-50 py-2 text-sm font-semibold text-red-600 hover:bg-red-100 transition-colors"
                      >
                        <Ban className="w-4 h-4" />
                        Cancelar pedido
                      </button>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>

      {showExtrasModal && selectedProduct && (
        <ExtraIngredientsModal
          product={selectedProduct}
          availableExtras={selectedProduct.modifiers}
          onConfirm={handleAddWithExtras}
          onCancel={() => {
            setShowExtrasModal(false);
            setSelectedProduct(null);
          }}
        />
      )}

      {cancelTarget && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-2">Cancelar pedido activo</h2>
            <p className="text-sm text-gray-500 mb-4">
              Folio {cancelTarget.folio}. Esta acción solo aplica antes del cobro.
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
                onClick={() => void handleCancelActiveOrder()}
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
    </MainLayout>
  );
}
