import { useState } from 'react';
import { useNavigate } from 'react-router';
import { MainLayout } from '../components/MainLayout';
import { usePOS, Product, ExtraIngredient } from '../context/POSContext';
import { useShift } from '../context/ShiftContext';
import { Plus, Trash2, ShoppingCart, Lock } from 'lucide-react';
import { ExtraIngredientsModal } from '../components/ExtraIngredientsModal';

export function POSPage() {
  const { categories, products, cart, addToCart, removeFromCart, loadingProducts } = usePOS();
  const { currentShift } = useShift();
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [showExtrasModal, setShowExtrasModal] = useState(false);
  const navigate = useNavigate();

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

  return (
    <MainLayout>
      <div className="flex h-full">
        <div className="flex-1 p-6 overflow-y-auto">
          <h1 className="text-2xl font-bold text-gray-800 mb-6">Nuevo Pedido</h1>

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

          <div className="flex-1 overflow-y-auto mb-4">
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
            <button
              onClick={handleCheckout}
              disabled={cart.length === 0}
              className="w-full bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Proceder a Pago
            </button>
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
    </MainLayout>
  );
}
