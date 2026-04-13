package com.achobeta.trigger.http;

import com.achobeta.domain.conversation.service.IConversationService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.conversation.AiResponseHandler;
import com.achobeta.api.dto.SendMessageRequestDTO;
import com.achobeta.api.dto.SolveWithContextRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * @Auth : Malog
 * @Desc : 处理AI对话相关的请求
 * @Time : 2025/11/11
 */
@Slf4j
@Validated
@RestController
@CrossOrigin("${app.config.cross-origin}:*")
@RequestMapping("/api/${app.config.api-version}/conversation/")
@RequiredArgsConstructor
public class ConversationController {

    private final IConversationService conversationService;

    /**
     * 发送消息并获取AI回复（流式）
     */
    @PostMapping("send-message")
    public SseEmitter sendMessage(@Valid @RequestBody SendMessageRequestDTO requestDTO) {
        // 设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 使用AtomicBoolean确保线程安全
        final java.util.concurrent.atomic.AtomicBoolean isConnectionActive = new java.util.concurrent.atomic.AtomicBoolean(true);

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("会话SSE连接超时，conversationId: {}", requestDTO.getConversationId());
            isConnectionActive.set(false);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("超时时完成emitter出错: {}", e.getMessage());
            }
        });

        emitter.onCompletion(() -> {
            log.info("会话SSE连接已完成，conversationId: {}", requestDTO.getConversationId());
            isConnectionActive.set(false);
        });

        emitter.onError((ex) -> {
            // 区分不同类型的错误，避免记录客户端主动断开的错误
            if (ex instanceof java.io.IOException && ex.getMessage() != null && 
                (ex.getMessage().contains("Broken pipe") || ex.getMessage().contains("Connection reset"))) {
                log.debug("客户端主动断开SSE连接，conversationId: {}", requestDTO.getConversationId());
            } else {
                log.error("会话SSE连接出错，conversationId: {}", requestDTO.getConversationId(), ex);
            }
            isConnectionActive.set(false);
        });

        // 异步处理AI调用，避免阻塞HTTP线程
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                log.info("发送消息开始，conversationId:{}", requestDTO.getConversationId());

                // 创建AI回复处理器
                AiResponseHandler responseHandler = AiResponseHandler.builder()
                        .onStreaming(pureContent -> {
                            // 检查连接是否仍然活跃
                            if (!isConnectionActive.get()) {
                                log.debug("连接已不活跃，停止发送内容");
                                return;
                            }
                            
                            try {
                                emitter.send(SseEmitter.event().data(pureContent));
                            } catch (IOException e) {
                                // 检查是否是客户端断开连接
                                if (e.getMessage() != null && 
                                    (e.getMessage().contains("Broken pipe") || 
                                     e.getMessage().contains("Connection reset") ||
                                     e.getMessage().contains("ClientAbortException"))) {
                                    log.debug("发送SSE数据时客户端断开连接: {}", e.getMessage());
                                } else {
                                    log.error("发送SSE流式事件出错: {}", e.getMessage());
                                }
                                isConnectionActive.set(false);
                            } catch (IllegalStateException e) {
                                log.debug("SSE连接已关闭: {}", e.getMessage());
                                isConnectionActive.set(false);
                            } catch (Exception e) {
                                log.error("发送SSE流式事件时发生意外错误", e);
                                isConnectionActive.set(false);
                            }
                        })
                        .onCompleted(pureContent -> {
                            // 检查连接是否仍然活跃
                            if (!isConnectionActive.get()) {
                                log.debug("连接已不活跃，停止发送内容");
                                return;
                            }
                            
                            try {
                                emitter.send(SseEmitter.event().data(pureContent));
                            } catch (IOException e) {
                                if (e.getMessage() != null && 
                                    (e.getMessage().contains("Broken pipe") || 
                                     e.getMessage().contains("Connection reset") ||
                                     e.getMessage().contains("ClientAbortException"))) {
                                    log.debug("发送SSE完成数据时客户端断开连接: {}", e.getMessage());
                                } else {
                                    log.error("发送SSE完成事件出错: {}", e.getMessage());
                                }
                                isConnectionActive.set(false);
                            } catch (IllegalStateException e) {
                                log.debug("SSE连接已关闭: {}", e.getMessage());
                                isConnectionActive.set(false);
                            } catch (Exception e) {
                                log.error("发送SSE完成事件时发生意外错误", e);
                                isConnectionActive.set(false);
                            }
                        })
                        .onError(errorContent -> {
                            log.error("AI回复错误: {}", errorContent);
                            if (isConnectionActive.get()) {
                                try {
                                    emitter.completeWithError(new RuntimeException("AI回复错误: " + errorContent));
                                } catch (Exception e) {
                                    log.debug("完成emitter时出错: {}", e.getMessage());
                                }
                            }
                        })
                        .onFinish(() -> {
                            if (isConnectionActive.get()) {
                                try {
                                    emitter.complete();
                                } catch (Exception e) {
                                    log.debug("完成emitter出错: {}", e.getMessage());
                                }
                            }
                        })
                        .build();

                // 调用会话服务，使用Redis存储上下文
                boolean success = conversationService.sendMessageWithStream(
                        requestDTO.getConversationId(),
                        requestDTO.getMessage(),
                        responseHandler::handle);

                if (!success) {
                    log.error("发送消息失败，conversationId:{}", requestDTO.getConversationId());
                    if (isConnectionActive.get()) {
                        try {
                            emitter.completeWithError(new RuntimeException("发送消息失败"));
                        } catch (Exception e) {
                            log.debug("完成emitter时出错: {}", e.getMessage());
                        }
                    }
                } else {
                    log.info("发送消息成功，conversationId:{}", requestDTO.getConversationId());
                }

            } catch (Exception e) {
                log.error("发送消息时发生异常，conversationId:{}", requestDTO.getConversationId(), e);
                if (isConnectionActive.get()) {
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ex) {
                        log.debug("完成emitter时出错: {}", ex.getMessage());
                    }
                }
            }
        });

        return emitter;
    }

    /**
     * 基于错题ID的AI对话接口（支持上下文记忆）
     */
    @GlobalInterception
    @PostMapping("solve-with-context")
    public SseEmitter solveWithContext(@Valid @RequestBody SolveWithContextRequestDTO requestDTO) {
        //分钟
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 使用AtomicBoolean确保线程安全
        final java.util.concurrent.atomic.AtomicBoolean isConnectionActive = new java.util.concurrent.atomic.AtomicBoolean(true);

        // 获取用户ID并进行早期验证
        String userId = UserContext.getUserId();
        if (userId == null) {
            try {
                emitter.completeWithError(new RuntimeException("用户信息获取失败"));
            } catch (Exception e) {
                log.debug("完成emitter时出错: {}", e.getMessage());
            }
            return emitter;
        }

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("错题对话SSE连接超时，questionId: {}", requestDTO.getQuestionId());
            isConnectionActive.set(false);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("超时时完成emitter出错: {}", e.getMessage());
            }
        });

        emitter.onCompletion(() -> {
            log.info("错题对话SSE连接已完成，questionId: {}", requestDTO.getQuestionId());
            isConnectionActive.set(false);
        });

        emitter.onError((ex) -> {
            // 区分不同类型的错误，避免记录客户端主动断开的错误
            if (ex instanceof java.io.IOException && ex.getMessage() != null && 
                (ex.getMessage().contains("Broken pipe") || ex.getMessage().contains("Connection reset"))) {
                log.debug("客户端主动断开SSE连接，questionId: {}", requestDTO.getQuestionId());
            } else {
                log.error("错题对话SSE连接出错，questionId: {}", requestDTO.getQuestionId(), ex);
            }
            isConnectionActive.set(false);
        });

        // 异步处理AI调用，避免阻塞HTTP线程
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                log.info("基于错题ID的AI对话开始，userId:{} questionId:{}", userId, requestDTO.getQuestionId());

                // 创建AI回复处理器
                AiResponseHandler responseHandler = AiResponseHandler.builder()
                        .onStreaming(pureContent -> {
                            // 检查连接是否仍然活跃
                            if (!isConnectionActive.get()) {
                                log.debug("连接已不活跃，停止发送内容");
                                return;
                            }
                            
                            try {
                                emitter.send(SseEmitter.event().data(pureContent));
                            } catch (IOException e) {
                                // 检查是否是客户端断开连接
                                if (e.getMessage() != null && 
                                    (e.getMessage().contains("Broken pipe") || 
                                     e.getMessage().contains("Connection reset") ||
                                     e.getMessage().contains("ClientAbortException"))) {
                                    log.debug("发送SSE数据时客户端断开连接: {}", e.getMessage());
                                } else {
                                    log.error("发送SSE流式事件出错: {}", e.getMessage());
                                }
                                isConnectionActive.set(false);
                            } catch (IllegalStateException e) {
                                log.debug("SSE连接已关闭: {}", e.getMessage());
                                isConnectionActive.set(false);
                            } catch (Exception e) {
                                log.error("发送SSE流式事件时发生意外错误", e);
                                isConnectionActive.set(false);
                            }
                        })
                        .onCompleted(pureContent -> {
                            // 检查连接是否仍然活跃
                            if (!isConnectionActive.get()) {
                                log.debug("连接已不活跃，停止发送内容");
                                return;
                            }
                            
                            try {
                                emitter.send(SseEmitter.event().data(pureContent));
                            } catch (IOException e) {
                                if (e.getMessage() != null && 
                                    (e.getMessage().contains("Broken pipe") || 
                                     e.getMessage().contains("Connection reset") ||
                                     e.getMessage().contains("ClientAbortException"))) {
                                    log.debug("发送SSE完成数据时客户端断开连接: {}", e.getMessage());
                                } else {
                                    log.error("发送SSE完成事件出错: {}", e.getMessage());
                                }
                                isConnectionActive.set(false);
                            } catch (IllegalStateException e) {
                                log.debug("SSE连接已关闭: {}", e.getMessage());
                                isConnectionActive.set(false);
                            } catch (Exception e) {
                                log.error("发送SSE完成事件时发生意外错误", e);
                                isConnectionActive.set(false);
                            }
                        })
                        .onError(errorContent -> {
                            log.error("AI回复错误: {}", errorContent);
                            if (isConnectionActive.get()) {
                                try {
                                    emitter.completeWithError(new RuntimeException("AI回复错误: " + errorContent));
                                } catch (Exception e) {
                                    log.debug("完成emitter时出错: {}", e.getMessage());
                                }
                            }
                        })
                        .onFinish(() -> {
                            if (isConnectionActive.get()) {
                                try {
                                    emitter.complete();
                                } catch (Exception e) {
                                    log.debug("完成emitter出错: {}", e.getMessage());
                                }
                            }
                        })
                        .build();

                // 直接使用错题ID作为会话ID进行对话（不需要创建数据库会话）
                boolean success = conversationService.sendMessageWithStream(
                        requestDTO.getQuestionId(), // 使用错题ID作为会话ID
                        requestDTO.getUserQuestion(),
                        responseHandler::handle);

                if (!success) {
                    log.error("AI对话失败，questionId:{}", requestDTO.getQuestionId());
                    if (isConnectionActive.get()) {
                        try {
                            emitter.completeWithError(new RuntimeException("AI对话失败"));
                        } catch (Exception e) {
                            log.debug("完成emitter时出错: {}", e.getMessage());
                        }
                    }
                } else {
                    log.info("AI对话成功，questionId:{}", requestDTO.getQuestionId());
                }

            } catch (Exception e) {
                log.error("基于错题ID的AI对话时发生异常，questionId:{}", requestDTO.getQuestionId(), e);
                if (isConnectionActive.get()) {
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ex) {
                        log.debug("完成emitter时出错: {}", ex.getMessage());
                    }
                }
            }
        });

        return emitter;
    }

    /**
     * 删除会话（清除Redis中的对话历史）
     */
    @DeleteMapping("delete/{conversationId}")
    public Response<Boolean> deleteConversation(@PathVariable String conversationId) {
        try {
            log.info("删除会话开始，conversationId:{}", conversationId);

            boolean success = conversationService.deleteConversation(conversationId);

            if (success) {
                log.info("删除会话成功，conversationId:{}", conversationId);
                return Response.SYSTEM_SUCCESS(true);
            } else {
                log.error("删除会话失败，conversationId:{}", conversationId);
                return Response.SERVICE_ERROR("删除会话失败");
            }
        } catch (Exception e) {
            log.error("删除会话时发生异常，conversationId:{}", conversationId, e);
            return Response.SERVICE_ERROR("系统异常: " + e.getMessage());
        }
    }
}