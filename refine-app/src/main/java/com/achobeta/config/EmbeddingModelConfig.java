package com.achobeta.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌入模型配置类
 * 用于配置 LangChain4j 的嵌入模型
 */
@Configuration
public class EmbeddingModelConfig {

    @Value("${langchain4j.community.dashscope.embedding-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.community.dashscope.embedding-model.model-name}")
    private String modelName;

    /**
     * 创建 DashScope 嵌入模型 Bean
     * 
     * @return EmbeddingModel 实例
     */
    @Bean("qwenEmbeddingModel")
    public EmbeddingModel qwenEmbeddingModel() {
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }
}