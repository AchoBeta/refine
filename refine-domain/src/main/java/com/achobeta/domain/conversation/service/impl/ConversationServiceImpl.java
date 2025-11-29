package com.achobeta.domain.conversation.service.impl;

import com.achobeta.domain.ai.service.IAiService;
import com.achobeta.domain.conversation.adapter.port.redis.IConversationRedisRepository;
import com.achobeta.domain.conversation.model.entity.ConversationMessageEntity;
import com.achobeta.domain.conversation.service.IConversationService;
import com.achobeta.domain.ocr.adapter.port.redis.IQuestionRedisRepository;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Auth : Malog
 * @Desc : 会话服务实现（纯Redis实现）
 * @Time : 2025/11/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements IConversationService {

    private final IAiService aiService;
    private final IConversationRedisRepository conversationRedisRepository;
    private final IQuestionRedisRepository questionRedisRepository;

    @Override
    public boolean sendMessageWithStream(String conversationId, String userMessage, Consumer<String> contentCallback) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                log.warn("会话ID为空，无法发送消息");
                return false;
            }
            if (userMessage == null || userMessage.trim().isEmpty()) {
                log.warn("用户消息为空，无法发送");
                return false;
            }

            // 从Redis获取会话历史（如果是第一次对话，历史为空）
            List<ConversationMessageEntity> conversationHistory = conversationRedisRepository.getConversationHistory(conversationId);
            if (conversationHistory == null) {
                conversationHistory = new ArrayList<>();
                log.info("未找到会话历史，使用空历史列表: conversationId={}", conversationId);
            }

            // 尝试从Redis获取题目信息，如果conversationId是questionId的话
            QuestionEntity questionEntity = questionRedisRepository.getQuestion(conversationId);
            String contextMessage = userMessage;
            
            // 如果找到了题目信息，将题目内容加入到用户消息的上下文中
            if (questionEntity != null) {
                contextMessage = "题目内容：" + questionEntity.getQuestionText() + "\n\n用户问题：" + userMessage;
                log.info("找到题目信息，已加入对话上下文: questionId={}", conversationId);
            } else {
                log.debug("未找到题目信息，使用原始用户消息: conversationId={}", conversationId);
            }

            // 创建包装的回调，用于处理AI回复完成后的保存操作
            Consumer<String> wrappedCallback = createWrappedCallback(conversationId, userMessage, conversationHistory, contentCallback);

            // 调用AI服务获取回复（带上下文）
            aiService.aiSolveQuestionWithContext(conversationId, contextMessage, conversationHistory, wrappedCallback);

            return true;
        } catch (Exception e) {
            log.error("发送消息时发生异常: conversationId={}, userMessage={}", conversationId, userMessage, e);
            return false;
        }
    }

    @Override
    public boolean deleteConversation(String conversationId) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                log.warn("会话ID为空，无法删除会话");
                return false;
            }

            // 删除Redis中的会话历史
            boolean redisDeleted = conversationRedisRepository.deleteConversation(conversationId);
            if (!redisDeleted) {
                log.warn("删除Redis会话历史失败: conversationId={}", conversationId);
                return false;
            }

            log.info("会话删除成功: conversationId={}", conversationId);
            return true;
        } catch (Exception e) {
            log.error("删除会话时发生异常: conversationId={}", conversationId, e);
            return false;
        }
    }

    /**
     * 创建包装的回调函数，用于处理AI回复完成后的保存操作
     */
    private Consumer<String> createWrappedCallback(String conversationId, String userMessage, 
                                                  List<ConversationMessageEntity> conversationHistory, 
                                                  Consumer<String> originalCallback) {
        return content -> {
            // 检查是否是AI回复完成的特殊标记
            if (content.startsWith("###AI_RESPONSE_END###")) {
                // 提取完整的AI回复内容
                String aiResponse = content.substring("###AI_RESPONSE_END###".length());
                
                // 保存完整的对话到Redis
                saveConversationToRedis(conversationId, userMessage, aiResponse, conversationHistory);
                
                // 移除特殊标记，只发送纯内容给前端
                if (originalCallback != null) {
                    originalCallback.accept(aiResponse);
                }
            } else {
                // 普通流式输出，直接传递给前端
                if (originalCallback != null) {
                    originalCallback.accept(content);
                }
            }
        };
    }

    /**
     * 保存完整的对话到Redis
     */
    private void saveConversationToRedis(String conversationId, String userMessage, String aiResponse, 
                                        List<ConversationMessageEntity> conversationHistory) {
        try {
            // 创建新的会话历史列表
            List<ConversationMessageEntity> updatedHistory = new ArrayList<>(conversationHistory);
            
            // 添加用户消息
            ConversationMessageEntity userMessageEntity = ConversationMessageEntity.builder()
                    .conversationId(conversationId)
                    .messageType(1) // 1-用户消息
                    .messageContent(userMessage)
                    .messageOrder(updatedHistory.size() + 1)
                    .build();
            updatedHistory.add(userMessageEntity);
            
            // 添加AI回复
            ConversationMessageEntity aiMessageEntity = ConversationMessageEntity.builder()
                    .conversationId(conversationId)
                    .messageType(2) // 2-AI回复
                    .messageContent(aiResponse)
                    .messageOrder(updatedHistory.size() + 1)
                    .build();
            updatedHistory.add(aiMessageEntity);
            
            // 保存到Redis，设置60分钟过期时间
            boolean success = conversationRedisRepository.saveConversationHistory(conversationId, updatedHistory);
            if (success) {
                log.debug("保存对话到Redis成功: conversationId={}", conversationId);
            } else {
                log.error("保存对话到Redis失败: conversationId={}", conversationId);
            }
        } catch (Exception e) {
            log.error("保存对话到Redis时发生异常: conversationId={}", conversationId, e);
        }
    }
}