'use client';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { http } from '@/lib/api';
import type { Page, User } from '@/lib/types';

export default function UsersPage() {
  const [page, setPage] = useState(0);
  const size = 10;

  const q = useQuery({
    queryKey: ['users', page, size],
    queryFn: () => http.get<Page<User>>(`/api/v1/users?page=${page}&size=${size}`, 'cookie'),
    retry: 1,
  });

  function next() { if ((q.data?.totalPages ?? 0) > page + 1) setPage(p => p + 1); }
  function prev() { if (page > 0) setPage(p => p - 1); }

  if (q.isLoading) return <p>로딩중…</p>;
  if (q.isError) return <p>에러: {(q.error as Error).message}</p>;

  return (
    <section>
      <h1>사용자 목록</h1>
      <table>
        <thead><tr><th>ID</th><th>Email</th><th>Name</th><th>Created</th></tr></thead>
        <tbody>
          {q.data?.content.map(u => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.email}</td>
              <td>{u.name}</td>
              <td>{u.createdAt ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{display:'flex', gap:8, marginTop:12, alignItems:'center'}}>
        <button className="button" onClick={prev} disabled={page===0}>이전</button>
        <span>페이지 {page+1} / {q.data?.totalPages ?? 1}</span>
        <button className="button" onClick={next} disabled={(q.data?.totalPages ?? 1) <= page+1}>다음</button>
      </div>

      <p style={{marginTop:12, opacity:.7}}>스프링 엔드포인트: GET /api/v1/users?page=0&size=10 (Page&lt;User&gt;)</p>
    </section>
  );
}
