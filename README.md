# PortfolioIQ - GitHub Repository Quality Analysis Service 

## 📋 프로젝트 개요
### PortfolioIQ = Portfolio + IQ (Insight & Quality)

> **“당신의 포트폴리오에 인사이트와 품질을 더하다."**

**PortfolioIQ**는 GitHub 저장소의 품질을 AI 기반으로 자동 분석하고 평가하는 웹 서비스입니다. <br />
개발자들이 포트폴리오로 사용하는 저장소의 README 품질, 테스트 구성, 커밋 이력, CI/CD 설정 등을 종합적으로 분석하여 객관적인 점수와 구체적인 개선 방향을 제시합니다.

### 🎯 프로젝트 목표
취업 및 이직을 준비하는 개발자들은 포트폴리오로 GitHub 저장소를 제출하지만, 객관적인 피드백을 즉시 받기 어렵습니다. 멘토나 현직자에게 리포지토리 검토를 요청하기에는 시간과 비용이 많이 들고, 스스로 점검하기에는 무엇을 개선해야 할지 판단하기 어렵습니다.

본 프로젝트는 사용자가 GitHub 저장소 URL만 입력하면 AI가 README 품질, 코드 구조, 커밋 패턴, 문서화 수준 등을 종합 분석하여 점수화하고, "README에 설치 방법이 없습니다", "테스트 코드를 추가해 보세요"와 같은 구체적이고 실질적인 개선 방향을 제시하는 서비스를 제공하고자 합니다. 이를 통해 개발자들이 포트폴리오로 사용하는 리포지토리의 품질을 스스로 개선하고, 취업 경쟁력을 높일 수 있도록 돕는 것이 목표입니다.
- **즉각적인 피드백 제공**: GitHub 저장소 URL 입력만으로 실시간 품질 분석
- **객관적인 평가 기준**: AI 기반 정량적 점수(0-100점)와 정성적 피드백 제공
- **구체적인 개선 방향**: "README에 설치 방법 추가", "테스트 코드 작성" 등 실질적인 가이드 제시
- **커뮤니티**: 분석 결과 오픈소스화를 통한 집단 지성 기반 포트폴리오 품질 향상

  
### 👥 타겟 사용자

- 취업/이직 준비 중인 주니어·시니어 개발자
- 포트폴리오 개선이 필요한 개발자
- 객관적인 리뷰를 받기 어려운 독학 개발자

### 📅 개발 기간 & 팀 구성

- **개발 기간**: 2025-10-10 ~ 2025-10-27 (3주)
- **팀 구성**: 4인 (Full-stack)
  - 임병수: User 도메인 (회원가입, 로그인, 인증/인가)
  - 우성현: Evaluation 도메인 (OpenAI API 연동)
  - 오혜승: Repository 도메인 (GitHub API 연동)
  - 양희원: Community 도메인 (게시판, 댓글)

---

## 🏗️ 기술 스택

### Backend
```
- Java 21 (LTS)
- Spring Boot 3.5.6
- Spring Security 6.x (JWT 인증)
- Spring Data JPA (Hibernate 6.x)
- MySQL 8.0+ / H2 (테스트)
- Redis
```

### Frontend
```
- Next.js 15.5.3 (App Router)
- React 19.1.0
- TypeScript 5.9.2
- Tailwind CSS 4.1.13
- Context API + useReducer
```

### External APIs
```
- GitHub REST API (저장소 메타데이터 수집)
- OpenAI API (gpt-4o-mini, 품질 평가)
```

### Communication
```
- SSE (Server-Sent Events) - 실시간 분석 진행 상황 전달
- JavaMailSender (SMTP) - 이메일 인증
```

### DevOps & Tools
```
- Build: Gradle (Kotlin DSL)
- API Docs: Springdoc OpenAPI 2.7.0 (Swagger UI)
- Version Control: Git/GitHub (GitHub Flow)
- Testing: Postman, JUnit 5
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

`.env` 파일 생성:
```properties
# Database
DB_URL=jdbc:mysql://localhost:3306/DATABASE_NAME?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=YOUR_USERNAME
DB_PASSWORD=YOUR_PASSWORD

# GitHub API
GITHUB_TOKEN=발급받은 토큰 값

# 추후 내용 추가 예정

```

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
http://localhost:8080/swagger-ui.html
```

---

## 📝 협업 규칙

### Git 브랜치 전략 (GitHub Flow)

```
main (배포용, 직접 Push 금지, 병합 후 브랜치 삭)
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
| 임병수 | User Domain | [@LimByeongSu](https://github.com/LimByeongSu) |
| 우성현 | Evaluation Domain | [@samuel426](https://github.com/samuel426) |
| 오혜승 | Repository Domain | [@Hyeseung-OH](https://github.com/Hyeseung-OH) |
| 양희원 | Community Domain | [@Plectranthus](https://github.com/Plectranthus) |

---

## 📄 라이선스

이 프로젝트는 교육 목적으로 개발되었습니다.

---

## ❓ 문의사항
프로젝트에 대한 문의사항이나 버그 리포트는 [Issues](https://github.com/prgrms-be-devcourse/NBE7-9-1-Team04/issues)에 등록해 주세요. <br />
개발 기간 중 README.md 내용은 수정될 수 있습니다.
