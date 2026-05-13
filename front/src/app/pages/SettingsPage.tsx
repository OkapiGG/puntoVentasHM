import { useEffect, useState } from 'react';
import { MainLayout } from '../components/MainLayout';
import { useSettings } from '../context/SettingsContext';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';
import { User, Bell, Database, CheckCircle, Eye, EyeOff, Loader2, Lock } from 'lucide-react';

export function SettingsPage() {
  const { settings, saveSettings, loading } = useSettings();
  const { user } = useAuth();

  const [form, setForm] = useState({
    restaurantName: settings.restaurantName,
    cashLimit: settings.cashLimit,
    deliveryFee: settings.deliveryFee,
    notifications: { ...settings.notifications },
  });
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [saveError, setSaveError] = useState('');

  const [passwordForm, setPasswordForm] = useState({
    current: '',
    next: '',
    confirm: '',
  });
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNext, setShowNext] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [passwordOk, setPasswordOk] = useState(false);
  const [changingPin, setChangingPin] = useState(false);

  useEffect(() => {
    setForm({
      restaurantName: settings.restaurantName,
      cashLimit: settings.cashLimit,
      deliveryFee: settings.deliveryFee,
      notifications: { ...settings.notifications },
    });
  }, [settings]);

  const handleSave = async () => {
    setSaveError('');
    setSaved(false);
    setSaving(true);
    try {
      await saveSettings({
        restaurantName: form.restaurantName,
        cashLimit: Number(form.cashLimit),
        deliveryFee: Number(form.deliveryFee),
        notifications: form.notifications,
      });
      setSaved(true);
      window.setTimeout(() => setSaved(false), 2500);
    } catch (error) {
      setSaveError(error instanceof Error ? error.message : 'No se pudo guardar la configuración');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordChange = async () => {
    setPasswordError('');
    setPasswordOk(false);

    if (passwordForm.next.length < 4) {
      setPasswordError('El nuevo PIN debe tener al menos 4 caracteres.');
      return;
    }
    if (passwordForm.next !== passwordForm.confirm) {
      setPasswordError('Los PIN no coinciden.');
      return;
    }

    setChangingPin(true);
    try {
      await apiRequest('/auth/cambiar-pin', {
        method: 'POST',
        body: JSON.stringify({
          pinActual: passwordForm.current,
          pinNuevo: passwordForm.next,
        }),
      });
      setPasswordOk(true);
      setPasswordForm({ current: '', next: '', confirm: '' });
    } catch (error) {
      setPasswordError(error instanceof Error ? error.message : 'No se pudo cambiar el PIN');
    } finally {
      setChangingPin(false);
    }
  };

  if (user?.role !== 'admin' && user?.role !== 'manager') {
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
      <div className="p-8 max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-800 mb-2">Configuración</h1>
        <p className="text-gray-500 mb-8">Personaliza el sistema con datos persistidos en la base.</p>

        <div className="space-y-6">
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center gap-3 mb-5">
              <Database className="w-5 h-5 text-orange-500" />
              <h2 className="text-lg font-bold text-gray-800">Datos del Sistema</h2>
              {loading && <Loader2 className="w-4 h-4 animate-spin text-gray-400" />}
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre del restaurante
                </label>
                <input
                  type="text"
                  value={form.restaurantName}
                  onChange={(e) => setForm((f) => ({ ...f, restaurantName: e.target.value }))}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Límite de efectivo en caja ($)
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={form.cashLimit}
                    onChange={(e) => setForm((f) => ({ ...f, cashLimit: Number(e.target.value) }))}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Costo de envío predeterminado ($)
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={form.deliveryFee}
                    onChange={(e) => setForm((f) => ({ ...f, deliveryFee: Number(e.target.value) }))}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center gap-3 mb-5">
              <Bell className="w-5 h-5 text-orange-500" />
              <h2 className="text-lg font-bold text-gray-800">Notificaciones</h2>
            </div>
            <div className="space-y-3">
              {([
                { key: 'cashAlerts', label: 'Alertas de caja', desc: 'Avisar cuando la caja alcance el límite.' },
                { key: 'newOrders', label: 'Nuevos pedidos', desc: 'Mostrar aviso al registrar nuevos pedidos.' },
                { key: 'dailyReports', label: 'Reportes diarios', desc: 'Habilitar recordatorios del cierre diario.' },
              ] as const).map(({ key, label, desc }) => (
                <div key={key} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-800">{label}</p>
                    <p className="text-sm text-gray-500">{desc}</p>
                  </div>
                  <button
                    onClick={() =>
                      setForm((f) => ({
                        ...f,
                        notifications: { ...f.notifications, [key]: !f.notifications[key] },
                      }))
                    }
                    className={`relative w-12 h-6 rounded-full transition-colors ${
                      form.notifications[key] ? 'bg-orange-500' : 'bg-gray-300'
                    }`}
                  >
                    <span
                      className={`absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-transform ${
                        form.notifications[key] ? 'translate-x-7' : 'translate-x-1'
                      }`}
                    />
                  </button>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center gap-3 mb-5">
              <User className="w-5 h-5 text-orange-500" />
              <h2 className="text-lg font-bold text-gray-800">Cambiar PIN</h2>
              <span className="text-sm text-gray-400">({user?.username})</span>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">PIN actual</label>
                <div className="relative">
                  <input
                    type={showCurrent ? 'text' : 'password'}
                    value={passwordForm.current}
                    onChange={(e) => {
                      setPasswordForm((f) => ({ ...f, current: e.target.value }));
                      setPasswordError('');
                      setPasswordOk(false);
                    }}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none ${
                      passwordError ? 'border-red-400 bg-red-50' : 'border-gray-300'
                    }`}
                    placeholder="••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowCurrent((v) => !v)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showCurrent ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Nuevo PIN</label>
                  <div className="relative">
                    <input
                      type={showNext ? 'text' : 'password'}
                      value={passwordForm.next}
                      onChange={(e) => {
                        setPasswordForm((f) => ({ ...f, next: e.target.value }));
                        setPasswordError('');
                        setPasswordOk(false);
                      }}
                      className="w-full px-4 py-3 pr-12 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                      placeholder="Mín. 4 caracteres"
                    />
                    <button
                      type="button"
                      onClick={() => setShowNext((v) => !v)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showNext ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                    </button>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Confirmar PIN</label>
                  <input
                    type="password"
                    value={passwordForm.confirm}
                    onChange={(e) => {
                      setPasswordForm((f) => ({ ...f, confirm: e.target.value }));
                      setPasswordError('');
                      setPasswordOk(false);
                    }}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none"
                    placeholder="Repite el PIN"
                  />
                </div>
              </div>

              {passwordError && <p className="text-red-500 text-sm">{passwordError}</p>}
              {passwordOk && (
                <div className="flex items-center gap-2 text-green-600 text-sm">
                  <CheckCircle className="w-4 h-4" />
                  <span>PIN actualizado correctamente.</span>
                </div>
              )}

              <button
                onClick={handlePasswordChange}
                disabled={changingPin || !passwordForm.current || !passwordForm.next || !passwordForm.confirm}
                className="px-6 py-2.5 bg-gray-800 text-white rounded-lg font-medium hover:bg-gray-700 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
              >
                {changingPin ? 'Actualizando...' : 'Actualizar PIN'}
              </button>
            </div>
          </div>

          {saveError && <p className="text-red-500 text-sm">{saveError}</p>}

          <button
            onClick={() => void handleSave()}
            disabled={saving}
            className="w-full bg-gradient-to-r from-orange-500 to-yellow-500 text-white py-4 rounded-xl font-semibold hover:from-orange-600 hover:to-yellow-600 transition-all shadow-lg flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {saving ? <Loader2 className="w-5 h-5 animate-spin" /> : saved ? <CheckCircle className="w-5 h-5" /> : null}
            {saving ? 'Guardando...' : saved ? 'Cambios guardados' : 'Guardar Cambios'}
          </button>
        </div>
      </div>
    </MainLayout>
  );
}
