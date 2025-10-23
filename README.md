# PortfolioIQ

## 📋 프로젝트 개요
### PortfolioIQ = Portfolio + IQ (Insight & Quality)

> **“당신의 포트폴리오에 인사이트와 품질을 더하다."**
> GitHub Repository Quality Analysis Service 

**PortfolioIQ**는 GitHub 저장소의 품질을 AI 기반으로 자동 분석하고 평가하는 웹 서비스입니다. <br />
개발자들이 포트폴리오로 사용하는 저장소의 README 품질, 테스트 구성, 커밋 이력, CI/CD 설정 등을 종합적으로 분석하여 객관적인 점수와 구체적인 개선 방향을 제시합니다.

### 🎯 프로젝트 목표

- **즉각적인 피드백 제공**: GitHub 저장소 URL 입력만으로 실시간 품질 분석
- **객관적인 평가 기준**: AI 기반 정량적 점수(0-100점)와 정성적 피드백 제공
- **구체적인 개선 방향**: "README에 설치 방법 추가", "테스트 코드 작성" 등 실질적인 가이드 제시
- **커뮤니티**: 오픈 커뮤니티 기반으로 우수 사례를 공유할 수 있는 플랫폼 구축

  
### 👥 타겟 사용자

- 취업/이직 준비 중인 주니어·시니어 개발자
- 포트폴리오 개선이 필요한 개발자
- 객관적인 리뷰를 받기 어려운 독학 개발자

### 📅 개발 기간 & 팀 구성

- **개발 기간**: 2025-10-10 ~ 2025-10-27 (3주)
- **팀 구성**: 4인 (Full-stack)
  - 임병수: User 도메인 (회원가입, 로그인, 인증/인가)
  - 우성현: Evaluation 도메인 (OpenAI API 연동)
  - 오혜승: Analysis, Repository 도메인 (GitHub API 연동)
  - 양희원: Analysis, Community 도메인 (게시판, 댓글)

### 📊 데이터 흐름

---

## 🏗️ 기술 스택

### Backend
```
- Java 21 (LTS)
- Spring Boot 3.5.6
- Spring Security 6.x + JWT (jjwt 0.13.0)
- Spring Data JPA (Hibernate)
- Spring WebFlux (비동기 HTTP 클라이언트)
- MySQL 8.0+ / H2 (테스트)
- Redis (이메일 인증 코드 캐싱)
- Spring Validation (입력값 검증)
```

### Frontend
```
- Next.js 15.0.0 (App Router)
- React 18.3.1
- TypeScript 5.6.2
- Tailwind CSS 4.1.15
- TanStack React Query 5.56.2 (서버 상태 관리)
- Zod 3.23.8 (스키마 검증)
- Framer Motion 12.23.24 (애니메이션)
- Recharts 3.3.0 (차트 시각화)
```

### External APIs
```
- GitHub REST API (저장소 메타데이터 수집)
- OpenAI API (gpt-5-nano, 품질 평가)
```

### Communication
```
- SSE (Server-Sent Events) - 실시간 분석 진행 상황 전달
  - Backend: Spring WebFlux SseEmitter
  - Frontend: @microsoft/fetch-event-source 2.0.1
- JavaMailSender (SMTP) - 이메일 인증
```

### DevOps & Tools
```
- Build: Gradle 8.x (Kotlin DSL)
- Containerization: Docker
- API Docs: Springdoc OpenAPI 2.7.0 (Swagger UI)
- Version Control: Git/GitHub (GitHub Flow)
- Testing: JUnit 5, Mockito, Spring Security Test
- IDE: IntelliJ IDEA, Cursor
```

---

## 🚀 시작하기

### 사전 요구사항

```bash
- Java 21 이상
- Node.js 20.x LTS
- MySQL 8.0+
- Redis
```

### 환경 변수 설정

#### 1. GitHub Personal Access Token 발급
1. GitHub 로그인 후 Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token 클릭
3. 필요한 권한 체크: `repo` (전체), `read:org`
4. 생성된 토큰을 `GITHUB_TOKEN`에 설정

