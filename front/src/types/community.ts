// community 도메인 타입
export type Repo = {
  id: number
  userName: string
  userImage?: string
  repoName: string
  summary: string
  languages?: string[]
  totalScore: number
}