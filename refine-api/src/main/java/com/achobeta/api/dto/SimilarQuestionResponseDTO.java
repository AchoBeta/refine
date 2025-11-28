package com.achobeta.api.dto;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @Auth : Malog
 * @Desc : 相似题目响应DTO
 * @Time : 2025/11/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarQuestionResponseDTO {
    
    /**
     * 题目ID
     */
    private String questionId;
    
    /**
     * 题目内容
     */
    private String questionContent;
    
    /**
     * 行为类型
     */
    private String actionType;
    
    /**
     * 科目
     */
    private String subject;
    
    /**
     * 相似度分数
     */
    private Double similarity;
    
    /**
     * 创建时间
     */
    private String createdAt;
}