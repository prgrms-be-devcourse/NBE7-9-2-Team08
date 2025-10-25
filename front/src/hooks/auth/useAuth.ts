'use client';

import { useEffect, useMemo, useState } from 'react';
import { useToast } from '@/components/ui/Toast';
import { authApi, type GetUserResponse } from '@/lib/api/auth';

export function useAuth() {
  const toast = useToast();
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<GetUserResponse | null>(null);
  const [isLoadingUser, setIsLoadingUser] = useState(false);
  const [isInitializing, setIsInitializing] = useState(true); 
  const [refreshTrigger, setRefreshTrigger] = useState(0);


  const fetchUserInfo = async () => {
    try {
      setIsLoadingUser(true);
      const userData = await authApi.getCurrentUser();
  
      if (!userData) {
        // 게스트라면 초기화만 하고 반환
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
        return;
      }
      
      setUser(userData);
      setToken('session');
      localStorage.setItem('user', JSON.stringify(userData));
      localStorage.setItem('accessToken', 'session');
    } catch (error) {
      console.error('사용자 정보 가져오기 실패:', error);
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
    } finally {
      setIsLoadingUser(false);
      setIsInitializing(false);
    }
  };

  useEffect(() => {
    if (typeof window === 'undefined') return;

    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
        setToken('session');
      } catch {
        localStorage.removeItem('user');
      } finally {
        setIsInitializing(false);
      }
    } else {
      // 로컬 스토리지가 비어 있어도 쿠키가 남아 있으면 서버에서 판별
      fetchUserInfo();
    }
  }, []);

    // ✅ 로그인된 상태일 때 자동 로그아웃 타이머 (2시간 후)
    useEffect(() => {
    if (!token) return;

    const logoutTimer = setTimeout(() => {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
      toast.push('세션이 만료되어 자동 로그아웃되었습니다.');
      window.location.href = '/';
    }, 2 * 60 * 60 * 1000);

    return () => clearTimeout(logoutTimer);
  }, [token, toast]);

  const isAuthed = useMemo(() => !!token && !!user, [token, user]);
  
  function loginWithToken(userData: GetUserResponse) {
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('accessToken', 'session');
    setUser(userData);
    setToken('session');
    toast.push('로그인되었습니다.');
  }

  async function logout() {
    try {
      // ✅ 1️⃣ 서버 세션 로그아웃 요청
      await authApi.logout();

      // ✅ 2️⃣ 클라이언트 측 상태 초기화
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);

      // ✅ 3️⃣ 피드백 토스트
      toast.push('로그아웃되었습니다.');
      window.location.href = '/';
    } catch (error) {
      console.error('❌ 로그아웃 실패:', error);
      toast.push('로그아웃 중 오류가 발생했습니다.');
    }
  }

  // ✅ 사용자 정보 업데이트 후 전역 상태 갱신
  function updateUserInfo(updatedUser: GetUserResponse) {
    console.log('사용자 정보 업데이트:', updatedUser);
    
    // ✅ 상태 업데이트와 로컬 스토리지 저장을 동시에
    setUser(prevUser => {
      console.log('상태 업데이트 - 이전:', prevUser, '새로운:', updatedUser);
      localStorage.setItem('user', JSON.stringify(updatedUser));
      return updatedUser;
    });
    
    // ✅ 강제 리렌더링을 위한 트리거 업데이트
    setRefreshTrigger(prev => prev + 1);
    
    console.log('전역 상태 업데이트 완료:', updatedUser);
  }


  return { 
    isAuthed, 
    token, 
    user, 
    isLoadingUser,
    isInitializing,
    refreshTrigger,
    loginWithToken, 
    logout,
    fetchUserInfo,
    updateUserInfo
  };
}
