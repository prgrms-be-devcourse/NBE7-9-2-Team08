'use client'

import { useRepositories } from '@/hooks/community/useCommunity'
import RepositoryCard from './RepoCard'
import { Button } from '@/components/ui/Button' // ✅ 소문자 b로 경로 통일 (shadcn 규칙)
import { Loader2 } from 'lucide-react'

export default function RepositoryList() {
  const {
    repositories,
    loading,
    error,
    sortType,
    setSortType,
    page,
    setPage,
    totalPages,
  } = useRepositories()

  // ✅ 로딩 상태
  if (loading)
    return (
      <div className="flex justify-center items-center py-20 text-muted-foreground">
        <Loader2 className="w-5 h-5 animate-spin mr-2" />
        데이터를 불러오는 중입니다...
      </div>
    )

  // ✅ 에러 상태
  if (error)
    return <p className="text-red-500 text-center py-8">에러 발생: {error}</p>

  return (
    <section className="flex flex-col gap-6 mt-6">
      {/* 헤더 + 정렬 버튼 한 줄 정렬 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">커뮤니티</h1>
          <p className="text-muted-foreground text-sm">
            다른 사용자의 분석 결과를 둘러보세요.
          </p>
        </div>

        {/* 정렬 버튼 그룹 */}
        <div className="flex gap-2">
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
      </div>

      {/* 리포지토리 목록 */}
      {repositories.length === 0 ? (
        <p className="text-center text-muted-foreground py-8">
          아직 공개된 분석이 없습니다.
        </p>
      ) : (
        <div className="flex flex-col gap-6">
          {repositories.map((item) => (
            <RepositoryCard key={item.repositoryId} item={item} />
          ))}
        </div>
      )}

      {/* ✅ 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mt-8">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            이전
          </Button>

          <span className="text-sm text-muted-foreground">
            {page + 1} / {totalPages}
          </span>

          <Button
            variant="outline"
            size="sm"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage(page + 1)}
          >
            다음
          </Button>
        </div>
      )}
    </section>
  )
}
