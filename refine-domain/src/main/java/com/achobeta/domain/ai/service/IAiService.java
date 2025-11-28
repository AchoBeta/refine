package com.achobeta.domain.ai.service;

import com.achobeta.domain.conversation.model.entity.ConversationMessageEntity;

import java.util.List;
import java.util.function.Consumer;

/**
 * @Auth : Malog
 * @Desc : AI服务接口
 * @Time : 2025/11/2 14:34
 */
public interface IAiService {

    /**
     * 抽取第一个问题
     *
     * @param content 文件内容
     * @return 第一个问题
     */
    String extractTheFirstQuestion(String content);

    /**
     * AI解答问题（无上下文）
     *
     * @param question 问题内容
     * @param contentCallback 流式回调函数
     */
    void aiSolveQuestion(String question, Consumer<String> contentCallback);

    /**
     * AI聊天
     *
     * @param question 问题内容
     * @param contentCallback 流式回调函数
     */
    void aiChat(String question, Consumer<String> contentCallback);

    /**
     * AI解答问题（带上下文）
     *
     * @param questionId 错题ID（作为会话ID）
     * @param question 问题内容
     * @param conversationHistory 会话历史
     * @param contentCallback 流式回调函数
     */
    void aiSolveQuestionWithContext(String questionId, String question, List<ConversationMessageEntity> conversationHistory, Consumer<String> contentCallback);

}
