import { useEffect, useMemo, useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { Plus, Edit, Trash2, Tag, ToggleLeft, ToggleRight, Lock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';

interface AdminCategoryApi {
  idCategoria: number;
  nombre: string;
}

interface AdminProductApi {
  idProducto: number;
  nombre: string;
}

interface AdminModifierApi {
  idModificador: number;
  idProducto: number;
  nombre: string;
}

interface AdminPromotionApi {
  idPromocion: number;
  nombre: string;
  descripcion?: string | null;
  tipoRegla: string;
  tipoObjetivo: string;
  idsObjetivo: string;
  idModificadorGratis?: number | null;
  fechaInicio: string;
  fechaFin: string;
  horaInicio?: string | null;
  horaFin?: string | null;
  porcentajeDescuento?: number | null;
  montoDescuento?: number | null;
  cantidadMinima?: number | null;
  activa: boolean;
}

interface AdminCatalogApi {
  categorias: AdminCategoryApi[];
  productos: AdminProductApi[];
  modificadores: AdminModifierApi[];
  promociones: AdminPromotionApi[];
}

const today = new Date().toISOString().slice(0, 10);

export function PromotionsPage() {
  const { user } = useAuth();
  const [catalog, setCatalog] = useState<AdminCatalogApi>({
    categorias: [],
    productos: [],
    modificadores: [],
    promociones: [],
  });
  const [showModal, setShowModal] = useState(false);
  const [editingPromotion, setEditingPromotion] = useState<AdminPromotionApi | null>(null);
  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    tipoRegla: 'PORCENTAJE',
    tipoObjetivo: 'PRODUCTO',
    selectedIds: [] as string[],
    idModificadorGratis: '',
    porcentajeDescuento: '',
    montoDescuento: '',
    cantidadMinima: '',
    fechaInicio: today,
    fechaFin: today,
    horaInicio: '',
    horaFin: '',
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

  const promotions = useMemo(
    () => catalog.promociones.filter((promotion) => promotion.tipoRegla !== 'COMBO'),
    [catalog.promociones]
  );

  const objectiveOptions = formData.tipoObjetivo === 'CATEGORIA' ? catalog.categorias : catalog.productos;

  const resolveObjectiveNames = (idsObjetivo: string, tipoObjetivo: string) => {
    const ids = idsObjetivo
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean);

    const source = tipoObjetivo === 'CATEGORIA' ? catalog.categorias : catalog.productos;
    return ids
      .map((id) => source.find((item) => String('idCategoria' in item ? item.idCategoria : item.idProducto) === id)?.nombre)
      .filter(Boolean) as string[];
  };

  const resetForm = () => {
    setEditingPromotion(null);
    setFormData({
      nombre: '',
      descripcion: '',
      tipoRegla: 'PORCENTAJE',
      tipoObjetivo: 'PRODUCTO',
      selectedIds: [],
      idModificadorGratis: '',
      porcentajeDescuento: '',
      montoDescuento: '',
      cantidadMinima: '',
      fechaInicio: today,
      fechaFin: today,
      horaInicio: '',
      horaFin: '',
      activa: true,
    });
  };

  const handleEdit = (promotion: AdminPromotionApi) => {
    setEditingPromotion(promotion);
    setFormData({
      nombre: promotion.nombre,
      descripcion: promotion.descripcion ?? '',
      tipoRegla: promotion.tipoRegla,
      tipoObjetivo: promotion.tipoObjetivo,
      selectedIds: promotion.idsObjetivo.split(',').map((value) => value.trim()).filter(Boolean),
      idModificadorGratis: promotion.idModificadorGratis ? String(promotion.idModificadorGratis) : '',
      porcentajeDescuento: promotion.porcentajeDescuento != null ? String(promotion.porcentajeDescuento) : '',
      montoDescuento: promotion.montoDescuento != null ? String(promotion.montoDescuento) : '',
      cantidadMinima: promotion.cantidadMinima != null ? String(promotion.cantidadMinima) : '',
      fechaInicio: promotion.fechaInicio,
      fechaFin: promotion.fechaFin,
      horaInicio: promotion.horaInicio ?? '',
      horaFin: promotion.horaFin ?? '',
      activa: promotion.activa,
    });
    setShowModal(true);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!user?.idNegocio) return;

    const payload = {
      nombre: formData.nombre,
      descripcion: formData.descripcion || null,
      tipoRegla: formData.tipoRegla,
      tipoObjetivo: formData.tipoObjetivo,
      idsObjetivo: formData.selectedIds.join(','),
      idModificadorGratis: formData.tipoRegla === 'EXTRA_GRATIS' && formData.idModificadorGratis
        ? Number(formData.idModificadorGratis)
        : null,
      fechaInicio: formData.fechaInicio,
      fechaFin: formData.fechaFin,
      horaInicio: formData.horaInicio || null,
      horaFin: formData.horaFin || null,
      porcentajeDescuento: formData.porcentajeDescuento ? Number(formData.porcentajeDescuento) : null,
      montoDescuento: formData.montoDescuento ? Number(formData.montoDescuento) : null,
      cantidadMinima: formData.cantidadMinima ? Number(formData.cantidadMinima) : null,
      activa: formData.activa,
    };

    if (editingPromotion) {
      await apiRequest(`/admin/negocios/${user.idNegocio}/promociones/${editingPromotion.idPromocion}`, {
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

  const toggleActive = async (promotion: AdminPromotionApi) => {
    if (!user?.idNegocio) return;
    await apiRequest(`/admin/negocios/${user.idNegocio}/promociones/${promotion.idPromocion}`, {
      method: 'PUT',
      body: JSON.stringify({
        nombre: promotion.nombre,
        descripcion: promotion.descripcion ?? null,
        tipoRegla: promotion.tipoRegla,
        tipoObjetivo: promotion.tipoObjetivo,
        idsObjetivo: promotion.idsObjetivo,
        idModificadorGratis: promotion.idModificadorGratis ?? null,
        fechaInicio: promotion.fechaInicio,
        fechaFin: promotion.fechaFin,
        horaInicio: promotion.horaInicio ?? null,
        horaFin: promotion.horaFin ?? null,
        porcentajeDescuento: promotion.porcentajeDescuento ?? null,
        montoDescuento: promotion.montoDescuento ?? null,
        cantidadMinima: promotion.cantidadMinima ?? null,
        activa: !promotion.activa,
      }),
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

  const activePromotions = promotions.filter((promotion) => promotion.activa);
  const inactivePromotions = promotions.filter((promotion) => !promotion.activa);

  return (
    <MainLayout>
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Promociones</h1>
            <p className="text-gray-600 mt-1">Promociones persistidas en backend y aplicadas en venta.</p>
          </div>
          <button
            onClick={() => {
              resetForm();
              setShowModal(true);
            }}
            className="flex items-center gap-2 bg-gradient-to-r from-orange-500 to-yellow-500 text-white px-6 py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
          >
            <Plus className="w-5 h-5" />
            Nueva Promoción
          </button>
        </div>

        {[{ title: 'Promociones Activas', items: activePromotions }, { title: 'Promociones Inactivas', items: inactivePromotions }].map((group) => (
          <div key={group.title} className="mb-8">
            <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              <Tag className="w-6 h-6 text-orange-500" />
              {group.title}
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {group.items.map((promotion) => {
                const objectiveNames = resolveObjectiveNames(promotion.idsObjetivo, promotion.tipoObjetivo);
                return (
                  <div
                    key={promotion.idPromocion}
                    className={`rounded-xl p-6 shadow-md hover:shadow-lg transition-all ${
                      promotion.activa
                        ? 'bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200'
                        : 'bg-white opacity-80'
                    }`}
                  >
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <h3 className="text-lg font-bold text-gray-800">{promotion.nombre}</h3>
                        <span className={`inline-block px-2 py-1 text-xs rounded-full mt-1 ${promotion.activa ? 'bg-green-500 text-white' : 'bg-gray-300 text-gray-700'}`}>
                          {promotion.tipoRegla}
                        </span>
                      </div>
                      <div className="flex gap-2">
                        <button onClick={() => void toggleActive(promotion)} className="p-2 text-green-600 hover:bg-green-100 rounded-lg transition-colors">
                          {promotion.activa ? <ToggleRight className="w-5 h-5" /> : <ToggleLeft className="w-5 h-5" />}
                        </button>
                        <button onClick={() => handleEdit(promotion)} className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                          <Edit className="w-5 h-5" />
                        </button>
                        <button onClick={() => void handleDelete(promotion.idPromocion)} className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors">
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    </div>
                    <p className="text-sm text-gray-600 mb-2">{promotion.descripcion || 'Sin descripción'}</p>
                    <p className="text-sm text-gray-700 mb-1">
                      <strong>Objetivo:</strong> {promotion.tipoObjetivo}
                    </p>
                    <ul className="space-y-1 mb-3">
                      {objectiveNames.map((name) => (
                        <li key={name} className="text-sm text-gray-600">• {name}</li>
                      ))}
                    </ul>
                    <div className="text-sm text-gray-600 space-y-1">
                      {promotion.porcentajeDescuento != null && <p>Descuento: {promotion.porcentajeDescuento}%</p>}
                      {promotion.montoDescuento != null && <p>Monto: ${Number(promotion.montoDescuento).toFixed(2)}</p>}
                      {promotion.cantidadMinima != null && <p>Mínimo: {promotion.cantidadMinima}</p>}
                      <p>Desde: {new Date(promotion.fechaInicio).toLocaleDateString()}</p>
                      <p>Hasta: {new Date(promotion.fechaFin).toLocaleDateString()}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}

        {showModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-3xl w-full p-6 max-h-[90vh] overflow-y-auto">
              <h2 className="text-2xl font-bold text-gray-800 mb-6">
                {editingPromotion ? 'Editar Promoción' : 'Nueva Promoción'}
              </h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Nombre</label>
                    <input
                      type="text"
                      value={formData.nombre}
                      onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Tipo de regla</label>
                    <select
                      value={formData.tipoRegla}
                      onChange={(e) => setFormData({ ...formData, tipoRegla: e.target.value, idModificadorGratis: '' })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    >
                      <option value="PORCENTAJE">Porcentaje</option>
                      <option value="DOS_X_UNO">Dos por uno</option>
                      <option value="EXTRA_GRATIS">Extra gratis</option>
                      <option value="DESCUENTO_HORARIO">Descuento por horario</option>
                    </select>
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

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Tipo de objetivo</label>
                    <select
                      value={formData.tipoObjetivo}
                      onChange={(e) => setFormData({ ...formData, tipoObjetivo: e.target.value, selectedIds: [] })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    >
                      <option value="PRODUCTO">Producto</option>
                      <option value="CATEGORIA">Categoría</option>
                    </select>
                  </div>
                  {formData.tipoRegla === 'EXTRA_GRATIS' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Modificador gratis</label>
                      <select
                        value={formData.idModificadorGratis}
                        onChange={(e) => setFormData({ ...formData, idModificadorGratis: e.target.value })}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      >
                        <option value="">Seleccionar</option>
                        {catalog.modificadores.map((modifier) => (
                          <option key={modifier.idModificador} value={modifier.idModificador}>
                            {modifier.nombre}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Objetivos</label>
                  <div className="grid grid-cols-2 gap-2 max-h-52 overflow-y-auto border border-gray-200 rounded-lg p-3">
                    {objectiveOptions.map((option) => {
                      const optionId = String('idCategoria' in option ? option.idCategoria : option.idProducto);
                      const checked = formData.selectedIds.includes(optionId);
                      return (
                        <label key={optionId} className="flex items-center gap-2 text-sm text-gray-700">
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={(e) =>
                              setFormData((current) => ({
                                ...current,
                                selectedIds: e.target.checked
                                  ? [...current.selectedIds, optionId]
                                  : current.selectedIds.filter((id) => id !== optionId),
                              }))
                            }
                          />
                          {option.nombre}
                        </label>
                      );
                    })}
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Porcentaje</label>
                    <input
                      type="number"
                      step="0.01"
                      value={formData.porcentajeDescuento}
                      onChange={(e) => setFormData({ ...formData, porcentajeDescuento: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Monto</label>
                    <input
                      type="number"
                      step="0.01"
                      value={formData.montoDescuento}
                      onChange={(e) => setFormData({ ...formData, montoDescuento: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Cantidad mínima</label>
                    <input
                      type="number"
                      min="1"
                      value={formData.cantidadMinima}
                      onChange={(e) => setFormData({ ...formData, cantidadMinima: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    />
                  </div>
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

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Hora de inicio</label>
                    <input
                      type="time"
                      value={formData.horaInicio}
                      onChange={(e) => setFormData({ ...formData, horaInicio: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Hora de fin</label>
                    <input
                      type="time"
                      value={formData.horaFin}
                      onChange={(e) => setFormData({ ...formData, horaFin: e.target.value })}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    />
                  </div>
                </div>

                <label className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={formData.activa}
                    onChange={(e) => setFormData({ ...formData, activa: e.target.checked })}
                  />
                  Activa
                </label>

                <div className="flex gap-3 pt-4">
                  <button
                    type="submit"
                    className="flex-1 bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
                  >
                    {editingPromotion ? 'Guardar Cambios' : 'Crear Promoción'}
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
