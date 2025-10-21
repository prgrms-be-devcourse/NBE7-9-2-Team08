package com.backend.domain.evaluation.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.analysis.repository.ScoreRepository;
import com.backend.domain.evaluation.dto.AiDto;
import com.backend.domain.evaluation.dto.EvaluationDto.AiResult;
import com.backend.domain.evaluation.dto.EvaluationDto.Scores;
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

    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ScoreRepository scoreRepository;

    @Transactional
    public Long evaluateAndSave(RepositoryData data) {
        AiResult ai = callAiAndParse(data);

        String url = data.getRepositoryUrl();
        Repositories repo = repositoryJpaRepository.findByHtmlUrl(url)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        AnalysisResult analysis = AnalysisResult.builder()
                .repositories(repo)
                .summary(safe(ai.summary()))
                .strengths(joinBullets(ai.strengths()))
                .improvements(joinBullets(ai.improvements()))
                .createDate(LocalDateTime.now())
                .build();

        // AnalysisResult 먼저 저장
        AnalysisResult saved = analysisResultRepository.save(analysis);

        Scores sc = ai.scores();
        Score score = Score.builder()
                .analysisResult(saved)              // 저장된 analysis에 연결
                .readmeScore(sc.readme())
                .testScore(sc.test())
                .commitScore(sc.commit())
                .cicdScore(sc.cicd())
                .build();

        scoreRepository.save(score);

        log.info("✅ Evaluation saved. analysisResultId={}", saved.getId());
        return saved.getId();
    }

    public AiResult callAiAndParse(RepositoryData data) {
        try {
            String content = objectMapper.writeValueAsString(data);
            String prompt = """
                    You are a senior software engineering reviewer.
                    Analyze the given GitHub repository data and return ONLY a valid JSON. No commentary.

                    Scoring: total 100 (README 0~30, TEST 0~30, COMMIT 0~25, CICD 0~15).
                    Consider test folders, CI configs (.github/workflows), commit frequency/messages, README depth, etc.

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
            return objectMapper.readValue(json, new TypeReference<AiResult>() {});
        } catch (Exception e) {
            log.error("AI evaluation failed", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String extractJson(String text) {
        if (text == null) throw new IllegalArgumentException("AI result is null");
        String cleaned = text.replaceAll("```json", "```").trim();
        Matcher m = Pattern.compile("\\{.*}", Pattern.DOTALL).matcher(cleaned);
        if (m.find()) return m.group();
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
