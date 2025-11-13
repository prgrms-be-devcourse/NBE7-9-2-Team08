"use client"

import {
  Radar,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Legend,
  ResponsiveContainer,
} from "recharts"

interface RepoChart {
  name: string
  color: string
  data: { category: string; score: number }[]
}

interface ComparisonRadarChartProps {
  repositories: RepoChart[]
}

export function ComparisonRadarChart({ repositories }: ComparisonRadarChartProps) {
  if (!repositories || repositories.length === 0)
    return <p className="text-center text-sm text-muted-foreground">비교할 리포지토리를 선택해주세요.</p>

  // 데이터 통합 (Recharts는 같은 key를 가진 객체 배열이 필요함)
  const chartData = repositories[0].data.map((d, idx) => {
    const merged: any = { category: d.category }
    repositories.forEach((repo) => {
      merged[repo.name] = repo.data[idx].score
    })
    return merged
  })

  return (
    <div className="w-full h-[400px]">
      <ResponsiveContainer width="100%" height="100%">
        <RadarChart outerRadius="75%" data={chartData}>
          <PolarGrid />
          <PolarAngleAxis dataKey="category" />
          <PolarRadiusAxis domain={[0, 100]} />
          {repositories.map((repo) => (
            <Radar
              key={repo.name}
              name={repo.name}
              dataKey={repo.name}
              stroke={repo.color}
              fill={repo.color}
              fillOpacity={0.4}
            />
          ))}
          <Legend />
        </RadarChart>
      </ResponsiveContainer>
    </div>
  )
}
