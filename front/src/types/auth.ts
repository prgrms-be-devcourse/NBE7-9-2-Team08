// auth 도메인 타입
export interface LoginRequest {
  email: string; // email 또는 username
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
  user: {
    id: number;
    email: string;
    name: string;
    imageUrl: string | null;
  };
}

export interface SignupResponse {
  message: string;
  userId?: number;
}

export interface User {
  id: number;
  email: string;
  name: string;
  imageUrl: string | null;
}