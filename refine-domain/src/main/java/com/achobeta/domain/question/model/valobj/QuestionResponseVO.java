package com.achobeta.domain.question.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseVO {

    /**
     * redis中的键
     */
    private String questionId;

    // 题目内容(含选项)
    private String questionContent;

    // 答案
    private String answer;

    //解析
    private String analysis;


}