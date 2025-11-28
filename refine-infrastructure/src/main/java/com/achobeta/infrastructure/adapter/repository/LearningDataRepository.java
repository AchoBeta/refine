package com.achobeta.infrastructure.adapter.repository;

import com.achobeta.domain.rag.adapter.port.ILearningDataRepository;
import com.achobeta.domain.rag.model.entity.LearningVectorEntity;
import com.achobeta.domain.rag.model.valobj.LearningStatisticsVO;
import com.achobeta.infrastructure.dao.IVectorDao;
import com.achobeta.infrastructure.dao.po.LearningVector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auth : Malog
 * @Desc : 学习数据仓储实现
 * @Time : 2025/11/25
 */
@Slf4j
@Repository
public class LearningDataRepository implements ILearningDataRepository {

    @Autowired
    private IVectorDao vectorDao;

    @Override
    public List<LearningVectorEntity> getUserRecentLearningData(String userId, int days) {
        try {
            List<LearningVector> vectors = vectorDao.getUserRecentLearningData(userId, days);
            return vectors.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户最近学习数据失败，userId:{} days:{}", userId, days, e);
            return Collections.emptyList();
        }
    }

    @Override
    public LearningStatisticsVO getUserLearningStatistics(String userId, int days) {
        try {
            Map<String, Object> statistics = vectorDao.getUserLearningStatistics(userId, days);
            if (statistics == null || statistics.isEmpty()) {
                return null;
            }

            return LearningStatisticsVO.builder()
                    .totalActivities(getIntValue(statistics, "total_activities"))
                    .actionTypesCount(getIntValue(statistics, "action_types_count"))
                    .subjectsCount(getIntValue(statistics, "subjects_count"))
                    .activeDays(getIntValue(statistics, "active_days"))
                    .firstActivity(getTimestampValue(statistics, "first_activity"))
                    .lastActivity(getTimestampValue(statistics, "last_activity"))
                    .build();

        } catch (Exception e) {
            log.error("获取用户学习统计失败，userId:{} days:{}", userId, days, e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getUserLearningDataBySubject(String userId, int days) {
        try {
            return vectorDao.getUserLearningDataBySubject(userId, days);
        } catch (Exception e) {
            log.error("获取用户按科目分组的学习数据失败，userId:{} days:{}", userId, days, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getUserLearningDataByActionType(String userId, int days) {
        try {
            return vectorDao.getUserLearningDataByActionType(userId, days);
        } catch (Exception e) {
            log.error("获取用户按行为类型分组的学习数据失败，userId:{} days:{}", userId, days, e);
            return Collections.emptyList();
        }
    }

    /**
     * 转换为领域实体
     */
    private LearningVectorEntity convertToEntity(LearningVector po) {
        return LearningVectorEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .questionId(po.getQuestionId())
                .actionType(po.getActionType())
                .questionContent(po.getQuestionContent())
                .subject(po.getSubject())
                .knowledgePointId(po.getKnowledgePointId())
                .embedding(po.getEmbedding())
                .metadata(po.getMetadata())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    /**
     * 安全获取整数值
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 安全获取时间戳值
     */
    private java.time.LocalDateTime getTimestampValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        return null;
    }
}