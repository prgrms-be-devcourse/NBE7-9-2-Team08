"use client"

import { useMemo } from "react"
import { Github, ExternalLink, Calendar, X } from "lucide-react"
import { RepositoryComparisonResponse } from "@/types/analysis"
import { formatRelativeTimeKST } from "@/lib/utils/formatDate"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/Button"
import { Checkbox } from "@/components/ui/checkbox"
import { ScoreBadge } from "@/components/ui/score-badge"
import { ComparisonRadarChart } from "@/components/ui/comparison-radar-chart"

interface HistoryCompareProps {
  repositories: RepositoryComparisonResponse[]
  selectedRepoIds: number[]
  onSelectRepo: (repoId: number) => void
  loading: boolean
  error: string | null
  onExit: () => void
}

export function HistoryCompare({
  repositories,
  selectedRepoIds,
  onSelectRepo,
  loading,
  error,
  onExit,
}: HistoryCompareProps) {
  const comparisonChartData = useMemo(() => {
    const palette = ["hsl(var(--primary))", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6"]
    return selectedRepoIds.map((repoId, index) => {
      const repo = repositories.find((item) => item.repositoryId === repoId)!
      return {
        name: repo.name.split("/").pop() ?? repo.name,
        color: palette[index % palette.length],
        data: [
          { category: "README", score: repo.latestAnalysis.scores.readme },
          { category: "TEST", score: repo.latestAnalysis.scores.test },
          { category: "COMMIT", score: repo.latestAnalysis.scores.commit },
          { category: "CI/CD", score: repo.latestAnalysis.scores.cicd },
        ],
      }
    })
  }, [repositories, selectedRepoIds])

  if (loading)
    return (
      <Card className="p-10 text-center">
        <p className="text-muted-foreground">비교 데이터를 불러오는 중입니다…</p>
      </Card>
    )

  if (error)
    return (
      <Card className="p-10 text-center space-y-4">
        <p className="text-red-500">{error}</p>
        <Button variant="outline" onClick={onExit}>
          비교 모드 나가기
        </Button>
      </Card>
    )

  if (repositories.length === 0)
    return (
      <Card className="p-10 text-center space-y-4">
        <p className="text-muted-foreground">비교 가능한 분석이 아직 없습니다.</p>
        <Button variant="outline" onClick={onExit}>
          돌아가기
        </Button>
      </Card>
    )

  return (
    <div className="space-y-6">
      {selectedRepoIds.length > 0 && (
        <Card className="p-4">
          <div className="mb-2 text-sm font-medium">선택된 리포지토리 ({selectedRepoIds.length}/5)</div>
          <div className="flex flex-wrap gap-2">
            {selectedRepoIds.map((repoId) => {
              const repo = repositories.find((item) => item.repositoryId === repoId)!
              return (
                <Badge key={repoId} variant="secondary" className="gap-1">
                  {repo.name.split("/").pop() ?? repo.name}
                  <button
                    type="button"
                    onClick={() => onSelectRepo(repoId)}
                    className="ml-1 rounded-full hover:bg-muted"
                  >
                    <X className="h-3 w-3" />
                  </button>
                </Badge>
              )
            })}
          </div>
        </Card>
      )}

      {selectedRepoIds.length >= 2 && (
        <>
          <ComparisonRadarChart repositories={comparisonChartData} />
          <Card className="mt-4 p-6">
            <h3 className="mb-4 text-lg font-semibold">세부 점수 비교</h3>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="pb-3 text-left font-medium">항목</th>
                    {comparisonChartData.map((repo) => (
                      <th key={repo.name} className="pb-3 text-center font-medium">
                        {repo.name}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {["README", "TEST", "COMMIT", "CI/CD"].map((category, rowIndex) => (
                    <tr key={category} className="border-b last:border-0">
                      <td className="py-3 font-medium">{category}</td>
                      {comparisonChartData.map((repo) => (
                        <td key={repo.name} className="py-3 text-center">
                          <span className="font-semibold" style={{ color: repo.color }}>
                            {repo.data[rowIndex].score}
                          </span>
                        </td>
                      ))}
                    </tr>
                  ))}
                  <tr className="border-t-2 font-semibold">
                    <td className="py-3">총점</td>
                    {selectedRepoIds.map((repoId) => {
                      const repo = repositories.find((item) => item.repositoryId === repoId)!
                      return (
                        <td key={repoId} className="py-3 text-center">
                          {repo.latestAnalysis.scores.total}
                        </td>
                      )
                    })}
                  </tr>
                </tbody>
              </table>
            </div>
          </Card>
        </>
      )}

      <div className="space-y-4">
        {repositories.map((repo) => {
          const isSelected = selectedRepoIds.includes(repo.repositoryId)
          return (
            <Card
              key={repo.repositoryId}
              className={`p-6 transition-all cursor-pointer hover:border-primary/50 ${
                isSelected ? "border-primary ring-2 ring-primary/20" : ""
              }`}
              onClick={() => onSelectRepo(repo.repositoryId)}
            >
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex items-center">
                  <Checkbox
                    checked={isSelected}
                    onCheckedChange={() => onSelectRepo(repo.repositoryId)}
                    onClick={(e: React.MouseEvent<HTMLButtonElement>) => e.stopPropagation()}
                  />
                </div>

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
                  </div>

                  <div className="mb-3 flex flex-wrap gap-2">
                    {repo.languages.map((lang) => (
                      <Badge key={lang} variant="secondary" className="text-xs">
                        {lang}
                      </Badge>
                    ))}
                  </div>

                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Calendar className="h-4 w-4" />
                    <span>{formatRelativeTimeKST(repo.latestAnalysis.analyzedAt)}</span>
                  </div>
                </div>

                <div className="text-center">
                  <div className="mb-1 text-sm text-muted-foreground">점수</div>
                  <ScoreBadge score={repo.latestAnalysis.scores.total} size="sm" />
                </div>
              </div>
            </Card>
          )
        })}
      </div>
    </div>
  )
}
