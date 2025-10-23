// analysis, repository 도메인 타입 types/analysis.ts

// 분석 요청 DTO, 사용자가 GitHub 저장소 URL을 입력하여 분석을 요청할 때 사용
export interface AnalysisRequest {
    /** GitHub 저장소 URL (예: https://github.com/user/repo) */
    githubUrl: string
}

export interface AnalysisStartResponse {
    repositoryId: number
}

// Repository 기본 정보 응답 DTO, 사용자의 Repository 목록 조회 시 사용
export interface RepositoryResponse {
    id: number
    name: string
    description: string | null
    htmlUrl: string
    publicRepository: boolean
    mainBranch: string
    languages: string[]
  }
  
// Repository 상세 정보 + 분석 버전 목록 응답 DTO, 특정 Repository의 모든 분석 버전을 조회할 때 사용
export interface HistoryResponseDto {
    repository: RepositoryResponse
    analysisVersions: AnalysisVersionDto[]
}
  
// 분석 버전(히스토리) 단일 항목 DTO
export interface AnalysisVersionDto {
    analysisId: number
    analysisDate: string        // ISO 8601 문자열 (LocalDateTime → string)
    totalScore: number
    versionLabel: string        // 예: "v3 (2025-10-21)"
}

// 특정 분석 결과의 상세 정보 응답 DTO
export interface AnalysisResultResponseDto {
    totalScore: number
    readmeScore: number
    testScore: number
    commitScore: number
    cicdScore: number
    summary: string
    strengths: string
    improvements: string
    createDate: string
}
  