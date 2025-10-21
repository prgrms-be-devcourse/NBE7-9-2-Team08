// 회원가입 페이지
'use client';
import { useState, FormEvent } from 'react';
import { authApi } from '@/lib/api/auth';
import type { SignupRequest } from '@/types/auth';
import { useToast } from '@/components/ui/Toast';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/Button';

export default function SignupPage() {
  const toast = useToast();
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (password !== confirmPassword) {
      toast.push('비밀번호가 일치하지 않습니다.');
      return;
    }
    const payload: SignupRequest = { username, email, password, confirmPassword };
    try {
      setLoading(true);
      const res = await authApi.signup(payload);
      toast.push(res.message ?? '회원가입 성공');
      router.push('/login');
    } catch (e: any) {
      toast.push(`회원가입 실패: ${e.message}`);
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
              <CardTitle className="text-2xl">회원가입</CardTitle>
            </CardHeader>
            <CardContent>
              <form className="space-y-6" onSubmit={submit}>
                <div className="space-y-2">
                  <Label htmlFor="username">아이디</Label>
                  <Input id="username" value={username} onChange={(e) => setUsername(e.target.value)} required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">이메일</Label>
                  <Input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">비밀번호</Label>
                  <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">비밀번호 확인</Label>
                  <Input id="confirmPassword" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
                </div>
                <Button type="submit" className="w-full" size="lg" disabled={loading}>
                  {loading ? '처리중...' : '회원가입'}
                </Button>
                <p className="text-xs text-muted-foreground">스프링 엔드포인트: POST /api/v1/auth/signup</p>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}