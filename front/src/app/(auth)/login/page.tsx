// 로그인 페이지
'use client';
import Form from '@/components/Form';
import { http } from '@/lib/api/client';
import type { LoginRequest, LoginResponse } from '@/lib/types';
import { useToast } from '@/components/ui/Toast';
import { useAuth } from '@/hooks/auth/useAuth';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const toast = useToast();
  const auth = useAuth();
  const router = useRouter();

  async function submit(values: Record<string,string>) {
    const payload: LoginRequest = { username: values.username, password: values.password };
    try {
      const res = await http.post<LoginResponse>('/api/v1/auth/login', payload, 'cookie'); // 쿠키 세션 예시
      if (res.accessToken) {
        auth.loginWithToken(res.accessToken);
      }
      toast.push(res.message ?? '로그인 성공');
      router.push('/');
    } catch (e: any) {
      toast.push(`로그인 실패: ${e.message}`);
    }
  }

  return (
    <section>
      <h1>로그인</h1>
      <Form
        fields={[
          { name: 'username', label: '아이디' },
          { name: 'password', label: '비밀번호', type: 'password' },
        ]}
        onSubmit={submit}
        submitText="로그인"
      />
      <p style={{marginTop:12, opacity:.7}}>스프링 엔드포인트: POST /api/v1/auth/login</p>
    </section>
  );
}
