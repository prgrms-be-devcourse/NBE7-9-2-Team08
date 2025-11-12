"use client"

import { useState } from "react"
import { Button } from "@/components/ui/Button"
import { Textarea } from "@/components/ui/textarea"
import { postComment } from "@/lib/api/community"
import { useAuth } from "@/hooks/auth/useAuth" // ✅ 로그인 유저 정보 가져오기

interface CommentFormProps {
  analysisResultId: number
  onCommentAdded?: () => void // 새 댓글 등록 후 목록 리프레시용 콜백
}

export default function CommentForm({ analysisResultId, onCommentAdded }: CommentFormProps) {
  const { user } = useAuth()
  const [content, setContent] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!user) {
      setError("로그인이 필요합니다.")
      return
    }

    if (!content.trim()) return

    try {
      setLoading(true)
      setError(null)

      // ✅ postComment 호출 (백엔드 DTO에 맞게)
      await postComment(analysisResultId, user.id, content)

      // ✅ 입력값 초기화 및 콜백 실행
      setContent("")
      onCommentAdded?.()
    } catch (err) {
      console.error("❌ 댓글 작성 중 오류:", err)
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
