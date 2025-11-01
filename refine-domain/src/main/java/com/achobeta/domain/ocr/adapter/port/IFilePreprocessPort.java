package com.achobeta.domain.ocr.adapter.port;

import java.io.IOException;

/**
 * @Auth : Malog
 * @Desc : 文件预处理端口（由基础设施实现）
 * @Time : 2025/10/31
 */
public interface IFilePreprocessPort {

    /**
     * 将 PDF 文件转换为第一页的图片
     * @param pdfBytes PDF 文件字节数组
     * @return 第一页图片的字节数组
     * @throws IOException 转换过程中发生的异常
     */
    byte[] convertPdfToFirstImage(byte[] pdfBytes) throws IOException;

    /**
     * 从 DOCX 文件中提取第一页的图片或文本
     * @param docxBytes DOCX 文件字节数组
     * @return 第一页图片或文本的字节数组
     * @throws IOException 提取过程中发生的异常
     */
    byte[] extractFirstImageOrText(byte[] docxBytes) throws IOException;
}



