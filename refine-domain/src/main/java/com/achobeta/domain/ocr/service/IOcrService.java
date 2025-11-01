package com.achobeta.domain.ocr.service;

/**
 * @Auth : Malog
 * @Desc : 从用户上传的文件中提取第一道题目
 * @Time : 2025/10/31 17:29
 */
public interface IOcrService {

    QuestionItem extractFirstQuestion(byte[] fileBytes, String fileType);

}
