package com.achobeta.infrastructure.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
 * @Auth : Malog
 * @Desc : 百度OCR RPC
 * @Time : 2025/10/31
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "baidu.ocr")
public class BaiduOcrRPC {

    /**
     * 是否启用OCR功能
     */
    @Setter
    private boolean enabled = false;

    /**
     * API密钥
     */
    @Setter
    private String apiKey;

    /**
     * 密钥
     */
    @Setter
    private String secretKey;

    /**
     * 访问令牌URL地址
     */
    @Setter
    private String accessTokenUrl = "https://aip.baidubce.com/oauth/2.0/token";

    /**
     * OCR识别服务URL地址
     */
    @Setter
    private String ocrUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";

    /**
     * JSON对象映射器，用于序列化和反序列化JSON数据
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 访问令牌，用于API认证授权
     */
    private volatile String accessToken;

    /**
     * 令牌过期时间，表示accessToken的有效截止时间戳
     */
    private volatile long tokenExpireTime = 0;


    /**
     * 识别图片并返回 JSON 字符串
     * 字段约定：
     * - provider: "baidu"
     * - version: 使用的 API 版本
     * - text: 解析出的纯文本
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
            // 获取百度OCR的访问令牌，用于后续API调用的身份验证
            String token = getAccessToken();
            if (isBlank(token)) {
                return toJson(skeletonJson("Baidu OCR call failed: Failed to get access token", null, ""));
            }

            // 将图像字节数据编码为Base64字符串，并构造POST请求参数
            String imageBase64 = Base64.encodeBase64String(imageBytes);
            String params = "image=" + URLEncoder.encode(imageBase64, StandardCharsets.UTF_8);

            // 构造OCR请求URL并打开HTTP连接，设置请求方法和内容类型
            URL url = new URL(ocrUrl + "?access_token=" + token);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // 向服务器发送图像数据
            try (OutputStream os = connection.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            // 读取服务器返回的响应数据
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            String raw = response.toString();

            // 解析百度OCR响应，提取识别出的文本内容
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
                log.info("获取百度API访问令牌失败，响应内容：" + response);
                return null;
            }
        } catch (Exception e) {
            log.info("获取百度API访问令牌失败，异常信息：" + e.getMessage());
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

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}