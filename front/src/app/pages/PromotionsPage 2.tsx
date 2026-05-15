import { useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { Plus, Edit, Trash2, Tag, ToggleLeft, ToggleRight } from 'lucide-react';

interface Promotion {
  id: string;
  name: string;
  products: string[];
  price: number;
  startDate: string;
  endDate: string;
  active: boolean;
}

export function PromotionsPage() {
  const [promotions, setPromotions] = useState<Promotion[]>([
    {
      id: '1',
      name: 'Combo Cumpleaños',
      products: ['Hamburguesa Premium', 'Papas Fritas', 'Helado', 'Bebida Grande'],
      price: 189,
      startDate: '2026-05-01',
      endDate: '2026-05-31',
      active: true,
    },
    {
      id: '2',
      name: 'Día de las Madres',
      products: ['2 Hamburguesas Especiales', '2 Postres', '2 Bebidas'],
      price: 249,
      startDate: '2026-05-10',
      endDate: '2026-05-12',
      active: true,
    },
  ]);

  const [showModal, setShowModal] = useState(false);
  const [editingPromotion, setEditingPromotion] = useState<Promotion | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    products: '',
    price: '',
    startDate: '',
    endDate: '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const productsArray = formData.products.split(',').map((p) => p.trim());

    if (editingPromotion) {
      setPromotions(
        promotions.map((p) =>
          p.id === editingPromotion.id
            ? {
                ...p,
                name: formData.name,
                products: productsArray,
                price: parseFloat(formData.price),
                startDate: formData.startDate,
                endDate: formData.endDate,
              }
            : p
        )
      );
    } else {
      setPromotions([
        ...promotions,
        {
          id: Date.now().toString(),
          name: formData.name,
          products: productsArray,
          price: parseFloat(formData.price),
          startDate: formData.startDate,
          endDate: formData.endDate,
          active: false,
        },
      ]);
    }

    setShowModal(false);
    setEditingPromotion(null);
    setFormData({ name: '', products: '', price: '', startDate: '', endDate: '' });
  };

  const handleEdit = (promotion: Promotion) => {
    setEditingPromotion(promotion);
    setFormData({
      name: promotion.name,
      products: promotion.products.join(', '),
      price: promotion.price.toString(),
      startDate: promotion.startDate,
      endDate: promotion.endDate,
    });
    setShowModal(true);
  };

  const handleDelete = (id: string) => {
    setPromotions(promotions.filter((p) => p.id !== id));
  };

  const toggleActive = (id: string) => {
    setPromotions(promotions.map((p) => (p.id === id ? { ...p, active: !p.active } : p)));
  };

  const activePromotions = promotions.filter((p) => p.active);
  const inactivePromotions = promotions.filter((p) => !p.active);

  return (
    <MainLayout>
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Promociones</h1>
            <p className="text-gray-600 mt-1">Gestiona promociones temporales</p>
          </div>
          <button
            onClick={() => {
              setEditingPromotion(null);
              setFormData({ name: '', products: '', price: '', startDate: '', endDate: '' });
              setShowModal(true);
            }}
            className="flex items-center gap-2 bg-gradient-to-r from-orange-500 to-yellow-500 text-white px-6 py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
          >
            <Plus className="w-5 h-5" />
            Nueva Promoción
          </button>
        </div>

        <div className="mb-8">
          <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
            <Tag className="w-6 h-6 text-green-500" />
            Promociones Activas
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {activePromotions.map((promotion) => (
              <div
                key={promotion.id}
                className="bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200 rounded-xl p-6 shadow-md hover:shadow-lg transition-all"
              >
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-lg font-bold text-gray-800">{promotion.name}</h3>
                    <span className="inline-block px-2 py-1 bg-green-500 text-white text-xs rounded-full mt-1">
                      Activa
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => toggleActive(promotion.id)}
                      className="p-2 text-green-600 hover:bg-green-100 rounded-lg transition-colors"
                    >
                      <ToggleRight className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleEdit(promotion)}
                      className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
                    >
                      <Edit className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(promotion.id)}
                      className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
                <ul className="space-y-1 mb-4">
                  {promotion.products.map((product, index) => (
                    <li key={index} className="text-sm text-gray-600">
                      • {product}
                    </li>
                  ))}
                </ul>
                <p className="text-2xl font-bold text-green-600 mb-3">${promotion.price}</p>
                <div className="text-xs text-gray-600">
                  <p>Desde: {new Date(promotion.startDate).toLocaleDateString()}</p>
                  <p>Hasta: {new Date(promotion.endDate).toLocaleDateString()}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div>
          <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
            <Tag className="w-6 h-6 text-gray-400" />
            Promociones Inactivas
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {inactivePromotions.map((promotion) => (
              <div key={promotion.id} className="bg-white rounded-xl p-6 shadow-md hover:shadow-lg transition-all opacity-75">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-lg font-bold text-gray-800">{promotion.name}</h3>
                    <span className="inline-block px-2 py-1 bg-gray-300 text-gray-700 text-xs rounded-full mt-1">
                      Inactiva
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => toggleActive(promotion.id)}
                      className="p-2 text-gray-500 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                      <ToggleLeft className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleEdit(promotion)}
                      className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
                    >
                      <Edit className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(promotion.id)}
                      className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
                <ul className="space-y-1 mb-4">
                  {promotion.products.map((product, index) => (
                    <li key={index} className="text-sm text-gray-600">
                      • {product}
                    </li>
                  ))}
                </ul>
                <p className="text-2xl font-bold text-gray-600 mb-3">${promotion.price}</p>
                <div className="text-xs text-gray-600">
                  <p>Desde: {new Date(promotion.startDate).toLocaleDateString()}</p>
                  <p>Hasta: {new Date(promotion.endDate).toLocaleDateString()}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {showModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-6">
                {editingPromotion ? 'Editar Promoción' : 'Nueva Promoción'}
              </h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre de la Promoción
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
                    Productos Incluidos (separados por coma)
                  </label>
                  <textarea
                    value={formData.products}
                    onChange={(e) => setFormData({ ...formData, products: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    rows={3}
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Precio Promocional
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
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Fecha de Inicio
                    </label>
                    <input
                      type="date"
                      value={formData.startDate}
                      onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Fecha de Fin
                    </label>
                    <input
                      type="date"
                      value={formData.endDate}
                      onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      required
                    />
                  </div>
                </div>
                <div className="flex gap-3 pt-4">
                  <button
                    type="submit"
                    className="flex-1 bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
                  >
                    {editingPromotion ? 'Guardar Cambios' : 'Crear Promoción'}
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
