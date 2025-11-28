package com.achobeta.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * @Auth : Malog
 * @Desc : 发送消息请求DTO
 * @Time : 2025/11/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequestDTO {

    /** 会话ID */
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    /** 用户消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String message;
}