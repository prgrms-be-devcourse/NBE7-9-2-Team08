"use client"

import { useEffect, useState, useMemo } from "react"
import { analysisApi } from "@/lib/api/analysis"
import type { RepositoryResponse as RepoBaseResponse } from "@/types/history"
import type { HistoryResponseDto } from "@/types/analysis"

export function useHistory(memberId: number) {
  const [repositories, setRepositories] = useState<RepoBaseResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [sortType, setSortType] = useState<"latest" | "score">("latest")

  useEffect(() => {
    console.log("🧾 repositories:", repositories.map(r => ({
      id: r.id,
      createDate: r.createDate,
      latestScore: r.latestScore
    })))
  }, [repositories])

  
  async function load() {
    try {
      setLoading(true)
      const baseRepos = await analysisApi.getUserRepositories()

      const enrichedRepos: RepoBaseResponse[] = await Promise.all(
        baseRepos.map(async (repo): Promise<RepoBaseResponse> => {
          try {
            const historyData: HistoryResponseDto = await analysisApi.getRepositoryHistory(repo.id)
            
            const versions = [...historyData.analysisVersions].sort(
              (a, b) => new Date(b.analysisDate).getTime() - new Date(a.analysisDate).getTime()
            )
            const latest = versions[0] ?? null

            return {
              ...repo,
              latestScore: latest?.totalScore ?? null,
              latestAnalysisDate: latest?.analysisDate ?? null,
            }
          } catch (err) {
            console.error(`❌ 점수 불러오기 실패 (repoId: ${repo.id})`, err)
            return repo
          }
        })
      )

      setRepositories(enrichedRepos)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const parseDate = (date?: string | null) => {
    if (!date) return 0
    return Date.parse(date.split(".")[0] + "Z")
  }
  
  const sortedRepositories = useMemo(() => {
    if (sortType === "score") {
      return [...repositories].sort((a, b) => (b.latestScore ?? 0) - (a.latestScore ?? 0))
    }
  
    return [...repositories].sort(
      (a, b) =>
        parseDate(b.latestAnalysisDate ?? b.createDate) -
        parseDate(a.latestAnalysisDate ?? a.createDate)
    )
  }, [repositories, sortType])
  

  async function handleDelete(repoId: number) {
    try {
      await analysisApi.deleteRepository(memberId, repoId)
      setRepositories((prev) => prev.filter((repo) => repo.id !== repoId))
    } catch (err) {
      console.error("삭제 실패:", err)
      alert("삭제 중 오류가 발생했습니다.")
    }
  }

  return { repositories: sortedRepositories, loading, error, handleDelete, sortType, setSortType }
}