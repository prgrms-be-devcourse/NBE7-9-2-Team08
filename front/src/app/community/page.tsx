//커뮤니티 사용자 분석 결과 상세 페이지, 댓글 기능 있어야 하는 곳
// 커뮤니티 메인 페이지
import RepositoryList from '@/components/community/RepoList';

export default function HomePage() {
  return (
    <main className="max-w-3xl mx-auto p-6">
      <RepositoryList />
    </main>
  );
}