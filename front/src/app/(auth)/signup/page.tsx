// 회원가입 페이지
'use client';
import { useState, FormEvent, useEffect } from 'react';
import { authApi } from '@/lib/api/auth';
import type { SignupRequest } from '@/types/auth';
import { useToast } from '@/components/ui/Toast';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/Button';
import { Github, CheckCircle, Edit3 } from 'lucide-react';

export default function SignupPage() {
  const toast = useToast();
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [passwordCheck, setPasswordCheck] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [loading, setLoading] = useState(false);
  
  // 이메일 인증 관련 상태
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [verificationCode, setVerificationCode] = useState('');
  const [isVerifying, setIsVerifying] = useState(false);
  const [isEmailLocked, setIsEmailLocked] = useState(false);
  const [timeLeft, setTimeLeft] = useState(0); // 남은 시간 (초)
  const [isTimerActive, setIsTimerActive] = useState(false);

  // 타이머 효과
  useEffect(() => {
    let interval: NodeJS.Timeout;
    
    if (isTimerActive && timeLeft > 0) {
      interval = setInterval(() => {
        setTimeLeft((time) => {
          if (time <= 1) {
            setIsTimerActive(false);
            return 0;
          }
          return time - 1;
        });
      }, 1000);
    }
    
    return () => clearInterval(interval);
  }, [isTimerActive, timeLeft]);

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  const handleEmailVerification = async () => {
    if (!email) {
      toast.push('이메일을 입력해주세요.');
      return;
    }
    
    try {
      setIsVerifying(true);
      await authApi.requestEmailVerification({ email });
      toast.push('인증번호가 이메일로 전송되었습니다.');
      setIsEmailLocked(true); // 이메일 잠금
      setTimeLeft(240); // 4분 = 240초
      setIsTimerActive(true);
    } catch (e: any) {
      toast.push(`인증 요청 실패: ${e.message}`);
    } finally {
      setIsVerifying(false);
    }
  };

  const handleVerifyCode = async () => {
    if (!verificationCode) {
      toast.push('인증번호를 입력해주세요.');
      return;
    }
    
    try {
      setIsVerifying(true);
      const res = await authApi.verifyEmailCode({ email, code: verificationCode });
      if (res.code === '200') {
        setIsEmailVerified(true);
        setIsTimerActive(false);
        toast.push('이메일 인증이 완료되었습니다!');
      }
    } catch (e: any) {
      toast.push(`인증 실패: ${e.message}`);
    } finally {
      setIsVerifying(false);
    }
  };

  const handleEmailEdit = () => {
    const confirmed = window.confirm('이메일을 변경하시겠습니까? 다시 재 인증을 받아야 합니다.');
    if (confirmed) {
      setIsEmailVerified(false);
      setIsEmailLocked(false);
      setVerificationCode('');
      setIsTimerActive(false);
      setTimeLeft(0);
    }
  };

  const handleGithubSignup = () => {
    alert("아직 개발중!");
  };

  // 가입하기 버튼 활성화 조건
  const isSignupEnabled = isEmailVerified && 
    email && 
    name && 
    password && 
    passwordCheck && 
    password === passwordCheck;

  async function submit(e: FormEvent) {
    e.preventDefault();
    
    if (!isEmailVerified) {
      toast.push('이메일 인증을 완료해주세요.');
      return;
    }
    
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
        <div className="grid lg:grid-cols-2 gap-12 max-w-6xl mx-auto">
          {/* 왼쪽: 회원가입 폼 */}
          <div className="space-y-8">
            <Card className="shadow-lg">
              <CardHeader>
                <CardTitle className="text-2xl">회원가입</CardTitle>
              </CardHeader>
              <CardContent>
                <form className="space-y-6" onSubmit={submit}>
                  {/* 이메일 입력 및 인증 */}
                  <div className="space-y-2">
                    <Label htmlFor="email">이메일</Label>
                    <div className="flex gap-2">
                      <Input 
                        id="email" 
                        type="email" 
                        value={email} 
                        onChange={(e) => setEmail(e.target.value)} 
                        placeholder="you@example.com"
                        disabled={isEmailLocked}
                        required 
                      />
                      {isEmailLocked && (
                        <Button 
                          type="button" 
                          variant="outline" 
                          size="sm"
                          onClick={handleEmailEdit}
                        >
                          <Edit3 className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground">로그인 및 알림용 이메일</p>
                    {isEmailVerified && (
                      <div className="flex items-center gap-2 text-green-600 text-sm">
                        <CheckCircle className="h-4 w-4" />
                        인증에 성공하셨습니다!
                      </div>
                    )}
                  </div>

                  {/* 이메일 인증 섹션 */}
                  {!isEmailVerified && (
                    <div className="space-y-3 p-4 border rounded-lg bg-muted/50">
                      <div className="flex items-center justify-between">
                        <Button 
                          type="button" 
                          variant="outline" 
                          onClick={handleEmailVerification}
                          disabled={isVerifying || !email || isEmailLocked}
                          className="flex-1"
                        >
                          {isVerifying ? '인증번호 전송 중...' : '인증번호 전송'}
                        </Button>
                        {isTimerActive && timeLeft > 0 && (
                          <div className="ml-3 text-sm text-muted-foreground">
                            {formatTime(timeLeft)}
                          </div>
                        )}
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="verificationCode">인증번호</Label>
                        <div className="flex gap-2">
                          <Input 
                            id="verificationCode"
                            value={verificationCode} 
                            onChange={(e) => setVerificationCode(e.target.value)} 
                            placeholder="인증번호를 입력하세요"
                          />
                          <Button 
                            type="button" 
                            onClick={handleVerifyCode}
                            disabled={isVerifying || !verificationCode}
                          >
                            인증하기
                          </Button>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* 이름 */}
                  <div className="space-y-2">
                    <Label htmlFor="name">이름</Label>
                    <Input 
                      id="name" 
                      value={name} 
                      onChange={(e) => setName(e.target.value)} 
                      placeholder="홍길동"
                      required 
                    />
                    <p className="text-xs text-muted-foreground">결과 화면 및 프로필에 표시</p>
                  </div>

                  {/* 비밀번호 */}
                  <div className="space-y-2">
                    <Label htmlFor="password">비밀번호</Label>
                    <Input 
                      id="password" 
                      type="password" 
                      value={password} 
                      onChange={(e) => setPassword(e.target.value)} 
                      placeholder="8자 이상, 특수문자 포함"
                      required 
                    />
                  </div>

                  {/* 비밀번호 확인 */}
                  <div className="space-y-2">
                    <Label htmlFor="passwordCheck">비밀번호 확인</Label>
                    <Input 
                      id="passwordCheck" 
                      type="password" 
                      value={passwordCheck} 
                      onChange={(e) => setPasswordCheck(e.target.value)} 
                      placeholder="다시 입력"
                      required 
                    />
                  </div>

                  {/* 사진 주소 */}
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

                  {/* 가입하기 버튼 */}
                  <Button type="submit" className="w-full" size="lg" disabled={loading || !isSignupEnabled}>
                    {loading ? '처리중...' : '가입하기'}
                  </Button>

                  {/* 구분선 */}
                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-background px-2 text-muted-foreground">또는</span>
                    </div>
                  </div>

                  {/* GitHub 가입 버튼 */}
                  <Button 
                    type="button" 
                    variant="outline" 
                    className="w-full" 
                    size="lg" 
                    onClick={handleGithubSignup}
                  >
                    <Github className="mr-2 h-4 w-4" />
                    GitHub로 계속하기
                  </Button>

                  {/* 법적 고지 */}
                  <p className="text-xs text-muted-foreground text-center">
                    가입하면 서비스 약관과 개인정보 처리방침에 동의하게 됩니다.
                  </p>
                </form>
              </CardContent>
            </Card>
          </div>

          {/* 오른쪽: 가입 혜택 */}
          <div className="space-y-8">
            <Card className="shadow-lg">
              <CardHeader>
                <CardTitle className="text-xl">가입하면 가능한 것들</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-4">
                  <li className="flex items-start gap-3">
                    <div className="w-2 h-2 bg-primary rounded-full mt-2 flex-shrink-0" />
                    <div>
                      <h3 className="font-medium">AI 포트폴리오 점수 및 조언 제공</h3>
                      <p className="text-sm text-muted-foreground">코드 품질, 문서화, 프로젝트 구조를 종합 분석하여 객관적 점수와 개선 제안을 받으세요.</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <div className="w-2 h-2 bg-primary rounded-full mt-2 flex-shrink-0" />
                    <div>
                      <h3 className="font-medium">분석 히스토리 저장 (전/후 비교)</h3>
                      <p className="text-sm text-muted-foreground">개선 전후의 점수 변화를 추적하고 성장 과정을 시각적으로 확인할 수 있습니다.</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <div className="w-2 h-2 bg-primary rounded-full mt-2 flex-shrink-0" />
                    <div>
                      <h3 className="font-medium">선택형 코드 2파일 심층 리뷰</h3>
                      <p className="text-sm text-muted-foreground">중요한 파일들을 선별하여 상세한 코드 리뷰와 개선 방안을 제공합니다.</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <div className="w-2 h-2 bg-primary rounded-full mt-2 flex-shrink-0" />
                    <div>
                      <h3 className="font-medium">PDF/Markdown 내보내기</h3>
                      <p className="text-sm text-muted-foreground">분석 결과를 다양한 형식으로 내보내어 포트폴리오나 문서에 활용하세요.</p>
                    </div>
                  </li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}