#### 2. OpenAI API Key 발급
1. [OpenAI Platform](https://platform.openai.com/) 회원가입/로그인
2. API keys 메뉴에서 Create new secret key
3. 생성된 키를 `OPENAI_API_KEY`에 설정

#### 3. Google 앱 비밀번호 발급
1. Google 계정 2단계 인증 활성화 필수
2. [앱 비밀번호 생성](https://myaccount.google.com/apppasswords)
3. 생성된 16자리 비밀번호를 `MAIL_PASSWORD`에 설정

#### 4. JWT Secret Key 생성
```bash
# 안전한 랜덤 키 생성 (최소 32자 이상 권장)
openssl rand -base64 32
```

#### 5. `.env` 파일 생성 (프로젝트 루트에 생성)
```properties
# Database
DB_URL=jdbc:mysql://localhost:3306/portfolioiq?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=root
DB_PASSWORD=yourpassword

# GitHub API
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# OpenAI API
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Google SMTP
MAIL_ID=your-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx

# JWT
SECRET_KEY=생성된_32자_이상의_랜덤_키
```

**⚠️ 보안 주의사항**
- `.env` 파일은 절대 Git에 커밋하지 마세요 (`.gitignore`에 포함 필수)
- 각자 본인의 API 키를 발급받아 사용하세요
- 프로덕션 환경에서는 환경 변수로 관리하세요


### 실행 방법

#### Backend 실행
```bash
cd backend
./gradlew bootRun
```

#### Frontend 실행
```bash
cd frontend
npm install
npm run dev
```

### API 문서 접근
```
http://localhost:8080/swagger-ui/index.html
```

---

## 📝 협업 규칙

### Git 브랜치 전략 (GitHub Flow)

```
main (배포용, 직접 Push 금지, 병합 후 브랜치 삭제 권장)
    ├── feature/user-login
    ├── feature/github-api-integration
    ├── feature/analysis-orchestration
    └── feature/community-board
```

### 커밋 컨벤션

```
<타입>(<도메인>): <제목>
```

#### 타입 종류
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `chore`: 빌드 설정, 패키지 관리
- `docs`: 문서 수정
- `test`: 테스트 코드
- `init`: 프로젝트 초기 설정
- `style`: 코드 스타일 변경

#### 예시
```
feat(analysis): GitHub 저장소 메타데이터 수집 API 구현
```

### 코드 리뷰 프로세스

1. **PR 생성**: feature 브랜치 → dev 브랜치
2. **리뷰 요청**: 최소 1명 이상의 팀원
3. **리뷰 체크리스트**
   - 기능이 의도대로 동작하는가?
   - 코딩 컨벤션을 준수하는가?
   - 보안 취약점이 없는가?
   - 예외 처리가 적절한가?
4. **승인 후 병합**: 매일 오전 병합 시간에 통합

---

## 📚 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [GitHub REST API 문서](https://docs.github.com/en/rest)
- [OpenAI API 문서](https://platform.openai.com/docs/api-reference)

---

## 👤 개발자

| 이름 | 역할 | GitHub |
|------|------|--------|
| 임병수 | [BE] User Domain | [@LimByeongSu](https://github.com/LimByeongSu) |
| 우성현 | [FULL] Evaluation Domain | [@samuel426](https://github.com/samuel426) |
| 오혜승 | [FULL] Analysis, Repository Domain | [@Hyeseung-OH](https://github.com/Hyeseung-OH) |
| 양희원 | [FULL] Analysis, Community Domain | [@Plectranthus](https://github.com/Plectranthus) |

---

## 📄 라이선스
본 프로젝트는 교육 목적으로 개발되었으며, 다음 오픈소스 라이브러리들을 사용합니다:

#### Backend
- Spring Boot Framework (Apache 2.0)
- Spring Security (Apache 2.0)
- MySQL Connector/J (GPL 2.0 with FOSS Exception)
- OpenAI Java SDK (MIT)
- jjwt (Apache 2.0)

#### Frontend
- Next.js (MIT)
- React (MIT)
- TanStack React Query (MIT)
- Tailwind CSS (MIT)

### 외부 API 이용 약관
- **GitHub API**: [GitHub Terms of Service](https://docs.github.com/en/site-policy/github-terms/github-terms-of-service) 준수
- **OpenAI API**: [OpenAI Terms of Use](https://openai.com/policies/terms-of-use) 준수

---

## ❓ 문의사항
프로젝트에 대한 문의사항이나 버그 리포트는 [Issues](https://github.com/prgrms-be-devcourse/NBE7-9-1-Team04/issues)에 등록해 주세요. <br />
개발 기간 중 README.md 내용은 수정될 수 있습니다.
