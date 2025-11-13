"use client"

import { useEffect, useState } from "react"
import { Card } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { fetchComments, deleteComment, updateComment } from "@/lib/api/community"
import type { Comment, PageResponse } from "@/types/community"
import { formatDistanceToNow } from "date-fns"
import { ko } from "date-fns/locale"
import { Button } from "@/components/ui/Button"
import { useAuth } from "@/hooks/auth/useAuth"

export default function CommentList({ analysisResultId }: { analysisResultId: number }) {
  const { user } = useAuth()
  const [comments, setComments] = useState<Comment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // ✅ 페이징 관련 상태
  const [page, setPage] = useState(0)
  const [size] = useState(5)
  const [totalPages, setTotalPages] = useState(0)

  // ✅ 수정 상태
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editContent, setEditContent] = useState("")

  useEffect(() => {
    loadData()
  }, [analysisResultId, page, size])

  const loadData = async () => {
    setLoading(true)
    setError(null)
    try {
      const res: PageResponse<Comment> = await fetchComments(analysisResultId, page, size)
      setComments(res.content ?? [])
      setTotalPages(res.totalPages ?? 0)
    } catch (err) {
      console.error("❌ 댓글 불러오기 실패:", err)
      setError("댓글 불러오는 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  // ✅ 삭제 핸들러
  const handleDelete = async (commentId: number) => {
    if (!confirm("정말 이 댓글을 삭제하시겠습니까?")) return
    try {
      await deleteComment(commentId)
      alert("댓글이 삭제되었습니다.")
      loadData()
    } catch (err) {
      console.error("삭제 실패:", err)
      alert("댓글 삭제 중 오류가 발생했습니다.")
    }
  }

  // ✅ 수정 핸들러
  const handleEdit = async (commentId: number) => {
    if (!editContent.trim()) return alert("내용을 입력해주세요.")
    try {
      await updateComment(commentId, editContent)
      setEditingId(null)
      alert("댓글이 수정되었습니다.")
      loadData()
    } catch (err) {
      console.error("수정 실패:", err)
      alert("댓글 수정 중 오류가 발생했습니다.")
    }
  }

  if (loading) return <p className="text-muted-foreground">댓글을 불러오는 중...</p>
  if (error) return <p className="text-red-500">{error}</p>
  if (comments.length === 0)
    return <p className="text-muted-foreground">아직 댓글이 없습니다.</p>

  const toDate = (d: string) => {
    const base = d?.split(".")[0] ?? d
    return new Date(`${base}Z`)
  }

  return (
    <div className="flex flex-col gap-4">
      {comments.map((c) => {
        const timeAgo = formatDistanceToNow(toDate(c.createDate), {
          addSuffix: true,
          locale: ko,
        })

        const isMyComment = user && user.id === c.memberId

        return (
          <Card key={c.commentId} className="p-5 rounded-2xl shadow-sm flex flex-col gap-3">
            <div className="flex justify-between">
              <div className="flex gap-3 items-center">
                <Avatar className="h-10 w-10">
                  <AvatarImage src="/userInit.png" alt={`User #${c.memberId}`} />
                  <AvatarFallback>
                    <img src="/userInit.png" alt="기본 이미지" />
                  </AvatarFallback>
                </Avatar>

                <div>
                  <p className="font-semibold">{c.name}</p>
                </div>
              </div>
              <span className="text-sm text-muted-foreground">{timeAgo}</span>
            </div>

            {/* ✏️ 댓글 내용 or 수정 입력창 */}
            {editingId === c.commentId ? (
              <div className="flex flex-col gap-2">
                <textarea
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  className="border rounded-md p-2 text-sm w-full"
                />
                <div className="flex justify-end gap-2">
                  <Button size="sm" variant="outline" onClick={() => setEditingId(null)}>
                    취소
                  </Button>
                  <Button size="sm" onClick={() => handleEdit(c.commentId)}>
                    수정 완료
                  </Button>
                </div>
              </div>
            ) : (
              <p className="text-[15px] text-gray-800 leading-relaxed">{c.comment}</p>
            )}

            {/* 🔒 본인 댓글일 경우에만 버튼 노출 */}
            {isMyComment && editingId !== c.commentId && (
              <div className="flex justify-end gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setEditingId(c.commentId)
                    setEditContent(c.comment)
                  }}
                >
                  수정
                </Button>
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => handleDelete(c.commentId)}
                >
                  삭제
                </Button>
              </div>
            )}
          </Card>
        )
      })}

      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mt-6">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            이전
          </Button>
          <span className="text-sm text-muted-foreground">
            {page + 1} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage(page + 1)}
          >
            다음
          </Button>
        </div>
      )}
    </div>
  )
}
