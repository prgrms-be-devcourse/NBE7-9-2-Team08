"use client"

import { useEffect, useState } from "react"
import { Repo } from "@/types/community"

export function useCommunity() {
  const [repos, setRepos] = useState<Repo[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    fetch("http://localhost:8080/api/community/repositories")
      .then(res => res.json())
      .then(data => {
        setRepos(data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setError(err)
        setLoading(false)
      })
  }, [])

  return { repos, loading, error }
}