package com.achobeta.infrastructure.adapter.repository;

import com.achobeta.domain.rag.model.valobj.LearningInsightVO;
import com.achobeta.domain.rag.model.valobj.SimilarQuestionVO;
import com.achobeta.domain.rag.service.IVectorService;
import com.achobeta.infrastructure.dao.vector.IVectorDao;
import com.achobeta.infrastructure.dao.po.LearningVector;
import com.achobeta.infrastructure.gateway.DashScopeEmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.achobeta.types.enums.ActionType.MISTAKE;

/**
 * 向量数据库适配器实现
 */
@Slf4j
@Repository
public class VectorRepository implements IVectorService {

    @Autowired
    private IVectorDao vectorDao;

    @Autowired
    private DashScopeEmbeddingService embeddingService; // 使用DashScope嵌入服务

    public VectorRepository(DashScopeEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * 存储学习向量信息
     *
     * @param userId           用户ID
     * @param questionId       题目ID
     * @param questionContent  题目内容
     * @param actionType       操作类型
     * @param subject          科目
     * @param knowledgePointId 知识点ID
     * @return 存储成功返回true，失败返回false
     */
    @Override
    public boolean storeLearningVector(String userId, String questionId, String questionContent,
                                       String actionType, String subject, Integer knowledgePointId) {
        try {
            // 构建向量化文本
            String vectorText = buildVectorText(actionType, questionContent, subject);

            // 生成向量
            float[] embedding = embeddingService.generateEmbedding(vectorText);

            // 构建数据库实体
            LearningVector vector = LearningVector.builder()
                    .userId(userId)
                    .questionId(questionId)
                    .actionType(actionType)
                    .questionContent(questionContent)
                    .subject(subject)
                    .knowledgePointId(knowledgePointId)
                    .embedding(embedding)
                    .metadata(buildMetadata(actionType, subject, knowledgePointId))
                    .build();

            int result = vectorDao.insertLearningVector(vector);

            log.info("成功存储学习向量，userId:{} questionId:{} actionType:{}",
                    userId, questionId, actionType);
            return result > 0;

        } catch (Exception e) {
            log.error("存储学习向量失败，userId:{} questionId:{}", userId, questionId, e);
            return false;
        }
    }


    /**
     * 搜索与给定文本相似的题目。
     *
     * @param userId    用户ID，用于限定搜索范围
     * @param queryText 查询文本，将被转换为向量进行相似度匹配
     * @param limit     返回结果的最大数量限制
     * @return 相似的题目列表，封装为 SimilarQuestionVO 对象；若发生异常则返回空列表
     */
    @Override
    public List<SimilarQuestionVO> searchSimilarQuestions(String userId, String queryText, int limit) {
        try {
            // 生成查询向量
            float[] queryEmbedding = embeddingService.generateEmbedding(queryText);

            // 执行向量相似度搜索
            List<LearningVector> similarVectors = vectorDao.searchSimilarVectors(
                    userId, queryEmbedding, limit);

            // 转换为值对象
            return similarVectors.stream()
                    .map(this::convertToSimilarQuestionVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("搜索相似题目失败，userId:{} queryText:{}", userId, queryText, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户的薄弱知识点分析结果。
     *
     * @param userId 用户ID
     * @return 用户的薄弱点信息列表，封装为 LearningInsightVO 对象；若发生异常则返回空列表
     */
    @Override
    public List<LearningInsightVO> getUserWeaknesses(String userId) {
        try {
            // 分析用户错题模式
            List<LearningVector> userVectors = vectorDao.getUserVectorsByActionType(userId, MISTAKE.getActionType());

            // 使用聚类分析找出薄弱点
            return analyzeWeaknesses(userVectors);

        } catch (Exception e) {
            log.error("获取用户薄弱点失败，userId:{}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据用户的学习行为数据生成个性化推荐内容。
     *
     * @param userId 用户ID
     * @return 推荐内容列表，封装为 LearningInsightVO 对象；若发生异常则返回空列表
     */
    @Override
    public List<LearningInsightVO> getUserRecommendations(String userId) {
        try {
            // 获取用户所有学习向量
            List<LearningVector> allVectors = vectorDao.getUserAllVectors(userId);

            // 生成学习推荐
            return generateRecommendations(allVectors);

        } catch (Exception e) {
            log.error("获取用户学习推荐失败，userId:{}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 分析指定用户的学习行为模式，并将分析结果保存到数据库中。
     *
     * @param userId 用户ID
     */
    @Override
    public void analyzeUserLearningPatterns(String userId) {
        try {
            log.info("开始分析用户学习模式，userId:{}", userId);

            // 获取用户所有学习向量
            List<LearningVector> allVectors = vectorDao.getUserAllVectors(userId);

            if (allVectors.isEmpty()) {
                log.info("用户暂无学习数据，userId:{}", userId);
                return;
            }

            // 分析学习模式并生成洞察
            List<LearningInsightVO> insights = generateLearningInsights(allVectors);

            // 保存分析结果
            for (LearningInsightVO insight : insights) {
                vectorDao.saveLearningInsight(userId, insight);
            }
            log.info("用户学习模式分析完成，userId:{} 生成洞察数量:{}", userId, insights.size());

        } catch (Exception e) {
            log.error("分析用户学习模式失败，userId:{}", userId, e);
        }
    }


    /**
     * 构建向量化文本
     */
    private String buildVectorText(String actionType, String questionContent, String subject) {
        StringBuilder textBuilder = new StringBuilder();

        // 添加行为描述
        textBuilder.append("用户").append(getActionDescription(actionType));

        // 添加科目信息
        if (subject != null && !subject.trim().isEmpty()) {
            textBuilder.append("了一道").append(subject).append("题目：");
        } else {
            textBuilder.append("了一道题目：");
        }

        // 添加题目内容
        if (questionContent != null && !questionContent.trim().isEmpty()) {
            // 限制题目内容长度，避免向量化文本过长
            String content = questionContent.trim();
            if (content.length() > 500) {
                content = content.substring(0, 500) + "...";
            }
            textBuilder.append(content);
        }

        return textBuilder.toString();
    }

    /**
     * 获取行为类型描述
     */
    private String getActionDescription(String actionType) {
        switch (actionType) {
            case "upload":
                return "上传";
            case "review":
                return "复习";
            case "qa":
                return "问答";
            case "mistake":
                return "做错";
            default:
                return "学习";
        }
    }

    /**
     * 构建元数据
     */
    private Map<String, Object> buildMetadata(String actionType, String subject, Integer knowledgePointId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionType", actionType);
        if (subject != null) metadata.put("subject", subject);
        if (knowledgePointId != null) metadata.put("knowledgePointId", knowledgePointId);
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("embeddingModel", "dashscope-text-embedding-v1");
        return metadata;
    }

    /**
     * 转换为相似题目值对象
     */
    private SimilarQuestionVO convertToSimilarQuestionVO(LearningVector vector) {
        return SimilarQuestionVO.builder()
                .questionId(vector.getQuestionId())
                .questionContent(vector.getQuestionContent())
                .actionType(vector.getActionType())
                .subject(vector.getSubject())
                .similarity(vector.getSimilarity())
                .createdAt(vector.getCreatedAt() != null ? vector.getCreatedAt().toString() : null)
                .build();
    }

    /**
     * 分析薄弱点
     */
    private List<LearningInsightVO> analyzeWeaknesses(List<LearningVector> vectors) {
        List<LearningInsightVO> insights = new ArrayList<>();

        if (vectors.isEmpty()) {
            return insights;
        }

        // 按科目分组分析
        Map<String, List<LearningVector>> subjectGroups = vectors.stream()
                .filter(v -> v.getSubject() != null && !v.getSubject().trim().isEmpty())
                .collect(Collectors.groupingBy(LearningVector::getSubject));

        for (Map.Entry<String, List<LearningVector>> entry : subjectGroups.entrySet()) {
            String subject = entry.getKey();
            List<LearningVector> subjectVectors = entry.getValue();

            if (subjectVectors.size() >= 3) { // 至少3道错题才认为是薄弱点
                insights.add(LearningInsightVO.builder()
                        .type("weakness")
                        .title(subject + "学科薄弱")
                        .description(String.format("在%s学科中错误%d道题目，建议加强练习",
                                subject, subjectVectors.size()))
                        .confidenceScore(Math.min(0.9, 0.5 + subjectVectors.size() * 0.1))
                        .relatedQuestions(subjectVectors.stream()
                                .map(LearningVector::getQuestionId)
                                .collect(Collectors.toList()))
                        .createdAt(java.time.LocalDateTime.now())
                        .isActive(true)
                        .build());
            }
        }

        // 分析总体错题频率
        if (vectors.size() > 10) {
            insights.add(LearningInsightVO.builder()
                    .type("weakness")
                    .title("错题频率较高")
                    .description(String.format("总共有%d道错题，建议系统性复习基础知识", vectors.size()))
                    .confidenceScore(0.8)
                    .relatedQuestions(vectors.stream()
                            .limit(10)
                            .map(LearningVector::getQuestionId)
                            .collect(Collectors.toList()))
                    .createdAt(java.time.LocalDateTime.now())
                    .isActive(true)
                    .build());
        }

        return insights;
    }

    /**
     * 生成学习推荐
     */
    private List<LearningInsightVO> generateRecommendations(List<LearningVector> vectors) {
        List<LearningInsightVO> recommendations = new ArrayList<>();

        if (vectors.isEmpty()) {
            recommendations.add(LearningInsightVO.builder()
                    .type("recommendation")
                    .title("开始学习之旅")
                    .description("还没有学习记录，建议开始上传题目进行学习")
                    .confidenceScore(1.0)
                    .createdAt(java.time.LocalDateTime.now())
                    .isActive(true)
                    .build());
            return recommendations;
        }

        // 分析学习频率
        long recentWeekCount = vectors.stream()
                .filter(v -> v.getCreatedAt() != null &&
                        v.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusWeeks(1)))
                .count();

        if (recentWeekCount > 10) {
            recommendations.add(LearningInsightVO.builder()
                    .type("recommendation")
                    .title("保持学习节奏")
                    .description("近一周学习活跃，建议继续保持良好的学习习惯")
                    .confidenceScore(0.9)
                    .createdAt(java.time.LocalDateTime.now())
                    .isActive(true)
                    .build());
        } else if (recentWeekCount < 3) {
            recommendations.add(LearningInsightVO.builder()
                    .type("recommendation")
                    .title("增加学习频率")
                    .description("建议增加学习时间，每天至少学习30分钟")
                    .confidenceScore(0.8)
                    .createdAt(java.time.LocalDateTime.now())
                    .isActive(true)
                    .build());
        }

        // 分析科目分布
        Map<String, Long> subjectCounts = vectors.stream()
                .filter(v -> v.getSubject() != null && !v.getSubject().trim().isEmpty())
                .collect(Collectors.groupingBy(LearningVector::getSubject, Collectors.counting()));

        if (subjectCounts.size() == 1) {
            String subject = subjectCounts.keySet().iterator().next();
            recommendations.add(LearningInsightVO.builder()
                    .type("recommendation")
                    .title("扩展学习科目")
                    .description(String.format("目前主要学习%s，建议尝试其他科目的学习", subject))
                    .confidenceScore(0.7)
                    .createdAt(java.time.LocalDateTime.now())
                    .isActive(true)
                    .build());
        }

        return recommendations;
    }

    /**
     * 生成学习洞察
     */
    private List<LearningInsightVO> generateLearningInsights(List<LearningVector> vectors) {
        List<LearningInsightVO> insights = new ArrayList<>();

        // 分析学习频率
        long recentWeekCount = vectors.stream()
                .filter(v -> v.getCreatedAt() != null &&
                        v.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusWeeks(1)))
                .count();

        if (recentWeekCount > 15) {
            insights.add(LearningInsightVO.builder()
                    .type("strength")
                    .title("学习积极性高")
                    .description("近一周学习非常活跃，保持良好的学习习惯")
                    .confidenceScore(0.9)
                    .createdAt(java.time.LocalDateTime.now())
                    .isActive(true)
                    .build());
        }

        // 分析学习连续性
        long activeDays = vectors.stream()
                .filter(v -> v.getCreatedAt() != null &&
                        v.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusWeeks(1)))
                .map(v -> v.getCreatedAt().toLocalDate())
                .distinct()
                .count();

        if (activeDays >= 5) {
            insights.add(LearningInsightVO.builder()
                    .type("achievement")
                    .title("学习坚持性优秀")
                    .description(String.format("近一周有%d天进行了学习，坚持性很好", activeDays))
                    .confidenceScore(0.8)
                    .createdAt(java.time.LocalDateTime.now())
                    .isActive(true)
                    .build());
        }

        return insights;
    }
}