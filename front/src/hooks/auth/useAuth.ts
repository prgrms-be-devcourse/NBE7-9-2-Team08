'use client';
import { useEffect, useMemo, useState } from 'react';
import { useToast } from '@/components/ui/Toast';

export function useAuth() {
  const toast = useToast();
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const t = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
    setToken(t);
  }, []);

  const isAuthed = useMemo(() => !!token, [token]);

  function loginWithToken(token: string) {
    localStorage.setItem('accessToken', token);
    setToken(token);
    toast.push('로그인되었습니다.');
  }

  function logout() {
    localStorage.removeItem('accessToken');
    setToken(null);
    toast.push('로그아웃되었습니다.');
  }

  return { isAuthed, token, loginWithToken, logout };
}
