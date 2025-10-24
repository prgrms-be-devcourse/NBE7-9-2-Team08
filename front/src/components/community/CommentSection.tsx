"use client"

import { useState } from "react"
import { useAuth } from "@/hooks/auth/useAuth"
import { CommentList } from "./CommentList"
import { CommentForm } from "./CommentForm"

interface CommentSectionProps {
  analysisResultId: number
}

export function CommentSection({ analysisResultId }: CommentSectionProps) {
  const { isAuthed } = useAuth()
  const [refreshKey, setRefreshKey] = useState(0)

  return (
    <div className="mt-6 space-y-6">
      {/* ✏️ 댓글 작성 폼 */}
      {isAuthed ? (
        <CommentForm
          analysisResultId={analysisResultId}
          onCommentAdded={() => setRefreshKey((k) => k + 1)}
        />
      ) : (
        <p className="text-sm text-muted-foreground">로그인 후 댓글을 작성할 수 있어요.</p>
      )}

      {/* 💬 댓글 목록 */}
      <CommentList key={refreshKey} analysisResultId={analysisResultId} />
    </div>
  )
}
