import { useEffect, useMemo, useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { Users, Circle, Lock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';

interface MesaApi {
  idMesa: number;
  numero: number;
  estado: string;
  meseroAsignado?: string | null;
  referenciaOrden?: string | null;
}

const estadoLabel: Record<string, string> = {
  LIBRE: 'Libre',
  OCUPADA: 'Ocupada',
  RESERVADA: 'Reservada',
  LIMPIEZA: 'Limpieza',
};

export function TablesPage() {
  const { user } = useAuth();
  const [tables, setTables] = useState<MesaApi[]>([]);
  const [selectedTable, setSelectedTable] = useState<MesaApi | null>(null);
  const [waiterName, setWaiterName] = useState('');
  const [orderReference, setOrderReference] = useState('');
  const [status, setStatus] = useState('LIBRE');

  const canUseTables = user?.role === 'admin' || user?.role === 'manager' || user?.role === 'cashier';

  const loadTables = async () => {
    if (!user?.idNegocio || !canUseTables) return;
    const response = await apiRequest<MesaApi[]>(`/mesas/negocios/${user.idNegocio}`);
    setTables(response);
  };

  useEffect(() => {
    void loadTables();
  }, [user?.idNegocio, canUseTables]);

  useEffect(() => {
    if (!selectedTable) return;
    setWaiterName(selectedTable.meseroAsignado ?? '');
    setOrderReference(selectedTable.referenciaOrden ?? '');
    setStatus(selectedTable.estado);
  }, [selectedTable]);

  const summary = useMemo(() => ({
    occupied: tables.filter((table) => table.estado === 'OCUPADA').length,
    free: tables.filter((table) => table.estado === 'LIBRE').length,
  }), [tables]);

  const handleSave = async () => {
    if (!selectedTable || !user?.idNegocio) return;
    await apiRequest<MesaApi>(`/mesas/negocios/${user.idNegocio}/${selectedTable.idMesa}`, {
      method: 'PUT',
      body: JSON.stringify({
        estado: status,
        meseroAsignado: waiterName || null,
        referenciaOrden: orderReference || null,
      }),
    });
    setSelectedTable(null);
    await loadTables();
  };

  if (!canUseTables) {
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
        <h1 className="text-3xl font-bold text-gray-800 mb-2">Gestión de Mesas</h1>
        <p className="text-gray-600 mb-8">Estado persistido de mesas en la base de datos</p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-xl p-6 shadow-lg">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm">Total de Mesas</p>
                <p className="text-3xl font-bold text-gray-800">{tables.length}</p>
              </div>
              <Users className="w-12 h-12 text-gray-400" />
            </div>
          </div>

          <div className="bg-gradient-to-br from-red-500 to-red-600 rounded-xl p-6 shadow-lg text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-red-100 text-sm">Mesas Ocupadas</p>
                <p className="text-3xl font-bold">{summary.occupied}</p>
              </div>
              <Circle className="w-12 h-12" fill="currentColor" />
            </div>
          </div>

          <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 shadow-lg text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-100 text-sm">Mesas Libres</p>
                <p className="text-3xl font-bold">{summary.free}</p>
              </div>
              <Circle className="w-12 h-12" fill="currentColor" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl p-8 shadow-lg">
          <h2 className="text-xl font-bold text-gray-800 mb-6">Mapa de Mesas</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
            {tables.map((table) => (
              <button
                key={table.idMesa}
                onClick={() => setSelectedTable(table)}
                className={`aspect-square rounded-2xl p-6 shadow-md hover:shadow-xl transition-all flex flex-col items-center justify-center text-white ${
                  table.estado === 'LIBRE'
                    ? 'bg-gradient-to-br from-green-500 to-green-600'
                    : table.estado === 'OCUPADA'
                      ? 'bg-gradient-to-br from-red-500 to-red-600'
                      : 'bg-gradient-to-br from-amber-500 to-amber-600'
                }`}
              >
                <p className="text-sm opacity-90 mb-2">Mesa</p>
                <p className="text-4xl font-bold">{table.numero}</p>
                <p className="text-xs opacity-90 mt-2">{estadoLabel[table.estado] ?? table.estado}</p>
              </button>
            ))}
          </div>
        </div>

        {selectedTable && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-4">Mesa {selectedTable.numero}</h2>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Estado</label>
                  <select
                    value={status}
                    onChange={(e) => setStatus(e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  >
                    <option value="LIBRE">Libre</option>
                    <option value="OCUPADA">Ocupada</option>
                    <option value="RESERVADA">Reservada</option>
                    <option value="LIMPIEZA">Limpieza</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Mesero asignado</label>
                  <input
                    type="text"
                    value={waiterName}
                    onChange={(e) => setWaiterName(e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    placeholder="Nombre del mesero"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Referencia de orden</label>
                  <input
                    type="text"
                    value={orderReference}
                    onChange={(e) => setOrderReference(e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    placeholder="Folio o referencia"
                  />
                </div>
              </div>

              <div className="flex gap-3 mt-6">
                <button
                  onClick={() => void handleSave()}
                  className="flex-1 py-3 rounded-lg font-semibold bg-gradient-to-r from-orange-500 to-yellow-500 text-white hover:from-orange-600 hover:to-yellow-600 transition-all"
                >
                  Guardar
                </button>
                <button
                  onClick={() => setSelectedTable(null)}
                  className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition-all"
                >
                  Cerrar
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </MainLayout>
  );
}
