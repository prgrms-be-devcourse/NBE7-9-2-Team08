"use client"

import Link from "next/link"
import { useRepositories } from "@/hooks/repositories/useRepositories"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/Button"
import { Github, ExternalLink } from "lucide-react"

export default function HistoryPage() {
  const memberId = 1 // 예시로 로그인된 유저 id
  const { repositories, loading, error } = useRepositories(memberId)

  if (loading) return <p className="p-8">로딩 중...</p>
  if (error) return <p className="p-8 text-red-500">{error}</p>

  return (
    <div className="container mx-auto px-6 py-8">
      <h1 className="text-3xl font-bold mb-6">분석된 리포지토리 목록</h1>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {repositories.map((repo) => (
          <Card
            key={repo.id}
            className="p-6 hover:border-primary/60 transition-all"
          >
            <div className="flex items-center gap-2 mb-2">
              <Github className="h-5 w-5 text-muted-foreground" />
              <a
                href={repo.htmlUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="font-semibold text-primary hover:underline flex items-center gap-1"
              >
                {repo.name}
                <ExternalLink className="h-3 w-3" />
              </a>
            </div>

            <p className="text-sm text-muted-foreground mb-4 line-clamp-2">
              {repo.description || "설명이 없습니다."}
            </p>

            <div className="flex flex-wrap gap-2 mb-4">
              {repo.languages.map((lang) => (
                <Badge key={lang} variant="secondary">
                  {lang}
                </Badge>
              ))}
            </div>

            <div className="flex items-center justify-between">
              <Badge
                variant={repo.publicRepository ? "default" : "secondary"}
                className="gap-1"
              >
                <div
                  className={`h-2 w-2 rounded-full ${
                    repo.publicRepository ? "bg-green-500" : "bg-gray-400"
                  }`}
                />
                {repo.publicRepository ? "Public" : "Private"}
              </Badge>

              <Button asChild variant="outline" size="sm">
                <Link href={`/history/${repo.id}`}>히스토리 보기</Link>
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}