"use client"

import { useEffect, useState } from "react"
import { fetchRepositories } from "@/lib/api/repository"
import type { RepositoryResponse } from "@/types/repository"

export function useRepositories(memberId: number) {
  const [repositories, setRepositories] = useState<RepositoryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function load() {
      try {
        const result = await fetchRepositories(memberId)
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