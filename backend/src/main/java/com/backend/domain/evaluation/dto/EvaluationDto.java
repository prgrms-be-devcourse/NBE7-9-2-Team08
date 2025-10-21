package com.backend.domain.evaluation.dto;

import java.util.List;

public class EvaluationDto {

    /** AI가 반환해야 할 점수 묶음 */
    public record Scores(
            int readme,   // 0~25
            int test,     // 0~25
            int commit,   // 0~25
            int cicd      // 0~25
    ) {}

    /** AI가 반환해야 할 전체 결과 */
    public record AiResult(
            String summary,
            List<String> strengths,
            List<String> improvements,
            Scores scores
    ) {}
}
