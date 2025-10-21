"use client"

import Link from "next/link"
import { useRouter } from "next/navigation"
import { Sparkles } from "lucide-react"
import { Button } from "@/components/ui/Button"
import { useAuth } from "@/hooks/auth/useAuth"

export default function Header() {
  const router = useRouter()
  const { isAuthed, logout } = useAuth()

  const guardNav = (path: string) => () => {
    if (!isAuthed) {
      alert("로그인해주세요")
      router.push("/login")
      return
    }
    router.push(path)
  }

  return (
    <nav className="border-b border-gray-200 bg-background/80 backdrop-blur-xl">
      {/* ✅ 가로폭 전체 사용 */}
      <div className="w-full max-w-none px-8">
        <div className="flex h-16 items-center justify-between">
          {/* 로고 */}
          <Link
            href="/"
            className="flex items-center gap-2 hover:opacity-80 transition-opacity"
          >
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
              <Sparkles className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="text-xl font-bold">PortfolioIQ</span>
          </Link>

          {/* 메뉴 */}
          <div className="flex items-center gap-6">
            {isAuthed ? (
              <>
                <button onClick={guardNav("/history")} className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  히스토리
                </button>
                <button onClick={guardNav("/community")} className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  커뮤니티
                </button>
                <button onClick={guardNav("/settings")} className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  마이페이지
                </button>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={logout}
                  className="text-muted-foreground hover:text-foreground"
                >
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <button onClick={guardNav("/community")} className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  커뮤니티
                </button>

                <div className="flex items-center gap-3">
                  <Button variant="ghost" size="sm" onClick={() => router.push("/login")}>
                    로그인
                  </Button>
                  <Button size="sm" className="bg-primary text-primary-foreground hover:bg-primary/90" onClick={() => router.push("/signup")}>
                    시작하기
                  </Button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
