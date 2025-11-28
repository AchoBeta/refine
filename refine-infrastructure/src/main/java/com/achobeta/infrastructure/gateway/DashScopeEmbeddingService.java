package com.achobeta.infrastructure.gateway;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @Auth : Malog
 * @Desc : 阿里云DashScope嵌入服务
 * @Time : 2025/11/25
 */
@Slf4j
@Service
public class DashScopeEmbeddingService {

    @Value("${dashscope.apiKey}")
    private String apiKey;

    private static final String EMBEDDING_MODEL = "text-embedding-v1";
    private static final int VECTOR_DIMENSION = 384;

    /**
     * 生成文本嵌入向量
     */
    public float[] generateEmbedding(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                log.warn("输入文本为空，返回零向量");
                return new float[VECTOR_DIMENSION];
            }

            TextEmbedding textEmbedding = new TextEmbedding();

            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .apiKey(apiKey)
                    .model(EMBEDDING_MODEL)
                    .texts(List.of(text.trim()))
                    .build();

            TextEmbeddingResult result = textEmbedding.call(param);

            if (result != null && result.getOutput() != null && !result.getOutput().getEmbeddings().isEmpty()) {
                List<Double> embedding = result.getOutput().getEmbeddings().get(0).getEmbedding();

                // 转换为float数组
                float[] vector = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    vector[i] = embedding.get(i).floatValue();
                }

                log.debug("成功生成嵌入向量，文本长度:{} 向量维度:{}", text.length(), vector.length);
                return vector;
            } else {
                log.error("嵌入向量生成失败，返回结果为空");
                return generateFallbackEmbedding(text);
            }

        } catch (ApiException e) {
            log.error("调用DashScope嵌入API失败，API错误: {}", e.getMessage());
            return generateFallbackEmbedding(text);
        } catch (NoApiKeyException e) {
            log.error("DashScope API Key未配置或无效: {}", e.getMessage());
            return generateFallbackEmbedding(text);
        } catch (Exception e) {
            log.error("生成嵌入向量时发生未知错误", e);
            return generateFallbackEmbedding(text);
        }
    }

    /**
     * 备用的嵌入向量生成方法
     * 当DashScope API调用失败时使用
     */
    private float[] generateFallbackEmbedding(String text) {
        log.warn("使用备用方法生成嵌入向量，文本: {}", text != null ? text.substring(0, Math.min(text.length(), 50)) + "..." : "null");

        float[] vector = new float[VECTOR_DIMENSION];

        if (text == null || text.trim().isEmpty()) {
            return vector; // 返回零向量
        }

        // 基于文本内容生成确定性向量
        // 使用多个哈希函数来增加向量的质量
        String normalizedText = text.trim().toLowerCase();

        // 使用文本的多个特征来生成向量
        int hash1 = normalizedText.hashCode();
        int hash2 = normalizedText.length();
        int hash3 = normalizedText.chars().sum();

        // 组合多个种子
        long seed = ((long) hash1 << 32) | (hash2 ^ hash3);
        Random random = new Random(seed);

        // 生成向量
        for (int i = 0; i < VECTOR_DIMENSION; i++) {
            vector[i] = (float) (random.nextGaussian() * 0.1f); // 使用高斯分布，标准差0.1
        }

        // 归一化向量
        normalizeVector(vector);

        return vector;
    }

    /**
     * 将向量归一化为单位向量（L2）
     *
     * @param vector 待归一化的浮点数向量数组
     */
    private void normalizeVector(float[] vector) {
        // 计算向量的模长（欧几里得范数）
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        // 避免除零错误，当模长大于0时进行归一化处理
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

    }
}