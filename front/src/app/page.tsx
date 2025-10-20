"use client"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/Button"
import { ArrowRight, Github, Sparkles, BarChart3, Users, Shield } from "lucide-react"
import { useAuth } from "@/hooks/auth/useAuth"

export default function LandingPage() {
  const router = useRouter()
  const { isAuthed } = useAuth()

  const handleStartAnalysis = () => {
    if (!isAuthed) {
      alert("로그인해주세요")
      return
    }
    router.push("/analysis")
  }

  const handleGoCommunity = () => {
    if (!isAuthed) {
      alert("로그인해주세요")
      return
    }
    router.push("/community")
  }
  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section */}
      <section className="relative flex items-center justify-center overflow-hidden min-h-screen">
        <div className="absolute inset-0 grid-dots opacity-20" />
        <div className="container relative z-10 text-center">
          <div className="mx-auto max-w-4xl text-center">
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/10 px-4 py-1.5 text-sm text-primary">
              <Sparkles className="h-4 w-4" />
              <span>AI 기반 저장소 분석</span>
            </div>

            <h1 className="mb-6 text-5xl font-bold leading-tight tracking-tight text-balance sm:text-6xl lg:text-7xl">
              저장소 분석으로
              <span className="block bg-gradient-to-r from-primary via-purple-400 to-primary bg-clip-text text-transparent">
                포트폴리오 업그레이드
              </span>
            </h1>

            <p className="mb-10 text-lg text-muted-foreground text-pretty sm:text-xl">
              AI가 당신의 GitHub 저장소를 종합 분석하여 객관적 점수와 실용적 개선 제안을 제공합니다. <br />
              코드 품질부터 문서화, 테스트 구성, 프로젝트 구조까지 전문적으로 평가받으세요.
            </p>

            <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
              <Button size="lg" className="group" onClick={handleStartAnalysis}>
                분석 시작하기
                <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />
              </Button>
              <Button size="lg" variant="outline" onClick={handleGoCommunity}>
                <Users className="mr-2 h-4 w-4" />
                커뮤니티 보기
              </Button>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}