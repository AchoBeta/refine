package com.achobeta.infrastructure.dao.vector;

import com.achobeta.infrastructure.dao.po.LearningVector;
import com.achobeta.domain.rag.model.valobj.LearningInsightVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Auth : Malog
 * @Desc : 向量数据访问接口
 * @Time : 2025/11/10
 */
@Mapper
public interface IVectorDao {

    /**
     * 插入学习向量
     */
    int insertLearningVector(LearningVector vector);

    /**
     * 向量相似度搜索
     */
    List<LearningVector> searchSimilarVectors(@Param("userId") String userId,
                                              @Param("queryVector") float[] queryVector,
                                              @Param("limit") int limit);

    /**
     * 根据行为类型获取用户向量
     */
    List<LearningVector> getUserVectorsByActionType(@Param("userId") String userId,
                                                    @Param("actionType") String actionType);

    /**
     * 获取用户所有向量
     */
    List<LearningVector> getUserAllVectors(@Param("userId") String userId);

    /**
     * 保存学习洞察
     */
    int saveLearningInsight(@Param("userId") String userId,
                            @Param("insight") LearningInsightVO insight);

    /**
     * 获取用户学习洞察
     */
    List<LearningInsightVO> getUserLearningInsights(@Param("userId") String userId,
                                                    @Param("insightType") String insightType);

    /**
     * 获取用户最近N天的学习向量数据
     */
    List<LearningVector> getUserRecentLearningData(@Param("userId") String userId, @Param("days") int days);

    /**
     * 获取用户最近N天的学习统计数据
     */
    Map<String, Object> getUserLearningStatistics(@Param("userId") String userId, @Param("days") int days);

    /**
     * 获取用户最近N天按科目分组的学习数据
     */
    List<Map<String, Object>> getUserLearningDataBySubject(@Param("userId") String userId, @Param("days") int days);

    /**
     * 获取用户最近N天按行为类型分组的学习数据
     */
    List<Map<String, Object>> getUserLearningDataByActionType(@Param("userId") String userId, @Param("days") int days);
}