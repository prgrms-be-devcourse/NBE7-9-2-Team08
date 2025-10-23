"use client"

import { Card } from "@/components/ui/card"

interface HistoryStatsProps {
  repositories: { latestScore?: number | null }[]
}

/** ✅ 리포지토리 통계 카드 */
export function HistoryStats({ repositories }: HistoryStatsProps) {
  const total = repositories.length
  const validScores = repositories
    .map((r) => r.latestScore ?? 0)
    .filter((s) => s > 0)

  const average =
    validScores.length > 0
      ? (validScores.reduce((a, b) => a + b, 0) / validScores.length).toFixed(1)
      : "0.0"

  const max = validScores.length > 0 ? Math.max(...validScores) : 0

  return (
    <div className="mb-8 grid gap-4 sm:grid-cols-3">
      <Card className="p-6">
        <div className="mb-2 text-sm text-muted-foreground">총 리포지토리 수</div>
        <div className="text-3xl font-bold">{total}</div>
      </Card>

      <Card className="p-6">
        <div className="mb-2 text-sm text-muted-foreground">평균 점수</div>
        <div className="text-3xl font-bold text-score-good">{average}</div>
      </Card>

      <Card className="p-6">
        <div className="mb-2 text-sm text-muted-foreground">최고 점수</div>
        <div className="text-3xl font-bold text-score-excellent">{max}</div>
      </Card>
    </div>
  )
}
