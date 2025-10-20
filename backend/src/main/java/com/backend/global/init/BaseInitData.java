package com.backend.global.init;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.analysis.repository.ScoreRepository;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.entity.RepositoryLanguage;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {

    private final UserRepository userRepository;
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ScoreRepository scoreRepository;

    @Bean
    @Transactional
    CommandLineRunner initPortfolioIQData() {
        return args -> {
            // 1. 기본 사용자 계정 생성
            createDefaultUsers();

            // 2. 테스트용 저장소 데이터 생성
            createTestRepositories();

            // 3. 분석 결과 및 점수 데이터 생성
            createAnalysisResults();

            System.out.println("✅ PortfolioIQ 초기 데이터 생성 완료");
        };
    }

    /**
     * 기본 테스트 사용자들을 생성합니다.
     * 중복 생성을 방지하기 위해 이미 사용자가 존재하는지 확인합니다.
     */
    private void createDefaultUsers() {
        if (userRepository.count() > 0) {
            System.out.println("사용자 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        System.out.println("테스트 사용자 계정 생성 시작...");

        // 테스트 사용자들 (실제 운영에서는 패스워드 암호화 필요)
        createUserIfNotExists("alice@example.com", "password1", "Alice");
        createUserIfNotExists("bob@example.com", "password2", "Bob");
        createUserIfNotExists("charlie@example.com", "password3", "Charlie");

        System.out.println("테스트 사용자 계정 생성 완료");
    }

    /**
     * 개별 사용자를 생성합니다.
     * @param email 사용자 이메일
     * @param password 비밀번호 (테스트용, 실제로는 암호화 필요)
     * @param name 사용자 이름
     */
    private void createUserIfNotExists(String email, String password, String name) {
        try {
            User user = new User(email, password, name);
            userRepository.save(user);
            System.out.println("사용자 계정 생성 완료: " + email);
        } catch (Exception e) {
            System.out.println("사용자 생성 실패: " + email + " - " + e.getMessage());
        }
    }

    /**
     * 다양한 기술 스택의 테스트 저장소들을 생성합니다.
     * 각 저장소는 실제 GitHub URL 형태를 따르며, 다양한 언어와 공개/비공개 설정을 가집니다.
     */
    private void createTestRepositories() {
        if (repositoryJpaRepository.count() > 0) {
            System.out.println("저장소 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        System.out.println("테스트 저장소 데이터 생성 시작...");

        // 사용자 조회 (User-Repository 연관관계 설정을 위해)
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("사용자가 존재하지 않아 저장소를 생성할 수 없습니다.");
            return;
        }

        // 다양한 기술 스택의 저장소들 생성 (User와 연결)
        createRepositoryWithLanguages("spring-boot-app",
                "Spring Boot 기반의 REST API 서버입니다. JWT 인증, JPA, MySQL을 사용하며 Docker로 컨테이너화되어 있습니다.",
                "https://github.com/alice/spring-boot-app",
                "main", true, List.of(Language.JAVA), users.get(0));

        createRepositoryWithLanguages("java-algorithms",
                "Java로 구현한 알고리즘 문제 풀이 모음입니다. 백준, 프로그래머스, LeetCode 문제들을 체계적으로 정리했습니다.",
                "https://github.com/alice/java-algorithms",
                "master", true, List.of(Language.JAVA), users.get(0));

        createRepositoryWithLanguages("python-data-tool",
                "데이터 분석용 Python 툴킷입니다. Pandas, NumPy, Matplotlib을 활용한 데이터 시각화 기능을 제공합니다.",
                "https://github.com/bob/python-data-tool",
                "main", false, List.of(Language.PYTHON),
                users.size() > 1 ? users.get(1) : users.get(0));

        createRepositoryWithLanguages("frontend-portfolio",
                "React 기반 개인 포트폴리오 사이트입니다. TypeScript, Tailwind CSS, Framer Motion을 사용했습니다.",
                "https://github.com/charlie/frontend-portfolio",
                "main", true, List.of(Language.JAVASCRIPT),
                users.size() > 2 ? users.get(2) : users.get(0));

        createRepositoryWithLanguages("fullstack-analyzer",
                "백엔드와 프론트엔드를 함께 분석하는 풀스택 프로젝트입니다. Spring Boot + React 조합으로 구성되어 있습니다.",
                "https://github.com/alice/fullstack-analyzer",
                "main", true, List.of(Language.JAVA, Language.JAVASCRIPT, Language.PYTHON), users.get(0));

        createRepositoryWithLanguages("react-native-app",
                "모바일 앱 개발 프로젝트입니다. React Native와 TypeScript를 사용하여 크로스 플랫폼 앱을 개발했습니다.",
                "https://github.com/bob/react-native-app",
                "develop", true, List.of(Language.JAVASCRIPT, Language.TYPESCRIPT),
                users.size() > 1 ? users.get(1) : users.get(0));

        createRepositoryWithLanguages("vue-dashboard",
                "Vue.js 기반 관리자 대시보드입니다. Vuex, Vue Router, Chart.js를 활용한 데이터 시각화 대시보드입니다.",
                "https://github.com/charlie/vue-dashboard",
                "main", false, List.of(Language.JAVASCRIPT),
                users.size() > 2 ? users.get(2) : users.get(0));

        System.out.println("테스트 저장소 데이터 생성 완료");
    }

    /**
     * 개별 저장소와 해당 언어들을 생성합니다.
     * @param name 저장소 이름
     * @param description 저장소 설명
     * @param htmlUrl GitHub URL
     * @param mainBranch 메인 브랜치명
     * @param isPublic 공개 여부
     * @param languages 사용 언어 목록
     * @param user 저장소 소유자
     */
    private void createRepositoryWithLanguages(String name, String description, String htmlUrl,
                                               String mainBranch, boolean isPublic,
                                               List<Language> languages, User user) {

        // Repository 생성 (User 연관관계 포함 - 빌더에 user 파라미터 사용)
        Repositories repository = Repositories.builder()
                .user(user)  // 중요: User 연관관계 설정
                .name(name)
                .description(description)
                .htmlUrl(htmlUrl)
                .publicRepository(isPublic)
                .mainBranch(mainBranch)
                .build();

        // 언어 설정
        if (languages != null && !languages.isEmpty()) {
            for (Language language : languages) {
                RepositoryLanguage repoLanguage = RepositoryLanguage.builder()
                        .language(language)
                        .repositories(repository)
                        .build();
                repository.addLanguage(repoLanguage);
            }
        }

        repositoryJpaRepository.save(repository);
        System.out.println("저장소 생성 완료: " + name + " (소유자: " + user.getName() + ", 언어: " + languages + ")");
    }

    /**
     * 생성된 저장소들에 대한 분석 결과와 점수 데이터를 생성합니다.
     * 실제 AI 분석 없이 테스트용 데이터를 생성하여 화면 테스트가 가능하도록 합니다.
     */
    private void createAnalysisResults() {
        if (analysisResultRepository.count() > 0) {
            System.out.println("분석 결과 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        System.out.println("분석 결과 및 점수 데이터 생성 시작...");

        List<Repositories> repositories = repositoryJpaRepository.findAll();

        for (int i = 0; i < repositories.size(); i++) {
            Repositories repo = repositories.get(i);

            // 각 Repository당 2~3개의 분석 결과 생성 (버전 관리 테스트용)
            int analysisCount = (i % 2 == 0) ? 3 : 2; // 짝수 인덱스: 3개, 홀수: 2개

            for (int versionIndex = 0; versionIndex < analysisCount; versionIndex++) {
                // 분석 날짜를 과거로 설정 (최신 분석일수록 최근 날짜)
                LocalDateTime analysisDate = LocalDateTime.now()
                        .minusDays((analysisCount - versionIndex - 1) * 7L); // 7일 간격

                // 분석 결과 생성 (버전별로 점수가 점진적으로 개선되는 시나리오)
                AnalysisResult analysisResult = AnalysisResult.builder()
                        .repositories(repo)
                        .summary(getAnalysisSummary(i, versionIndex))
                        .strengths(getStrengths(i, versionIndex))
                        .improvements(getImprovements(i, versionIndex))
                        .createDate(analysisDate)
                        .build();

                // 분석 결과 저장
                AnalysisResult savedAnalysisResult = analysisResultRepository.save(analysisResult);

                // 점수 생성 (버전이 올라갈수록 점수 향상)
                Score score = Score.builder()
                        .analysisResult(savedAnalysisResult)
                        .readmeScore(getReadmeScore(i, versionIndex))
                        .testScore(getTestScore(i, versionIndex))
                        .commitScore(getCommitScore(i, versionIndex))
                        .cicdScore(getCicdScore(i, versionIndex))
                        .build();

                scoreRepository.save(score);

                System.out.println(String.format("분석 결과 생성 완료: %s (v%d, 날짜: %s)",
                        repo.getName(), versionIndex + 1, analysisDate.toLocalDate()));
            }
        }

        System.out.println("분석 결과 및 점수 데이터 생성 완료");
    }

    // ===== 테스트 데이터 생성을 위한 헬퍼 메서드들 =====

    private String getAnalysisSummary(int repoIndex, int versionIndex) {
        String[][] summaries = {
                // spring-boot-app
                {
                        "Spring Boot 프로젝트로 기본 구조는 양호하나 테스트 코드와 문서화가 부족합니다.",
                        "테스트 코드가 추가되었으며 REST API 설계가 개선되었습니다. 문서화 작업이 진행 중입니다.",
                        "전체적으로 코드 품질이 우수하며 Spring Boot 베스트 프랙티스를 잘 따르고 있습니다. 테스트 코드와 문서화가 체계적으로 관리되고 있어 유지보수성이 뛰어납니다."
                },
                // java-algorithms
                {
                        "알고리즘 구현은 정확하나 주석과 설명이 부족합니다.",
                        "코드 가독성이 개선되었고 복잡도 분석 주석이 추가되었습니다.",
                        "클린 코드 원칙이 잘 지켜지고 있으며 알고리즘 구현이 효율적입니다. 복잡도 분석과 최적화에 대한 고민이 잘 드러나는 코드입니다."
                },
                // python-data-tool
                {
                        "데이터 처리 기능은 작동하나 에러 핸들링과 테스트가 미흡합니다.",
                        "예외 처리가 강화되었고 기본적인 테스트 코드가 추가되었습니다.",
                        "데이터 처리 로직이 견고하며 다양한 Python 라이브러리를 효과적으로 활용하고 있습니다. 데이터 분석 파이프라인이 체계적으로 구성되어 있습니다."
                },
                // frontend-portfolio
                {
                        "UI 구성은 완성되었으나 성능 최적화와 접근성 개선이 필요합니다.",
                        "번들 크기가 최적화되었고 반응형 디자인이 개선되었습니다.",
                        "UI/UX 구조가 명확하고 컴포넌트 기반 개발이 잘 되어 있습니다. 반응형 디자인과 사용자 경험을 고려한 인터페이스 설계가 돋보입니다."
                },
                // fullstack-analyzer
                {
                        "풀스택 구조는 갖춰져 있으나 API 문서화와 테스트 통합이 부족합니다.",
                        "API 문서가 추가되었고 통합 테스트가 구축되었습니다.",
                        "풀스택 아키텍처가 명확하고 기술 스택 통합이 효과적입니다. 프론트엔드와 백엔드 간의 API 설계가 RESTful 원칙을 잘 따르고 있습니다."
                },
                // react-native-app
                {
                        "앱 기본 기능은 구현되었으나 성능 최적화가 필요합니다.",
                        "메모리 관리가 개선되었고 TypeScript 타입 안정성이 강화되었습니다.",
                        "모바일 앱 구조가 체계적이고 네이티브 기능 활용이 좋습니다. 크로스 플랫폼 개발의 장점을 잘 살린 구조입니다."
                },
                // vue-dashboard
                {
                        "대시보드 기본 구성은 완료되었으나 보안과 실시간 데이터 처리가 미흡합니다.",
                        "보안 취약점이 패치되었고 데이터 업데이트 로직이 개선되었습니다.",
                        "Vue.js 생태계를 효과적으로 활용한 대시보드 구현입니다. 상태 관리와 컴포넌트 재사용성이 우수하며 데이터 시각화가 직관적입니다."
                }
        };
        return summaries[repoIndex % summaries.length][Math.min(versionIndex, 2)];
    }

    /**
     * 저장소별, 버전별 강점을 반환합니다.
     */
    private String getStrengths(int repoIndex, int versionIndex) {
        String[][] strengths = {
                {
                        "Spring Boot 기본 구조가 잘 잡혀 있습니다.",
                        "REST API 엔드포인트 설계가 개선되었고 예외 처리가 추가되었습니다.",
                        "테스트 코드와 문서화가 잘 되어 있고, REST API 설계가 명확합니다. Spring Security를 활용한 인증/인가 구조가 안전하고 확장 가능합니다."
                },
                {
                        "알고리즘 로직이 정확하게 구현되어 있습니다.",
                        "시간복잡도 분석이 추가되었고 코드 가독성이 향상되었습니다.",
                        "효율적인 알고리즘 사용과 코드 가독성이 우수합니다. 시간복잡도와 공간복잡도를 고려한 최적화가 잘 되어 있습니다."
                },
                {
                        "Pandas를 활용한 데이터 처리가 잘 구현되어 있습니다.",
                        "데이터 시각화 기능이 추가되었고 파이프라인이 체계화되었습니다.",
                        "데이터 분석 파이프라인이 체계적이고 시각화가 우수합니다. 대용량 데이터 처리를 위한 최적화 기법이 적용되어 있습니다."
                },
                {
                        "React 컴포넌트 구조가 잘 설계되어 있습니다.",
                        "컴포넌트 재사용성이 개선되었고 상태 관리가 효율적으로 변경되었습니다.",
                        "반응형 디자인과 컴포넌트 재사용성이 뛰어납니다. 사용자 인터페이스가 직관적이고 접근성을 고려한 설계입니다."
                },
                {
                        "프론트엔드와 백엔드 통신 구조가 명확합니다.",
                        "API 명세가 문서화되었고 에러 핸들링이 통합되었습니다.",
                        "모듈 간 의존성 분리가 우수하고 테스트 커버리지가 넓습니다. 마이크로서비스 아키텍처를 고려한 확장 가능한 구조입니다."
                },
                {
                        "크로스 플랫폼 개발 구조가 잘 갖춰져 있습니다.",
                        "네이티브 모듈 연동이 개선되었고 상태 관리가 효율화되었습니다.",
                        "TypeScript 사용으로 타입 안전성이 확보되고 상태 관리가 효율적입니다. 네이티브 기능과의 연동이 원활합니다."
                },
                {
                        "Vue.js 컴포넌트 구성이 체계적입니다.",
                        "차트 라이브러리 연동이 최적화되었고 데이터 바인딩이 개선되었습니다.",
                        "데이터 바인딩과 차트 라이브러리 연동이 우수합니다. 실시간 데이터 업데이트와 사용자 인터랙션이 자연스럽습니다."
                }
        };
        return strengths[repoIndex % strengths.length][Math.min(versionIndex, 2)];
    }

    /**
     * 저장소별, 버전별 개선점을 반환합니다.
     */
    private String getImprovements(int repoIndex, int versionIndex) {
        String[][] improvements = {
                {
                        "테스트 코드 추가가 시급하며 API 문서화가 필요합니다. CI/CD 파이프라인 구축을 권장합니다.",
                        "통합 테스트 추가와 모니터링 시스템 도입이 필요합니다.",
                        "CI/CD 자동화 구축과 API 문서 자동화가 필요합니다. 모니터링 및 로깅 시스템 도입으로 운영 안정성을 향상시킬 수 있습니다."
                },
                {
                        "주석 추가와 테스트 케이스 작성이 필요합니다.",
                        "성능 벤치마크 테스트 추가가 권장됩니다.",
                        "주석 보완과 테스트 케이스 추가가 필요합니다. 코드 복잡도 분석과 성능 벤치마크 테스트 추가를 권장합니다."
                },
                {
                        "단위 테스트 추가와 에러 핸들링 강화가 시급합니다.",
                        "통합 테스트와 데이터 검증 로직 보강이 필요합니다.",
                        "테스트 커버리지 향상과 에러 핸들링 강화가 필요합니다. 데이터 파이프라인의 예외 상황 처리를 보강하면 좋겠습니다."
                },
                {
                        "번들 크기 최적화와 Lighthouse 점수 개선이 필요합니다.",
                        "이미지 lazy loading과 코드 스플리팅 적용이 권장됩니다.",
                        "성능 최적화와 접근성 개선이 필요합니다. 번들 크기 최적화와 SEO 개선을 통해 사용자 경험을 향상시킬 수 있습니다."
                },
                {
                        "E2E 테스트 구축과 에러 로깅 시스템 도입이 필요합니다.",
                        "데이터베이스 쿼리 최적화와 캐싱 전략 수립이 권장됩니다.",
                        "빌드 속도 최적화와 캐싱 전략 개선이 필요합니다. 서비스 간 통신 최적화와 데이터베이스 쿼리 성능 튜닝을 권장합니다."
                },
                {
                        "앱 번들 크기 줄이기와 메모리 누수 해결이 시급합니다.",
                        "배터리 사용량 최적화와 오프라인 모드 구현이 필요합니다.",
                        "앱 번들 크기 최적화와 오프라인 기능 추가가 필요합니다. 배터리 사용량 최적화와 메모리 관리 개선이 필요합니다."
                },
                {
                        "보안 취약점 패치와 XSS 방어 강화가 필요합니다.",
                        "대용량 데이터 처리 최적화가 권장됩니다.",
                        "실시간 데이터 처리와 보안 강화가 필요합니다. 대용량 데이터 처리를 위한 가상화 기법 도입을 고려해보세요."
                }
        };
        return improvements[repoIndex % improvements.length][Math.min(versionIndex, 2)];
    }

    /**
     * README 문서 품질 점수 (30점 만점)
     * 버전이 올라갈수록 점수 향상
     */
    private int getReadmeScore(int repoIndex, int versionIndex) {
        int[][] scores = {
                {18, 23, 25},  // spring-boot-app
                {22, 27, 30},  // java-algorithms
                {15, 18, 20},  // python-data-tool
                {20, 23, 25},  // frontend-portfolio
                {23, 26, 28},  // fullstack-analyzer
                {17, 20, 22},  // react-native-app
                {21, 24, 26}   // vue-dashboard
        };
        return scores[repoIndex % scores.length][Math.min(versionIndex, 2)];
    }

    /**
     * 테스트 구성 점수 (30점 만점)
     */
    private int getTestScore(int repoIndex, int versionIndex) {
        int[][] scores = {
                {12, 18, 20},
                {18, 22, 25},
                {8, 12, 15},
                {20, 23, 25},
                {22, 25, 27},
                {19, 22, 24},
                {13, 16, 18}
        };
        return scores[repoIndex % scores.length][Math.min(versionIndex, 2)];
    }

    /**
     * 커밋 이력 품질 점수 (25점 만점)
     */
    private int getCommitScore(int repoIndex, int versionIndex) {
        int[][] scores = {
                {10, 13, 15},
                {20, 23, 25},
                {7, 9, 10},
                {15, 18, 20},
                {18, 21, 23},
                {16, 18, 20},
                {12, 14, 16}
        };
        return scores[repoIndex % scores.length][Math.min(versionIndex, 2)];
    }

    /**
     * CI/CD 구성 점수 (15점 만점)
     */
    private int getCicdScore(int repoIndex, int versionIndex) {
        int[][] scores = {
                {5, 8, 10},
                {7, 9, 10},
                {3, 4, 5},
                {12, 14, 15},
                {9, 11, 12},
                {5, 7, 8},
                {8, 10, 11}
        };
        return scores[repoIndex % scores.length][Math.min(versionIndex, 2)];
    }
}