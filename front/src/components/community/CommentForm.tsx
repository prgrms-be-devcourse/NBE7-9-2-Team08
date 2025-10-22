"use client"

import { useState } from "react"
import { Button } from "@/components/ui/Button"
import { Textarea } from "@/components/ui/textarea"
import { postComment } from "@/lib/api/community"

interface CommentFormProps {
  analysisResultId: string
  memberId: number
  onCommentAdded?: () => void // 작성 후 목록 새로고침용 콜백
}

export function CommentForm({ analysisResultId, memberId, onCommentAdded }: CommentFormProps) {
  const [content, setContent] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!content.trim()) return

    try {
      setLoading(true)
      setError(null)

      await postComment(analysisResultId, memberId, content)
      setContent("") // 작성 후 textarea 초기화
      if (onCommentAdded) onCommentAdded()
    } catch (err) {
      console.error(err)
      setError("댓글 작성 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3 mt-6">
      <Textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="댓글을 입력하세요..."
        className="min-h-[100px]"
      />
      {error && <p className="text-sm text-red-500">{error}</p>}
      <div className="flex justify-end">
        <Button type="submit" disabled={loading || !content.trim()}>
          {loading ? "작성 중..." : "댓글 작성"}
        </Button>
      </div>
    </form>
  )
}
