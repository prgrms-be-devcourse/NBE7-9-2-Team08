"use client"

import { useState } from "react"
import { analysisApi } from "@/lib/api/analysis"
import type { ApiError } from "@/lib/errors/custom-errors"

export function useAnalysis() {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  const requestAnalysis = async (githubUrl: string) => {
    try {
      setIsLoading(true)
      setError(null)
      
      await analysisApi.requestAnalysis(githubUrl)
      
    } catch (err) {
      if (err instanceof Error) {
        setError(err as ApiError)
      } else {
        setError(new Error('분석 요청 중 오류가 발생했습니다.') as ApiError)
      }
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const getMemberHistory = async (memberId: number) => {
    try {
      setIsLoading(true)
      setError(null)
      
      const history = await analysisApi.getMemberHistory(memberId)
      return history
      
    } catch (err) {
      if (err instanceof Error) {
        setError(err as ApiError)
      }
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  return {
    requestAnalysis,
    getMemberHistory,
    isLoading,
    error,
    clearError: () => setError(null)
  }
}