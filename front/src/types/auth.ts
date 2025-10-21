// auth 도메인 타입
export interface LoginRequest {
  username: string; // email 또는 username
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  passwordCheck: string;
  name: string;
  imageUrl?: string; // 선택사항
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
  email: string;
  name: string;
  imageUrl?: string;
}