package com.backend.domain.analysis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalysisProgressService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        sendEvent(userId, "connected", "SSE 연결 완료");
        return emitter;
    }

    public void sendEvent(Long userId, String eventName, String data) {
        SseEmitter emitter = emitters.get(userId);
        if(emitter == null) return;

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch(IOException e) {
            emitters.remove(userId);
        }
    }
}
