"use client"

import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/Button"
import { Switch } from "@/components/ui/switch"
import { ShareButton } from "@/components/analysis/ShareButton"
import { Globe, Lock, MessageSquare, Share2, ThumbsUp } from "lucide-react"
import { useRepositoryPublic } from "@/hooks/analysis/useRepositoryPublic"
import { useState } from "react"

interface Props {
  userId: number
  repoId: number
  initialPublic: boolean
}

export function RepositoryPublicSection({ userId, repoId, initialPublic }: Props) {
  const { isPublic, togglePublic } = useRepositoryPublic(initialPublic, userId, repoId)
  const [liked, setLiked] = useState(false)
  const [likeCount, setLikeCount] = useState(42)

  const handleLike = () => {
    setLiked((prev) => !prev)
    setLikeCount((prev) => (liked ? prev - 1 : prev + 1))
  }

  return (
    <>
      {/* 🌐 공개 설정 */}
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

      {/* 💬 커뮤니티 섹션 */}
      {isPublic ? (
        <>
          <Card className="p-6 mb-8">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 className="mb-1 font-semibold">커뮤니티 반응</h3>
                <p className="text-sm text-muted-foreground">다른 개발자들과 소통하세요.</p>
              </div>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" className="gap-2 bg-transparent">
                  <MessageSquare className="h-4 w-4" />
                  댓글 (n)
                </Button>
                <ShareButton />
              </div>
            </div>
          </Card>

          {/* ⚠️ TODO: 댓글 컴포넌트 연결 위치 */}
          {/*
            <CommentSection 
              repoId={repoId} 
              userId={userId} 
            />
          */}
        </>
      ) : (
        <Card className="p-6 text-center text-muted-foreground">
          🔒 이 리포지토리는 현재 비공개 상태입니다.
        </Card>
      )}
    </>
  )
}
