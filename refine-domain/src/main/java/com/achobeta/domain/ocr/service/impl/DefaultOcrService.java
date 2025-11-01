package com.achobeta.domain.ocr.service.impl;

import com.achobeta.domain.ocr.adapter.port.IFilePreprocessPort;
import com.achobeta.domain.ocr.adapter.port.IOcrPort;
import com.achobeta.domain.ocr.service.IOcrService;
import com.achobeta.domain.ocr.service.QuestionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @Auth : Malog
 * @Desc : 默认 OCR 识别服务实现
 * @Time : 2025/10/31
 */
@Service
@RequiredArgsConstructor
public class DefaultOcrService implements IOcrService {

    private final IFilePreprocessPort filePreprocessPort;
    private final IOcrPort ocrPort;

    /**
     * 抽取第一个问题
     *
     * @param fileBytes 文件字节数组
     * @param fileType  文件类型
     * @return 第一个问题
     */
    @Override
    public QuestionItem extractQuestionContent(byte[] fileBytes, String fileType) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("fileBytes is empty");
        }
        if (fileType == null || fileType.isEmpty()) {
            throw new IllegalArgumentException("fileType is empty");
        }

        String lowerType = fileType.toLowerCase();
        String recognizedText;
        try {
            // 处理 PDF 文件：转换为第一张图片并进行 OCR 识别
            if (lowerType.endsWith(".pdf") || "pdf".equals(lowerType)) {
                byte[] image = filePreprocessPort.convertPdfToFirstImage(fileBytes);
                recognizedText = ocrPort.recognizeImage(image);
            }
            // 处理 DOCX 文件：尝试从中提取图像或文本内容
            else if (lowerType.endsWith(".docx") || "docx".equals(lowerType)) {
                try {
                    byte[] data = filePreprocessPort.extractFirstImageOrText(fileBytes);
                    // 判断是否可能是文本（尽力而为：若可被 UTF-8 解码且包含换行/字母，视作文本）
                    String asText = new String(data, StandardCharsets.UTF_8);
                    boolean looksLikeText = asText.chars().anyMatch(ch -> ch == '\n' || Character.isLetterOrDigit(ch));
                    if (looksLikeText) {
                        recognizedText = asText;
                    } else {
                        recognizedText = ocrPort.recognizeImage(data);
                    }
                } catch (Exception parseDocxEx) {
                    // 非有效 DOCX 或解析失败，退化为图片 OCR 尝试
                    recognizedText = ocrPort.recognizeImage(fileBytes);
                }
            }
            // 处理常见图片格式：直接使用 OCR 进行识别
            else if (lowerType.endsWith(".png") || lowerType.endsWith(".jpg") || lowerType.endsWith(".jpeg")
                    || "png".equals(lowerType) || "jpg".equals(lowerType) || "jpeg".equals(lowerType)) {
                recognizedText = ocrPort.recognizeImage(fileBytes);
            }
            // 处理纯文本文件：直接按 UTF-8 编码读取文本内容
            else if (lowerType.endsWith(".txt") || "txt".equals(lowerType)) {
                recognizedText = new String(fileBytes, StandardCharsets.UTF_8);
            } else {
                // 默认尝试当作图片识别
                recognizedText = ocrPort.recognizeImage(fileBytes);
            }
        } catch (Exception e) {
            throw new RuntimeException("OCR 处理失败: " + e.getMessage(), e);
        }

        // TODO 填充AI提取题干字段，AI生成答案和生成题目详细解析字段可暂时删除或滞后
        // 构造返回对象，当前只填充 questionText 字段，其余字段暂设为 null
        QuestionItem item = new QuestionItem();
        item.setQuestionText(recognizedText);
        item.setOptions(null);
        item.setAnswer(null);
        item.setAnalysis(null);
        return item;
    }
}


