import { useState } from 'react';
import { Product, ExtraIngredient } from '../context/POSContext';
import { X, Plus, Minus } from 'lucide-react';

interface ExtraIngredientsModalProps {
  product: Product;
  availableExtras: ExtraIngredient[];
  onConfirm: (extras: ExtraIngredient[]) => void;
  onCancel: () => void;
}

export function ExtraIngredientsModal({
  product,
  availableExtras,
  onConfirm,
  onCancel,
}: ExtraIngredientsModalProps) {
  const [selectedExtras, setSelectedExtras] = useState<ExtraIngredient[]>([]);

  const toggleExtra = (extra: ExtraIngredient) => {
    if (selectedExtras.find((e) => e.id === extra.id)) {
      setSelectedExtras(selectedExtras.filter((e) => e.id !== extra.id));
    } else {
      setSelectedExtras([...selectedExtras, extra]);
    }
  };

  const total = product.price + selectedExtras.reduce((sum, e) => sum + e.price, 0);

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-2xl font-bold text-gray-800">Ingredientes Extra</h2>
          <button
            onClick={onCancel}
            className="text-gray-500 hover:bg-gray-100 p-2 rounded-lg transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="p-6">
          <div className="mb-6 p-4 bg-gradient-to-r from-orange-50 to-yellow-50 rounded-lg">
            <h3 className="font-semibold text-gray-800 mb-1">{product.name}</h3>
            <p className="text-lg font-bold text-orange-600">${product.price}</p>
          </div>

          <h3 className="font-semibold text-gray-700 mb-3">Selecciona ingredientes extra:</h3>
          <div className="space-y-2 mb-6">
            {availableExtras.map((extra) => {
              const isSelected = selectedExtras.find((e) => e.id === extra.id);
              return (
                <button
                  key={extra.id}
                  onClick={() => toggleExtra(extra)}
                  className={`w-full flex items-center justify-between p-4 rounded-lg border-2 transition-all ${
                    isSelected
                      ? 'border-orange-500 bg-orange-50'
                      : 'border-gray-200 bg-white hover:border-gray-300'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all ${
                        isSelected
                          ? 'border-orange-500 bg-orange-500'
                          : 'border-gray-300'
                      }`}
                    >
                      {isSelected && <Plus className="w-4 h-4 text-white" />}
                    </div>
                    <span className="font-medium text-gray-800">{extra.name}</span>
                  </div>
                  <span className="font-semibold text-orange-600">+${extra.price}</span>
                </button>
              );
            })}
          </div>

          <div className="border-t pt-4 mb-4">
            <div className="flex justify-between items-center mb-2">
              <span className="text-gray-700">Producto base:</span>
              <span className="font-semibold">${product.price}</span>
            </div>
            {selectedExtras.length > 0 && (
              <div className="space-y-1 mb-2">
                {selectedExtras.map((extra) => (
                  <div key={extra.id} className="flex justify-between items-center text-sm">
                    <span className="text-gray-600">{extra.name}:</span>
                    <span className="text-orange-600">+${extra.price}</span>
                  </div>
                ))}
              </div>
            )}
            <div className="flex justify-between items-center text-xl font-bold">
              <span>Total:</span>
              <span className="text-orange-600">${total.toFixed(2)}</span>
            </div>
          </div>

          <button
            onClick={() => onConfirm(selectedExtras)}
            className="w-full bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
          >
            Agregar al Pedido
          </button>
        </div>
      </div>
    </div>
  );
}
