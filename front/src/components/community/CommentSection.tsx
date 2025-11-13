"use client"

import { useState, useEffect } from "react"
import { Card } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/Button"
import { Textarea } from "@/components/ui/textarea"
import { deleteComment, fetchComments, postComment, updateComment } from "@/lib/api/community"
import type { Comment, PageResponse } from "@/types/community"
import { useAuth } from "@/hooks/auth/useAuth"
import { formatDistanceToNow } from "date-fns"
import { ko } from "date-fns/locale"

export default function CommentSection({ analysisResultId }: { analysisResultId: number }) {
  const { user } = useAuth()
  const [content, setContent] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [comments, setComments] = useState<Comment[]>([])
  const [page, setPage] = useState(0)
  const [size] = useState(5)
  const [totalPages, setTotalPages] = useState(0)
  const [refreshKey, setRefreshKey] = useState(0)

  // ✅ 댓글 목록 로드
  useEffect(() => {
    let isMounted = true
    const loadComments = async () => {
      setLoading(true)
      try {
        const res: PageResponse<Comment> = await fetchComments(analysisResultId, page, size)
        if (isMounted) {
          setComments(res.content ?? [])
          setTotalPages(res.totalPages ?? 0)
        }
      } catch (err: any) {
        console.error("❌ 댓글 불러오기 실패:", err)
        if (isMounted) setError(err?.message ?? "댓글 불러오기 실패")
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    loadComments()
    return () => {
      isMounted = false
    }
  }, [analysisResultId, page, size, refreshKey])

  // ✅ 댓글 작성
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
      await postComment(analysisResultId, user.id, content)
      setContent("")
      setRefreshKey((prev) => prev + 1)
    } catch (err) {
      console.error("댓글 작성 실패:", err)
      setError("댓글 작성 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  const toDate = (d: string) => {
    const base = d?.split(".")[0] ?? d
    return new Date(`${base}`)
  }

  return (
    <div className="flex flex-col gap-6 mt-6">
      {/* ✏️ 댓글 작성 영역 */}
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <Textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder={user ? "댓글을 입력하세요..." : "로그인 후 댓글을 작성할 수 있습니다."}
          className="min-h-[100px]"
          disabled={!user || loading}
        />
        {error && <p className="text-sm text-red-500">{error}</p>}
        <div className="flex justify-end">
          <Button type="submit" disabled={loading || !content.trim() || !user}>
            {loading ? "작성 중..." : "댓글 작성"}
          </Button>
        </div>
      </form>

      {/* 💬 댓글 목록 */}
      {loading && comments.length === 0 ? (
        <p className="text-muted-foreground">댓글을 불러오는 중...</p>
      ) : comments.length === 0 ? (
        <p className="text-muted-foreground">아직 댓글이 없습니다.</p>
      ) : (
        <div className="flex flex-col gap-4">
          {comments.map((c) => {
            const timeAgo = formatDistanceToNow(toDate(c.createDate), {
              addSuffix: true,
              locale: ko,
            })

            const isMyComment = user && Number(user.id) === c.memberId

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

                <p className="text-[15px] text-gray-800 leading-relaxed">{c.comment}</p>

                {/* ✅ 내 댓글일 경우 버튼 노출 */}
                {isMyComment && (
                  <div className="flex justify-end gap-2 mt-2">
                    {/* ✏️ 수정 버튼 */}
                    <Button
                      size="sm"
                      variant="ghost"
                      className="text-gray-400 hover:text-black hover:bg-transparent transition-colors"
                      onClick={async () => {
                        const newContent = prompt("수정할 내용을 입력하세요", c.comment)
                        if (!newContent || newContent.trim() === "") return
                        try {
                          await updateComment(c.commentId, newContent)
                          alert("댓글이 수정되었습니다.")
                          setRefreshKey((prev) => prev + 1)
                        } catch (err) {
                          console.error("댓글 수정 실패:", err)
                          alert("댓글 수정 중 오류가 발생했습니다.")
                        }
                      }}
                    >
                      수정
                    </Button>

                    {/* 🗑️ 삭제 버튼 */}
                    <Button
                      size="sm"
                      variant="ghost"
                      className="text-gray-400 hover:text-red-500 hover:bg-transparent transition-colors"
                      onClick={async () => {
                        if (!confirm("정말 이 댓글을 삭제하시겠습니까?")) return
                        try {
                          await deleteComment(c.commentId)
                          alert("댓글이 삭제되었습니다.")
                          setRefreshKey((prev) => prev + 1)
                        } catch (err) {
                          console.error("댓글 삭제 실패:", err)
                          alert("댓글 삭제 중 오류가 발생했습니다.")
                        }
                      }}
                    >
                      삭제
                    </Button>
                  </div>
                )}
              </Card>
            )
          })}
        </div>
      )}

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <Pager
          page={page}
          totalPages={totalPages}
          onPrev={() => setPage(Math.max(0, page - 1))}
          onNext={() => setPage(Math.min(totalPages - 1, page + 1))}
        />
      )}
    </div>
  )
}

/** 페이지네이션 컴포넌트 */
function Pager({
  page,
  totalPages,
  onPrev,
  onNext,
}: {
  page: number
  totalPages: number
  onPrev: () => void
  onNext: () => void
}) {
  return (
    <div className="mt-4 flex items-center justify-center gap-3">
      <button
        className="px-3 py-1 rounded-md border disabled:opacity-50"
        disabled={page === 0}
        onClick={onPrev}
      >
        이전
      </button>
      <span className="text-sm text-muted-foreground">
        {totalPages > 0 ? `${page + 1} / ${totalPages}` : "1 / 1"}
      </span>
      <button
        className="px-3 py-1 rounded-md border disabled:opacity-50"
        disabled={page + 1 >= totalPages}
        onClick={onNext}
      >
        다음
      </button>
    </div>
  )
}
