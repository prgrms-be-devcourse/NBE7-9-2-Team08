// 분석 요청 페이지
// app/(dashboard)/analysis/page.tsx
"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/Button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card } from "@/components/ui/card"
import { Sparkles, Github, Clock, TrendingUp, Search } from "lucide-react"
import { isValidGitHubUrl } from "@/lib/utils/validation"
import { useAnalysis } from "@/hooks/analysis/useAnalysis"
import { useRequireAuth } from "@/hooks/auth/useRequireAuth"

export default function AnalyzePage() {
  const [repoUrl, setRepoUrl] = useState("")
  const [isValidUrl, setIsValidUrl] = useState(true)
  const router = useRouter()
  const { requestAnalysis, isLoading, error } = useAnalysis()

  const { user } = useRequireAuth()
  if (!user) return null

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
  
    if (!isValidGitHubUrl(repoUrl)) {
      setIsValidUrl(false);
      return;
    }
  
    setIsValidUrl(true);
    
    // 여기서 API 요청하지 않고 repoUrl만 전달
    const encodedUrl = encodeURIComponent(repoUrl);
    router.push(`/analysis/loading?repoUrl=${encodedUrl}`);
  };

  const handleUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setRepoUrl(e.target.value)
    if (!isValidUrl) setIsValidUrl(true)
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="mx-auto max-w-3xl">
          <div className="mb-12 text-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/10 px-4 py-1.5 text-sm text-primary">
              <Sparkles className="h-4 w-4" />
              <span>AI 기반 분석</span>
            </div>
            <h1 className="mb-4 text-4xl font-bold tracking-tight text-balance sm:text-5xl">
              리포지토리 분석하기
            </h1>
            <p className="text-lg text-muted-foreground text-pretty">
              GitHub 리포지토리 URL을 입력하여 관리 상태와 협업 품질을 분석하세요
            </p>
          </div>

          <Card className="p-8 shadow-lg">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="repo-url" className="text-base">
                  GitHub 리포지토리 URL
                </Label>
                <div className="relative">
                  <Github className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="repo-url"
                    type="url"
                    placeholder="https://github.com/username/repository"
                    value={repoUrl}
                    onChange={handleUrlChange}
                    className={`pl-10 h-12 text-base ${!isValidUrl ? "border-destructive" : ""}`}
                    disabled={isLoading}
                    required
                  />
                </div>
                {!isValidUrl && (
                  <p className="text-sm text-destructive">올바른 GitHub 리포지토리 URL을 입력하세요</p>
                )}
                {error && (
                  <p className="text-sm text-destructive">{error.message}</p>
                )}
              </div>

              <Button
                type="button"  // ✅ form submit 방지
                onClick={handleSubmit}  // ✅ 직접 이벤트 트리거
                className="w-full"
                size="lg"
                disabled={isLoading}
              >
                <Search className="mr-2 h-5 w-5" />
                {isLoading ? "분석 요청 중..." : "분석 시작하기"}
              </Button>
            </form>
          </Card>

          {/* Info Cards */}
          <div className="mt-12 grid gap-6 sm:grid-cols-3">
            <Card className="p-6 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <Clock className="h-6 w-6" />
              </div>
              <h3 className="mb-1 font-semibold">빠른 분석</h3>
              <p className="text-sm text-muted-foreground">30초 이내 결과 제공</p>
            </Card>

            <Card className="p-6 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <TrendingUp className="h-6 w-6" />
              </div>
              <h3 className="mb-1 font-semibold">상세한 인사이트</h3>
              <p className="text-sm text-muted-foreground">종합적인 평가 시스템</p>
            </Card>

            <Card className="p-6 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <Sparkles className="h-6 w-6" />
              </div>
              <h3 className="mb-1 font-semibold">AI 기반</h3>
              <p className="text-sm text-muted-foreground">스마트한 개선 제안</p>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}