// src/hooks/analysis/useAnalysis.ts
"use client"

import { useState } from "react"
import { analysisApi } from "@/lib/api/analysis"
import type { ApiError } from "@/lib/errors/custom-errors"
import type { RepositoryResponse } from "@/types/analysis"

export function useAnalysis() {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  /** 🔍 분석 요청 */
  const requestAnalysis = async (githubUrl: string) => {
    setIsLoading(true)
    setError(null)

    try {
      // ✅ API 호출 — client.ts가 data 추출하므로 result.data 대신 바로 result
      const result = await analysisApi.requestAnalysis({ githubUrl })
      return result // { repositoryId, message, ... } 형식의 결과 예상
    } catch (err: any) {
      // ✅ 에러 객체 일관 처리
      const message =
        err?.response?.data?.message ||
        err?.message ||
        "분석 요청 중 오류가 발생했습니다."
      const apiError = new Error(message) as ApiError
      setError(apiError)
      throw apiError
    } finally {
      setIsLoading(false)
    }
  }

  /** 📦 사용자별 저장소 목록 조회 */
  const getUserRepositories = async (userId: number): Promise<RepositoryResponse[]> => {
    setIsLoading(true)
    setError(null)

    try {
      const repositories = await analysisApi.getUserRepositories(userId)
      return repositories
    } catch (err: any) {
      const message =
        err?.response?.data?.message ||
        err?.message ||
        "저장소 목록을 불러오는 중 오류가 발생했습니다."
      const apiError = new Error(message) as ApiError
      setError(apiError)
      throw apiError
    } finally {
      setIsLoading(false)
    }
  }

  /** ❌ 에러 초기화 */
  const clearError = () => setError(null)

  return {
    requestAnalysis,
    getUserRepositories,
    isLoading,
    error,
    clearError,
  }
}
