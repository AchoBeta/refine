package com.achobeta.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@EnableConfigurationProperties(RagConfigProperties.class)
public class RagConfig {

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private RagConfigProperties ragConfigProperties;

    private EmbeddingStore<TextSegment> embeddingStore;

    @PostConstruct
    public void initialize() {
        this.embeddingStore = PgVectorEmbeddingStore.builder()
                .host(ragConfigProperties.getHost())
                .port(ragConfigProperties.getPort())
                .database(ragConfigProperties.getDatabase())
                .user(ragConfigProperties.getUser())
                .password(ragConfigProperties.getPassword())
                .table("knowledge_embeddings")
                .dimension(qwenEmbeddingModel.dimension())
                .build();
        log.info("EmbeddingStore初始化完成");
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return this.embeddingStore;
    }

    @Bean
    public ContentRetriever contentRetriever() {
        // RAG
        // 加载文档
        String docPath = "refine-app/src/main/resources/docs";
        File docDir = new File(docPath);

        // 检查目录是否存在以及是否有文件
        if (docDir.exists() && docDir.isDirectory()) {
            // 获取目录下的文件（排除子目录，只考虑文件）
            File[] files = docDir.listFiles(File::isFile);
            if (files != null && files.length > 0) {
                // 先清空向量表的所有旧数据
                truncateEmbeddingTable(); // 全删逻辑（替换原来的按文件名删）

                // 加载文档并重新入库
                List<Document> documents = FileSystemDocumentLoader.loadDocuments(docPath);
                log.info("待录入文档总数：{} 个，开始分段并生成向量", documents.size());


                // 文档分段
                DocumentByParagraphSplitter documentSplitter = new DocumentByParagraphSplitter(800, 350);
                // 自定义文档加载器，把文档转换成向量并存储到向量数据库中
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .documentSplitter(documentSplitter)
                        .textSegmentTransformer(textSegment -> TextSegment.from(
                                textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                                textSegment.metadata()
                        ))
                        .embeddingModel(qwenEmbeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();
                // 加载文档
                ingestor.ingest(documents);
                log.info("文档全量入库完成! 共加载 {} 个文件，已清空旧数据，当前表中为最新全量数据", files.length);
            } else {
                log.info("文档目录 {} 下无可用文件，跳过文档加载（表中数据保持不变）", docPath);
            }
        } else {
            log.warn("文档目录 {} 不存在或不是目录，跳过文档加载", docPath);
        }

        // 自定义内容加载器
        return new ContentRetriever() {
            @Override
            public List<Content> retrieve(Query query) {
                // 1. 从查询元数据中动态提取业务传入的学科（target）
                Object obj = query.metadata().invocationContext().chatMemoryId();
                if (null == obj) {
                    log.warn("RAG检索 - 未传递 file_name 筛选条件，返回空结果");
                    return List.of();
                }
                String target = String.valueOf(obj);
                log.info("RAG检索 - 动态过滤 file_name（模糊匹配）: {}", target);

                // 2. 生成查询向量
                Embedding embeddedQuery = qwenEmbeddingModel.embed(query.text()).content();

                // 3. 用自定义的Filter构建动态过滤条件
                //Filter filter = MetadataFilterBuilder.metadataKey("subject").isEqualTo(subject);

                //模糊匹配，自定义的Filter构建动态过滤条件
                Filter filter = MetadataFilterBuilder.metadataKey("file_name").containsString(target);

                // 4. 执行带动态过滤的检索
                EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(embeddedQuery)
                        .maxResults(5) // 最多返回5条
                        .minScore(0.75) // 相关度阈值
                        .filter(filter) // 动态过滤条件
                        .build();

                EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

                // 5. 转换结果为Content列表
                return searchResult.matches().stream()
                        .map(match -> Content.from(match.embedded()))
                        .collect(Collectors.toList());
            }
        };
    }

    private void truncateEmbeddingTable() {
        // 方案1：TRUNCATE（推荐）- 快速清空表，不记录单行删除日志，效率极高
        String sql = String.format("TRUNCATE TABLE %s CASCADE", "knowledge_embeddings");
        // CASCADE：若表有外键关联，同时清空关联表（无外键可去掉CASCADE）

        // 方案2：DELETE（备选，无TRUNCATE权限时使用）
        // String sql = String.format("DELETE FROM %s", VECTOR_TABLE);

        try (
                // 复用配置类的连接信息，避免硬编码
                Connection conn = DriverManager.getConnection(
                        String.format("jdbc:postgresql://%s:%d/%s",
                                ragConfigProperties.getHost(),
                                ragConfigProperties.getPort(),
                                ragConfigProperties.getDatabase()),
                        ragConfigProperties.getUser(),
                        ragConfigProperties.getPassword()
                );
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.info("开始清空向量表：{}", "knowledge_embeddings");
            pstmt.executeUpdate();
            log.info("向量表 {} 清空成功！", "knowledge_embeddings");

        } catch (SQLException e) {
            // 若TRUNCATE失败（如无权限），自动降级为DELETE重试（可选逻辑）
            if (e.getMessage().contains("permission denied for table") || e.getMessage().contains("TRUNCATE")) {
                log.warn("TRUNCATE权限不足，尝试用DELETE清空表", e);
                deleteAllEmbeddingData(); // 调用DELETE全删方法
                return;
            }

            log.error("清空向量表 {} 失败", "knowledge_embeddings", e);
            throw new RuntimeException("清空旧数据异常，终止文档入库", e);
        }
    }

    /**
     * 备选方法：用DELETE清空全表（无TRUNCATE权限时触发）
     */
    private void deleteAllEmbeddingData() {
        String sql = String.format("DELETE FROM %s", "knowledge_embeddings");
        try (
                Connection conn = DriverManager.getConnection(
                        String.format("jdbc:postgresql://%s:%d/%s",
                                ragConfigProperties.getHost(),
                                ragConfigProperties.getPort(),
                                ragConfigProperties.getDatabase()),
                        ragConfigProperties.getUser(),
                        ragConfigProperties.getPassword()
                );
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            int deletedRows = pstmt.executeUpdate();
            log.info("用DELETE清空表 {} 成功，共删除 {} 条记录", "knowledge_embeddings", deletedRows);
        } catch (SQLException e) {
            log.error("DELETE清空表 {} 失败", "knowledge_embeddings", e);
            throw new RuntimeException("删除旧数据异常，终止文档入库", e);
        }
    }
}