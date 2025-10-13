# Next.js × Spring Boot Boilerplate (App Router, TS)

프론트(Next.js 15)와 스프링 백엔드(예: http://localhost:8080) 협업을 위한 최소 템플릿입니다.

## 특징
- App Router (src/app) + TypeScript
- React Query로 API 캐싱/에러/로딩 상태 관리
- `lib/api.ts`에서 공통 fetch 클라이언트 제공 (쿠키/토큰 둘 다 대응)
- Next `rewrites`로 `/api/*` 요청을 스프링으로 프록시 (개발용)
- 예시 페이지
  - 로그인 (POST /api/v1/auth/login)
  - 사용자 목록 (GET /api/v1/users?page=0&size=10)

## 빠른 시작
```bash
pnpm i       # or npm i / yarn
pnpm dev     # http://localhost:3000
```

## 환경 변수
`.env.local` 파일을 만들고 아래를 채우세요.
```
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
NEXT_PUBLIC_DEV_PROXY=true
NEXT_PUBLIC_AUTH_HEADER=Authorization
```

- 개발 중에는 Next가 `/api/*`를 `NEXT_PUBLIC_BACKEND_URL`로 프록시합니다.
- 운영 배포 시에는 프록시를 끄고(환경변수 false), `lib/api.ts`의 절대 URL을 사용하세요.

## 인증
- 쿠키 세션: `credentials: "include"` 로 전송됨. 백엔드에서 CORS에 `allowCredentials`와 오리진 허용 필요.
- 토큰: `localStorage`에 `accessToken` 저장 → `Authorization: Bearer <token>` 헤더 자동 부착.

## 스프링 CORS 예시
```java
@Bean
public WebMvcConfigurer corsConfigurer() {
  return new WebMvcConfigurer() {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
        .allowedOrigins("http://localhost:3000")
        .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
        .allowCredentials(true)
        .allowedHeaders("*")
        .exposedHeaders("Authorization");
    }
  };
}
```

## 폴더 구조
```
src/
  app/
    (auth)/login/page.tsx
    users/page.tsx
    layout.tsx
    page.tsx
    providers.tsx
  components/
    Form.tsx
    Header.tsx
    ui/Toast.tsx
  hooks/
    useAuth.ts
  lib/
    api.ts
    types.ts
  styles/
    globals.css
```

원하는 엔드포인트만 교체해서 쓰면 됩니다.
