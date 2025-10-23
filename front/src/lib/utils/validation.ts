// 유효성 검사 유틸리티 함수들

export interface ValidationResult {
  isValid: boolean
  message?: string
}

/**
 * 이름 유효성 검사
 * - 최소 3글자, 최대 10글자
 * - 특수문자 허용하지 않음
 */
export function validateName(name: string): ValidationResult {
  if (!name || name.trim().length === 0) {
    return { isValid: false, message: '이름을 입력해주세요.' }
  }

  const trimmedName = name.trim()
  
  if (trimmedName.length < 3) {
    return { isValid: false, message: '이름은 최소 3글자 이상이어야 합니다.' }
  }

  if (trimmedName.length > 10) {
    return { isValid: false, message: '이름은 최대 10글자까지 입력 가능합니다.' }
  }

  // 특수문자 검사 (한글, 영문, 숫자만 허용)
  const nameRegex = /^[가-힣a-zA-Z0-9]+$/
  if (!nameRegex.test(trimmedName)) {
    return { isValid: false, message: '이름에는 특수문자를 사용할 수 없습니다.' }
  }

  return { isValid: true }
}

/**
 * 비밀번호 유효성 검사
 * - 최소 8글자, 최대 20글자
 * - 특수문자 1개 이상 포함
 */
export function validatePassword(password: string): ValidationResult {
  if (!password || password.length === 0) {
    return { isValid: false, message: '비밀번호를 입력해주세요.' }
  }

  if (password.length < 8) {
    return { isValid: false, message: '비밀번호는 최소 8글자 이상이어야 합니다.' }
  }

  if (password.length > 20) {
    return { isValid: false, message: '비밀번호는 최대 20글자까지 입력 가능합니다.' }
  }

  // 특수문자 검사
  const specialCharRegex = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/
  if (!specialCharRegex.test(password)) {
    return { isValid: false, message: '비밀번호에는 특수문자를 1개 이상 포함해야 합니다.' }
  }

  return { isValid: true }
}

/**
 * 비밀번호 확인 검사
 */
export function validatePasswordConfirm(password: string, passwordConfirm: string): ValidationResult {
  if (!passwordConfirm || passwordConfirm.length === 0) {
    return { isValid: false, message: '비밀번호 확인을 입력해주세요.' }
  }

  if (password !== passwordConfirm) {
    return { isValid: false, message: '비밀번호가 일치하지 않습니다.' }
  }

  return { isValid: true }
}

/**
 * 전체 비밀번호 변경 폼 유효성 검사
 */
export function validatePasswordChangeForm(password: string, passwordConfirm: string): ValidationResult {
  const passwordValidation = validatePassword(password)
  if (!passwordValidation.isValid) {
    return passwordValidation
  }

  const confirmValidation = validatePasswordConfirm(password, passwordConfirm)
  if (!confirmValidation.isValid) {
    return confirmValidation
  }

  return { isValid: true }
}