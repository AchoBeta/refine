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

    @Override
    public byte[] convertPdfToFirstImage(byte[] pdfBytes) throws IOException {
        return filePreprocessor.convertPdfToFirstImage(pdfBytes);
    }

    @Override
    public byte[] extractFirstImageOrText(byte[] docxBytes) throws IOException {
        return FilePreprocessor.extractFirstImageOrText(docxBytes);
    }
}



