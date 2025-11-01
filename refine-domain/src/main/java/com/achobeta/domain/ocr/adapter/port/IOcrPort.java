package com.achobeta.domain.ocr.adapter.port;

/**
 * @Auth : Malog
 * @Desc : OCR 识别外部能力端口（由基础设施实现调用阿里云 OCR 等）
 * @Time : 2025/10/31
 */
public interface IOcrPort {

    /**
     * 识别图片中的文字，返回纯文本结果
     * @param imageBytes 图片字节（如 PNG/JPG）
     * @return 识别出的文本
     */
    String recognizeImage(byte[] imageBytes);
}



