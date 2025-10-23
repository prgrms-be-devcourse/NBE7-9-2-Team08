"use client"

import { useEffect, useState } from "react"
import { analysisApi } from "@/lib/api/analysis"
import type { HistoryResponseDto, AnalysisResultResponseDto } from "@/types/analysis"
import { formatDate } from "@/lib/utils/formatDate"

export function useAnalysisResult(userId?: number, repoId?: number) {
  const [history, setHistory] = useState<HistoryResponseDto | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [result, setResult] = useState<AnalysisResultResponseDto | null>(null)
  const [loading, setLoading] = useState(true)

  // Repository 분석 히스토리 불러오기
  useEffect(() => {
    if (!userId || !repoId) return
    ;(async () => {
      try {
        const data = await analysisApi.getRepositoryHistory(userId, repoId)
        const sorted = [...data.analysisVersions].sort(
          (a, b) => new Date(b.analysisDate).getTime() - new Date(a.analysisDate).getTime()
        )

        const relabeled = sorted.map((ver, idx) => ({
          ...ver,
          versionLabel: `v${sorted.length - idx} (${formatDate(ver.analysisDate)})`,
        }))

        setHistory({ ...data, analysisVersions: relabeled })
        if (relabeled.length > 0) setSelectedId(relabeled[0].analysisId)
      } finally {
        setLoading(false)
      }
    })()
  }, [userId, repoId])

  // 선택된 분석 결과 불러오기
  useEffect(() => {
    if (!selectedId || !userId || !repoId) return
    ;(async () => {
      const detail = await analysisApi.getAnalysisDetail(userId, repoId, selectedId)
      setResult(detail)
    })()
  }, [selectedId, userId, repoId])

  return { history, result, loading, selectedId, setSelectedId }
}
