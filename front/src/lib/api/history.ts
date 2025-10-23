// repository 도메인 API
import type { RepositoryResponse } from "@/types/history"

export async function fetchHistory(memberId: number): Promise<RepositoryResponse[]> {
  const res = await fetch(`http://localhost:8080/api/analysis/${memberId}/repositories`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  })

  if (!res.ok) throw new Error("히스토리 데이터를 불러오는 데 실패했습니다.")
  return res.json()
}