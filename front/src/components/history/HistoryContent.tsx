"use client"

import { useState, useCallback } from "react"
import { useHistory } from "@/hooks/history/useHistory"
import { HistoryStats } from "@/components/history/HistoryStatsProps"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/Button"
import { ScoreBadge } from "@/components/history/ScoreBadge"
import { formatRelativeTimeKST } from "@/lib/utils/formatDate"
import { Github, ExternalLink, Trash2, Calendar, GitCompare, X } from "lucide-react"
import { RepositoryComparisonResponse } from "@/types/analysis"
import { useRouter } from "next/navigation"
import { HistoryCompare } from "@/components/history/HistoryCompare"
import { analysisApi } from "@/lib/api/analysis"

interface HistoryContentProps {
  memberId: number
  name: string
}

export default function HistoryContent({ memberId, name }: HistoryContentProps) {
  const { repositories, loading, error, handleDelete, sortType, setSortType } = useHistory(memberId)
  const router = useRouter()

  const [compareMode, setCompareMode] = useState(false)
  const [selectedRepoIds, setSelectedRepoIds] = useState<number[]>([])
  const [comparisonRepos, setComparisonRepos] = useState<RepositoryComparisonResponse[]>([])
  const [comparisonLoading, setComparisonLoading] = useState(false)
  const [comparisonError, setComparisonError] = useState<string | null>(null)

  const fetchComparisonRepos = useCallback(async () => {
    setComparisonLoading(true)
    setComparisonError(null)
    try {
      const data = await analysisApi.getRepositoriesForComparison()
      setComparisonRepos(data)
    } catch (err) {
      setComparisonError(err instanceof Error ? err.message : "비교 데이터를 불러올 수 없습니다.")
    } finally {
      setComparisonLoading(false)
    }
  }, [])

  const handleToggleCompareMode = () => {
    const next = !compareMode
    setCompareMode(next)
    setSelectedRepoIds([])
    if (next && comparisonRepos.length === 0) {
      fetchComparisonRepos()
    }
  }

  const handleSelectRepo = (repoId: number) => {
    setSelectedRepoIds((prev) => {
      if (prev.includes(repoId)) return prev.filter((id) => id !== repoId)
      if (prev.length >= 5) return prev
      return [...prev, repoId]
    })
  }

  if (loading) return <p className="p-8 text-center">히스토리 불러오는 중...</p>
  if (error) return <p className="p-8 text-center text-red-500">{error}</p>

  return (
    <div className="max-w-3xl mx-auto p-6 space-y-6">
      <header className="flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">분석 히스토리</h1>
            <p className="text-sm text-muted-foreground">
              {compareMode
                ? "비교할 리포지토리를 선택하세요 (최대 5개)"
                : "시간에 따른 리포지토리 개선 사항을 추적하세요"}
            </p>
          </div>
          <Button
            variant={compareMode ? "default" : "outline"}
            className="gap-2"
            onClick={handleToggleCompareMode}
          >
            {compareMode ? (
              <>
                <X className="h-4 w-4" />
                비교 취소
              </>
            ) : (
              <>
                <GitCompare className="h-4 w-4" />
                리포지토리 비교
              </>
            )}
          </Button>
        </div>

        {!compareMode && (
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">
              정렬 기준을 선택해 히스토리를 확인하세요.
            </span>
            <div className="flex gap-2">
              <Button
                variant={sortType === "latest" ? "default" : "outline"}
                size="sm"
                onClick={() => setSortType("latest")}
              >
                최신순
              </Button>
              <Button
                variant={sortType === "score" ? "default" : "outline"}
                size="sm"
                onClick={() => setSortType("score")}
              >
                점수순
              </Button>
            </div>
          </div>
        )}
      </header>

      {compareMode ? (
        <HistoryCompare
          repositories={comparisonRepos}
          selectedRepoIds={selectedRepoIds}
          onSelectRepo={handleSelectRepo}
          loading={comparisonLoading}
          error={comparisonError}
          onExit={handleToggleCompareMode}
        />
      ) : (
        <>
          <HistoryStats repositories={repositories} />

          <div className="space-y-4">
            {repositories.length === 0 ? (
              <Card className="p-10 text-center bg-muted/30 border-dashed border-2 border-muted-foreground/20 rounded-2xl shadow-sm hover:shadow-md transition-all">
                <p className="text-lg mb-6 text-muted-foreground">
                  아직 분석 결과가 없습니다. 지금 바로{" "}
                  <span className="font-semibold text-primary">새 분석</span>을 시작해 보세요!
                </p>
                <Button size="lg" onClick={() => router.push("/analysis")} className="px-8">
                  🚀 새 분석 시작하기
                </Button>
              </Card>
            ) : (
              repositories.map((repo) => (
                <Card
                  key={repo.id}
                  className="p-6 transition-all hover:border-primary/50 cursor-pointer"
                  onClick={() => router.push(`/analysis/${repo.id}`)}
                >
                  <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex-1">
                      <div className="mb-2 flex items-center gap-2">
                        <Github className="h-4 w-4 text-muted-foreground" />
                        <a
                          href={repo.htmlUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="font-semibold text-primary hover:underline flex items-center gap-1"
                          onClick={(e) => e.stopPropagation()}
                        >
                          {repo.name}
                          <ExternalLink className="h-3 w-3" />
                        </a>
                        {repo.publicRepository ? (
                          <Badge variant="default" className="gap-1 bg-green-600 text-white">
                            <span className="h-2 w-2 rounded-full bg-white" />
                            Public
                          </Badge>
                        ) : (
                          <Badge variant="secondary" className="gap-1">
                            <span className="h-2 w-2 rounded-full bg-muted-foreground" />
                            Private
                          </Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground mb-3 line-clamp-2">
                        {repo.description || "설명이 없습니다."}
                      </p>
                      <div className="mb-3 flex flex-wrap gap-2">
                        {repo.languages.map((lang) => (
                          <Badge key={lang} variant="secondary" className="text-xs">
                            {lang}
                          </Badge>
                        ))}
                      </div>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <Calendar className="h-4 w-4" />
                        <span>{formatRelativeTimeKST(repo.latestAnalysisDate ?? repo.createDate)}</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-6">
                      {repo.latestScore != null ? (
                        <div className="text-center">
                          <div className="mb-1 text-sm text-muted-foreground">점수</div>
                          <ScoreBadge score={repo.latestScore} size="sm" />
                        </div>
                      ) : (
                        <div className="text-sm text-muted-foreground">점수 없음</div>
                      )}
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          if (confirm("정말 이 리포지토리를 삭제하시겠습니까?")) {
                            handleDelete(repo.id)
                          }
                        }}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </div>
                  </div>
                </Card>
              ))
            )}
          </div>
        </>
      )}
    </div>
  )
}
