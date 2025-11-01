package com.achobeta.infrastructure.adapter.port;

import com.achobeta.domain.ocr.adapter.port.IOcrPort;
import com.achobeta.infrastructure.gateway.BaiduOcrRPC;
import com.achobeta.infrastructure.gateway.OcrRPC;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OcrPort implements IOcrPort {

    private final OcrRPC ocrRPC;
    private final BaiduOcrRPC baiduOcrRPC;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ocr.provider:baidu}")
    private String ocrProvider;

    public OcrPort(OcrRPC ocrRPC, BaiduOcrRPC baiduOcrRPC) {
        this.ocrRPC = ocrRPC;
        this.baiduOcrRPC = baiduOcrRPC;
    }

    @Override
    public String recognizeImage(byte[] imageBytes) {
        // 根据配置决定使用哪个OCR服务提供商
        String json = "baidu".equalsIgnoreCase(ocrProvider) 
                ? baiduOcrRPC.recognizeImage(imageBytes)
                : ocrRPC.recognizeImage(imageBytes);
                
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode textNode = node.get("text");
            return textNode != null ? textNode.asText("") : "";
        } catch (Exception e) {
            return "";
        }
    }
}


