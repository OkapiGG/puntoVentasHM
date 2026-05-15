import { createContext, useContext, useState, ReactNode } from 'react';

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
  saveSettings: (next: Settings) => void;
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

export function SettingsProvider({ children }: { children: ReactNode }) {
  const [settings, setSettings] = useState<Settings>(defaultSettings);

  const saveSettings = (next: Settings) => setSettings(next);

  return (
    <SettingsContext.Provider value={{ settings, saveSettings }}>
      {children}
    </SettingsContext.Provider>
  );
}

export function useSettings() {
  const ctx = useContext(SettingsContext);
  if (!ctx) throw new Error('useSettings must be used within SettingsProvider');
  return ctx;
}
