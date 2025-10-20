// lib/errors/error-handler.ts
import { 
    ApiError, 
    NetworkError, 
    ValidationError,
    UnauthorizedError,
    ForbiddenError 
  } from './custom-errors';
  import { ERROR_CODES, type ErrorResponse } from '@/types/api';
  
  export class ErrorHandler {
    static handle(error: unknown): ApiError {
      // 이미 우리가 만든 커스텀 에러인 경우
      if (error instanceof ApiError) {
        return error;
      }
      
      // 네트워크 에러
      if (error instanceof TypeError && error.message.includes('fetch')) {
        return new NetworkError();
      }
      
      // 알 수 없는 에러
      return ApiError.fromCode(ERROR_CODES.INTERNAL_ERROR, 500);
    }
    
    static handleFetchError(response: Response, errorData?: ErrorResponse): ApiError {
      // 백엔드에서 보낸 구조화된 에러 응답 처리
      if (errorData?.code) {
        return ApiError.fromCode(errorData.code, response.status, errorData.data);
      }
      
      // HTTP 상태코드 기반 처리
      switch (response.status) {
        case 400:
          return new ValidationError('잘못된 요청입니다.');
        case 401:
          return new UnauthorizedError();
        case 403:
          return new ForbiddenError();
        case 404:
          return ApiError.fromCode(ERROR_CODES.ANALYSIS_NOT_FOUND, 404);
        case 429:
          return ApiError.fromCode(ERROR_CODES.GITHUB_RATE_LIMIT_EXCEEDED, 429);
        default:
          return ApiError.fromCode(ERROR_CODES.INTERNAL_ERROR, response.status);
      }
    }
    
    // 에러 로깅 메서드 추가
    static logError(error: ApiError, context?: string): void {
      console.error(`[${context || 'API_ERROR'}]`, {
        code: error.code,
        message: error.message,
        status: error.status,
        data: error.data,
        stack: error.stack,
      });
    }
  }