package com.achobeta.domain.question.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 错题实体（领域层，不含与业务无关的数据库字段）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MistakeQuestionEntity {

    private Long id;

    private String userId;

    /**
     * 错题ID
     */
    private String questionId;

    /**
     * 错题内容
     */
    private String questionContent;

    /**
     * 错题科目
     */
    private String subject;

    /**
     * 错误原因（后续识别后填充）
     */
    private Integer isCareless;
    private Integer isUnfamiliar;
    private Integer isCalcularErr;
    private Integer isTimeShortage;
    private String otherReason;

    /**
     * 知识点归属ID
     */
    private Integer knowledgePointId;

    /**
     * 学习笔记
     */
    private String studyNote;

    /**
     * 题目状态 0-未理解 1-已理解
     */
    private Integer questionStatus;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}