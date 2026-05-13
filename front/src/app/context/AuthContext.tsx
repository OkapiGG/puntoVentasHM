import { createContext, useContext, useEffect, useMemo, useState, ReactNode } from 'react';
import { apiRequest, loadStoredSession, saveStoredSession, type StoredSession } from '../lib/api';

export type UserRole = 'admin' | 'manager' | 'cashier' | 'delivery';

export interface User extends StoredSession {
  username: string;
}

interface LoginResponse {
  idUsuario: number;
  nombre: string;
  rol: string;
  idNegocio: number;
  avatarUrl?: string | null;
}

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  validatePassword: (username: string, password: string) => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function mapBackendRole(backendRole: string): UserRole {
  switch (backendRole?.toUpperCase()) {
    case 'ADMIN':
      return 'admin';
    case 'GERENTE':
      return 'manager';
    case 'CAJERO':
      return 'cashier';
    case 'REPARTIDOR':
      return 'delivery';
    default:
      return 'cashier';
  }
}

function mapLoginResponse(response: LoginResponse): User {
  return {
    idUsuario: response.idUsuario,
    nombre: response.nombre,
    username: response.nombre,
    role: mapBackendRole(response.rol),
    backendRole: response.rol,
    idNegocio: response.idNegocio,
    avatarUrl: response.avatarUrl,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const stored = loadStoredSession();
    if (!stored) {
      return;
    }

    setUser({
      ...stored,
      username: stored.nombre,
    });
  }, []);

  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      const response = await apiRequest<LoginResponse>('/auth/login', {
        method: 'POST',
        skipAuth: true,
        body: JSON.stringify({
          usuario: username,
          password,
        }),
      });

      const nextUser = mapLoginResponse(response);
      setUser(nextUser);
      saveStoredSession({
        idUsuario: nextUser.idUsuario,
        nombre: nextUser.nombre,
        role: nextUser.role,
        backendRole: nextUser.backendRole,
        idNegocio: nextUser.idNegocio,
        avatarUrl: nextUser.avatarUrl,
      });
      return true;
    } catch {
      return false;
    }
  };

  const validatePassword = async (username: string, password: string): Promise<boolean> => {
    try {
      const response = await apiRequest<LoginResponse>('/auth/login', {
        method: 'POST',
        skipAuth: true,
        body: JSON.stringify({
          usuario: username,
          password,
        }),
      });

      return response.idUsuario === user?.idUsuario;
    } catch {
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    saveStoredSession(null);
  };

  const value = useMemo(
    () => ({
      user,
      login,
      logout,
      validatePassword,
    }),
    [user]
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
