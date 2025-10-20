import { ERROR_CODES, ERROR_MESSAGES } from '@/types/api';

export class ApiError extends Error {
  constructor(
    public code: string,
    public message: string,
    public status: number,
    public data?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }

  // 🔥 에러 코드로부터 메시지 자동 생성
  static fromCode(code: string, status: number, data?: any): ApiError {
    const message = ERROR_MESSAGES[code] || '알 수 없는 오류가 발생했습니다.';
    return new ApiError(code, message, status, data);
  }
}

export class ValidationError extends ApiError {
  constructor(message: string, field?: string) {
    super(ERROR_CODES.VALIDATION_FAILED, message, 400, { field });
    this.name = 'ValidationError';
  }
}

export class NetworkError extends ApiError {
  constructor(message: string = ERROR_MESSAGES[ERROR_CODES.NETWORK_ERROR]) {
    super(ERROR_CODES.NETWORK_ERROR, message, 0);
    this.name = 'NetworkError';
  }
}

export class GitHubApiError extends ApiError {
  constructor(code: string, message: string, status: number) {
    super(code, message, status);
    this.name = 'GitHubApiError';
  }
}

// 🔥 인증 관련 에러들
export class UnauthorizedError extends ApiError {
  constructor(message: string = ERROR_MESSAGES[ERROR_CODES.UNAUTHORIZED]) {
    super(ERROR_CODES.UNAUTHORIZED, message, 401);
    this.name = 'UnauthorizedError';
  }
}

export class ForbiddenError extends ApiError {
  constructor(message: string = ERROR_MESSAGES[ERROR_CODES.FORBIDDEN]) {
    super(ERROR_CODES.FORBIDDEN, message, 403);
    this.name = 'ForbiddenError';
  }
}