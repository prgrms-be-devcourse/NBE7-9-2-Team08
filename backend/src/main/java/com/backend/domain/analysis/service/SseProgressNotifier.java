package com.backend.domain.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseProgressNotifier implements ProgressNotifier {
    private final AnalysisProgressService progressService;

    @Override
    public void notify(Long userId, String event, String message) {
        if(userId != null) {  // 수정!
            log.debug("[SSE] userId={}, event={}, message={}", userId, event, message);
            progressService.sendEvent(userId, event, message);
        } else {
            log.warn("[SSE] userId가 null입니다. 이벤트 전송 실패: {}", event);
        }
    }
}
