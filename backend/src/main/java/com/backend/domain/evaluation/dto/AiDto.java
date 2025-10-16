package com.backend.domain.evaluation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 요청/응답 DTO 묶음
 */
public class AiDto {

    public record CompleteRequest(
            @NotBlank(message = "content는 비어 있을 수 없습니다.") String content,
            @NotBlank(message = "prompt는 비어 있을 수 없습니다.") String prompt
    ) {}

    public record CompleteResponse(String result) {}
}