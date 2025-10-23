// hooks/useRequireAuth.ts
"use client"
import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/hooks/auth/useAuth"

export function useRequireAuth() {
  const router = useRouter()
  const { isAuthed, user } = useAuth()

  useEffect(() => {
    if (!isAuthed || !user) {
      router.replace("/login")
    }
  }, [isAuthed, user, router])

  return { user, isAuthed }
}
