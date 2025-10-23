"use client"

import { useEffect, useState } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/Button" // ✅ 대소문자 수정
import { Switch } from "@/components/ui/switch"
import { ShareButton } from "@/components/analysis/ShareButton"
import { Globe, Lock, MessageSquare } from "lucide-react"
import { useRepositoryPublic } from "@/hooks/analysis/useRepositoryPublic"
import { CommentSection } from "@/components/community/CommentSection"
import { analysisApi } from "@/lib/api/analysis"
import type { HistoryResponseDto } from "@/types/analysis"

interface Props {
  userId: number
  repoId: number
  initialPublic: boolean
}

export function RepositoryPublicSection({ userId, repoId, initialPublic }: Props) {
  const { isPublic, togglePublic } = useRepositoryPublic(initialPublic, userId, repoId)

  const [analysisResultId, setAnalysisResultId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [currentUserId, setCurrentUserId] = useState<number | null>(null)

  useEffect(() => {
    const stored = localStorage.getItem("user")
    if (stored) {
      try {
        const parsed = JSON.parse(stored)
        setCurrentUserId(Number(parsed.id))
      } catch (err) {
        console.error("유저 정보 파싱 실패:", err)
      }
    }
  }, [])

  useEffect(() => {
    if (!userId || !repoId) return

    const loadAnalysisId = async () => {
      try {
        const historyResponse: HistoryResponseDto = await analysisApi.getRepositoryHistory(userId, repoId)
        // ✅ 최신 분석 결과 ID 추출
        if (Array.isArray(historyResponse.analysisVersions) && historyResponse.analysisVersions.length > 0) {
          const latest = historyResponse.analysisVersions[0]
          setAnalysisResultId(latest.analysisId)
        } else {
          console.warn("이 리포지토리에 분석 기록이 없습니다.")
        }
      } catch (err) {
        console.error("❌ 분석 히스토리 조회 실패:", err)
      } finally {
        setLoading(false)
      }
    }

    loadAnalysisId()
  }, [userId, repoId])

  if (currentUserId === null) {
    return <div className="p-6 text-center text-muted-foreground">사용자 정보를 불러오는 중...</div>
  }

  return (
    <>
      {/* 🌐 공개 설정 */}
      {currentUserId === userId && (
      <Card className="mb-8 p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            {isPublic ? (
              <Globe className="h-5 w-5 text-green-500" />
            ) : (
              <Lock className="h-5 w-5 text-muted-foreground" />
            )}
            <div>
              <h3 className="font-semibold">리포지토리 공개 설정</h3>
              <p className="text-sm text-muted-foreground">
                {isPublic
                  ? "이 리포지토리의 분석 결과가 커뮤니티에 공개됩니다."
                  : "이 리포지토리의 분석 결과는 비공개 상태입니다."}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">{isPublic ? "공개" : "비공개"}</span>
            <Switch checked={isPublic} onCheckedChange={togglePublic} />
          </div>
        </div>
      </Card>
      )}

      {/* 💬 커뮤니티 섹션 */}
      {isPublic ? (
        <>
          <Card className="p-6 mb-8">
            {/* 헤더 영역 */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between mb-4">
              <div>
                <h3 className="mb-1 font-semibold">커뮤니티 반응</h3>
                <p className="text-sm text-muted-foreground">다른 개발자들과 소통하세요.</p>
              </div>
              <div className="flex gap-2">
                <ShareButton />
              </div>
            </div>

            {/* 본문 영역: 댓글 작성 → 댓글 목록 */}
            {loading ? (
              <p className="flex flex-col gap-6">분석 데이터를 불러오는 중...</p>
            ) : analysisResultId ? (
              <div className="text-muted-foreground text-sm">
                {/* ✏️ 댓글 작성 폼 */}
                <CommentSection analysisResultId={analysisResultId} memberId={userId} />

                {/* 💬 댓글 목록 */}
                <div className="border-t pt-6">
                  {/* 댓글 목록이 CommentSection 안에서 렌더링되거나 별도 CommentList가 있다면 여기에 배치 */}
                  {/* 예: <CommentList analysisResultId={analysisResultId} /> */}
                  {/* CommentSection이 이미 작성+조회 포함한다면 그대로 둬도 됨 */}
                </div>
              </div>
            ) : (
              <p className="text-muted-foreground text-sm">아직 분석 기록이 없습니다.</p>
            )}
          </Card>
        </>
      ) : (
        <Card className="p-6 text-center text-muted-foreground">
          🔒 이 리포지토리는 현재 비공개 상태입니다.
        </Card>
      )}
    </>
  )
}
