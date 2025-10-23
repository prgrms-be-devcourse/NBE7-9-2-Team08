import { cn } from "@/lib/utils"

interface ScoreBadgeProps {
  score: number
  size?: "sm" | "md" | "lg"
  showLabel?: boolean
}

export function ScoreBadge({ score, size = "md", showLabel = false }: ScoreBadgeProps) {
  const getScoreColor = (score: number) => {
    if (score >= 90) return "text-score-excellent"
    if (score >= 80) return "text-score-good"
    if (score >= 70) return "text-score-fair"
    return "text-score-poor"
  }

  const getScoreLabel = (score: number) => {
    if (score >= 90) return "Excellent"
    if (score >= 80) return "Good"
    if (score >= 70) return "Fair"
    return "Needs Work"
  }

  const sizeClasses = {
    sm: "text-2xl",
    md: "text-4xl",
    lg: "text-6xl",
  }

  return (
    <div className="flex flex-col items-center gap-1">
      <span className={cn("font-bold tabular-nums", sizeClasses[size], getScoreColor(score))}>{score}</span>
      {showLabel && <span className="text-sm text-muted-foreground">{getScoreLabel(score)}</span>}
    </div>
  )
}
