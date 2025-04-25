package com.gaia3d.mago.server.controller;

import com.gaia3d.mago.server.domain.ConversionProcess;
import com.gaia3d.mago.server.domain.NotificationResponse;
import com.gaia3d.mago.server.service.ExecutionStatusService;
import com.gaia3d.mago.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class ServerSentEventController {
    private final NotificationService notificationService;
    private final ExecutionStatusService executionStatusService;

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(String lastEventId) {
        lastEventId = "user00";
        SseEmitter sseEmitter = notificationService.createAndGetEmitter(lastEventId);
        log.info(":: Subscribe emitter for {}", lastEventId);
        try {
            List<ConversionProcess> processList = executionStatusService.getProcessList();

            String message = "Connection successful";
            NotificationResponse notificationResponse = NotificationResponse.builder()
                    .status("open")
                    .name(message)
                    .conversionProcesses(processList)
                    .build();
            sseEmitter.send(SseEmitter
                    .event()
                    .name("connection")
                    .data(notificationResponse)
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sseEmitter;
    }
}
