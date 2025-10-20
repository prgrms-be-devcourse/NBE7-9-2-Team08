package com.backend.domain.evaluation.service.impl;

import com.backend.domain.evaluation.service.AiGateway;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression; // ★ import
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).hasText(environment['openai.api.key'])")
public class NoopAiGateway implements AiGateway {

    @PostConstruct
    void logActive() { System.out.println("[AiGateway] NoopAiGateway ACTIVE"); }

    @Override
    public String complete(String content, String prompt) {
        return "[OPENAI 비활성화] OPENAI_API_KEY가 설정되지 않았습니다.";
    }
}
