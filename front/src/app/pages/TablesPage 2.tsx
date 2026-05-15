import { useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { Users, Circle } from 'lucide-react';

interface Table {
  id: number;
  status: 'free' | 'occupied';
  waiter?: string;
  orderId?: string;
}

export function TablesPage() {
  const [tables, setTables] = useState<Table[]>([
    { id: 1, status: 'occupied', waiter: 'Juan', orderId: 'ORD-001' },
    { id: 2, status: 'free' },
    { id: 3, status: 'free' },
    { id: 4, status: 'occupied', waiter: 'María', orderId: 'ORD-002' },
    { id: 5, status: 'free' },
    { id: 6, status: 'free' },
    { id: 7, status: 'occupied', waiter: 'Pedro', orderId: 'ORD-003' },
    { id: 8, status: 'free' },
    { id: 9, status: 'free' },
    { id: 10, status: 'free' },
    { id: 11, status: 'free' },
    { id: 12, status: 'free' },
  ]);

  const [selectedTable, setSelectedTable] = useState<Table | null>(null);
  const [waiterName, setWaiterName] = useState('');

  const occupiedCount = tables.filter((t) => t.status === 'occupied').length;
  const freeCount = tables.filter((t) => t.status === 'free').length;

  const handleAssignWaiter = () => {
    if (selectedTable && waiterName) {
      setTables(
        tables.map((t) =>
          t.id === selectedTable.id ? { ...t, waiter: waiterName } : t
        )
      );
      setSelectedTable(null);
      setWaiterName('');
    }
  };

  const handleToggleStatus = (table: Table) => {
    setTables(
      tables.map((t) =>
        t.id === table.id
          ? {
              ...t,
              status: t.status === 'free' ? 'occupied' : 'free',
              waiter: t.status === 'free' ? t.waiter : undefined,
              orderId: t.status === 'free' ? `ORD-${Date.now()}` : undefined,
            }
          : t
      )
    );
  };

  return (
    <MainLayout>
      <div className="p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-2">Gestión de Mesas</h1>
        <p className="text-gray-600 mb-8">Vista general del restaurante</p>

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
                <p className="text-3xl font-bold">{occupiedCount}</p>
              </div>
              <Circle className="w-12 h-12" fill="currentColor" />
            </div>
          </div>

          <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 shadow-lg text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-100 text-sm">Mesas Libres</p>
                <p className="text-3xl font-bold">{freeCount}</p>
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
                key={table.id}
                onClick={() => setSelectedTable(table)}
                className={`aspect-square rounded-2xl p-6 shadow-md hover:shadow-xl transition-all flex flex-col items-center justify-center ${
                  table.status === 'occupied'
                    ? 'bg-gradient-to-br from-red-500 to-red-600 text-white'
                    : 'bg-gradient-to-br from-green-500 to-green-600 text-white'
                }`}
              >
                <p className="text-sm opacity-90 mb-2">Mesa</p>
                <p className="text-4xl font-bold">{table.id}</p>
                {table.waiter && (
                  <p className="text-xs opacity-90 mt-2">{table.waiter}</p>
                )}
              </button>
            ))}
          </div>
        </div>

        {selectedTable && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-4">
                Mesa {selectedTable.id}
              </h2>

              <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                <p className="text-sm text-gray-600 mb-1">Estado:</p>
                <p className={`text-lg font-bold ${selectedTable.status === 'occupied' ? 'text-red-600' : 'text-green-600'}`}>
                  {selectedTable.status === 'occupied' ? 'Ocupada' : 'Libre'}
                </p>
                {selectedTable.waiter && (
                  <>
                    <p className="text-sm text-gray-600 mt-3 mb-1">Mesero asignado:</p>
                    <p className="text-lg font-bold text-gray-800">{selectedTable.waiter}</p>
                  </>
                )}
                {selectedTable.orderId && (
                  <>
                    <p className="text-sm text-gray-600 mt-3 mb-1">Pedido:</p>
                    <p className="text-lg font-bold text-gray-800">{selectedTable.orderId}</p>
                  </>
                )}
              </div>

              {selectedTable.status === 'occupied' && !selectedTable.waiter && (
                <div className="mb-6">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Asignar Mesero
                  </label>
                  <input
                    type="text"
                    value={waiterName}
                    onChange={(e) => setWaiterName(e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    placeholder="Nombre del mesero"
                  />
                  <button
                    onClick={handleAssignWaiter}
                    disabled={!waiterName}
                    className="w-full mt-3 bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Asignar
                  </button>
                </div>
              )}

              <div className="flex gap-3">
                <button
                  onClick={() => handleToggleStatus(selectedTable)}
                  className={`flex-1 py-3 rounded-lg font-semibold transition-all ${
                    selectedTable.status === 'occupied'
                      ? 'bg-green-500 text-white hover:bg-green-600'
                      : 'bg-red-500 text-white hover:bg-red-600'
                  }`}
                >
                  {selectedTable.status === 'occupied' ? 'Liberar Mesa' : 'Ocupar Mesa'}
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
