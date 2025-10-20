// api 응답 타입

// ========== 백엔드 ApiResponse 구조 ==========
export interface ApiResponse<T> {
    code: string;
    message: string;
    data: T;
  }
  
  // ========== 에러 응답 구조 ==========
  export interface ErrorResponse {
    code: string;
    message: string;
    data?: any;
  }
  
  // ========== HTTP 관련 타입들 ==========
  export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  
  export type AuthType = 'cookie' | 'token' | 'none';
  
  // ========== 공통 요청/응답 타입들 ==========
  export interface PaginationRequest {
    page: number;
    size: number;
    sort?: string;
  }
  
  export interface PaginationResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
  }
  
  export interface SearchRequest extends PaginationRequest {
    keyword?: string;
    category?: string;
  }
  
  // ========== 공통 응답 상태 ==========
  export type ApiStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  
  // ========== 백엔드 ErrorCode 매핑 ==========
  export const ERROR_CODES = {
    // 공통 에러
    VALIDATION_FAILED: 'CMN001',
    INTERNAL_ERROR: 'CMN002',
    INVALID_INPUT_VALUE: 'CMN003',
    INVALID_TYPE_VALUE: 'CMN004',
    MISSING_REQUEST_PARAMETER: 'CMN005',
    
    // Analysis 도메인
    INVALID_GITHUB_URL: 'A001',
    INVALID_REPOSITORY_PATH: 'A002',
    ANALYSIS_NOT_FOUND: 'A003',
    
    // Repository 도메인
    GITHUB_REPO_NOT_FOUND: 'G001',
    GITHUB_API_SERVER_ERROR: 'G002',
    GITHUB_RATE_LIMIT_EXCEEDED: 'G003',
    GITHUB_INVALID_TOKEN: 'G004',
    GITHUB_RESPONSE_PARSE_ERROR: 'G005',
    GITHUB_API_FAILED: 'G006',
    
    // 프론트엔드 전용 에러
    NETWORK_ERROR: 'FE001',
    TIMEOUT_ERROR: 'FE002',
    INVALID_RESPONSE: 'FE003',
    UNAUTHORIZED: 'FE004',
    FORBIDDEN: 'FE005',
  } as const;
  
  export type ErrorCode = typeof ERROR_CODES[keyof typeof ERROR_CODES];
  
  // ========== 에러 메시지 매핑 ==========
  export const ERROR_MESSAGES: Record<string, string> = {
    [ERROR_CODES.VALIDATION_FAILED]: '입력값 검증에 실패했습니다.',
    [ERROR_CODES.INTERNAL_ERROR]: '서버 내부 오류가 발생했습니다.',
    [ERROR_CODES.INVALID_INPUT_VALUE]: '잘못된 입력값입니다.',
    [ERROR_CODES.INVALID_GITHUB_URL]: '올바른 GitHub 저장소 URL이 아닙니다.',
    [ERROR_CODES.INVALID_REPOSITORY_PATH]: '저장소 URL 형식이 잘못되었습니다.',
    [ERROR_CODES.ANALYSIS_NOT_FOUND]: '분석 결과를 찾을 수 없습니다.',
    [ERROR_CODES.GITHUB_REPO_NOT_FOUND]: 'GitHub 저장소를 찾을 수 없습니다.',
    [ERROR_CODES.GITHUB_API_SERVER_ERROR]: 'GitHub API 서버에서 오류가 발생했습니다.',
    [ERROR_CODES.GITHUB_RATE_LIMIT_EXCEEDED]: 'GitHub API 호출 제한을 초과했습니다.',
    [ERROR_CODES.NETWORK_ERROR]: '네트워크 연결을 확인해주세요.',
    [ERROR_CODES.TIMEOUT_ERROR]: '요청 시간이 초과되었습니다.',
    [ERROR_CODES.UNAUTHORIZED]: '인증이 필요합니다.',
    [ERROR_CODES.FORBIDDEN]: '접근 권한이 없습니다.',
  };
  
  // ========== API 요청 옵션 ==========
  export interface ApiRequestOptions {
    method?: HttpMethod;
    body?: unknown;
    headers?: Record<string, string>;
    auth?: AuthType;
    timeout?: number;
  }