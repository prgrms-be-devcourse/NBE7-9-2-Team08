'use client';
import { useEffect, useMemo, useState } from 'react';
import { useToast } from '@/components/ui/Toast';
import { authApi, type GetUserResponse } from '@/lib/api/auth';

export function useAuth() {
  const toast = useToast();
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<GetUserResponse | null>(null);
  const [isLoadingUser, setIsLoadingUser] = useState(false);

  const fetchUserInfo = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
      setIsLoadingUser(true);
      console.log('사용자 정보 요청 시작');
      const userData = await authApi.getCurrentUser();
      console.log('사용자 정보 받음:', userData);
      setUser(userData);
    } catch (error) {
      console.error('사용자 정보 가져오기 실패:', error);
      // 에러 발생 시 로그아웃하지 않고 그냥 넘어감 (무한 루프 방지)
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
        } catch (error) {
          console.error('사용자 정보 파싱 실패:', error);
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
        window.location.reload();
      }, 2 * 60 * 60 * 1000); // ✅ 2시간 (7200000ms)
  
      return () => clearTimeout(logoutTimer);
    }, [token]); // token이 새로 설정될 때마다 타이머 재설정

  const isAuthed = useMemo(() => !!token, [token]);

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
      window.location.reload();
    } catch (error) {
      console.error('❌ 로그아웃 실패:', error);
      toast.push('로그아웃 중 오류가 발생했습니다.');
    }
  }


  return { 
    isAuthed, 
    token, 
    user, 
    isLoadingUser,
    loginWithToken, 
    logout,
    fetchUserInfo 
  };
}
