"use client"

import { useSearchParams } from "next/navigation"
import { Progress } from "@/components/ui/progress"
import LoadingHeader from "@/components/analysis/LoadingHeader"
import LoadingStepList from "@/components/analysis/LoadingStepList"
import LoadingInfoBox from "@/components/analysis/LoadingInfoBox"
import { useAnalysisProgress } from "@/hooks/analysis/useLoadingProgress"

export default function LoadingContent() {
  const searchParams = useSearchParams()
  const repoUrl = searchParams.get("repoUrl") // ✅ 'repo' → 'repoUrl'로 수정

  // ✅ repoUrl을 훅에 전달해야 실제 API 요청이 실행됨
  const { progress, currentStep, steps, statusMessage } = useAnalysisProgress(repoUrl)

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="mx-auto max-w-2xl">
          {/* 분석 중인 저장소 URL 헤더 */}
          <LoadingHeader repoUrl={repoUrl} />

          <div className="mb-12 space-y-3">
            {/* 진행률 바 + 퍼센트 표시 */}
            <Progress value={progress} className="h-2" />
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">
                {Math.round(progress)}% 완료
              </span>
              <span className="text-muted-foreground">
                {currentStep + 1} / {steps.length} 단계
              </span>
            </div>
          </div>

          {/* 상태 메시지 출력 (선택) */}
          <p className="text-center text-sm text-muted-foreground mb-4">
            {statusMessage}
          </p>

          {/* 단계 리스트 및 추가 정보 */}
          <LoadingStepList steps={steps} currentStep={currentStep} />
          <LoadingInfoBox />
        </div>
      </div>
    </div>
  )
}
