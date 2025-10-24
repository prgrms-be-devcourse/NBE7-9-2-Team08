"use client"

import { useEffect, useState, useCallback } from "react"
import { analysisApi } from "@/lib/api/analysis"
import type { HistoryResponseDto, AnalysisResultResponseDto } from "@/types/analysis"
import { formatDateTimeKST } from "@/lib/utils/formatDate"

export function useAnalysisResult(userId?: number, repoId?: number) {
  const [history, setHistory] = useState<HistoryResponseDto | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [result, setResult] = useState<AnalysisResultResponseDto | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    if (!userId || !repoId) return
    setLoading(true)

    try {
      // 1️⃣ 히스토리 불러오기
      const data = await analysisApi.getRepositoryHistory(userId, repoId)

      // 최신순 정렬
      const sorted = [...data.analysisVersions].sort(
        (a, b) => new Date(b.analysisDate).getTime() - new Date(a.analysisDate).getTime()
      )

      // 라벨링
      const relabeled = sorted.map((ver, idx) => ({
        ...ver,
        versionLabel: `v${sorted.length - idx} (${formatDateTimeKST(ver.analysisDate)})`,
      }))

      const updatedHistory = { ...data, analysisVersions: relabeled }
      setHistory(updatedHistory)

      // 2️⃣ 최신 버전 자동 선택
      const latestId = relabeled[0]?.analysisId ?? null
      setSelectedId(latestId)

      // 3️⃣ 선택된 분석 결과 불러오기
      if (latestId) {
        const detail = await analysisApi.getAnalysisDetail(userId, repoId, latestId)
        setResult(detail)
      } else {
        setResult(null)
      }
    } catch (err) {
      console.error("❌ useAnalysisResult load() error:", err)
    } finally {
      setLoading(false)
    }
  }, [userId, repoId])

  /**
   * 🎬 최초 로드
   */
  useEffect(() => {
    load()
  }, [load])

  /**
   * 🔄 특정 버전 선택 시 분석 결과 다시 로드
   */
  useEffect(() => {
    if (!selectedId || !userId || !repoId) return
    ;(async () => {
      const detail = await analysisApi.getAnalysisDetail(userId, repoId, selectedId)
      setResult(detail)
    })()
  }, [selectedId, userId, repoId])

  return {
    history,
    result,
    loading,
    selectedId,
    setSelectedId,
    reload: load, // ✅ 외부에서 다시 불러올 수 있도록 노출
  }
}