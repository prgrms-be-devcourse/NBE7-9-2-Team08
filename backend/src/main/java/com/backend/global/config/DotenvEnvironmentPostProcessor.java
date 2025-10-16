package com.backend.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 루트 .env 로드 (없으면 무시)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .filename(".env")
                .systemProperties() // System.setProperty에도 주입 → OpenAI fromEnv()도 읽음
                .load();

        Map<String, Object> map = new HashMap<>();
        putIfPresent(dotenv, map, "DB_URL");
        putIfPresent(dotenv, map, "DB_USERNAME");
        putIfPresent(dotenv, map, "DB_PASSWORD");
        putIfPresent(dotenv, map, "OPENAI_API_KEY");

        // spring.datasource.* 기본 매핑(이미 yml에 있으면 yml이 이깁니다)
        if (!environment.containsProperty("spring.datasource.url") && dotenv.get("DB_URL") != null)
            map.put("spring.datasource.url", dotenv.get("DB_URL"));
        if (!environment.containsProperty("spring.datasource.username") && dotenv.get("DB_USERNAME") != null)
            map.put("spring.datasource.username", dotenv.get("DB_USERNAME"));
        if (!environment.containsProperty("spring.datasource.password") && dotenv.get("DB_PASSWORD") != null)
            map.put("spring.datasource.password", dotenv.get("DB_PASSWORD"));
        if (!environment.containsProperty("spring.datasource.driver-class-name"))
            map.put("spring.datasource.driver-class-name", "org.h2.Driver");

        if (!map.isEmpty()) {
            // 가장 높은 우선순위로 추가(=다른 소스보다 먼저 조회)
            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", map));
        }
    }

    private static void putIfPresent(Dotenv dotenv, Map<String, Object> map, String key) {
        String v = dotenv.get(key);
        if (v != null) map.put(key, v);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
