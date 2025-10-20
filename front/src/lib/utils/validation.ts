// lib/utils/validation.ts

/**
 * GitHub URL 유효성 검증
 */
export function isValidGitHubUrl(url: string): boolean {
    if (!url || typeof url !== 'string') return false
    
    // GitHub URL 패턴 정규식
    const githubUrlPattern = /^https:\/\/github\.com\/[a-zA-Z0-9_.-]+\/[a-zA-Z0-9_.-]+\/?$/
    
    try {
      const trimmedUrl = url.trim()
      return githubUrlPattern.test(trimmedUrl)
    } catch {
      return false
    }
  }
  
  /**
   * 이메일 유효성 검증
   */
  export function isValidEmail(email: string): boolean {
    if (!email || typeof email !== 'string') return false
    
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailPattern.test(email.trim())
  }
  
  /**
   * 비밀번호 유효성 검증
   * 최소 8자, 대문자, 소문자, 숫자 포함
   */
  export function isValidPassword(password: string): boolean {
    if (!password || typeof password !== 'string') return false
    
    const minLength = password.length >= 8
    const hasUpperCase = /[A-Z]/.test(password)
    const hasLowerCase = /[a-z]/.test(password)
    const hasNumber = /\d/.test(password)
    
    return minLength && hasUpperCase && hasLowerCase && hasNumber
  }
  
  /**
   * 사용자명 유효성 검증
   * 2-20자, 영문/숫자/언더스코어만 허용
   */
  export function isValidUsername(username: string): boolean {
    if (!username || typeof username !== 'string') return false
    
    const usernamePattern = /^[a-zA-Z0-9_]{2,20}$/
    return usernamePattern.test(username.trim())
  }
  
  /**
   * 분석 상태 유효성 검증
   */
  export function isValidAnalysisStatus(status: string): boolean {
    const validStatuses = ['PENDING', 'ANALYZING', 'COMPLETED', 'FAILED']
    return validStatuses.includes(status)
  }
  
  /**
   * 점수 유효성 검증 (0-100)
   */
  export function isValidScore(score: number): boolean {
    return typeof score === 'number' && score >= 0 && score <= 100
  }
  
  /**
   * 페이지네이션 파라미터 유효성 검증
   */
  export function isValidPaginationParams(page: number, size: number): boolean {
    return (
      typeof page === 'number' && 
      typeof size === 'number' && 
      page >= 0 && 
      size > 0 && 
      size <= 100
    )
  }
  
  /**
   * 검색 키워드 유효성 검증
   */
  export function isValidSearchKeyword(keyword: string): boolean {
    if (!keyword || typeof keyword !== 'string') return false
    
    const trimmed = keyword.trim()
    return trimmed.length >= 1 && trimmed.length <= 100
  }
  
  /**
   * URL이 안전한지 검증 (기본적인 XSS 방지)
   */
  export function isSafeUrl(url: string): boolean {
    if (!url || typeof url !== 'string') return false
    
    try {
      const parsedUrl = new URL(url)
      // HTTP/HTTPS만 허용
      return ['http:', 'https:'].includes(parsedUrl.protocol)
    } catch {
      return false
    }
  }
  
  /**
   * 분석 요청 데이터 유효성 검증
   */
  export function validateAnalysisRequest(data: { repositoryUrl: string }): {
    isValid: boolean
    errors: string[]
  } {
    const errors: string[] = []
    
    if (!data.repositoryUrl) {
      errors.push('저장소 URL이 필요합니다.')
    } else if (!isValidGitHubUrl(data.repositoryUrl)) {
      errors.push('올바른 GitHub 저장소 URL을 입력해주세요.')
    }
    
    return {
      isValid: errors.length === 0,
      errors
    }
  }
  
  /**
   * 회원가입 데이터 유효성 검증
   */
  export function validateSignupData(data: {
    email: string
    password: string
    username: string
  }): {
    isValid: boolean
    errors: Record<string, string>
  } {
    const errors: Record<string, string> = {}
    
    if (!isValidEmail(data.email)) {
      errors.email = '올바른 이메일 주소를 입력해주세요.'
    }
    
    if (!isValidPassword(data.password)) {
      errors.password = '비밀번호는 8자 이상, 대소문자와 숫자를 포함해야 합니다.'
    }
    
    if (!isValidUsername(data.username)) {
      errors.username = '사용자명은 2-20자의 영문, 숫자, 언더스코어만 가능합니다.'
    }
    
    return {
      isValid: Object.keys(errors).length === 0,
      errors
    }
  }