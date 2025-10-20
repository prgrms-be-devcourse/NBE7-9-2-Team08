import type { 
  ApiResponse, 
  ErrorResponse, 
  HttpMethod, 
  AuthType, 
  ApiRequestOptions 
} from '@/types/api';
import { ErrorHandler } from '@/lib/errors/error-handler';

const BASE = process.env.NEXT_PUBLIC_BACKEND_URL;
const AUTH_HEADER = process.env.NEXT_PUBLIC_AUTH_HEADER || 'Authorization';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('accessToken');
}

export async function api<T = unknown>(
  path: string,
  opts: ApiRequestOptions & { next?: NextFetchRequestConfig } = {}
): Promise<T> {
  const {
    method = 'GET',
    body,
    headers = {},
    auth = 'token',
    next,
  } = opts;

  const isAbsolute = path.startsWith('http');
  const url = isAbsolute 
    ? path 
    : (process.env.NEXT_PUBLIC_DEV_PROXY === 'true' 
        ? `/api${path.startsWith('/') ? path : `/${path}`}` 
        : `${BASE}${path.startsWith('/') ? path : `/${path}`}`);

  const h: Record<string, string> = {
    'Content-Type': 'application/json',
    ...headers,
  };

  if (auth === 'token') {
    const token = getToken();
    if (token) h[AUTH_HEADER] = `Bearer ${token}`;
  }

  try {
    const res = await fetch(url, {
      method,
      headers: h,
      body: body ? JSON.stringify(body) : undefined,
      credentials: auth === 'cookie' ? 'include' : 'same-origin',
      cache: 'no-store',
      next,
    });

    const text = await res.text();
    const responseData = text ? JSON.parse(text) : null;

    if (!res.ok) {
      // 🔥 에러 응답 데이터를 함께 전달
      const errorResponse = responseData as ErrorResponse;
      const handledError = ErrorHandler.handleFetchError(res, errorResponse);
      
      // 에러 로깅
      ErrorHandler.logError(handledError, `API_${method}_${path}`);
      
      throw handledError;
    }

    // 백엔드 ApiResponse<T> 구조에서 data 추출
    const apiResponse = responseData as ApiResponse<T>;
    return apiResponse.data;

  } catch (error) {
    // 🔥 이미 ApiError인 경우 재처리하지 않음
    if (error instanceof ErrorHandler.constructor.prototype.constructor) {
      throw error;
    }
    
    // 🔥 다른 에러들만 ErrorHandler로 처리
    const handledError = ErrorHandler.handle(error);
    ErrorHandler.logError(handledError, `API_${method}_${path}`);
    throw handledError;
  }
}

export const http = {
  get: <T>(path: string, auth: AuthType = 'token') => 
    api<T>(path, { method: 'GET', auth }),
  
  post: <T>(path: string, body?: unknown, auth: AuthType = 'token') => 
    api<T>(path, { method: 'POST', body, auth }),
  
  put: <T>(path: string, body?: unknown, auth: AuthType = 'token') => 
    api<T>(path, { method: 'PUT', body, auth }),
  
  patch: <T>(path: string, body?: unknown, auth: AuthType = 'token') => 
    api<T>(path, { method: 'PATCH', body, auth }),
  
  delete: <T>(path: string, auth: AuthType = 'token') => 
    api<T>(path, { method: 'DELETE', auth }),
};

export const publicHttp = {
  get: <T>(path: string) => http.get<T>(path, 'none'),
  post: <T>(path: string, body?: unknown) => http.post<T>(path, body, 'none'),
  put: <T>(path: string, body?: unknown) => http.put<T>(path, body, 'none'),
  patch: <T>(path: string, body?: unknown) => http.patch<T>(path, body, 'none'),
  delete: <T>(path: string) => http.delete<T>(path, 'none'),
};