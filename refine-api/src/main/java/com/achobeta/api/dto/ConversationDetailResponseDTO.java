package com.achobeta.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auth : Malog
 * @Desc : 会话详情响应DTO
 * @Time : 2025/11/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDetailResponseDTO {

    /** 会话ID */
    private String conversationId;

    /** 用户ID */
    private String userId;

    /** 错题ID */
    private String questionId;

    /** 题目内容 */
    private String questionContent;

    /** AI解答内容 */
    private String aiSolution;

    /** 会话消息列表 */
    private List<ConversationMessageResponseDTO> messages;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 操作是否成功 */
    private Boolean success;

    /** 错误信息 */
    private String message;
}