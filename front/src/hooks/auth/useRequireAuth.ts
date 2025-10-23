// hooks/useRequireAuth.ts
"use client"
import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/hooks/auth/useAuth"

export function useRequireAuth() {
  const router = useRouter()
  const { isAuthed, user, isInitializing } = useAuth()

  useEffect(() => {
    // ⬇️ 초기화 중이면 리다이렉트하지 않음
    if (isInitializing) {
      console.log("인증 상태 확인 중...")
      return
    }
    
    // ⬇️ 초기화 완료 후 인증 실패 시에만 리다이렉트
    if (!isAuthed || !user) {
      console.log("인증 실패, /login으로 리다이렉트")
      router.replace("/login")
    } else {
      console.log("인증 성공:", user)
    }
  }, [isAuthed, user, isInitializing, router])

  return { user, isAuthed, isInitializing }
}