package com.gaia3d.mago.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final long DEFAULT_TIMEOUT = 1000L * 60 * 30;
    private final HashMap<String, SseEmitter> emitters = new HashMap<>();

    public SseEmitter createAndGetEmitter(String id) {
        createEmitter(id);
        return getEmitter(id);
    }

    public SseEmitter getEmitter(String id) {
        return emitters.get(id);
    }

    public void removeEmitter(String id) {
        emitters.remove(id);
    }

    private void createEmitter(String id) {
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitter.onCompletion(() -> {
            log.info("emitter completion");
        });
        sseEmitter.onError((e) -> {
            log.error("emitter error", e);
        });
        sseEmitter.onTimeout(() -> {
            log.info("emitter timeout");
        });
        emitters.put(id, sseEmitter);
    }
}
