package com.achobeta.domain.ocr.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Auth : Malog
 * @Desc : 封装题目信息
 * @Time : 2025/10/31 17:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionEntity implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 题目ID
     */
    private String questionId;

    /**
     * 题目文本内容
     */
    private String questionText;

}
