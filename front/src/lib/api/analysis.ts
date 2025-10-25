// src/lib/api/analysis.ts
import { http } from "./client"
import { fetchEventSource } from "@microsoft/fetch-event-source"

import type {
  AnalysisRequest,
  AnalysisStartResponse, 
  RepositoryResponse,
  HistoryResponseDto,
  AnalysisResultResponseDto,
} from "@/types/analysis"

// ===== Analysis API =====
export const analysisApi = {
  /** 🔍 GitHub 저장소 분석 요청 (POST)
   *  백엔드: POST /api/analysis
   *  - JWT에서 자동으로 userId 추출
   *  - client.ts에서 이미 data를 추출하므로 직접 AnalysisStartResponse 반환
   */
  requestAnalysis: (
    data: AnalysisRequest
  ): Promise<AnalysisStartResponse> =>  // ✅ ApiResponse 제거
    http.post(`/analysis`, data),

  /** 📦 사용자별 Repository 목록 조회
   *  GET /api/analysis/repositories
   *  - userId는 로그인한 사용자의 ID여야 함 (JWT 검증)
   */
  getUserRepositories: (): Promise<RepositoryResponse[]> =>  // ✅ ApiResponse 제거
    http.get(`/analysis/repositories`),

  /** 🕓 특정 Repository의 분석 히스토리 조회
   *  GET /api/analysis/repositories/{repoId}
   */
  getRepositoryHistory: (
    repoId: number
  ): Promise<HistoryResponseDto> =>  // ✅ ApiResponse 제거
    http.get(`/analysis/repositories/${repoId}`),

  /** 🧠 특정 분석 결과 상세 조회
   *  GET /api/analysis/repositories/{repoId}/results/{analysisId}
   */
  getAnalysisDetail: (
    repoId: number,
    analysisId: number
  ): Promise<AnalysisResultResponseDto> =>  // ✅ ApiResponse 제거
    http.get(
      `/analysis/repositories/${repoId}/results/${analysisId}`
    ),

  /** 🗑️ Repository 삭제
   *  DELETE /api/analysis/{userId}/repositories/{repositoryId}
   */
  deleteRepository: (
    userId: number,
    repositoryId: number
  ): Promise<void> =>  // ✅ ApiResponse 제거
    http.delete(`/analysis/${userId}/repositories/${repositoryId}`),

  /** 🗑️ 특정 분석 결과 삭제
   *  DELETE /api/analysis/{userId}/repositories/{repositoryId}/results/{analysisId}
   */
  deleteAnalysisResult: (
    userId: number,
    repositoryId: number,
    analysisId: number
  ): Promise<void> =>  // ✅ ApiResponse 제거
    http.delete(
      `/analysis/${userId}/repositories/${repositoryId}/results/${analysisId}`
    ),

  /** 🌐 분석 결과 공개 여부 변경
   *  PUT /api/analysis/{userId}/repositories/{repositoryId}/public
   */
  updatePublicStatus: (
    userId: number,
    repositoryId: number
  ): Promise<void> =>  // ✅ ApiResponse 제거
    http.put(`/analysis/${userId}/repositories/${repositoryId}/public`),

  /** 📡 SSE: 분석 진행 현황 구독 (쿠키 기반 인증용) */
  connectStream: (userId: number) => {
    const baseUrl = process.env.NEXT_PUBLIC_BACKEND_URL
    const controller = new AbortController()
  
    fetchEventSource(`${baseUrl}/api/analysis/stream/${userId}`, {
      method: "GET",
      headers: { Accept: "text/event-stream" },
      credentials: "include",
      openWhenHidden: true,
      signal: controller.signal,
      onopen: async (response) => {
        if (!response.ok) {
          console.error("[SSE][error] 연결 실패", response.status)
          if ([401, 403].includes(response.status))
            window.dispatchEvent(new CustomEvent("SSE_AUTH_ERROR"))
          throw new Error(`SSE 연결 실패: ${response.status}`)
        }
        console.log("[SSE][connected] 연결 성공")
      },
      onmessage(event) {
        console.log("[SSE][message]", event.event, event.data)
        window.dispatchEvent(
          new CustomEvent(`SSE_${event.event.toUpperCase()}`, { detail: event.data })
        )
      },
      onerror(err) {
        console.error("[SSE][error]", err)
        window.dispatchEvent(new CustomEvent("SSE_ERROR", { detail: err }))
        return 1000 // 1초 후 재연결 시도
      },
    })
  
    return () => controller.abort() // 컴포넌트 unmount 시 종료
  }  
}