package com.achobeta.domain.ocr.service.impl;

import cn.hutool.core.lang.UUID;
import com.achobeta.domain.ai.service.IAiService;
import com.achobeta.domain.ocr.adapter.port.IFilePreprocessPort;
import com.achobeta.domain.ocr.adapter.port.IOcrPort;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import com.achobeta.domain.ocr.service.IOcrService;
import com.achobeta.domain.rag.service.IVectorService;
import com.achobeta.types.enums.ActionType;
import com.achobeta.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Auth : Malog
 * @Desc : OCR 服务实现结合ai提取识别到的第一道题目
 * @Time : 2025/10/31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultOcrService implements IOcrService {

    private final IFilePreprocessPort filePreprocessPort;
    private final IOcrPort ocrPort;
    private final IAiService aiService;
    private final IVectorService vectorService;

    /**
     * 抽取第一个问题
     *
     * @param userId    用户ID
     * @param fileBytes 文件字节数组
     * @param fileType  文件类型
     * @return 第一个问题
     */
    @Override
    public QuestionEntity extractQuestionContent(String userId, byte[] fileBytes, String fileType) {
        if (fileBytes == null || fileBytes.length == 0) {
            log.error("文件内容：{}", fileBytes);
            throw new AppException("文件为空");
        }
        if (fileType == null || fileType.isEmpty()) {
            log.error("文件类型：{}", fileType);
            throw new AppException("文件类型为空");
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
        } catch (IOException e) {
            log.error("文件处理IO异常，文件类型: {}", fileType, e);
            throw new AppException("文件读取失败，请检查文件是否损坏");
        } catch (IllegalArgumentException e) {
            log.error("文件格式参数异常，文件类型: {}", fileType, e);
            throw new AppException("不支持的文件格式，请上传PDF、DOCX、图片或TXT文件");
        } catch (Exception e) {
            log.error("OCR处理未知异常，文件类型: {}", fileType, e);
            throw new AppException("文件识别失败，请稍后重试或联系客服");
        }

        // 使用 AI 模型尝试提取第一个问题
        recognizedText = aiService.extractTheFirstQuestion(recognizedText);
        String uuid = UUID.fastUUID().toString();

        // 创建问题实体
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setQuestionText(recognizedText);
        questionEntity.setQuestionId(uuid);
        questionEntity.setUserId(userId);

        // 存储学习行为向量到向量数据库
        try {
            // 调用向量服务存储学习向量，行为类型为"upload"表示用户上传题目
            boolean vectorStored = vectorService.storeLearningVector(
                    userId,
                    uuid,
                    recognizedText,
                    ActionType.UPLOAD.getActionType(),
                    null,
                    null
            );

            if (vectorStored) {
                log.info("成功存储题目向量到向量数据库，userId:{} questionId:{}", userId, uuid);
            } else {
                log.warn("存储题目向量到向量数据库失败，但不影响OCR主流程，userId:{} questionId:{}", userId, uuid);
            }
        } catch (Exception e) {
            // 向量存储失败不应该影响OCR主流程，只记录日志
            log.error("存储题目向量到向量数据库时发生异常，userId:{} questionId:{}", userId, uuid, e);
        }

        // TODO 将题干存储到Redis中，通过uuid可以查询，设置24小时过期时间
//        String redisKey = "ocr:question:" + uuid;
//        redissonService.setValue(redisKey, recognizedText, 24 * 60 * 60 * 1000L); // 24小时过期，单位毫秒

        return questionEntity;
    }
}


