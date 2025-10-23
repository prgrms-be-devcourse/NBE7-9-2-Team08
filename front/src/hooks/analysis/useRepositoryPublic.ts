"use client"

import { useState } from "react"
import { analysisApi } from "@/lib/api/analysis"

export function useRepositoryPublic(initial: boolean, userId?: number, repoId?: number) {
  const [isPublic, setIsPublic] = useState(initial)
  const [loading, setLoading] = useState(false)

  const togglePublic = async () => {
    if (!userId || !repoId) return
    try {
      setLoading(true)
      await analysisApi.updatePublicStatus(userId, repoId)
      setIsPublic((prev) => !prev)
    } catch (err) {
      console.error("❌ 공개 상태 변경 실패:", err)
      alert("리포지토리 공개 설정 변경 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  return { isPublic, togglePublic, loading }
}
