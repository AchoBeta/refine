package com.achobeta.infrastructure.ocr;

import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Auth : Malog
 * @Desc : 文件预处理
 * @Time : 2025/10/31 17:02
 */
@Component
public class FilePreprocessor {

    // PDF 转第一张图片（使用 PDFBox）
    public byte[] convertPdfToFirstImage(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            // 创建PDF渲染器并渲染第一页为图片
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(0); // 第一页
            // 将图片转换为字节数组输出流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }


    // DOCX 优先提取第一张内嵌图片；没有图片则提取全文文字（UTF-8）
    public static byte[] extractFirstImageOrText(byte[] docxBytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {

            /* 1. 先尝试拿第一张图片 */
            for (XWPFParagraph p : doc.getParagraphs()) {
                for (XWPFRun run : p.getRuns()) {
                    for (XWPFPicture pic : run.getEmbeddedPictures()) {
                        return pic.getPictureData().getData(); // 找到即返回
                    }
                }
            }

            /* 2. 没有图片，退而求其次：拼全文文字 */
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append(System.lineSeparator());
            }
            String text = sb.toString().trim();
            if (text.isEmpty()) {
                throw new IllegalArgumentException("DOCX 中既无图片也无文字");
            }
            return text.getBytes(StandardCharsets.UTF_8);
        }
    }
}
