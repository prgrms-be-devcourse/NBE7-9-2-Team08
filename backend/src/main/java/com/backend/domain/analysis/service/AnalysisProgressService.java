package com.backend.domain.analysis.service;

import com.backend.domain.user.util.JwtUtil;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisProgressService {
    private final JwtUtil jwtUtil;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId, HttpServletRequest request) {
        Long requestUserId = jwtUtil.getUserId(request);
        if (requestUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!requestUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        sendEvent(userId, "connected", "SSE 연결 완료");
        return emitter;
    }

    public void sendEvent(Long userId, String eventName, String data) {
        SseEmitter emitter = emitters.get(userId);
        if(emitter == null) {
            log.debug("SSE Emitter 없음: userId={}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
            log.debug("SSE 전송 성공: userId={}, event={}", userId, eventName);
        } catch(IOException e) {
            log.debug("SSE 연결 끊김 (정상): userId={}, event={}", userId, eventName);
            emitters.remove(userId);
        }
    }
}
