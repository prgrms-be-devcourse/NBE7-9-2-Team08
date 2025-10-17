// src/main/java/com/backend/global/config/DotenvEnvironmentPostProcessor.java
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
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().filename(".env").load();

        Map<String,Object> map = new HashMap<>();
        // 원본 env 노출
        put(dotenv, map, "OPENAI_API_KEY");
        // yml에서 쓸 별칭 프로퍼티도 같이 제공
        if (dotenv.get("OPENAI_API_KEY") != null) {
            map.put("openai.api.key", dotenv.get("OPENAI_API_KEY"));
        }
        // 이미 있는 다른 DB/GitHub 등도 여기서 같이 올려도 됨

        if (!map.isEmpty()) {
            env.getPropertySources().addFirst(new MapPropertySource("dotenv", map));
        }
    }
    private static void put(Dotenv d, Map<String,Object> m, String k) { if (d.get(k) != null) m.put(k, d.get(k)); }
    @Override public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}
