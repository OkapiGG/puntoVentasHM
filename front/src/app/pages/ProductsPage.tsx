import { useEffect, useMemo, useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { Plus, Edit, Trash2, Package, Lock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';

interface AdminCategoryApi {
  idCategoria: number;
  nombre: string;
  totalProductos: number;
}

interface AdminProductApi {
  idProducto: number;
  idCategoria: number;
  categoriaNombre: string;
  nombre: string;
  precio: number;
  costoEstimado?: number | null;
  imagenUrl?: string | null;
  activo: boolean;
  esPopular: boolean;
  esNuevo: boolean;
}

interface AdminCatalogApi {
  categorias: AdminCategoryApi[];
  productos: AdminProductApi[];
}

export function ProductsPage() {
  const { user } = useAuth();
  const [catalog, setCatalog] = useState<AdminCatalogApi>({ categorias: [], productos: [] });
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState<AdminProductApi | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    categoryId: '',
    price: '',
    active: true,
    popular: false,
    nuevo: false,
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.idNegocio) return;

    const payload = {
      idCategoria: Number(formData.categoryId),
      nombre: formData.name,
      precio: Number(formData.price),
      imagenUrl: null,
      activo: formData.active,
      esPopular: formData.popular,
      esNuevo: formData.nuevo,
      receta: [],
    };

    if (editingProduct) {
      await apiRequest(`/admin/negocios/${user.idNegocio}/productos/${editingProduct.idProducto}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });
    } else {
      await apiRequest(`/admin/negocios/${user.idNegocio}/productos`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    }

    setShowModal(false);
    setEditingProduct(null);
    setFormData({ name: '', categoryId: '', price: '', active: true, popular: false, nuevo: false });
    await loadCatalog();
  };

  const handleEdit = (product: AdminProductApi) => {
    setEditingProduct(product);
    setFormData({
      name: product.nombre,
      categoryId: String(product.idCategoria),
      price: String(product.precio),
      active: product.activo,
      popular: product.esPopular,
      nuevo: product.esNuevo,
    });
    setShowModal(true);
  };

  const handleDelete = async (id: number) => {
    if (!user?.idNegocio) return;
    await apiRequest(`/admin/negocios/${user.idNegocio}/productos/${id}`, {
      method: 'DELETE',
    });
    await loadCatalog();
  };

  const handleAddNew = () => {
    setEditingProduct(null);
    setFormData({
      name: '',
      categoryId: catalog.categorias[0] ? String(catalog.categorias[0].idCategoria) : '',
      price: '',
      active: true,
      popular: false,
      nuevo: false,
    });
    setShowModal(true);
  };

  const groupedProducts = useMemo(
    () =>
      catalog.categorias.reduce((acc, cat) => {
        acc[cat.nombre] = catalog.productos.filter((p) => p.idCategoria === cat.idCategoria);
        return acc;
      }, {} as Record<string, AdminProductApi[]>),
    [catalog]
  );

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

  return (
    <MainLayout>
      <div className="p-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Administración de Productos</h1>
            <p className="text-gray-600 mt-1">Gestiona el menú del restaurante</p>
          </div>
          <button
            onClick={handleAddNew}
            className="flex items-center gap-2 bg-gradient-to-r from-orange-500 to-yellow-500 text-white px-6 py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg"
          >
            <Plus className="w-5 h-5" />
            Agregar Producto
          </button>
        </div>

        {catalog.categorias.map((category) => (
          <div key={category.idCategoria} className="mb-8">
            <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              <Package className="w-6 h-6 text-orange-500" />
              {category.nombre}
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {groupedProducts[category.nombre]?.map((product) => (
                <div key={product.idProducto} className="bg-white rounded-xl p-6 shadow-md hover:shadow-lg transition-all">
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <h3 className="text-lg font-bold text-gray-800">{product.nombre}</h3>
                      <p className="text-sm text-gray-600">{product.categoriaNombre}</p>
                      <p className="text-xs text-gray-400 mt-1">
                        {product.activo ? 'Activo' : 'Inactivo'} · {product.esPopular ? 'Popular' : 'Normal'} · {product.esNuevo ? 'Nuevo' : 'Regular'}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <button onClick={() => handleEdit(product)} className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                        <Edit className="w-5 h-5" />
                      </button>
                      <button onClick={() => handleDelete(product.idProducto)} className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors">
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                  <p className="text-2xl font-bold text-orange-600">${Number(product.precio ?? 0).toFixed(2)}</p>
                </div>
              ))}
            </div>
          </div>
        ))}

        {showModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-6">
                {editingProduct ? 'Editar Producto' : 'Nuevo Producto'}
              </h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Nombre del Producto</label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Categoría</label>
                  <select
                    value={formData.categoryId}
                    onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  >
                    {catalog.categorias.map((cat) => (
                      <option key={cat.idCategoria} value={cat.idCategoria}>
                        {cat.nombre}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Precio</label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.price}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    required
                  />
                </div>
                <div className="grid grid-cols-3 gap-3 text-sm">
                  <label className="flex items-center gap-2">
                    <input type="checkbox" checked={formData.active} onChange={(e) => setFormData({ ...formData, active: e.target.checked })} />
                    Activo
                  </label>
                  <label className="flex items-center gap-2">
                    <input type="checkbox" checked={formData.popular} onChange={(e) => setFormData({ ...formData, popular: e.target.checked })} />
                    Popular
                  </label>
                  <label className="flex items-center gap-2">
                    <input type="checkbox" checked={formData.nuevo} onChange={(e) => setFormData({ ...formData, nuevo: e.target.checked })} />
                    Nuevo
                  </label>
                </div>
                <div className="flex gap-3 pt-4">
                  <button type="submit" className="flex-1 bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg">
                    {editingProduct ? 'Guardar Cambios' : 'Agregar'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowModal(false);
                      setEditingProduct(null);
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
