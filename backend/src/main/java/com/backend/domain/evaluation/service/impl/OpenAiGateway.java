package com.backend.domain.evaluation.service.impl;

import com.backend.domain.evaluation.service.AiGateway;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression; // ★ import
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText(environment['openai.api.key'])")
@RequiredArgsConstructor
public class OpenAiGateway implements AiGateway {

    private final OpenAIClient client;

    @PostConstruct
    void logActive() { System.out.println("[AiGateway] OpenAiGateway ACTIVE"); }

    @Override
    public String complete(String content, String prompt) {
        String input = """
                [SYSTEM PROMPT]
                %s

                [USER CONTENT]
                %s

                [RESPONSE RULE]
                - 항상 한국어 텍스트만 출력하세요.
                """.formatted(
                prompt == null ? "" : prompt,
                content == null ? "" : content
        );

        var params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_5_NANO)
                .input(input)
                .build();

        Response res = client.responses().create(params);

        try {
            Method m = res.getClass().getMethod("outputText");
            Object out = m.invoke(res);
            if (out instanceof String s && !s.trim().isEmpty()) return s.trim();
        } catch (Throwable ignore) {}

        String joined = extractOutputText(res);
        if (!joined.isEmpty()) return joined;

        return String.valueOf(res);
    }

    private static String extractOutputText(Response res) {
        if (res == null || res.output() == null) return "";
        return res.output().stream()
                .map(ResponseOutputItem::message)
                .flatMap(Optional::stream)
                .flatMap(msg -> {
                    List<ResponseOutputMessage.Content> cs = msg.content();
                    return (cs == null) ? Stream.<ResponseOutputMessage.Content>empty() : cs.stream();
                })
                .map(c -> {
                    try {
                        ResponseOutputText t = c.asOutputText();
                        return (t != null) ? t.text() : null;
                    } catch (Exception ignore) { return null; }
                })
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining());
    }
}
