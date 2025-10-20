package com.backend.domain.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AnalysisRequest(
        @NotBlank(message = "GitHub URL은 필수입니다")
        @Pattern(regexp = "^https://github\\.com/[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+/?$",
                message = "올바른 GitHub 저장소 URL 형식이 아닙니다")
        String githubUrl
) {}
