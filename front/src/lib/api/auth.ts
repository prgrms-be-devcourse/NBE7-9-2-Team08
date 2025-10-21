// auth 도메인 API
import { http } from './client'
import type { LoginRequest, LoginResponse, SignupRequest, SignupResponse } from '@/types/auth'

export const authApi = {
  /**
   * 로그인
   * POST /api/v1/auth/login
   */
  login: (data: LoginRequest): Promise<LoginResponse> =>
    http.post('/api/v1/auth/login', data, 'cookie'),

  /**
   * 회원가입
   * POST /api/v1/auth/signup
   */
  signup: (data: SignupRequest): Promise<SignupResponse> =>
    http.post('/api/v1/auth/signup', data, 'none'),

  /**
   * 로그아웃
   * POST /api/v1/auth/logout
   */
  logout: (): Promise<void> =>
    http.post('/api/v1/auth/logout'),

  /**
   * 토큰 갱신
   * POST /api/v1/auth/refresh
   */
  refreshToken: (): Promise<LoginResponse> =>
    http.post('/api/v1/auth/refresh'),
}