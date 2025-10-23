import type { 
  ApiResponse, 
  ErrorResponse, 
  HttpMethod, 
  AuthType, 
  ApiRequestOptions 
} from '@/types/api';
import { ErrorHandler } from '@/lib/errors/error-handler';
import { getJwtToken } from '@/lib/utils/cookies';

const BASE = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';
const AUTH_HEADER = process.env.NEXT_PUBLIC_AUTH_HEADER || 'Authorization';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  
  // 쿠키에서 JWT 토큰 가져오기
  const jwtToken = getJwtToken();
  if (jwtToken) {
    return jwtToken;
  }
  
  // 쿠키에 없으면 localStorage에서 가져오기 (기존 방식)
  const localToken = localStorage.getItem('accessToken');
  if (localToken && localToken !== 'logged_in') {
    return localToken;
  }
  
  return null;
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
  // 프록시 모드 비활성화 - 항상 백엔드 서버로 직접 요청
  const url = isAbsolute 
    ? path 
    : `${BASE}${path.startsWith('/') ? path : `/${path}`}`;

  console.log('=== API 호출 디버깅 ===');
  console.log('BASE:', BASE);
  console.log('path:', path);
  console.log('isAbsolute:', isAbsolute);
  console.log('NEXT_PUBLIC_DEV_PROXY:', process.env.NEXT_PUBLIC_DEV_PROXY);
  console.log('최종 URL:', url);
  console.log('========================');

  const h: Record<string, string> = {
    'Content-Type': 'application/json',
    ...headers,
  };

  // 인증 토큰 추가
  if (auth === 'token') {
    const token = getToken();
    console.log('API 호출 - 토큰:', token ? `${token.substring(0, 20)}...` : '없음');
    if (token) {
      h[AUTH_HEADER] = `Bearer ${token}`;
    }
  }

  try {
    console.log('=== Fetch 요청 시작 ===');
    console.log('요청 URL:', url);
    console.log('요청 메서드:', method);
    console.log('요청 헤더:', h);
    console.log('요청 바디:', body);
    console.log('=======================');
    
    const res = await fetch(url, {
      method,
      headers: h,
      body: body ? JSON.stringify(body) : undefined,
      credentials: 'include',
      cache: 'no-store',
      next,
    });
    
    console.log('=== Fetch 응답 받음 ===');
    console.log('응답 상태:', res.status);
    console.log('응답 URL:', res.url);
    console.log('=======================');

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