package com.backend.domain.evaluation.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.evaluation.dto.EvaluationDto;
import com.backend.domain.evaluation.dto.EvaluationDto.AiResult;
import com.backend.domain.evaluation.dto.EvaluationDto.Scores;
import com.backend.domain.evaluation.service.AiService;
import com.backend.domain.evaluation.dto.AiDto; // 이미 존재하는 DTO(complete 요청용)
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final AiService aiService;                                  // OpenAI 호출 래퍼(이미 프로젝트에 있음)
    private final ObjectMapper objectMapper = new ObjectMapper();       // JSON 파싱용
    private final RepositoryJpaRepository repositoryJpaRepository;      // Repo 엔티티 조회
    private final AnalysisResultRepository analysisResultRepository;    // 결과 저장

    /**
     * 평가 + 저장까지 한 번에 처리합니다.
     * @param data (RepositoryData) 수집된 깃허브 종합 데이터
     * @return 저장된 분석결과 ID(Long)
     */
    @Transactional
    public Long evaluateAndSave(RepositoryData data) {
        // 1) AI 호출
        AiResult ai = callAiAndParse(data);

        // 2) Repositories 엔티티 찾기 (URL로 조회)
        String url = data.getRepositoryUrl();
        Repositories repo = repositoryJpaRepository.findByHtmlUrl(url)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        // 3) 엔티티 생성 (AnalysisResult + Score)
        AnalysisResult analysis = AnalysisResult.builder()
                .repositories(repo)
                .summary(safe(ai.summary()))
                .strengths(joinBullets(ai.strengths()))
                .improvements(joinBullets(ai.improvements()))
                .createDate(LocalDateTime.now())
                .build();

        Score score = Score.builder()
                .analysisResult(analysis)                     // ★ Score가 연관관계의 주인일 가능성이 높음
                .readmeScore(ai.scores().readme())
                .testScore(ai.scores().test())
                .commitScore(ai.scores().commit())
                .cicdScore(ai.scores().cicd())
                .build();

        // AnalysisResult ←→ Score 양쪽 세팅이 필요하다면,
        // AnalysisResult에 세터가 없을 수 있어도 JPA가 연관 찾아옵니다(지연 로딩).
        // 만약 세터가 있다면 아래 한 줄도 함께 두세요.
        // analysis.setScore(score);

        // 4) 저장 (cascade = ALL 이라면 Score도 함께 저장됩니다)
        AnalysisResult saved = analysisResultRepository.save(analysis);
        log.info("✅ Evaluation saved. analysisResultId={}", saved.getId());
        return saved.getId();
    }

    /**
     * 실제 AI 호출부
     * @param data (RepositoryData)
     * @return (EvaluationDto.AiResult) AI 결과
     */
    public AiResult callAiAndParse(RepositoryData data) {
        try {
            // content: 원본 데이터(JSON으로 넘기면 모델이 읽기 쉽습니다)
            String content = objectMapper.writeValueAsString(data);

            // prompt: 모델에게 "이 스키마로만 JSON"을 만들라고 강하게 지시
            String prompt = """
                    You are a senior software engineering reviewer.
                    Analyze the given GitHub repository data and return ONLY a valid JSON. No commentary.

                    Scoring: total 100 (README 0~25, TEST 0~25, COMMIT 0~25, CICD 0~25).
                    Consider test folders, CI configs(e.g., .github/workflows), commit frequency/messages, README depth, etc.

                    JSON schema:
                    {
                      "summary": "one-paragraph summary in Korean",
                      "strengths": ["...","..."],
                      "improvements": ["...","..."],
                      "scores": { "readme": 0, "test": 0, "commit": 0, "cicd": 0 }
                    }
                    """;

            AiDto.CompleteResponse res =
                    aiService.complete(new AiDto.CompleteRequest(content, prompt));

            String raw = res.result();
            String json = extractJson(raw);
            // {"summary":...,"strengths":[...],...} → AiResult로 역직렬화
            return objectMapper.readValue(json, new TypeReference<AiResult>() {});
        } catch (Exception e) {
            log.error("AI evaluation failed", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /** 모델이 앞/뒤에 텍스트를 붙여도 JSON 본문만 뽑아냅니다. */
    private String extractJson(String text) {
        if (text == null) throw new IllegalArgumentException("AI result is null");
        // ```json ... ``` 같은 포맷도 대비
        String cleaned = text.replaceAll("```json", "```").trim();

        // 가장 바깥 { ... } 블록을 정규식으로 추출
        Pattern p = Pattern.compile("\\{.*}", Pattern.DOTALL);
        Matcher m = p.matcher(cleaned);
        if (m.find()) return m.group();

        // 못 찾으면 그대로 시도(모델이 운좋게 순수 JSON만 준 경우)
        return cleaned;
    }

    private String joinBullets(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> "- " + s)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
