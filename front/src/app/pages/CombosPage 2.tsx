import { useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { Plus, Edit, Trash2, Layers } from 'lucide-react';

interface Combo {
  id: string;
  name: string;
  type: 'fixed' | 'promotion';
  products: string[];
  price: number;
}

export function CombosPage() {
  const [combos, setCombos] = useState<Combo[]>([
    {
      id: '1',
      name: 'Combo Familiar',
      type: 'fixed',
      products: ['4 Hamburguesas', '2 Papas Fritas', '4 Bebidas'],
      price: 299,
    },
    {
      id: '2',
      name: 'Combo Individual',
      type: 'fixed',
      products: ['1 Hamburguesa', 'Papas Fritas', 'Bebida'],
      price: 129,
    },
    {
      id: '3',
      name: 'Martes 2x1',
      type: 'promotion',
      products: ['2 Hamburguesas', '1 Papas Fritas'],
      price: 99,
    },
  ]);

  const [showModal, setShowModal] = useState(false);
  const [editingCombo, setEditingCombo] = useState<Combo | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    type: 'fixed' as 'fixed' | 'promotion',
    products: '',
    price: '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const productsArray = formData.products.split(',').map((p) => p.trim());

    if (editingCombo) {
      setCombos(
        combos.map((c) =>
          c.id === editingCombo.id
            ? {
                ...c,
                name: formData.name,
                type: formData.type,
                products: productsArray,
                price: parseFloat(formData.price),
              }
            : c
        )
      );
    } else {
      setCombos([
        ...combos,
        {
          id: Date.now().toString(),
          name: formData.name,
          type: formData.type,
          products: productsArray,
          price: parseFloat(formData.price),
        },
      ]);
    }

    setShowModal(false);
    setEditingCombo(null);
    setFormData({ name: '', type: 'fixed', products: '', price: '' });
  };

  const handleEdit = (combo: Combo) => {
    setEditingCombo(combo);
    setFormData({
      name: combo.name,
      type: combo.type,
      products: combo.products.join(', '),
      price: combo.price.toString(),
    });
    setShowModal(true);
  };

  const handleDelete = (id: string) => {
    setCombos(combos.filter((c) => c.id !== id));
  };

  const fixedCombos = combos.filter((c) => c.type === 'fixed');
  const promotionCombos = combos.filter((c) => c.type === 'promotion');

  return (
    <MainLayout>
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Administración de Combos</h1>
            <p className="text-gray-600 mt-1">Gestiona combos fijos y promocionales</p>
          </div>
          <button
            onClick={() => {
              setEditingCombo(null);
              setFormData({ name: '', type: 'fixed', products: '', price: '' });
              setShowModal(true);
            }}
            className="flex items-center gap-2 bg-gradient-to-r from-orange-500 to-yellow-500 text-white px-6 py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
          >
            <Plus className="w-5 h-5" />
            Crear Combo
          </button>
        </div>

        <div className="mb-8">
          <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
            <Layers className="w-6 h-6 text-orange-500" />
            Combos Fijos
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {fixedCombos.map((combo) => (
              <div key={combo.id} className="bg-white rounded-xl p-6 shadow-md hover:shadow-lg transition-all">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-lg font-bold text-gray-800">{combo.name}</h3>
                    <span className="inline-block px-2 py-1 bg-blue-100 text-blue-700 text-xs rounded-full mt-1">
                      Fijo
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleEdit(combo)}
                      className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
                    >
                      <Edit className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(combo.id)}
                      className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
                <ul className="space-y-1 mb-4">
                  {combo.products.map((product, index) => (
                    <li key={index} className="text-sm text-gray-600">
                      • {product}
                    </li>
                  ))}
                </ul>
                <p className="text-2xl font-bold text-orange-600">${combo.price}</p>
              </div>
            ))}
          </div>
        </div>

        <div>
          <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
            <Layers className="w-6 h-6 text-purple-500" />
            Combos Promocionales
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {promotionCombos.map((combo) => (
              <div key={combo.id} className="bg-gradient-to-br from-purple-50 to-pink-50 border border-purple-200 rounded-xl p-6 shadow-md hover:shadow-lg transition-all">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-lg font-bold text-gray-800">{combo.name}</h3>
                    <span className="inline-block px-2 py-1 bg-purple-100 text-purple-700 text-xs rounded-full mt-1">
                      Promoción
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleEdit(combo)}
                      className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
                    >
                      <Edit className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(combo.id)}
                      className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
                <ul className="space-y-1 mb-4">
                  {combo.products.map((product, index) => (
                    <li key={index} className="text-sm text-gray-600">
                      • {product}
                    </li>
                  ))}
                </ul>
                <p className="text-2xl font-bold text-purple-600">${combo.price}</p>
              </div>
            ))}
          </div>
        </div>

        {showModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-6">
                {editingCombo ? 'Editar Combo' : 'Crear Combo'}
              </h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre del Combo
                  </label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tipo de Combo
                  </label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value as 'fixed' | 'promotion' })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  >
                    <option value="fixed">Fijo</option>
                    <option value="promotion">Promoción</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Productos (separados por coma)
                  </label>
                  <textarea
                    value={formData.products}
                    onChange={(e) => setFormData({ ...formData, products: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    rows={3}
                    placeholder="Ej: 2 Hamburguesas, Papas Fritas, 2 Bebidas"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Precio del Combo
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.price}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
                <div className="flex gap-3 pt-4">
                  <button
                    type="submit"
                    className="flex-1 bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
                  >
                    {editingCombo ? 'Guardar Cambios' : 'Crear Combo'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition-all"
                  >
                    Cancelar
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </MainLayout>
  );
}
