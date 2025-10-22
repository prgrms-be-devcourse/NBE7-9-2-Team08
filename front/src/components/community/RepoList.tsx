'use client';

import { useRepositories } from '@/hooks/community/useCommunity';
import RepositoryCard from './RepoCard';

export default function RepositoryList() {
  const { data, loading, error } = useRepositories();

  if (loading) return <p>로딩 중...</p>;
  if (error) return <p className="text-red-500">에러 발생: {error}</p>;

  return (
    <section className="flex flex-col gap-6 mt-6">
      {data.map((item, i) => (
        <RepositoryCard key={i} item={item} />
      ))}
    </section>
  );
}
