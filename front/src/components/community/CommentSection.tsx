"use client"

import { useState } from "react"
import { CommentList } from "./CommentList"
import { CommentForm } from "./CommentForm"

export function CommentSection({ analysisResultId }: { analysisResultId: string }) {
  const [refreshKey, setRefreshKey] = useState(0)

  return (
    <div>
      <CommentList key={refreshKey} analysisResultId={analysisResultId} />
      <CommentForm
        analysisResultId={analysisResultId}
        memberId={1} // 로그인 후 실제 사용자 ID로 교체
        onCommentAdded={() => setRefreshKey((k) => k + 1)}
      />
    </div>
  )
}
