package com.achobeta.infrastructure.adapter.port;

import com.achobeta.domain.ocr.adapter.port.IFilePreprocessPort;
import com.achobeta.infrastructure.ocr.FilePreprocessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Auth : Malog
 * @Desc : 文件预处理适配器
 * @Time : 2025/10/31 17:02
 */
@Component
@RequiredArgsConstructor
public class FilePreprocessPort implements IFilePreprocessPort {

    private final FilePreprocessor filePreprocessor;

    /**
     * 将PDF文件转换为第一张图片
     *
     * @param pdfBytes PDF文件的字节数组
     * @return 转换后的第一张图片的字节数组
     * @throws IOException 当文件处理过程中发生IO异常时抛出
     */
    @Override
    public byte[] convertPdfToFirstImage(byte[] pdfBytes) throws IOException {
        return filePreprocessor.convertPdfToFirstImage(pdfBytes);
    }

    /**
     * 从DOCX文件中提取第一张图片或文本内容
     *
     * @param docxBytes DOCX文件的字节数组
     * @return 提取的第一张图片或文本内容的字节数组
     * @throws IOException 当文件处理过程中发生IO异常时抛出
     */
    @Override
    public byte[] extractFirstImageOrText(byte[] docxBytes) throws IOException {
        return FilePreprocessor.extractFirstImageOrText(docxBytes);
    }

}



