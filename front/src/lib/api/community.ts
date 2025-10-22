// community 도메인 API

// 공개 상태 리포지토리 조회
export async function fetchRepositories() {
  const res = await fetch("http://localhost:8080/api/community/repositories", {
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

// 댓글 조회
// 임시로 ${analysisResultId} 를 1로 설정해뒀습니다.
import { Comment } from "@/types/community"

export async function fetchComments(analysisResultId: string): Promise<Comment[]> {
  const res = await fetch(`http://localhost:8080/api/community/1/comments`)
  if (!res.ok) throw new Error("댓글 불러오기 실패")
  return res.json()
}