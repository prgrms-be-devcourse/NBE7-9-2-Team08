// repository 도메인 API
export async function fetchRepositories(memberId: number) {
    const res = await fetch(`http://localhost:8080/api/analysis/1/repositories`, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
      cache: "no-store", // Next.js에서 캐싱 방지
    })
  
    if (!res.ok) {
      throw new Error("리포지토리 목록을 불러오는 데 실패했습니다.")
    }
  
    return res.json()
  }