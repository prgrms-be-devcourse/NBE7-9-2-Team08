// 커뮤니티 사용자 분석 결과 상세 페이지, 댓글 기능 있어야 하는 곳
// import RepositoryList from '@/components/community/RepoList';

// export default function HomePage() {
//   return (
//     <main className="max-w-3xl mx-auto p-6">
//       <h1 className="text-3xl font-bold mb-2 text-blue-600">Portfol.ioQ</h1>
//       <p className="text-gray-500 text-sm mb-6">커뮤니티 레포지토리 피드</p>
//       <RepositoryList />
//     </main>
//   );
// }

import { CommentList } from "@/components/community/CommentList"

export default function CommunityPage() {
  return (
    <div className="max-w-2xl mx-auto py-10">
      <h1 className="text-2xl font-bold mb-6">커뮤니티 페이지</h1>

      {/* 기존 콘텐츠 */}
      <section className="mb-10">
        {/* ...기존 피드, 카드 등 */}
      </section>

      {/* ✅ 테스트용 댓글 목록 (확인 후 삭제 예정) */}
      <section className="border-t pt-6 mt-6">
        <h2 className="text-lg font-semibold mb-4">💬 댓글 테스트</h2>
        <CommentList analysisResultId="spring-boot-app" />
      </section>
    </div>
  )
}