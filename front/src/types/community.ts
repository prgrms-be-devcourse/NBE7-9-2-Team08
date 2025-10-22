// community 도메인 타입

// 공개 상태 repository 목록 조회
export interface RepositoryItem {
  userName: string;
  userImage: string | null;
  repositoryName: string;
  summary: string;
  language: string[];
  totalScore: number;
  viewingStatus: boolean;
}

// comment 목록 조회
export interface Comment {
  id: number
  memberId: number
  comment: string
  createDate: string
}