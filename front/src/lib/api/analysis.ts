// analysis 도메인 API
import { http } from './client'

// ===== 타입 정의 =====
export interface AnalysisRequest {
  githubUrl: string
}

export interface HistoryResponseDto {
  repositoryName: string
  createDate: string // LocalDateTime -> string (ISO 형식)
  languages: string[]
  totalScore: number
  publicStatus: boolean
}

// ===== Analysis API 함수들 =====
export const analysisApi = {
  /**
   * GitHub 저장소 분석 요청
   * POST /api/analysis
   */
  requestAnalysis: (githubUrl: string): Promise<void> =>
    http.post('/analysis', { githubUrl }),

  /**
   * 사용자별 분석 히스토리 조회
   * GET /api/analysis/user/{memberId}
   */
  getMemberHistory: (memberId: number): Promise<HistoryResponseDto[]> =>
    http.get(`/analysis/user/${memberId}`),
}