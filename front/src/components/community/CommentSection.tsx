"use client"

import { useState } from "react"
import { CommentList } from "./CommentList"
import { CommentForm } from "./CommentForm"

interface CommentSectionProps {
  analysisResultId: number
  memberId: number
}

export function CommentSection({ analysisResultId, memberId }: CommentSectionProps) {
  const [refreshKey, setRefreshKey] = useState(0)

  return (
    <div className="mt-6 space-y-6">
      {/* ✏️ 댓글 작성 폼 */}
      <CommentForm
        analysisResultId={analysisResultId}
        memberId={memberId}
        onCommentAdded={() => setRefreshKey((k) => k + 1)}
      />

      {/* 💬 댓글 목록 */}
      <CommentList key={refreshKey} analysisResultId={analysisResultId} />
    </div>
  )
}