"use client"

import { useEffect, useState } from "react"
import { fetchHistory } from "@/lib/api/history"
import type { RepositoryResponse } from "@/types/history"

export function useHistory(memberId: number) {
  const [repositories, setRepositories] = useState<RepositoryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function load() {
      try {
        const result = await fetchHistory(memberId)
        setRepositories(result)
      } catch (err) {
        setError((err as Error).message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [memberId])

  return { repositories, loading, error }
}