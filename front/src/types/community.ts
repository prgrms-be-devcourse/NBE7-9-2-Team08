// community 도메인 타입

// 공개 상태 repository 목록 조회
export interface RepositoryItem {
  userName: string
  userImage: string | null
  repositoryName: string
  repositoryId: number
  summary: string
  description: string
  language: string[]
  totalScore: number
  createDate: string // LocalDateTime → string
  viewingStatus: boolean
  htmlUrl: string
}


// comment 목록 조회
export interface Comment {
  id: number
  memberId: number
  name: string
  comment: string
  createDate: string
}