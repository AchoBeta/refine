package com.achobeta.domain.ocr.adapter.port.redis;

import com.achobeta.domain.ocr.model.entity.QuestionEntity;

/**
 * @Auth : Malog
 * @Desc : 题目Redis仓储接口
 * @Time : 2025/11/11
 */
public interface IQuestionRedisRepository {

    /**
     * 保存题目信息到Redis
     *
     * @param questionEntity 题目实体
     * @return 是否保存成功
     */
    boolean saveQuestion(QuestionEntity questionEntity);

    /**
     * 根据题目ID从Redis获取题目信息
     *
     * @param questionId 题目ID
     * @return 题目实体，如果不存在则返回null
     */
    QuestionEntity getQuestion(String questionId);

    /**
     * 删除Redis中的题目信息
     *
     * @param questionId 题目ID
     * @return 是否删除成功
     */
    boolean deleteQuestion(String questionId);

    /**
     * 设置题目信息的过期时间
     *
     * @param questionId 题目ID
     * @param expireTime 过期时间（毫秒）
     * @return 是否设置成功
     */
    boolean setExpireTime(String questionId, long expireTime);
}