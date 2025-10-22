"use client"

import Link from "next/link"
import { useHistory } from "@/hooks/history/useHistory"
import { useRouter } from "next/navigation"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/Button"
import { Github, ExternalLink, Trash2, Calendar } from "lucide-react"

export default function HistoryPage() {
  const memberId = 1 // 유저 정보 1로 고정
  const { repositories, loading, error } = useHistory(memberId)
  const router = useRouter()

  if (loading) return <p className="p-8 text-center">로딩 중...</p>
  if (error) return <p className="p-8 text-center text-red-500">{error}</p>

  return (
    <div className="flex flex-col items-center justify-center py-10 px-4">
      <div className="w-full max-w-3xl space-y-4"> {/* ✅ 중앙 정렬 + 최대 너비 */}
        {repositories.map((repo) => (
          <Card
            key={repo.id}
            className="p-6 transition-all hover:border-primary/50 cursor-pointer"
            onClick={() => router.push(`/history/${repo.id}`)}
          >
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              {/* 왼쪽 정보 영역 */}
              <div className="flex-1">
                <div className="mb-2 flex items-center gap-2">
                  <Github className="h-4 w-4 text-muted-foreground" />
                  <a
                    href={repo.htmlUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="font-semibold text-primary hover:underline flex items-center gap-1"
                    onClick={(e) => e.stopPropagation()}
                  >
                    {repo.name}
                    <ExternalLink className="h-3 w-3" />
                  </a>

                  {repo.publicRepository ? (
                    <Badge variant="default" className="gap-1 bg-green-600 text-white">
                      <div className="h-2 w-2 rounded-full bg-white" />
                      Public
                    </Badge>
                  ) : (
                    <Badge variant="secondary" className="gap-1">
                      <div className="h-2 w-2 rounded-full bg-muted-foreground" />
                      Private
                    </Badge>
                  )}
                </div>

                <p className="text-sm text-muted-foreground mb-3 line-clamp-2">
                  {repo.description || "설명이 없습니다."}
                </p>

                <div className="mb-3 flex flex-wrap gap-2">
                  {repo.languages.map((lang) => (
                    <Badge key={lang} variant="secondary" className="text-xs">
                      {lang}
                    </Badge>
                  ))}
                </div>

                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Calendar className="h-4 w-4" />
                  <span>브랜치: {repo.mainBranch}</span>
                </div>
              </div>

              <div className="flex items-center gap-6">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={(e) => {
                    e.stopPropagation()
                    console.log("삭제 요청:", repo.id)
                  }}
                >
                  <Trash2 className="h-4 w-4 text-destructive" />
                </Button>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}