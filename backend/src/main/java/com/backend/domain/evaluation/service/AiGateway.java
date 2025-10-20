package com.backend.domain.evaluation.service;

public interface AiGateway {
    String complete(String content, String prompt);
}
