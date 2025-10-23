// LoadingHeader.tsx
"use client"

import { Sparkles } from "lucide-react"

export default function LoadingHeader({ repoUrl }: { repoUrl: string | null }) {
  return (
    <div className="mb-12 text-center">
      <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-primary/10 animate-pulse">
        <Sparkles className="h-8 w-8 text-primary" />
      </div>
      <h1 className="mb-4 text-3xl font-bold tracking-tight">리포지토리 분석 중</h1>
      <p className="text-muted-foreground text-pretty">
        {repoUrl ? `${repoUrl.split("/").slice(-2).join("/")} 분석 중` : "리포지토리 처리 중"}
      </p>
    </div>
  )
}
