"use client"

import { useHistory } from "@/hooks/history/useHistory"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/Button"
import { Github, ExternalLink, Trash2, Calendar } from "lucide-react"
import { useRouter } from "next/navigation"

interface HistoryContentProps {
    memberId: number
    name: string
}

export default function HistoryContent({ memberId, name }: HistoryContentProps) {
    const { repositories, loading, error } = useHistory(memberId)
    const router = useRouter()

    if (loading) return <p className="p-8 text-center">히스토리 불러오는 중...</p>
    if (error) return <p className="p-8 text-center text-red-500">{error}</p>

    return (
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="mb-8">
                <h1 className="mb-2 text-3xl font-bold">분석 히스토리</h1>
                <p className="text-muted-foreground">시간에 따른 리포지토리 개선 사항을 추적하세요</p>
            </div>

            <div className="w-full max-w-3xl space-y-4">
                {repositories.map((repo) => (
                    <Card
                        key={repo.id}
                        className="p-6 transition-all hover:border-primary/50 cursor-pointer"
                        onClick={() => router.push(`/history/${repo.id}`)}
                    >
                        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                            {/* 왼쪽 정보 */}
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