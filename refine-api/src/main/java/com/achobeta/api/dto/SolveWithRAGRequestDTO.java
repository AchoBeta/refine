package com.achobeta.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * @Auth : Malog
 * @Desc : RAG增强对话请求DTO
 * @Time : 2025/11/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolveWithRAGRequestDTO {
    
    /**
     * 错题ID
     */
    @NotBlank(message = "错题ID不能为空")
    private String questionId;
    
    /**
     * 题目内容（可选）
     */
    private String questionContent;
    
    /**
     * 用户问题
     */
    @NotBlank(message = "用户问题不能为空")
    private String userQuestion;
}