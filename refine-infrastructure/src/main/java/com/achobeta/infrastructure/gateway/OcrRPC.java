package com.achobeta.infrastructure.gateway;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OCR 网关：调用阿里云 OCR 并统一返回 JSON 字符串
 */
@Component
@ConfigurationProperties(prefix = "aliyun.ocr")
@Deprecated
public class OcrRPC {

    // setters for @ConfigurationProperties
    @Setter
    private boolean enabled = false;
    @Setter
    private String accessKeyId;
    @Setter
    private String accessKeySecret;
    @Setter
    private String regionId = "cn-shanghai";
    @Setter
    private String endpoint; // 可选：自定义域名

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile IAcsClient client;

    /**
     * 识别图片并返回 JSON 字符串
     * 字段约定：
     * - provider: "aliyun"
     * - version: 使用的 OpenAPI 版本（占位）
     * - text: 解析出的纯文本（尽力而为）
     * - raw: 第三方原始返回字符串
     */
    public String recognizeImage(byte[] imageBytes) {
        if (!enabled) {
            return toJson(skeletonJson("Aliyun OCR disabled", null, ""));
        }
        if (isBlank(accessKeyId) || isBlank(accessKeySecret)) {
            return toJson(skeletonJson("Aliyun OCR call failed: AccessKeyId/Secret is empty", null, ""));
        }
        try {
            IAcsClient c = getClient();

            String imageBase64 = Base64.encodeBase64String(imageBytes);

            CommonRequest request = new CommonRequest();
            request.setSysProtocol(ProtocolType.HTTPS);
            request.setSysMethod(MethodType.POST);
            request.setSysDomain(endpoint != null && !endpoint.isEmpty()
                    ? endpoint
                    : "ocr-api.cn-hangzhou.aliyuncs.com");
            request.setSysVersion("2021-07-07");
            request.setSysAction("RecognizeEduPaper");
            // 新版 OCR API 采用 Body(JSON) 形式传参
            Map<String, Object> body = new HashMap<>();
            body.put("imageData", imageBase64);
            request.putBodyParameter("Body", toJson(body));

            CommonResponse response = c.getCommonResponse(request);
            String raw = response.getData();

            Map<String, Object> data = skeletonJson(raw, raw, "2021-07-07");
            return toJson(data);
        } catch (Exception e) {
            return toJson(skeletonJson("Aliyun OCR call failed: " + e.getMessage(), null, ""));
        }
    }

    private IAcsClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
                    try {
                        if (endpoint != null && !endpoint.isEmpty()) {
                            DefaultProfile.addEndpoint(regionId, "ocr-api", endpoint);
                        } else {
                            DefaultProfile.addEndpoint(regionId, "ocr-api", "ocr-api.cn-hangzhou.aliyuncs.com");
                        }
                    } catch (Exception ignore) {
                    }
                    client = new DefaultAcsClient(profile);
                }
            }
        }
        return client;
    }

    private Map<String, Object> skeletonJson(String text, String raw, String version) {
        Map<String, Object> map = new HashMap<>();
        map.put("provider", "aliyun");
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
            return "{\"provider\":\"aliyun\",\"text\":\"" + (map.get("text") == null ? "" : map.get("text").toString().replace("\"", "'")) + "\"}";
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}


