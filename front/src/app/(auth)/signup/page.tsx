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
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [passwordCheck, setPasswordCheck] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (password !== passwordCheck) {
      toast.push('비밀번호가 일치하지 않습니다.');
      return;
    }
    const payload: SignupRequest = { 
      email, 
      name, 
      password, 
      passwordCheck,
      imageUrl: imageUrl || undefined
    };
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
                  <Label htmlFor="email">이메일</Label>
                  <Input 
                    id="email" 
                    type="email" 
                    value={email} 
                    onChange={(e) => setEmail(e.target.value)} 
                    placeholder="example@email.com"
                    required 
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="name">이름</Label>
                  <Input 
                    id="name" 
                    value={name} 
                    onChange={(e) => setName(e.target.value)} 
                    placeholder="사용할 이름을 입력하세요"
                    required 
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">비밀번호</Label>
                  <Input 
                    id="password" 
                    type="password" 
                    value={password} 
                    onChange={(e) => setPassword(e.target.value)} 
                    placeholder="8자 이상 입력하세요"
                    required 
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="passwordCheck">비밀번호 확인</Label>
                  <Input 
                    id="passwordCheck" 
                    type="password" 
                    value={passwordCheck} 
                    onChange={(e) => setPasswordCheck(e.target.value)} 
                    placeholder="비밀번호를 다시 입력하세요"
                    required 
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="imageUrl">사진 주소 (선택사항)</Label>
                  <Input 
                    id="imageUrl" 
                    type="url" 
                    value={imageUrl} 
                    onChange={(e) => setImageUrl(e.target.value)} 
                    placeholder="https://example.com/profile.jpg"
                  />
                </div>
                <Button type="submit" className="w-full" size="lg" disabled={loading}>
                  {loading ? '처리중...' : '회원가입'}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}