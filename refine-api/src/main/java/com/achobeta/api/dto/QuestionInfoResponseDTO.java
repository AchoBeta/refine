package com.achobeta.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @Auth : Malog
 * @Desc :
 * @Time : 2025/11/5 21:05
 */
@Data
@Builder
public class QuestionInfoResponseDTO {

    /**
     * 题目文本内容
     */
    private String questionText;

    /**
     * 题目ID
     */
    private String questionId;

    /**
     * 用户ID
     */
    private String userId;

}
