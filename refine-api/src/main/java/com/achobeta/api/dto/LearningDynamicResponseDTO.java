package com.achobeta.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @Auth : Malog
 * @Desc : 学习动态响应DTO
 * @Time : 2025/11/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningDynamicResponseDTO {

    /**
     * 动态类型
     */
    private String type;

    /**
     * 动态标题
     */
    private String title;

    /**
     * 动态描述
     */
    private String description;

    /**
     * 相关科目
     */
    private String subject;

    /**
     * 重要程度
     */
    private Integer priority;

    /**
     * 建议行动
     */
    private String suggestion;

    /**
     * 相关题目数量
     */
    private Integer relatedQuestionCount;
}