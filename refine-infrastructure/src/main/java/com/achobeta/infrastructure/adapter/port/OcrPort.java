package com.achobeta.infrastructure.adapter.port;

import com.achobeta.domain.ocr.adapter.port.IOcrPort;
import com.achobeta.infrastructure.gateway.BaiduOcrRPC;
import com.achobeta.infrastructure.gateway.OcrRPC;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Auth : Malog
 * @Desc : OCR 适配器
 * @Time : 2025/10/31 17:02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcrPort implements IOcrPort {

    private final OcrRPC ocrRPC;
    private final BaiduOcrRPC baiduOcrRPC;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ocr.provider:baidu}")
    private String ocrProvider;

    /**
     * 调用OCR服务并返回纯文本
     * @param imageBytes 图片字节数组
     * @return 纯文本
     */
    @Override
    public String recognizeImage(byte[] imageBytes) {
        // 根据配置决定使用哪个OCR服务提供商
        String json = "baidu".equalsIgnoreCase(ocrProvider) 
                ? baiduOcrRPC.recognizeImage(imageBytes)
                : ocrRPC.recognizeImage(imageBytes);
        
        log.info("OCR服务提供商: {}, 返回JSON: {}", ocrProvider, json);
                
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode textNode = node.get("text");
            String text = textNode != null ? textNode.asText("") : "";
            log.info("OCR识别文本: {}", text);
            return text;
        } catch (Exception e) {
            log.error("解析OCR返回JSON失败: {}", json, e);
            return "";
        }
    }
}


