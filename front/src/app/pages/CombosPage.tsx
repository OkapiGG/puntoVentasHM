import { useEffect, useMemo, useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { Plus, Edit, Trash2, Layers, Lock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';

interface AdminProductApi {
  idProducto: number;
  nombre: string;
  precio: number;
}

interface AdminPromotionApi {
  idPromocion: number;
  nombre: string;
  descripcion?: string | null;
  tipoRegla: string;
  tipoObjetivo: string;
  idsObjetivo: string;
  fechaInicio: string;
  fechaFin: string;
  montoDescuento?: number | null;
  porcentajeDescuento?: number | null;
  activa: boolean;
}

interface AdminCatalogApi {
  productos: AdminProductApi[];
  promociones: AdminPromotionApi[];
}

const today = new Date().toISOString().slice(0, 10);

export function CombosPage() {
  const { user } = useAuth();
  const [catalog, setCatalog] = useState<AdminCatalogApi>({ productos: [], promociones: [] });
  const [showModal, setShowModal] = useState(false);
  const [editingCombo, setEditingCombo] = useState<AdminPromotionApi | null>(null);
  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    selectedIds: [] as string[],
    comboPrice: '',
    fechaInicio: today,
    fechaFin: today,
    activa: true,
  });

  const canManage = user?.role === 'admin' || user?.role === 'manager';

  const loadCatalog = async () => {
    if (!user?.idNegocio || !canManage) return;
    const response = await apiRequest<AdminCatalogApi>(`/admin/negocios/${user.idNegocio}/catalogo`);
    setCatalog(response);
  };

  useEffect(() => {
    void loadCatalog();
  }, [user?.idNegocio, canManage]);

  const combos = useMemo(
    () => catalog.promociones.filter((promotion) => promotion.tipoRegla === 'COMBO'),
    [catalog.promociones]
  );

  const getSelectedProducts = (ids: string[]) =>
    catalog.productos.filter((product) => ids.includes(String(product.idProducto)));

  const computeComboBase = (ids: string[]) =>
    getSelectedProducts(ids).reduce((sum, product) => sum + Number(product.precio ?? 0), 0);

  const computeDisplayedPrice = (combo: AdminPromotionApi) => {
    const ids = combo.idsObjetivo.split(',').map((value) => value.trim()).filter(Boolean);
    const subtotal = computeComboBase(ids);
    if (combo.montoDescuento != null) {
      return Math.max(subtotal - Number(combo.montoDescuento), 0);
    }
    if (combo.porcentajeDescuento != null) {
      return subtotal - subtotal * (Number(combo.porcentajeDescuento) / 100);
    }
    return subtotal;
  };

  const resetForm = () => {
    setEditingCombo(null);
    setFormData({
      nombre: '',
      descripcion: '',
      selectedIds: [],
      comboPrice: '',
      fechaInicio: today,
      fechaFin: today,
      activa: true,
    });
  };

  const handleEdit = (combo: AdminPromotionApi) => {
    const selectedIds = combo.idsObjetivo.split(',').map((value) => value.trim()).filter(Boolean);
    setEditingCombo(combo);
    setFormData({
      nombre: combo.nombre,
      descripcion: combo.descripcion ?? '',
      selectedIds,
      comboPrice: String(computeDisplayedPrice(combo).toFixed(2)),
      fechaInicio: combo.fechaInicio,
      fechaFin: combo.fechaFin,
      activa: combo.activa,
    });
    setShowModal(true);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!user?.idNegocio) return;

    const subtotal = computeComboBase(formData.selectedIds);
    const comboPrice = Number(formData.comboPrice);
    const discount = Math.max(subtotal - comboPrice, 0);

    const payload = {
      nombre: formData.nombre,
      descripcion: formData.descripcion || null,
      tipoRegla: 'COMBO',
      tipoObjetivo: 'COMBO',
      idsObjetivo: formData.selectedIds.join(','),
      idModificadorGratis: null,
      fechaInicio: formData.fechaInicio,
      fechaFin: formData.fechaFin,
      horaInicio: null,
      horaFin: null,
      porcentajeDescuento: null,
      montoDescuento: discount,
      cantidadMinima: null,
      activa: formData.activa,
    };

    if (editingCombo) {
      await apiRequest(`/admin/negocios/${user.idNegocio}/promociones/${editingCombo.idPromocion}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });
    } else {
      await apiRequest(`/admin/negocios/${user.idNegocio}/promociones`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    }

    setShowModal(false);
    resetForm();
    await loadCatalog();
  };

  const handleDelete = async (idPromocion: number) => {
    if (!user?.idNegocio) return;
    await apiRequest(`/admin/negocios/${user.idNegocio}/promociones/${idPromocion}`, {
      method: 'DELETE',
    });
    await loadCatalog();
  };

  if (!canManage) {
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

  const activeCombos = combos.filter((combo) => combo.activa);
  const inactiveCombos = combos.filter((combo) => !combo.activa);
  const selectedBase = computeComboBase(formData.selectedIds);

  return (
    <MainLayout>
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Administración de Combos</h1>
            <p className="text-gray-600 mt-1">Los combos se guardan como promociones de tipo COMBO.</p>
          </div>
          <button
            onClick={() => {
              resetForm();
              setShowModal(true);
            }}
            className="flex items-center gap-2 bg-gradient-to-r from-orange-500 to-yellow-500 text-white px-6 py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
          >
            <Plus className="w-5 h-5" />
            Crear Combo
          </button>
        </div>

        {[{ title: 'Combos Activos', items: activeCombos }, { title: 'Combos Inactivos', items: inactiveCombos }].map((group) => (
          <div key={group.title} className="mb-8">
            <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              <Layers className="w-6 h-6 text-orange-500" />
              {group.title}
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {group.items.map((combo) => {
                const ids = combo.idsObjetivo.split(',').map((value) => value.trim()).filter(Boolean);
                const products = getSelectedProducts(ids);
                const comboPrice = computeDisplayedPrice(combo);
                return (
                  <div key={combo.idPromocion} className="bg-white rounded-xl p-6 shadow-md hover:shadow-lg transition-all">
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <h3 className="text-lg font-bold text-gray-800">{combo.nombre}</h3>
                        <span className={`inline-block px-2 py-1 text-xs rounded-full mt-1 ${combo.activa ? 'bg-blue-100 text-blue-700' : 'bg-gray-200 text-gray-700'}`}>
                          {combo.activa ? 'Activo' : 'Inactivo'}
                        </span>
                      </div>
                      <div className="flex gap-2">
                        <button onClick={() => handleEdit(combo)} className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                          <Edit className="w-5 h-5" />
                        </button>
                        <button onClick={() => void handleDelete(combo.idPromocion)} className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors">
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    </div>
                    <ul className="space-y-1 mb-4">
                      {products.map((product) => (
                        <li key={product.idProducto} className="text-sm text-gray-600">• {product.nombre}</li>
                      ))}
                    </ul>
                    <p className="text-sm text-gray-500 mb-1">Precio lista: ${computeComboBase(ids).toFixed(2)}</p>
                    <p className="text-2xl font-bold text-orange-600">${comboPrice.toFixed(2)}</p>
                  </div>
                );
              })}
            </div>
          </div>
        ))}

        {showModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-2xl w-full p-6 max-h-[90vh] overflow-y-auto">
              <h2 className="text-2xl font-bold text-gray-800 mb-6">
                {editingCombo ? 'Editar Combo' : 'Crear Combo'}
              </h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Nombre del combo</label>
                    <input
                      type="text"
                      value={formData.nombre}
                      onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Precio final del combo</label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      max={selectedBase || undefined}
                      value={formData.comboPrice}
                      onChange={(e) => setFormData({ ...formData, comboPrice: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Descripción</label>
                  <textarea
                    value={formData.descripcion}
                    onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    rows={2}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Productos del combo</label>
                  <div className="grid grid-cols-2 gap-2 max-h-52 overflow-y-auto border border-gray-200 rounded-lg p-3">
                    {catalog.productos.map((product) => {
                      const checked = formData.selectedIds.includes(String(product.idProducto));
                      return (
                        <label key={product.idProducto} className="flex items-center gap-2 text-sm text-gray-700">
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={(e) =>
                              setFormData((current) => ({
                                ...current,
                                selectedIds: e.target.checked
                                  ? [...current.selectedIds, String(product.idProducto)]
                                  : current.selectedIds.filter((id) => id !== String(product.idProducto)),
                              }))
                            }
                          />
                          {product.nombre} (${Number(product.precio).toFixed(2)})
                        </label>
                      );
                    })}
                  </div>
                  <p className="text-sm text-gray-500 mt-2">Precio lista del combo: ${selectedBase.toFixed(2)}</p>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Fecha de inicio</label>
                    <input
                      type="date"
                      value={formData.fechaInicio}
                      onChange={(e) => setFormData({ ...formData, fechaInicio: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Fecha de fin</label>
                    <input
                      type="date"
                      value={formData.fechaFin}
                      onChange={(e) => setFormData({ ...formData, fechaFin: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      required
                    />
                  </div>
                </div>

                <label className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={formData.activa}
                    onChange={(e) => setFormData({ ...formData, activa: e.target.checked })}
                  />
                  Activo
                </label>

                <div className="flex gap-3 pt-4">
                  <button
                    type="submit"
                    className="flex-1 bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
                  >
                    {editingCombo ? 'Guardar Cambios' : 'Crear Combo'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowModal(false);
                      resetForm();
                    }}
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
