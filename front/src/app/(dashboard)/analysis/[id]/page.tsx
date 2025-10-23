"use client"

import { useParams } from "next/navigation"
import { useAnalysisResult } from "@/hooks/analysis/useAnalysisResult"
import { AnalysisHeader } from "@/components/analysis/AnalysisHeader"
import { AnalysisRadarCard } from "@/components/analysis/AnalysisRadarCard"
import { AnalysisResultTabs } from "@/components/analysis/AnalysisResultTabs"
import { AnalysisSummaryCard } from "@/components/analysis/AnalysisSummaryCard"
import { RepositoryPublicSection } from "@/components/analysis/RepositoryPublicSection"

export default function ResultsPage() {
  const params = useParams()
  const repoId = Number(params.id)
  const user = typeof window !== "undefined" ? JSON.parse(localStorage.getItem("user") || "{}") : null
  const userId = user?.id

  const { history, result, loading, selectedId, setSelectedId } = useAnalysisResult(userId, repoId)

  if (loading)
    return <div className="p-8 text-center text-muted-foreground">🕓 분석 결과를 불러오는 중...</div>

  if (!history || !result)
    return <div className="p-8 text-center text-muted-foreground">⚠️ 분석 데이터를 찾을 수 없습니다.</div>

  const radarData = [
    { category: "README", score: (result.readmeScore / 30) * 100 },
    { category: "TEST", score: (result.testScore / 30) * 100 },
    { category: "COMMIT", score: (result.commitScore / 25) * 100 },
    { category: "CI/CD", score: (result.cicdScore / 15) * 100 },
  ]

  return (
    <div className="flex justify-center">
      <div className="w-full max-w-5xl px-6 sm:px-8 lg:px-12 py-10">
        <AnalysisHeader history={history} selectedId={selectedId} onSelect={setSelectedId} />

        <AnalysisSummaryCard totalScore={result.totalScore} summary={result.summary} />

        <div className="grid gap-6 lg:grid-cols-2 items-stretch mb-8">
          <AnalysisRadarCard data={radarData} />
          <AnalysisResultTabs strengths={result.strengths} improvements={result.improvements} />
        </div>

        {/* 🌐 공개 설정 및 커뮤니티 섹션 */}
        <RepositoryPublicSection
          userId={history.repository.ownerId}
          repoId={repoId}
          initialPublic={history.repository.publicRepository}
        />
      </div>
    </div>
  )
}
