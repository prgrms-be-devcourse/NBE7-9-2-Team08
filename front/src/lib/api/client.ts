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
    method = "GET",
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

  try {
    const res = await fetch(url, {
      method,
      headers: h,
      body: body ? JSON.stringify(body) : undefined,
      credentials: 'include',
      cache: 'no-store',
      next,
    });

    const text = await res.text();
    const responseData = text ? JSON.parse(text) : null;

    if (res.status === 401) {
      console.warn("🔐 토큰 만료 — 로그인 페이지로 이동합니다.");
      if (typeof window !== "undefined") {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("user");
        window.location.href = "/login";
      }
      // 즉시 리턴해서 아래 로직 수행 안 함
      throw new Error("Unauthorized");
    }
    
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
  get: <T>(path: string) => api<T>(path, { method: "GET" }),
  post: <T>(path: string, body?: unknown) => api<T>(path, { method: "POST", body }),
  put: <T>(path: string, body?: unknown) => api<T>(path, { method: "PUT", body }),
  patch: <T>(path: string, body?: unknown) => api<T>(path, { method: "PATCH", body }),
  delete: <T>(path: string) => api<T>(path, { method: "DELETE" }),
};

// 공개 API (로그인 전)
export const publicHttp = {
  get: <T>(path: string) => api<T>(path, { method: "GET", auth: "none" }),
  post: <T>(path: string, body?: unknown) => api<T>(path, { method: "POST", body, auth: "none" }),
};