package com.achobeta.api.dto;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * @Auth : Malog
 * @Desc : 学习洞察响应DTO
 * @Time : 2025/11/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningInsightResponseDTO {
    
    /**
     * 洞察类型
     */
    private String type;
    
    /**
     * 洞察标题
     */
    private String title;
    
    /**
     * 洞察描述
     */
    private String description;
    
    /**
     * 置信度分数
     */
    private Double confidenceScore;
    
    /**
     * 相关题目ID列表
     */
    private List<String> relatedQuestions;
    
    /**
     * 创建时间
     */
    private String createdAt;
    
    /**
     * 是否激活状态
     */
    private Boolean isActive;
}