package com.achobeta.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Auth : Malog
 * @Desc : 会话消息响应DTO
 * @Time : 2025/11/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessageResponseDTO {

    /** 消息ID */
    private Long id;

    /** 会话ID */
    private String conversationId;

    /** 消息类型（1-用户消息，2-AI回复） */
    private Integer messageType;

    /** 消息类型描述 */
    private String messageTypeDesc;

    /** 消息内容 */
    private String messageContent;

    /** 消息顺序 */
    private Integer messageOrder;

    /** 创建时间 */
    private LocalDateTime createTime;
}