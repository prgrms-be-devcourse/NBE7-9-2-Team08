package com.backend.global.openai;

import com.backend.domain.evaluation.port.outbound.AiGateway;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Conditional(OpenAIKeyPresentCondition.class)
@RequiredArgsConstructor
public class OpenAiGateway implements AiGateway {

    private final OpenAIClient client;

    @Override
    public String complete(String content, String prompt) {
        String input = """
                [SYSTEM PROMPT]
                %s

                [USER CONTENT]
                %s

                [RESPONSE RULE]
                - 항상 한국어 텍스트로만 최종 답변을 출력하세요.
                - 필요 시 거절/도구 호출 대신 이유를 한 문장으로 설명하세요.
                """.formatted(
                prompt == null ? "" : prompt,
                content == null ? "" : content
        );

        var params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_5_NANO)
                .input(input)
                .build();

        Response res = client.responses().create(params);

        // 디버깅이 필요하면 주석 해제
        // debugLog(res);

        // 1) 헬퍼 우선
        try {
            Method m = res.getClass().getMethod("outputText");
            Object out = m.invoke(res);
            if (out instanceof String s) {
                String t = s.trim();
                if (!t.isEmpty()) return t;
            }
        } catch (Throwable ignore) {}

        // 2) 안전 추출
        String joined = extractOutputText(res);
        if (!joined.isEmpty()) return joined;

        // 3) 최후 수단
        log.warn("No text content found in OpenAI response.");
        return String.valueOf(res);
    }

    private static String extractOutputText(Response res) {
        if (res == null || res.output() == null) return "";

        return res.output().stream()
                .map(ResponseOutputItem::message)         // Optional<ResponseOutputMessage>
                .flatMap(Optional::stream)
                .flatMap(msg -> {
                    List<ResponseOutputMessage.Content> cs = msg.content();
                    return (cs == null)
                            ? Stream.<ResponseOutputMessage.Content>empty()
                            : cs.stream();
                })
                .map(c -> {
                    try {
                        ResponseOutputText t = c.asOutputText();
                        return (t != null) ? t.text() : null;
                    } catch (Exception ignore) {
                        return null; // 텍스트가 아니면 스킵
                    }
                })
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining());
    }

    @SuppressWarnings("unused")
    private static void debugLog(Response res) {
        System.out.println("=== RAW Response ===");
        System.out.println(res);
        System.out.println("=== Content Types ===");
        res.output().stream()
                .map(ResponseOutputItem::message)
                .flatMap(Optional::stream)
                .forEach(msg -> {
                    var contents = msg.content();
                    if (contents == null) return;
                    contents.forEach(c -> {
                        boolean printed = false;
                        try { if (c.asOutputText() != null) { System.out.println(" - TEXT"); printed = true; } } catch (Exception ignore) {}
                        if (!printed) System.out.println(" - NON_TEXT");
                    });
                });
    }
}
