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
        SseEmitter emitter = new SseEmitter();

        try {
            log.info("发送消息开始，conversationId:{}", requestDTO.getConversationId());

            // 创建AI回复处理器
            AiResponseHandler responseHandler = AiResponseHandler.builder()
                    .onStreaming(pureContent -> {
                        try {
                            emitter.send(SseEmitter.event().data(pureContent));
                        } catch (IOException e) {
                            log.error("发送SSE流式事件失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    .onCompleted(pureContent -> {
                        try {
                            emitter.send(SseEmitter.event().data(pureContent));
                        } catch (IOException e) {
                            log.error("发送SSE完成事件失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    .onError(errorContent -> {
                        log.error("AI回复错误: {}", errorContent);
                        emitter.completeWithError(new RuntimeException("AI回复错误: " + errorContent));
                    })
                    .onFinish(emitter::complete)
                    .build();

            // 调用会话服务，使用Redis存储上下文
            boolean success = conversationService.sendMessageWithStream(
                    requestDTO.getConversationId(),
                    requestDTO.getMessage(),
                    responseHandler::handle);

            if (!success) {
                log.error("发送消息失败，conversationId:{}", requestDTO.getConversationId());
                emitter.completeWithError(new RuntimeException("发送消息失败"));
            } else {
                log.info("发送消息成功，conversationId:{}", requestDTO.getConversationId());
            }

        } catch (Exception e) {
            log.error("发送消息时发生异常，conversationId:{}", requestDTO.getConversationId(), e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 基于错题ID的AI对话接口（支持上下文记忆）
     */
    @GlobalInterception
    @PostMapping("solve-with-context")
    public SseEmitter solveWithContext(@Valid @RequestBody SolveWithContextRequestDTO requestDTO) {
        SseEmitter emitter = new SseEmitter();

        try {
            String userId = UserContext.getUserId();
            if (userId == null) {
                emitter.completeWithError(new RuntimeException("用户信息获取失败"));
                return emitter;
            }

            log.info("基于错题ID的AI对话开始，userId:{} questionId:{}", userId, requestDTO.getQuestionId());

            // 创建AI回复处理器
            AiResponseHandler responseHandler = AiResponseHandler.builder()
                    .onStreaming(pureContent -> {
                        try {
                            emitter.send(SseEmitter.event().data(pureContent));
                        } catch (IOException e) {
                            log.error("发送SSE流式事件失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    .onCompleted(pureContent -> {
                        try {
                            emitter.send(SseEmitter.event().data(pureContent));
                        } catch (IOException e) {
                            log.error("发送SSE完成事件失败", e);
                            emitter.completeWithError(e);
                        }
                    })
                    .onError(errorContent -> {
                        log.error("AI回复错误: {}", errorContent);
                        emitter.completeWithError(new RuntimeException("AI回复错误: " + errorContent));
                    })
                    .onFinish(emitter::complete)
                    .build();

            // 直接使用错题ID作为会话ID进行对话（不需要创建数据库会话）
            boolean success = conversationService.sendMessageWithStream(
                    requestDTO.getQuestionId(), // 使用错题ID作为会话ID
                    requestDTO.getUserQuestion(),
                    responseHandler::handle);

            if (!success) {
                log.error("AI对话失败，questionId:{}", requestDTO.getQuestionId());
                emitter.completeWithError(new RuntimeException("AI对话失败"));
            } else {
                log.info("AI对话成功，questionId:{}", requestDTO.getQuestionId());
            }

        } catch (Exception e) {
            log.error("基于错题ID的AI对话时发生异常，questionId:{}", requestDTO.getQuestionId(), e);
            emitter.completeWithError(e);
        }

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