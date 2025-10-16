package com.backend.domain.evaluation.controller;

import com.backend.domain.evaluation.service.AiService;
import com.backend.domain.evaluation.dto.AiDto;
import com.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    /**
     * POST /api/ai/complete
     * {
     *   "content": "분석 대상 글",
     *   "prompt":  "역할/지시문"
     * }
     *
     * 응답:
     * {
     *   "code": "OK",
     *   "message": "성공",
     *   "data": { "result": "모델 응답" }
     * }
     */
    @PostMapping("/complete")
    public ApiResponse<AiDto.CompleteResponse> complete(@Valid @RequestBody AiDto.CompleteRequest request) {
        return ApiResponse.success(aiService.complete(request));
    }
}
