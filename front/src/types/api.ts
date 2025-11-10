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
    UNAUTHORIZED: 'CMN006',
    
    // user 도메인
    LOGIN_FAILED: 'U001',
    EMAIL_VERIFY_FAILED: 'U002',
    NAME_NOT_FOUND: 'U003',
    PASSWORD_NOT_FOUND: 'U004',
    PASSWORD_NOT_EQUAL: 'U005',
    EMAIL_NOT_FOUND: 'U006',
    ALREADY_REGISTERED_EMAIL: 'U007',

    // Analysis 도메인
    INVALID_GITHUB_URL: 'A001',
    INVALID_REPOSITORY_PATH: 'A002',
    ANALYSIS_NOT_FOUND: 'A003',
    USER_NOT_FOUND: 'A004',
    FORBIDDEN: 'A005',
    ANALYSIS_IN_PROGRESS: 'A006',
    
    // Repository 도메인
    GITHUB_REPO_NOT_FOUND: 'G001',
    GITHUB_API_SERVER_ERROR: 'G002',
    GITHUB_RATE_LIMIT_EXCEEDED: 'G003',
    GITHUB_INVALID_TOKEN: 'G004',
    GITHUB_RESPONSE_PARSE_ERROR: 'G005',
    GITHUB_API_FAILED: 'G006',
    GITHUB_REPO_TOO_LARGE: 'G007',

    // comment 도메인
    COMMENT_NOT_FOUND: 'R001',

    // 프론트엔드 전용 에러
    NETWORK_ERROR: 'FE001',
    TIMEOUT_ERROR: 'FE002',
    INVALID_RESPONSE: 'FE003'
  } as const;
  
  export type ErrorCode = typeof ERROR_CODES[keyof typeof ERROR_CODES];
  
  // ========== 에러 메시지 매핑 ==========
  export const ERROR_MESSAGES: Record<string, string> = {
    // 공통
    [ERROR_CODES.VALIDATION_FAILED]: '입력값 검증에 실패했습니다.',
    [ERROR_CODES.INTERNAL_ERROR]: '서버 내부 오류가 발생했습니다.',
    [ERROR_CODES.INVALID_INPUT_VALUE]: '잘못된 입력값입니다.',
    [ERROR_CODES.INVALID_TYPE_VALUE]: '잘못된 타입의 값입니다.',
    [ERROR_CODES.MISSING_REQUEST_PARAMETER]: '필수 요청 파라미터가 누락되었습니다.',
    [ERROR_CODES.UNAUTHORIZED]: '인증이 필요합니다.',
    
    // user 도메인
    [ERROR_CODES.LOGIN_FAILED]: '로그인에 실패했습니다.',
    [ERROR_CODES.EMAIL_VERIFY_FAILED]: '이메일 인증코드가 일치하지 않습니다.',
    [ERROR_CODES.NAME_NOT_FOUND]: '이름이 입력되지 않았습니다.',
    [ERROR_CODES.PASSWORD_NOT_FOUND]: '비밀번호가 입력되지 않았습니다.',
    [ERROR_CODES.PASSWORD_NOT_EQUAL]: '비밀번호 확인이 일치하지 않습니다.',
    [ERROR_CODES.EMAIL_NOT_FOUND]: '해당 이메일은 없는 계정입니다.',
    [ERROR_CODES.ALREADY_REGISTERED_EMAIL]: '이미 회원가입된 이메일입니다.',

    [ERROR_CODES.INVALID_GITHUB_URL]: '올바른 GitHub 저장소 URL이 아닙니다.',
    [ERROR_CODES.INVALID_REPOSITORY_PATH]:
      '저장소 URL 형식이 잘못되었습니다. (예: https://github.com/{owner}/{repo})',
    [ERROR_CODES.ANALYSIS_NOT_FOUND]: '분석 결과를 찾을 수 없습니다.',
    [ERROR_CODES.USER_NOT_FOUND]: '사용자 정보를 찾을 수 없습니다.',
    [ERROR_CODES.FORBIDDEN]: '접근 권한이 없습니다.',
    [ERROR_CODES.ANALYSIS_IN_PROGRESS]: '이미 분석이 진행 중입니다. 잠시 후 다시 시도해주세요.',

    // repository 도메인
    [ERROR_CODES.GITHUB_REPO_NOT_FOUND]: 'GitHub 저장소를 찾을 수 없습니다.',
    [ERROR_CODES.GITHUB_API_SERVER_ERROR]: 'GitHub API 서버에서 오류가 발생했습니다.',
    [ERROR_CODES.GITHUB_RATE_LIMIT_EXCEEDED]: 'GitHub API 호출 제한을 초과했습니다.',
    [ERROR_CODES.GITHUB_INVALID_TOKEN]: 'GitHub 인증 토큰이 유효하지 않습니다.',
    [ERROR_CODES.GITHUB_RESPONSE_PARSE_ERROR]:
      'GitHub 응답 데이터를 처리하는 중 오류가 발생했습니다.',
    [ERROR_CODES.GITHUB_API_FAILED]: 'GitHub API 응답에 실패했습니다.',
    [ERROR_CODES.GITHUB_REPO_TOO_LARGE]: '저장소가 너무 커서 분석할 수 없습니다.',

    // comment 도메인
    [ERROR_CODES.COMMENT_NOT_FOUND]: '댓글을 찾을 수 없습니다.',

    // 프론트엔드 전용
    [ERROR_CODES.NETWORK_ERROR]: '네트워크 연결을 확인해주세요.',
    [ERROR_CODES.TIMEOUT_ERROR]: '요청 시간이 초과되었습니다.',
    [ERROR_CODES.INVALID_RESPONSE]: '서버 응답 형식이 올바르지 않습니다.'
  };
  
  // ========== API 요청 옵션 ==========
  export interface ApiRequestOptions {
    method?: HttpMethod;
    body?: unknown;
    headers?: Record<string, string>;
    auth?: AuthType;
    timeout?: number;
  }