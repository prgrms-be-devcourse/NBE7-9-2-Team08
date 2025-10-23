// auth 도메인 API
import { http } from './client'
import type { LoginRequest, LoginResponse, SignupRequest, SignupResponse } from '@/types/auth'

export interface EmailVerificationRequest {
  email: string
}

export interface EmailVerificationResponse {
  message: string
}

export interface VerifyCodeRequest {
  email: string
  code: string
}

export interface VerifyCodeResponse {
  code: string
  message: string
  data: string
}

export interface GetUserRequest {
  email: string
}

export interface GetUserResponse {
  id: number
  email: string
  name: string
  imageUrl: string | null
}

export const authApi = {
  /**
   * 로그인
   * POST /api/v1/auth/login
   */
  login: (data: LoginRequest): Promise<LoginResponse> =>
    http.post('/login', data, 'cookie'),

  /**
   * 회원가입
   * POST /user
   */
  signup: (data: SignupRequest): Promise<SignupResponse> =>
    http.post('/user', data, 'none'),

  /**
   * 이메일 인증 요청
   * POST /auth
   */
  requestEmailVerification: async (data: EmailVerificationRequest): Promise<EmailVerificationResponse> => {
    const response = await fetch(
      process.env.NEXT_PUBLIC_DEV_PROXY === 'true' 
        ? `/api/auth` 
        : `${process.env.NEXT_PUBLIC_BACKEND_URL}/auth`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
        credentials: 'omit',
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    return result; // 전체 응답 객체 반환
  },

  /**
   * 인증번호 확인
   * POST /verify
   */
  verifyEmailCode: async (data: VerifyCodeRequest): Promise<VerifyCodeResponse> => {
    const response = await fetch(
      process.env.NEXT_PUBLIC_DEV_PROXY === 'true' 
        ? `/api/verify` 
        : `${process.env.NEXT_PUBLIC_BACKEND_URL}/verify`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
        credentials: 'omit',
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    return result; // 전체 응답 객체 반환
  },

  /**
   * 로그아웃
   * POST /api/v1/auth/logout
   */
  logout: (): Promise<void> =>
    http.post('/logout'),

  /**
   * 토큰 갱신
   * POST /api/v1/auth/refresh
   */
  refreshToken: (): Promise<LoginResponse> =>
    http.post('/api/refresh'),

  /**
   * 현재 로그인한 사용자 정보 조회
   * GET /api/user/me
   */
  getCurrentUser: async (): Promise<GetUserResponse> => {
    const response = await fetch(
      process.env.NEXT_PUBLIC_DEV_PROXY === 'true' 
        ? `/api/user/me` 
        : `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/user/me`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
        credentials: 'include',
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      console.error('사용자 정보 조회 실패:', response.status, errorText);
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    console.log('사용자 정보 응답:', result);
    return result.data.userDto; // GetResponse에서 userDto 추출
  },
}