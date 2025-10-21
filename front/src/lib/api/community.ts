// community 도메인 API
import { Repo } from "../../types/community"

export const fetchRepos = async (): Promise<Repo[]> => {
  const res = await fetch("http://localhost:8080/api/community/repositories")
  if (!res.ok) throw new Error("레포지토리 조회 실패")
  return res.json()
}