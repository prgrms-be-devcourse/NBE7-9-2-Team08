// 커뮤니티 사용자 분석 결과 상세 페이지, 댓글 기능 있어야 하는 곳
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
