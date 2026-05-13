import { createContext, useContext, useEffect, useMemo, useState, ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { apiRequest } from '../lib/api';

interface Notifications {
  cashAlerts: boolean;
  newOrders: boolean;
  dailyReports: boolean;
}

interface Settings {
  restaurantName: string;
  cashLimit: number;
  deliveryFee: number;
  notifications: Notifications;
}

interface SettingsContextType {
  settings: Settings;
  loading: boolean;
  saveSettings: (next: Settings) => Promise<void>;
  refreshSettings: () => Promise<void>;
}

interface NegocioConfigApi {
  idNegocio: number;
  nombre: string;
  limiteCaja: number;
  costoEnvioDefault: number;
  alertasCaja: boolean;
  notificaNuevosPedidos: boolean;
  notificaReportesDiarios: boolean;
}

const defaultSettings: Settings = {
  restaurantName: 'Hamburguesas May',
  cashLimit: 5000,
  deliveryFee: 30,
  notifications: {
    cashAlerts: true,
    newOrders: true,
    dailyReports: false,
  },
};

const SettingsContext = createContext<SettingsContextType | undefined>(undefined);

function mapApiToSettings(config: NegocioConfigApi): Settings {
  return {
    restaurantName: config.nombre,
    cashLimit: Number(config.limiteCaja ?? 0),
    deliveryFee: Number(config.costoEnvioDefault ?? 0),
    notifications: {
      cashAlerts: Boolean(config.alertasCaja),
      newOrders: Boolean(config.notificaNuevosPedidos),
      dailyReports: Boolean(config.notificaReportesDiarios),
    },
  };
}

export function SettingsProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [settings, setSettings] = useState<Settings>(defaultSettings);
  const [loading, setLoading] = useState(false);

  const refreshSettings = async () => {
    if (!user?.idNegocio) {
      setSettings(defaultSettings);
      return;
    }

    setLoading(true);
    try {
      const response = await apiRequest<NegocioConfigApi>(`/configuracion/negocios/${user.idNegocio}`);
      setSettings(mapApiToSettings(response));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshSettings();
  }, [user?.idNegocio]);

  const saveSettings = async (next: Settings) => {
    if (!user?.idNegocio) {
      throw new Error('No hay negocio activo para guardar la configuración');
    }

    const response = await apiRequest<NegocioConfigApi>(`/configuracion/negocios/${user.idNegocio}`, {
      method: 'PUT',
      body: JSON.stringify({
        nombre: next.restaurantName,
        limiteCaja: next.cashLimit,
        costoEnvioDefault: next.deliveryFee,
        alertasCaja: next.notifications.cashAlerts,
        notificaNuevosPedidos: next.notifications.newOrders,
        notificaReportesDiarios: next.notifications.dailyReports,
      }),
    });
    setSettings(mapApiToSettings(response));
  };

  const value = useMemo(
    () => ({
      settings,
      loading,
      saveSettings,
      refreshSettings,
    }),
    [settings, loading]
  );

  return <SettingsContext.Provider value={value}>{children}</SettingsContext.Provider>;
}

export function useSettings() {
  const ctx = useContext(SettingsContext);
  if (!ctx) throw new Error('useSettings must be used within SettingsProvider');
  return ctx;
}
