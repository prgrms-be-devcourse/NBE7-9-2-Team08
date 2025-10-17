// src/main/java/com/backend/global/openai/OpenAIKeyProbe.java
package com.backend.global.openai;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAIKeyProbe {
    @Value("${OPENAI_API_KEY:}") String rawEnv;           // OS/env 직접
    @Value("${openai.api.key:}") String mapped;           // yml 매핑(아래 2단계에서 설정)
    @PostConstruct
    void log() {
        System.out.println("[PROBE] OPENAI_API_KEY=" + (rawEnv.isEmpty() ? "<EMPTY>" : "****"));
        System.out.println("[PROBE] openai.api.key=" + (mapped.isEmpty() ? "<EMPTY>" : "****"));
    }
}
