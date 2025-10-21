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

export default function LoginPage() {
  const toast = useToast();
  const auth = useAuth();
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    const payload: LoginRequest = { username, password };
    try {
      setLoading(true);
      const res = await authApi.login(payload);
      if (res.accessToken) {
        auth.loginWithToken(res.accessToken);
      }
      toast.push(res.message ?? '로그인 성공');
      router.push('/');
    } catch (e: any) {
      toast.push(`로그인 실패: ${e.message}`);
    } finally {
      setLoading(false);
    }
  }

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
                  <Label htmlFor="username">아이디</Label>
                  <Input id="username" value={username} onChange={(e) => setUsername(e.target.value)} required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">비밀번호</Label>
                  <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                </div>
                <Button type="submit" className="w-full" size="lg" disabled={loading}>
                  {loading ? '처리중...' : '로그인'}
                </Button>
                <p className="text-xs text-muted-foreground">스프링 엔드포인트: POST /api/v1/auth/login</p>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
