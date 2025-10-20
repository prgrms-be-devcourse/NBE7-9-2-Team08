// auth 도메인 타입
export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  message?: string;
}

export interface SignupResponse {
  message: string;
  userId?: number;
}

export interface User {
  id: number;
  username: string;
  email: string;
  createdAt: string;
}