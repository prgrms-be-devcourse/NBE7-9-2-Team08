'use client';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';

export default function Header() {
  const { isAuthed, logout } = useAuth();

  return (
    <header style={{borderBottom:'1px solid #eee', padding:'12px 0', marginBottom:16}}>
      <nav className="container" style={{display:'flex', gap:12, alignItems:'center', justifyContent:'space-between'}}>
        <div style={{display:'flex', gap:12}}>
          <Link href="/">홈</Link>
          <Link href="/users">사용자</Link>
        </div>
        <div style={{display:'flex', gap:12}}>
          {!isAuthed ? <Link href="/(auth)/login">로그인</Link> : <button className="button" onClick={logout}>로그아웃</button>}
        </div>
      </nav>
    </header>
  );
}
