"use client"

import { useEffect, useState } from "react"
import HistoryContent from "@/components/history/HistoryContent"
import type { StoredUser } from "@/types/user"

export default function HistoryPage() {
  const [user, setUser] = useState<StoredUser | null>(null)

  useEffect(() => {
    const stored = localStorage.getItem("user")
    if (stored) {
      try {
        setUser(JSON.parse(stored))
      } catch (err) {
        console.error("유저 정보 파싱 실패:", err)
      }
    }
  }, [])

  if (!user) return <p className="p-8 text-center">유저 정보를 불러오는 중...</p>

  return <HistoryContent memberId={user.id} name={user.name} />
}