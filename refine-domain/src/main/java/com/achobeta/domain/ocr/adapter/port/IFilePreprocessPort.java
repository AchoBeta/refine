package com.achobeta.domain.ocr.adapter.port;

import java.io.IOException;

/**
 * @Auth : Malog
 * @Desc : 文件预处理端口（由基础设施实现）
 * @Time : 2025/10/31
 */
public interface IFilePreprocessPort {

    byte[] convertPdfToFirstImage(byte[] pdfBytes) throws IOException;

    byte[] extractFirstImageOrText(byte[] docxBytes) throws IOException;
}



