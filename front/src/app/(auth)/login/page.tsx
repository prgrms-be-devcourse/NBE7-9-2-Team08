// 로그인 페이지
'use client';
import { useState, FormEvent } from 'react';
import { authApi } from '@/lib/api/auth';
import type { LoginRequest } from '@/types/auth';
import { useToast } from '@/components/ui/Toast';
import { useAuth } from '@/hooks/auth/useAuth';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/Button';
import Link from 'next/link';
import { Github } from 'lucide-react';

export default function LoginPage() {
  const toast = useToast();
  const auth = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  async function submit(e: FormEvent) {
    e.preventDefault();
    const payload: LoginRequest = { email, password };
    try {
      setLoading(true);
      setErrorMessage(''); // 에러 메시지 초기화
      const res = await authApi.login(payload);
      
      // 로그인 응답에서 사용자 정보 저장
      auth.loginWithToken(res.user);
      
      toast.push('로그인 성공');
      
      // 페이지 리로드하여 상태 즉시 반영
      window.location.href = '/';
    } catch (e: any) {
      console.error('로그인 에러:', e);
      
      // 에러 메시지 파싱
      let errorMsg = '로그인에 실패했습니다.';
      
      // ApiError 객체인 경우 원본 메시지 사용
      if (e.code === 'U001') {
        errorMsg = '아이디/비밀번호를 다시 확인해주세요!';
      } else if (e.message) {
        errorMsg = e.message;
      }
      
      setErrorMessage(errorMsg);
      toast.push(`로그인 실패: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  }

  const handleGithubLogin = () => {
    alert("아직 개발중!");
  };

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="mx-auto max-w-md">
          <Card className="shadow-lg">
            <CardHeader>
              <CardTitle className="text-2xl">로그인</CardTitle>
            </CardHeader>
            <CardContent>
              <form className="space-y-6" onSubmit={submit}>
                <div className="space-y-2">
                  <Label htmlFor="email">이메일</Label>
                  <Input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">비밀번호</Label>
                  <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                </div>
                
                {/* 에러 메시지 표시 */}
                {errorMessage && (
                  <div className="p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
                    {errorMessage}
                  </div>
                )}
                
                <Button type="submit" className="w-full" size="lg" disabled={loading}>
                  {loading ? '처리중...' : '로그인'}
                </Button>
                <Button 
                  type="button" 
                  variant="outline" 
                  className="w-full" 
                  size="lg" 
                  onClick={handleGithubLogin}
                >
                  <Github className="mr-2 h-4 w-4" />
                  Github으로 로그인하기
                </Button>
                <div className="text-center">
                  <span className="text-sm text-muted-foreground">처음이신가요? </span>
                  <Link href="/signup" className="text-sm text-primary hover:underline">
                    회원가입하기
                  </Link>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
