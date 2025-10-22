//커뮤니티 사용자 분석 결과 상세 페이지, 댓글 기능 있어야 하는 곳
import RepositoryList from '@/components/community/RepoList';

export default function HomePage() {
  return (
    <main className="max-w-3xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-2 text-blue-600">Portfol.ioQ</h1>
      <p className="text-gray-500 text-sm mb-6">커뮤니티 레포지토리 피드</p>
      <RepositoryList />
    </main>
  );
}

// "use client"

// import { useState } from "react"
// import { CommentList } from "@/components/community/CommentList"
// import { CommentForm } from "@/components/community/CommentForm"

// export default function CommunityPage() {
//   const [refreshKey, setRefreshKey] = useState(0)

//   return (
//     <main className="max-w-2xl mx-auto py-10">
//       <h1 className="text-2xl font-bold mb-6">💬 댓글 테스트 페이지</h1>

//       {/* ✅ 댓글 작성 폼을 위로 이동 */}
//       <section className="mb-8">
//         <h2 className="text-lg font-semibold mb-3">댓글 작성</h2>
//         <CommentForm
//           analysisResultId="1"
//           memberId={1} // 로그인 후 실제 유저 ID로 대체
//           onCommentAdded={() => setRefreshKey((k) => k + 1)} // 작성 후 목록 새로고침
//         />
//       </section>

//       {/* ✅ 댓글 목록은 아래쪽에 표시 */}
//       <section className="border-t border-border pt-6">
//         <h2 className="text-lg font-semibold mb-3">댓글 목록</h2>
//         <CommentList key={refreshKey} analysisResultId="1" />
//       </section>
//     </main>
//   )
// }