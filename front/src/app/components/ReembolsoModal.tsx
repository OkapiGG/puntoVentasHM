import { useState } from 'react';
import { AlertTriangle, X } from 'lucide-react';

interface ReembolsoModalProps {
  folio: string;
  total: number;
  loading: boolean;
  error: string;
  onClose: () => void;
  onSubmit: (motivo: string, pinGerente: string) => Promise<void>;
}

export function ReembolsoModal({ folio, total, loading, error, onClose, onSubmit }: ReembolsoModalProps) {
  const [motivo, setMotivo] = useState('');
  const [pinGerente, setPinGerente] = useState('');
  const [localError, setLocalError] = useState('');

  const handleSubmit = async () => {
    if (!motivo.trim()) {
      setLocalError('Debes indicar el motivo del reembolso.');
      return;
    }
    if (!pinGerente.trim()) {
      setLocalError('Debes capturar el PIN de autorización.');
      return;
    }
    setLocalError('');
    await onSubmit(motivo.trim(), pinGerente.trim());
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
        <div className="flex justify-between items-start gap-4 mb-4">
          <div>
            <div className="flex items-center gap-2 text-red-600 mb-2">
              <AlertTriangle className="w-5 h-5" />
              <span className="text-sm font-bold uppercase tracking-wide">Operación sensible</span>
            </div>
            <h2 className="text-2xl font-bold text-gray-800">Reembolsar orden</h2>
          </div>
          <button onClick={onClose} disabled={loading} className="text-gray-400 hover:text-gray-600 disabled:opacity-50">
            <X className="w-6 h-6" />
          </button>
        </div>

        <p className="text-sm text-gray-500 mb-5">
          Folio {folio}. Se registrará un egreso por ${Number(total ?? 0).toFixed(2)} en la sesión de caja actual.
        </p>

        <label className="block text-sm font-semibold text-gray-700 mb-2">Motivo</label>
        <textarea
          value={motivo}
          onChange={(event) => setMotivo(event.target.value)}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none resize-none"
          rows={4}
          placeholder="Ej. producto incorrecto, cliente solicitó devolución"
          disabled={loading}
        />

        <label className="block text-sm font-semibold text-gray-700 mt-4 mb-2">PIN de autorización</label>
        <input
          value={pinGerente}
          onChange={(event) => setPinGerente(event.target.value)}
          type="password"
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent outline-none"
          placeholder="PIN de admin o gerente"
          disabled={loading}
        />

        {(localError || error) && (
          <div className="mt-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {localError || error}
          </div>
        )}

        <div className="flex gap-3 mt-6">
          <button
            onClick={() => void handleSubmit()}
            disabled={loading}
            className="flex-1 bg-gradient-to-r from-red-500 to-red-600 text-white py-3 rounded-lg font-semibold hover:from-red-600 hover:to-red-700 transition-all disabled:opacity-60"
          >
            {loading ? 'Procesando...' : 'Confirmar reembolso'}
          </button>
          <button
            onClick={onClose}
            disabled={loading}
            className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition-all disabled:opacity-60"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}
