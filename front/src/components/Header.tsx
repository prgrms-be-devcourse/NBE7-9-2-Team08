"use client"

import Link from "next/link"
import { useState } from "react"
import { Sparkles } from "lucide-react"
import { Button } from "@/components/ui/Button"

export default function Header() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)

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
            {isLoggedIn ? (
              <>
                <Link href="/history" className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  히스토리
                </Link>
                <Link href="/community" className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  커뮤니티
                </Link>
                <Link href="/settings" className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  마이페이지
                </Link>
                {/* 로그아웃 구현 필요요 */}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setIsLoggedIn(false)}
                  className="text-muted-foreground hover:text-foreground"
                >
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Link href="/community" className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                  커뮤니티
                </Link>

                <div className="flex items-center gap-3">
                  {/* 현재는 로그인 누르면 바로 로그인 상태로 변경 중 */}
                  {/* 로그인 누르면 로그인 화면으로, 시작하기 누르면 회원가입으로 넘어가야 함함 */}
                  <Button variant="ghost" size="sm" onClick={() => setIsLoggedIn(true)}>
                    로그인
                  </Button>
                  <Button size="sm" className="bg-primary text-primary-foreground hover:bg-primary/90" asChild>
                    <Link href="/signup">
                      시작하기
                    </Link>
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
