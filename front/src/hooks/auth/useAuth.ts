'use client';

import { useRouter } from "next/navigation"
import { useEffect, useMemo, useState } from 'react';
import { useToast } from '@/components/ui/Toast';
import { authApi, type GetUserResponse } from '@/lib/api/auth';

export function useAuth() {
  const router = useRouter()

  const toast = useToast();
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<GetUserResponse | null>(null);
  const [isLoadingUser, setIsLoadingUser] = useState(false);
  const [isInitializing, setIsInitializing] = useState(true); 
  const [refreshTrigger, setRefreshTrigger] = useState(0);


  const fetchUserInfo = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setIsInitializing(false);
      return;
    }

    try {
      setIsLoadingUser(true);
      const userData = await authApi.getCurrentUser();
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (error) {
      console.error('사용자 정보 가져오기 실패:', error);
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
    } finally {
      setIsLoadingUser(false);
    }
  };

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const t = localStorage.getItem('accessToken');
      const savedUser = localStorage.getItem('user');
      
      console.log('useAuth 초기화 - token:', t, 'savedUser:', savedUser);
      
      setToken(t);
      
      if (savedUser) {
        try {
          const parsedUser = JSON.parse(savedUser);
          console.log('저장된 사용자 정보 복원:', parsedUser);
          setUser(parsedUser);
          setIsInitializing(false);
        } catch (error) {
          console.error('사용자 정보 파싱 실패:', error);
          setIsInitializing(false);
        }
      } else if (t) {
        // 토큰은 있는데 사용자 정보가 없으면 가져오기
        console.log('토큰은 있지만 사용자 정보 없음, fetchUserInfo 호출');
        fetchUserInfo();
      }
    }
  }, []);

    // ✅ 로그인된 상태일 때 자동 로그아웃 타이머 (2시간 후)
    useEffect(() => {
      if (!token) return; // 로그인 안 되어 있으면 실행 안 함
  
      console.log('⏰ 2시간 자동 로그아웃 타이머 시작');
      const logoutTimer = setTimeout(() => {
        console.warn('🔒 토큰 만료 — 자동 로그아웃 실행');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
        toast.push('세션이 만료되어 로그아웃되었습니다.');

        window.location.href = '/';
      }, 2 * 60 * 60 * 1000); // ✅ 2시간 (7200000ms)
  
      return () => clearTimeout(logoutTimer);
    }, [token, router, toast]); // token이 새로 설정될 때마다 타이머 재설정

  const isAuthed = useMemo(() => !!token && !!user, [token, user]);

  function loginWithToken(userData: GetUserResponse) {
    console.log('loginWithToken 호출됨, userData:', userData);
    // 사용자 정보를 로컬 스토리지에 저장
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('accessToken', 'logged_in'); // accessToken도 저장
    setUser(userData);
    setToken('logged_in');
    console.log('사용자 정보 저장 완료');
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
