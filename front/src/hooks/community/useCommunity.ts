'use client';

import { useEffect, useState } from 'react';
import { fetchRepositories } from '@/lib/api/community';
import type { RepositoryItem } from '@/types/community';

export function useRepositories() {
  const [data, setData] = useState<RepositoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  return { data, loading, error };
}