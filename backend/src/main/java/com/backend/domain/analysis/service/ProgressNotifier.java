package com.backend.domain.analysis.service;

public interface ProgressNotifier {
    void notify(Long userId, String event, String message);
}
