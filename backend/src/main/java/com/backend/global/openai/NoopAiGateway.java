package com.backend.global.openai;

import com.backend.domain.evaluation.port.outbound.AiGateway;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OpenAIKeyMissingCondition.class)
public class NoopAiGateway implements AiGateway {
    @Override
    public String complete(String content, String prompt) {
        return "[OPENAI 비활성화] .env 또는 환경변수에 OPENAI_API_KEY를 설정하세요.";
    }
}
