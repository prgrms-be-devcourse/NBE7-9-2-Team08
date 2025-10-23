'use client';

import { useRepositories } from '@/hooks/community/useCommunity';
import RepositoryCard from './RepoCard';
import { Button } from '@/components/ui/Button'

export default function RepositoryList() {
  const {
    repositories,
    loading,
    error,
    sortType,
    setSortType,
  } = useRepositories()

  if (loading) return <p>로딩 중...</p>;
  if (error) return <p className="text-red-500">에러 발생: {error}</p>;

  return (
    <section className="flex flex-col gap-6 mt-6">
      {/* ✅ 정렬 버튼 */}
      <div className="flex justify-end gap-2">
        <Button
          variant={sortType === 'latest' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setSortType('latest')}
        >
          최신순
        </Button>
        <Button
          variant={sortType === 'score' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setSortType('score')}
        >
          점수순
        </Button>
      </div>

      {repositories.length === 0 ? (
        <p className="text-center text-muted-foreground">아직 공개된 분석이 없습니다.</p>
      ) : (
        <div className="flex flex-col gap-6">
          {repositories.map((item) => (
            <RepositoryCard key={item.repositoryId} item={item} />
          ))}
        </div>
      )}
    </section>
  );
}