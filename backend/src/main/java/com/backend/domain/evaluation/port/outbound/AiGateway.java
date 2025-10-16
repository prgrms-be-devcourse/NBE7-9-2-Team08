package com.backend.domain.evaluation.port.outbound;

public interface AiGateway {
    String complete(String content, String prompt);
}
