"use client"

import {
  Radar,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  ResponsiveContainer,
  Tooltip,
} from "recharts"
import { motion } from "framer-motion"
import { useTheme } from "next-themes"

type RadarChartComponentProps = {
  data: { category: string; score: number }[]
}

export function RadarChartComponent({ data }: RadarChartComponentProps) {
  const { theme } = useTheme()
  const isDark = theme === "dark"

  return (
    <motion.div
      className="w-full h-[350px] rounded-2xl border bg-card p-4 shadow-sm"
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
    >
      <ResponsiveContainer width="100%" height="100%">
        <RadarChart data={data}>
          <PolarGrid
            stroke={isDark ? "rgba(255,255,255,0.15)" : "rgba(0,0,0,0.1)"}
          />
          <PolarAngleAxis
            dataKey="category"
            tick={{
              fill: isDark ? "hsl(var(--muted-foreground))" : "hsl(var(--foreground))",
              fontSize: 13,
            }}
          />
          <PolarRadiusAxis domain={[0, 100]} tick={false} axisLine={false} />
          <Radar
            dataKey="score"
            stroke="hsl(var(--primary))"
            fill="hsl(var(--primary))"
            fillOpacity={0.4}
            strokeWidth={2}
            animationBegin={200}
            animationDuration={800}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: "hsl(var(--popover))",
              border: "1px solid hsl(var(--border))",
              borderRadius: "8px",
              color: "hsl(var(--foreground))",
            }}
            formatter={(value: number) => [`${Math.round(value)}점`, "점수"]}
          />
        </RadarChart>
      </ResponsiveContainer>
    </motion.div>
  )
}
