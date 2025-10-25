// asrc/app/(dashboard)/nalysis/loading/page.tsx
"use client"

import LoadingContent from "@/components/analysis/LoadingContent"
import { useRequireAuth } from "@/hooks/auth/useRequireAuth"

export default function LoadingPage() {
  const { user } = useRequireAuth()
  if (!user) return null
  
  return <LoadingContent />
}
