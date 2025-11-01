package com.achobeta.domain.ocr.service;

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
public class QuestionItem implements Serializable {

    /**
     * 题目文本内容
     */
    private String questionText;
    /**
     * 题目选项内容
     */
    private String options;
    /**
     * 题目正确答案
     */
    private String answer;
    /**
     * 题目解析说明
     */
    private String analysis;


}
