import type { RepositoryItem, Comment, PageResponse } from "@/types/community"

// ✅ 공개 리포지토리 조회 (페이징)
export async function fetchRepositories(page = 0, size = 5): Promise<PageResponse<RepositoryItem>> {
  const res = await fetch(
    `http://localhost:8080/api/community/repositories?page=${page}&size=${size}`,
    { cache: "no-store" }
  )
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

// ✅ 댓글 조회 (페이징)
export async function fetchComments(
  analysisResultId: number,
  page = 0,
  size = 5
): Promise<PageResponse<Comment>> {
  const res = await fetch(
    `http://localhost:8080/api/community/${analysisResultId}/comments?page=${page}&size=${size}`
  )
  if (!res.ok) throw new Error("댓글 조회 실패")
  return res.json()
}

// ✅ 댓글 작성
export async function postComment(analysisResultId: number, memberId: number, comment: string) {
  const res = await fetch(`http://localhost:8080/api/community/${analysisResultId}/write`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include", // 👈 추가 (쿠키 포함)
    body: JSON.stringify({ memberId, comment }),
  })
  if (!res.ok) throw new Error("댓글 작성 실패")
  return res.json()
}

// ✅ 댓글 수정
export async function updateComment(commentId: number, newComment: string) {
  const res = await fetch(`http://localhost:8080/api/community/modify/${commentId}/comment`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    credentials: "include", // 👈 추가 (JWT 포함)
    body: JSON.stringify({ newComment }),
  })
  if (!res.ok) throw new Error("댓글 수정 실패")
  return res.text()
}

// ✅ 댓글 삭제
export async function deleteComment(commentId: number) {
  const res = await fetch(`http://localhost:8080/api/community/delete/${commentId}`, {
    method: "DELETE",
    credentials: "include", // 👈 추가 (쿠키 포함)
  })
  if (!res.ok) throw new Error("댓글 삭제 실패")
  return res.text()
}

