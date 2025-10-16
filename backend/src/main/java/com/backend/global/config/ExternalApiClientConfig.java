package com.backend.global.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalApiClientConfig {

    // --- GitHub API 설정 ---
    @Value("${github.api.base-url}")
    private String githubBaseUrl;

    @Value("${github.api.token}")
    private String githubToken;

    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl(githubBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "PortpolioIQ-App")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                .build();
    };

    // --- OpenAi API 설정 (필요 시) ---
}
