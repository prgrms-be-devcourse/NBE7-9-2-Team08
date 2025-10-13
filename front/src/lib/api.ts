type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

const BASE = process.env.NEXT_PUBLIC_BACKEND_URL;
const AUTH_HEADER = process.env.NEXT_PUBLIC_AUTH_HEADER || 'Authorization';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('accessToken'); // 토큰 사용 시
}

export async function api<T = unknown>(
  path: string,
  opts: {
    method?: HttpMethod;
    body?: unknown;
    headers?: Record<string,string>;
    auth?: 'cookie' | 'token' | 'none';
    next?: NextFetchRequestConfig;
  } = {}
): Promise<T> {
  const {
    method = 'GET',
    body,
    headers = {},
    auth = 'cookie',
    next,
  } = opts;

  const isAbsolute = path.startsWith('http');
  const url = isAbsolute ? path : (process.env.NEXT_PUBLIC_DEV_PROXY === 'true' ? `/api${path.startsWith('/')?path:`/${path}`}` : `${BASE}${path.startsWith('/')?path:`/${path}`}`);

  const h: Record<string,string> = {
    'Content-Type': 'application/json',
    ...headers,
  };

  if (auth === 'token') {
    const token = getToken();
    if (token) h[AUTH_HEADER] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method,
    headers: h,
    body: body ? JSON.stringify(body) : undefined,
    credentials: auth === 'cookie' ? 'include' : 'same-origin',
    cache: 'no-store',
    next,
  });

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    const msg = data?.message || data?.error || res.statusText;
    throw new Error(msg);
  }
  return data as T;
}

export const http = {
  get: <T>(path: string, auth: 'cookie'|'token'|'none'='cookie') => api<T>(path, { method: 'GET', auth }),
  post: <T>(path: string, body?: unknown, auth: 'cookie'|'token'|'none'='cookie') => api<T>(path, { method: 'POST', body, auth }),
  put:  <T>(path: string, body?: unknown, auth: 'cookie'|'token'|'none'='cookie') => api<T>(path, { method: 'PUT', body, auth }),
  patch:<T>(path: string, body?: unknown, auth: 'cookie'|'token'|'none'='cookie') => api<T>(path, { method: 'PATCH', body, auth }),
  delete:<T>(path: string, auth: 'cookie'|'token'|'none'='cookie') => api<T>(path, { method: 'DELETE', auth }),
};
