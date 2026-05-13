import { RouterProvider } from 'react-router';
import { router } from './routes';
import { AuthProvider } from './context/AuthContext';
import { POSProvider } from './context/POSContext';
import { ShiftProvider } from './context/ShiftContext';
import { SettingsProvider } from './context/SettingsContext';

export default function App() {
  return (
    <AuthProvider>
      <SettingsProvider>
        <POSProvider>
          <ShiftProvider>
            <RouterProvider router={router} />
          </ShiftProvider>
        </POSProvider>
      </SettingsProvider>
    </AuthProvider>
  );
}
