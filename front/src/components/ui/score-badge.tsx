"use client"

import { cn } from "@/lib/utils"
import { motion } from "framer-motion"

type ScoreBadgeProps = {
  score: number
  size?: "sm" | "lg"
  showLabel?: boolean
}

export function ScoreBadge({
  score,
  size = "sm",
  showLabel = false,
}: ScoreBadgeProps) {
  const getColor = (score: number) => {
    if (score >= 90) return "bg-score-excellent text-white"
    if (score >= 75) return "bg-score-good text-white"
    if (score >= 50) return "bg-score-fair text-white"
    return "bg-score-poor text-white"
  }

  const getLabel = (score: number) => {
    if (score >= 90) return "Excellent"
    if (score >= 75) return "Good"
    if (score >= 50) return "Fair"
    return "Needs Improvement"
  }

  const sizeClass =
    size === "lg"
      ? "text-4xl font-bold px-6 py-4 rounded-2xl"
      : "text-sm font-semibold px-3 py-1.5 rounded-full"

  return (
    <motion.div
      className={cn(
        "inline-flex items-center justify-center shadow-sm transition-all",
        getColor(score),
        sizeClass
      )}
      initial={{ opacity: 0, scale: 0.8, y: 10 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
    >
      <motion.span
        key={score}
        initial={{ scale: 0.8, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.4 }}
      >
        {score}
      </motion.span>

      {showLabel && (
        <span className="ml-2 text-xs opacity-80">{getLabel(score)}</span>
      )}
    </motion.div>
  )
}
