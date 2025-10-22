// community 도메인 API

// 공개 상태 리포지토리 조회
export async function fetchRepositories() {
  const res = await fetch("http://localhost:8080/api/community/repositories", {
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}


// ✅ 댓글 조회
export async function fetchComments(analysisResultId: string) {
  const res = await fetch(`http://localhost:8080/api/community/2/comments`)
  if (!res.ok) {
    throw new Error("댓글 조회 실패")
  }
  return res.json()
}

// ✅ 댓글 작성
export async function postComment(analysisResultId: string, memberId: number, comment: string) {
  const res = await fetch(`http://localhost:8080/api/community/2/write`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      memberId,
      comment,
    }),
  })

  if (!res.ok) {
    throw new Error("댓글 작성 실패")
  }

  return res.json()
}