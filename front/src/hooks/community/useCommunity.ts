'use client';

import { useEffect, useState, useMemo } from 'react';
import { fetchRepositories } from '@/lib/api/community';
import type { RepositoryItem } from '@/types/community';

export function useRepositories() {
  const [data, setData] = useState<RepositoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sortType, setSortType] = useState<'latest' | 'score'>('latest')

  useEffect(() => {
    (async () => {
      try {
        const res = await fetchRepositories();
        setData(Array.isArray(res) ? res : [res]);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const sortedData = useMemo(() => {
    if (sortType === 'score') {
      return data
        .slice()
        .sort((a, b) => (b.totalScore ?? 0) - (a.totalScore ?? 0))
    }

    // 최신순
    const parseDate = (d?: string) => {
      if (!d) return 0
      // 백엔드 LocalDateTime 형식 → JS Date 변환 (마이크로초 제거 + UTC 보정)
      return Date.parse(d.split('.')[0] + 'Z')
    }

    return data
      .slice()
      .sort((a, b) => parseDate(b.createDate) - parseDate(a.createDate))
  }, [data, sortType])

  return {
    repositories: sortedData,
    loading,
    error,
    sortType,
    setSortType,
  }
}