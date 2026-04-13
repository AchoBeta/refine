package com.achobeta.trigger.http;

import com.achobeta.api.dto.AiSolveRequestDTO;
import com.achobeta.domain.ai.service.IAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Auth : Malog
 * @Desc : AI解题控制器 - 专门处理AI解题相关的请求
 * @Time : 2025/11/7 11:15
 */

@Slf4j
@Validated
@RestController()
@CrossOrigin("${app.config.cross-origin}:*")
@RequestMapping("/api/${app.config.api-version}/solve/")
@RequiredArgsConstructor
public class AiSolveController {

    private final IAiService aiService;

    @PostMapping("stream")
    public SseEmitter stream(@Valid @RequestBody AiSolveRequestDTO requestDTO) {
        String question = requestDTO.getQuestion();
        // 设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 使用AtomicBoolean确保线程安全
        final AtomicBoolean isConnectionActive = new java.util.concurrent.atomic.AtomicBoolean(true);

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout for question: {}", question);
            isConnectionActive.set(false);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Error completing emitter on timeout: {}", e.getMessage());
            }
        });

        emitter.onCompletion(() -> {
            log.info("SSE connection completed for question: {}", question);
            isConnectionActive.set(false);
        });

        emitter.onError((ex) -> {
            // 区分不同类型的错误，避免记录客户端主动断开的错误
            if (ex instanceof java.io.IOException && ex.getMessage() != null && 
                (ex.getMessage().contains("Broken pipe") || ex.getMessage().contains("Connection reset"))) {
                log.debug("Client disconnected during SSE stream for question: {}", question);
            } else {
                log.error("SSE connection error for question: {}", question, ex);
            }
            isConnectionActive.set(false);
        });

        // 异步处理AI调用，避免阻塞HTTP线程
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                aiService.aiSolveQuestion(question, content -> {
                    // 检查连接是否仍然活跃
                    if (!isConnectionActive.get()) {
                        log.debug("Connection is no longer active, stopping content sending");
                        return;
                    }
                    
                    try {
                        emitter.send(SseEmitter.event().data(content));
                    } catch (IOException e) {
                        // 检查是否是客户端断开连接
                        if (e.getMessage() != null && 
                            (e.getMessage().contains("Broken pipe") || 
                             e.getMessage().contains("Connection reset") ||
                             e.getMessage().contains("ClientAbortException"))) {
                            log.debug("Client disconnected while sending SSE data: {}", e.getMessage());
                        } else {
                            log.error("Error sending SSE event: {}", e.getMessage());
                        }
                        isConnectionActive.set(false);
                    } catch (IllegalStateException e) {
                        // 处理连接已关闭的情况
                        log.debug("SSE connection already closed: {}", e.getMessage());
                        isConnectionActive.set(false);
                    } catch (Exception e) {
                        log.error("Unexpected error sending SSE event", e);
                        isConnectionActive.set(false);
                    }
                });

                // 只有在连接仍然活跃时才完成emitter
                if (isConnectionActive.get()) {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.debug("Error completing emitter: {}", e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("Error during AI stream call", e);
                if (isConnectionActive.get()) {
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ex) {
                        log.debug("Error completing emitter with error: {}", ex.getMessage());
                    }
                }
            }
        });

        return emitter;
    }


}
