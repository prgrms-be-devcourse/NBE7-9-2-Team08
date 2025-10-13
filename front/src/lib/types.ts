export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  accessToken?: string; // 토큰 기반일 때
  user?: { id: number; email: string; name: string };
  message?: string;
};

export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type User = {
  id: number;
  email: string;
  name: string;
  createdAt?: string;
};
