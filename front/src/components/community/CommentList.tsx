"use client"

import { useEffect, useState } from "react"
import { Card } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { fetchComments } from "@/lib/api/community"
import { Comment } from "@/types/community"
import { formatDistanceToNow } from "date-fns/formatDistanceToNow"
import {ko } from "date-fns/locale/ko"

export function CommentList({ analysisResultId }: { analysisResultId: string }) {
  const [comments, setComments] = useState<Comment[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadData = async () => {
      try {
        const commentList = await fetchComments(analysisResultId)
        setComments(commentList)
      } catch (err) {
        console.error("댓글 불러오기 실패:", err)
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [analysisResultId])

  if (loading) return <p className="text-muted-foreground">댓글을 불러오는 중...</p>
  if (comments.length === 0) return <p className="text-muted-foreground">아직 댓글이 없습니다.</p>

  return (
    <div className="flex flex-col gap-4">
      {comments.map((c) => {
        const timeAgo = formatDistanceToNow(new Date(c.createDate), {
          addSuffix: true,
          locale: ko,
        })

        return (
          <Card key={c.id} className="p-5 rounded-2xl shadow-sm flex flex-col gap-3">
            {/* Header */}
            <div className="flex justify-between">
              <div className="flex gap-3 items-center">
                <Avatar className="h-10 w-10">
                  <AvatarImage src="/userInit.png" alt={`User #${c.memberId}`} />
                  <AvatarFallback>{`U${c.memberId}`}</AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-semibold">{`User #${c.memberId}`}</p>
                  <p className="text-sm text-muted-foreground">{`user${c.memberId}@example.com`}</p>
                </div>
              </div>
              <span className="text-sm text-muted-foreground">{timeAgo}</span>
            </div>

            {/* Content */}
            <p className="text-[15px] text-gray-800 leading-relaxed">{c.comment}</p>
          </Card>
        )
      })}
    </div>
  )
}