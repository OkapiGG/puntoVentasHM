const SESSION_STORAGE_KEY = 'pos-session';

export interface StoredSession {
  idUsuario: number;
  nombre: string;
  role: string;
  backendRole: string;
  idNegocio: number;
  avatarUrl?: string | null;
  token?: string;
}

export function loadStoredSession(): StoredSession | null {
  if (typeof window === 'undefined') {
    return null;
  }

  const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as StoredSession;
  } catch {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return null;
  }
}

export function saveStoredSession(session: StoredSession | null) {
  if (typeof window === 'undefined') {
    return;
  }

  if (!session) {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

type ApiOptions = RequestInit & {
  skipAuth?: boolean;
};

export async function apiRequest<T>(path: string, options: ApiOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  const session = loadStoredSession();

  if (!headers.has('Content-Type') && options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (!options.skipAuth && session?.token) {
    headers.set('Authorization', `Bearer ${session.token}`);
  }

  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? ''}/api${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    saveStoredSession(null);
    const errorText = await response.text();
    throw new Error(errorText || 'Sesión expirada');
  }

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || 'Error al comunicarse con el servidor');
  }

  if (response.status === 204) {
    return null as T;
  }

  return (await response.json()) as T;
}
