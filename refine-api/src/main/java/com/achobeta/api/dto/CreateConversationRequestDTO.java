package com.achobeta.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * @Auth : Malog
 * @Desc : 创建会话请求DTO
 * @Time : 2025/11/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequestDTO {

    /** 用户ID */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /** 错题ID */
    @NotBlank(message = "错题ID不能为空")
    private String questionId;

    /** 题目内容（可选，如果为空则从数据库查询） */
    private String questionContent;
}