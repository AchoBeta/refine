package com.achobeta.domain.question.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MistakeQuestionVO {

    // 用户id
    private String userId;
    // redis键
    private String questionId;
    // 识别出的题目内容（如果有选项则包含选项）
    private String questionContent;
    // 题目答案
    private String answer;
    // 解析
    private String analysis;
    // 科目
    private String subject;
    // 知识点id
    private Integer knowledgePointId;

}
