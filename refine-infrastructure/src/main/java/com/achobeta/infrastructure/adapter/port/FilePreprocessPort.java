package com.achobeta.infrastructure.adapter.port;

import com.achobeta.domain.ocr.adapter.port.IFilePreprocessPort;
import com.achobeta.infrastructure.ocr.FilePreprocessor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FilePreprocessPort implements IFilePreprocessPort {

    private final FilePreprocessor filePreprocessor;

    public FilePreprocessPort(FilePreprocessor filePreprocessor) {
        this.filePreprocessor = filePreprocessor;
    }

    @Override
    public byte[] convertPdfToFirstImage(byte[] pdfBytes) throws IOException {
        return filePreprocessor.convertPdfToFirstImage(pdfBytes);
    }

    @Override
    public byte[] extractFirstImageOrText(byte[] docxBytes) throws IOException {
        return FilePreprocessor.extractFirstImageOrText(docxBytes);
    }
}



