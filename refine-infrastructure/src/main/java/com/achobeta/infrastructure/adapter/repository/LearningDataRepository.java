package com.achobeta.infrastructure.adapter.repository;

import com.achobeta.domain.rag.adapter.port.ILearningDataRepository;
import com.achobeta.domain.rag.model.entity.LearningVectorEntity;
import com.achobeta.domain.rag.model.valobj.LearningStatisticsVO;
import com.achobeta.infrastructure.dao.vector.IVectorDao;
import com.achobeta.infrastructure.dao.po.LearningVector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auth : Malog
 * @Desc : 学习数据仓储实现类
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
            log.info("获取用户最近{}天的学习向量数据，userId:{}, days:{}", days, userId);
            
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("用户ID为空，无法查询学习数据");
                return Collections.emptyList();
            }
            
            if (days <= 0) {
                log.warn("查询天数无效，days:{}", days);
                return Collections.emptyList();
            }
            
            // 从数据库查询用户最近N天的学习数据
            List<LearningVector> vectors = vectorDao.getUserRecentLearningData(userId, days);
            
            if (CollectionUtils.isEmpty(vectors)) {
                log.info("用户最近{}天无学习数据，userId:{}", days, userId);
                return Collections.emptyList();
            }
            
            // 转换为领域实体
            List<LearningVectorEntity> result = vectors.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
            
            log.info("成功获取用户学习向量数据，userId:{}, 数据量:{}", userId, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取用户学习向量数据失败，userId:{}, days:{}", userId, days, e);
            return Collections.emptyList();
        }
    }

    @Override
    public LearningStatisticsVO getUserLearningStatistics(String userId, int days) {
        try {
            log.info("获取用户最近{}天的学习统计数据，userId:{}, days:{}", days, userId);
            
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("用户ID为空，无法查询学习统计数据");
                return null;
            }
            
            if (days <= 0) {
                log.warn("查询天数无效，days:{}", days);
                return null;
            }
            
            // 从数据库查询统计数据
            Map<String, Object> statisticsMap = vectorDao.getUserLearningStatistics(userId, days);
            
            if (statisticsMap == null || statisticsMap.isEmpty()) {
                log.info("用户最近{}天无学习统计数据，userId:{}", days, userId);
                return LearningStatisticsVO.builder()
                        .totalActivities(0)
                        .subjectsCount(0)
                        .activeDays(0)
                        .actionTypesCount(0)
                        .build();
            }
            
            // 转换为统计VO
            LearningStatisticsVO result = convertToStatisticsVO(statisticsMap);
            
            log.info("成功获取用户学习统计数据，userId:{}, 总活动次数:{}", userId, result.getTotalActivities());
            return result;
            
        } catch (Exception e) {
            log.error("获取用户学习统计数据失败，userId:{}, days:{}", userId, days, e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getUserLearningDataBySubject(String userId, int days) {
        try {
            log.info("获取用户最近{}天按科目分组的学习数据，userId:{}, days:{}", days, userId);
            
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("用户ID为空，无法查询按科目分组的学习数据");
                return Collections.emptyList();
            }
            
            if (days <= 0) {
                log.warn("查询天数无效，days:{}", days);
                return Collections.emptyList();
            }
            
            // 从数据库查询按科目分组的学习数据
            List<Map<String, Object>> result = vectorDao.getUserLearningDataBySubject(userId, days);
            
            if (CollectionUtils.isEmpty(result)) {
                log.info("用户最近{}天无按科目分组的学习数据，userId:{}", days, userId);
                return Collections.emptyList();
            }
            
            log.info("成功获取用户按科目分组学习数据，userId:{}, 科目数量:{}", userId, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取用户按科目分组学习数据失败，userId:{}, days:{}", userId, days, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getUserLearningDataByActionType(String userId, int days) {
        try {
            log.info("获取用户最近{}天按行为类型分组的学习数据，userId:{}, days:{}", days, userId);
            
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("用户ID为空，无法查询按行为类型分组的学习数据");
                return Collections.emptyList();
            }
            
            if (days <= 0) {
                log.warn("查询天数无效，days:{}", days);
                return Collections.emptyList();
            }
            
            // 从数据库查询按行为类型分组的学习数据
            List<Map<String, Object>> result = vectorDao.getUserLearningDataByActionType(userId, days);
            
            if (CollectionUtils.isEmpty(result)) {
                log.info("用户最近{}天无按行为类型分组的学习数据，userId:{}", days, userId);
                return Collections.emptyList();
            }
            
            log.info("成功获取用户按行为类型分组学习数据，userId:{}, 行为类型数量:{}", userId, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取用户按行为类型分组学习数据失败，userId:{}, days:{}", userId, days, e);
            return Collections.emptyList();
        }
    }

    /**
     * 将数据库PO转换为领域实体
     */
    private LearningVectorEntity convertToEntity(LearningVector vector) {
        if (vector == null) {
            return null;
        }
        
        return LearningVectorEntity.builder()
                .id(vector.getId())
                .userId(vector.getUserId())
                .questionId(vector.getQuestionId())
                .actionType(vector.getActionType())
                .questionContent(vector.getQuestionContent())
                .subject(vector.getSubject())
                .knowledgePointId(vector.getKnowledgePointId())
                .embedding(vector.getEmbedding())
                .metadata(vector.getMetadata())
                .createdAt(vector.getCreatedAt())
                .updatedAt(vector.getUpdatedAt())
                .build();
    }

    /**
     * 将数据库统计结果转换为统计VO
     */
    private LearningStatisticsVO convertToStatisticsVO(Map<String, Object> statisticsMap) {
        if (statisticsMap == null || statisticsMap.isEmpty()) {
            return LearningStatisticsVO.builder()
                    .totalActivities(0)
                    .subjectsCount(0)
                    .activeDays(0)
                    .actionTypesCount(0)
                    .build();
        }
        
        return LearningStatisticsVO.builder()
                .totalActivities(getIntValue(statisticsMap, "total_activities"))
                .actionTypesCount(getIntValue(statisticsMap, "action_types_count"))
                .subjectsCount(getIntValue(statisticsMap, "subjects_count"))
                .activeDays(getIntValue(statisticsMap, "active_days"))
                .firstActivity(getDateTimeValue(statisticsMap, "first_activity"))
                .lastActivity(getDateTimeValue(statisticsMap, "last_activity"))
                .build();
    }

    /**
     * 安全获取整数值
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析整数值: {} = {}", key, value);
            return 0;
        }
    }

    /**
     * 安全获取日期时间值
     */
    private LocalDateTime getDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        
        // 如果是其他类型，可以根据需要添加转换逻辑
        return null;
    }
}