package com.backend.global.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;

@Configuration
public class OpenAIConfig {

    @Bean
    @Conditional(OpenAIKeyPresentCondition.class)
    public OpenAIClient openAIClient() {
        String key = OpenAIKeyUtil.findKey(); // ← 여기 이름만 주의
        return OpenAIOkHttpClient.builder()
                .apiKey(key)   // 키를 명시로 박아서 .env만 있어도 확실히 동작
                .build();
    }
}
