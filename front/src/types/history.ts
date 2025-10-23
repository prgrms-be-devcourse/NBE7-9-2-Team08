// repository 도메인 타입
export interface RepositoryResponse {
  id: number
  name: string
  description: string
  htmlUrl: string
  publicRepository: boolean
  mainBranch: string
  languages: string[],
  createDate: string,
  latestScore?: number | null
  latestAnalysisDate?: string | null
}
  