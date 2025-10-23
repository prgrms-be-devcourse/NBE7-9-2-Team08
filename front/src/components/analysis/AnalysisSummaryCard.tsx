"use client"

import { Card } from "@/components/ui/card"
import { ScoreBadge } from "@/components/ui/score-badge"

interface AnalysisSummaryCardProps {
  totalScore: number
  summary: string
}

export function AnalysisSummaryCard({ totalScore, summary }: AnalysisSummaryCardProps) {
  return (
    <Card className="mb-8 p-8">
      <div className="flex flex-col md:flex-row justify-between items-center mb-6">
        <h2 className="text-xl font-semibold">종합 점수</h2>
        <ScoreBadge score={totalScore} size="lg" showLabel />
      </div>
      <p className="text-muted-foreground leading-relaxed">{summary}</p>
    </Card>
  )
}
