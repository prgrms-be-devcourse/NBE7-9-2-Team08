package com.backend.global.openai;

import io.github.cdimascio.dotenv.Dotenv;

public final class OpenAIKeyUtil {
    private OpenAIKeyUtil() {}

    public static String findKey() {
        // 1) OS 환경변수 → 2) JVM 시스템 프로퍼티 → 3) .env
        String k = System.getenv("OPENAI_API_KEY");
        if (k == null || k.isBlank()) k = System.getProperty("OPENAI_API_KEY");
        if (k == null || k.isBlank()) {
            try {
                k = Dotenv.configure().ignoreIfMissing().load().get("OPENAI_API_KEY");
            } catch (Throwable ignore) { /* .env 없으면 무시 */ }
        }
        return (k != null && !k.isBlank()) ? k : null;
    }
}
