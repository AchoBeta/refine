package com.achobeta.infrastructure.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Auth : Malog
 * @Desc : 百度OCR RPC
 * @Time : 2025/10/31
 */
@Component
@ConfigurationProperties(prefix = "baidu.ocr")
public class BaiduOcrRPC {

    // setters for @ConfigurationProperties
    @Setter
    private boolean enabled = false;
    @Setter
    private String apiKey;
    @Setter
    private String secretKey;
    @Setter
    private String accessTokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
    @Setter
    private String ocrUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile String accessToken;
    private volatile long tokenExpireTime = 0;

    /**
     * 识别图片并返回 JSON 字符串
     * 字段约定：
     * - provider: "baidu"
     * - version: 使用的 API 版本
     * - text: 解析出的纯文本（尽力而为）
     * - raw: 第三方原始返回字符串
     */
    public String recognizeImage(byte[] imageBytes) {
        if (!enabled) {
            return toJson(skeletonJson("Baidu OCR disabled", null, ""));
        }
        if (isBlank(apiKey) || isBlank(secretKey)) {
            return toJson(skeletonJson("Baidu OCR call failed: API Key/Secret Key is empty", null, ""));
        }
        try {
            // 确保有有效的访问令牌
            String token = getAccessToken();
            if (isBlank(token)) {
                return toJson(skeletonJson("Baidu OCR call failed: Failed to get access token", null, ""));
            }

            // 准备请求参数
            String imageBase64 = Base64.encodeBase64String(imageBytes);
            String params = "image=" + URLEncoder.encode(imageBase64, "UTF-8");

            // 发送OCR请求
            URL url = new URL(ocrUrl + "?access_token=" + token);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            String raw = response.toString();
            
            // 从百度OCR响应中提取文本
            String extractedText = extractTextFromBaiduResponse(raw);
            
            Map<String, Object> data = skeletonJson(extractedText, raw, "v1");
            return toJson(data);
        } catch (Exception e) {
            return toJson(skeletonJson("Baidu OCR call failed: " + e.getMessage(), null, ""));
        }
    }

    /**
     * 从百度OCR响应中提取文本
     */
    private String extractTextFromBaiduResponse(String jsonResponse) {
        try {
            StringBuilder text = new StringBuilder();
            Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
            
            if (response.containsKey("words_result")) {
                Object wordsResult = response.get("words_result");
                if (wordsResult instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> words = (java.util.List<Map<String, Object>>) wordsResult;
                    for (Map<String, Object> word : words) {
                        if (word.containsKey("words")) {
                            text.append(word.get("words")).append("\n");
                        }
                    }
                }
            }
            
            return text.toString().trim();
        } catch (Exception e) {
            return "Error extracting text: " + e.getMessage();
        }
    }

    /**
     * 获取百度API访问令牌
     */
    private synchronized String getAccessToken() {
        // 检查现有令牌是否有效
        long currentTime = System.currentTimeMillis();
        if (accessToken != null && tokenExpireTime > currentTime) {
            return accessToken;
        }

        try {
            // 准备获取令牌的请求参数
            String params = "grant_type=client_credentials" +
                    "&client_id=" + apiKey +
                    "&client_secret=" + secretKey;

            // 发送请求获取令牌
            URL url = new URL(accessTokenUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // 解析响应获取令牌
            Map<String, Object> tokenResponse = objectMapper.readValue(response.toString(), Map.class);
            if (tokenResponse.containsKey("access_token")) {
                accessToken = (String) tokenResponse.get("access_token");
                // 设置令牌过期时间（百度令牌通常有效期为30天，这里设置为29天以确保安全）
                int expiresIn = tokenResponse.containsKey("expires_in") ? 
                        ((Number) tokenResponse.get("expires_in")).intValue() : 2592000; // 默认30天
                tokenExpireTime = currentTime + (expiresIn - 86400) * 1000L; // 提前一天过期
                return accessToken;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> skeletonJson(String text, String raw, String version) {
        Map<String, Object> map = new HashMap<>();
        map.put("provider", "baidu");
        map.put("version", version);
        map.put("text", text == null ? "" : text.trim());
        map.put("raw", raw);
        return map;
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            // 回退为简单字符串
            return "{\"provider\":\"baidu\",\"text\":\"" + (map.get("text") == null ? "" : map.get("text").toString().replace("\"", "'")) + "\"}";
        }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}