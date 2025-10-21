import type React from "react"
import type { Metadata } from "next"
import { Inter } from "next/font/google"
import "@/styles/globals.css"
import { Suspense } from "react"
import Header from "@/components/Header"
import Footer from "@/components/Footer"
import { ToastProvider } from "@/components/ui/Toast"
import Link from "next/link";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
})

export const metadata: Metadata = {
  title: "PortfolioIQ - AI 기반 GitHub 저장소 분석",
  description:
    "AI로 GitHub 저장소를 분석하고 코드 품질, 문서화, 프로젝트 구조를 개선할 수 있는 실행 가능한 피드백을 받으세요.",
}

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko">
      <body className={`font-sans ${inter.variable} antialiased`}>
        {/* ✅ ToastProvider로 앱 전체 감싸기 */}
        <ToastProvider>
          <Header /> {/* Header는 클라이언트 컴포넌트 */}
          <main>
            <Suspense fallback={<div>로딩 중...</div>}>
              {children}
            </Suspense>
          </main>
          <Footer />
        </ToastProvider>
      </body>
    </html>
  )
}
