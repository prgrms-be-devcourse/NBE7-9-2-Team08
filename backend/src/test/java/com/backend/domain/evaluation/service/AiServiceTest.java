package com.backend.domain.evaluation.service;

import com.backend.domain.evaluation.dto.AiDto;
import com.backend.domain.evaluation.port.outbound.AiGateway;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceTest {

    static class FakeAiGateway implements AiGateway {
        @Override
        public String complete(String content, String prompt) {
            return "[PROMPT]" + prompt + " | [CONTENT]" + content;
        }
    }

    @Test
    void content와_prompt를_넣으면_결과가_반환된다() {
        AiGateway gateway = new FakeAiGateway();
        AiService service = new AiService(gateway);

        var req = new AiDto.CompleteRequest("안녕하세요", "요약해줘");
        var res = service.complete(req);

        assertThat(res.result()).isEqualTo("[PROMPT]요약해줘 | [CONTENT]안녕하세요");
    }
}
