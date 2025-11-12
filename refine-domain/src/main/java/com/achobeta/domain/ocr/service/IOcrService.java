package com.achobeta.domain.ocr.service;

import com.achobeta.domain.ocr.model.entity.QuestionEntity;

/**
 * @Auth : Malog
 * @Desc : 从用户上传的文件中提取第一道题目
 * @Time : 2025/10/31 17:29
 */
public interface IOcrService {

    /**
     * 从用户上传的文件中提取第一道题目
     *
     * @param fileBytes 文件字节数组
     * @param fileType  文件类型
     * @return 第一道题目信息
     */
    QuestionEntity extractQuestionContent(byte[] fileBytes, String fileType);

}
