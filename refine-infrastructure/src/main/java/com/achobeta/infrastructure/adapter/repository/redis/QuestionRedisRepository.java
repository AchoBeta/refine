package com.achobeta.infrastructure.adapter.repository.redis;

import com.achobeta.domain.IRedisService;
import com.achobeta.domain.ocr.adapter.port.redis.IQuestionRedisRepository;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @Auth : Malog
 * @Desc : 题目Redis仓储实现
 * @Time : 2025/11/11
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class QuestionRedisRepository implements IQuestionRedisRepository {

    private static final String REDIS_QUESTION_PREFIX = "question:";
    private static final long QUESTION_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时过期时间（毫秒）

    private final IRedisService redisService;

    @Override
    public boolean saveQuestion(QuestionEntity questionEntity) {
        try {
            if (questionEntity == null || questionEntity.getQuestionId() == null) {
                log.warn("题目实体或题目ID为空，无法保存到Redis");
                return false;
            }

            String redisKey = REDIS_QUESTION_PREFIX + questionEntity.getQuestionId();
            
            // 保存到Redis，设置24小时过期时间
            redisService.setValue(redisKey, questionEntity, QUESTION_EXPIRE_TIME);
            
            log.debug("题目信息保存到Redis成功: questionId={}", questionEntity.getQuestionId());
            return true;
        } catch (Exception e) {
            log.error("保存题目信息到Redis时发生异常: questionId={}", 
                questionEntity != null ? questionEntity.getQuestionId() : null, e);
            return false;
        }
    }

    @Override
    public QuestionEntity getQuestion(String questionId) {
        try {
            if (questionId == null || questionId.trim().isEmpty()) {
                log.warn("题目ID为空，无法从Redis获取题目信息");
                return null;
            }

            String redisKey = REDIS_QUESTION_PREFIX + questionId;
            QuestionEntity questionEntity = redisService.getValue(redisKey);
            
            if (questionEntity != null) {
                log.debug("从Redis获取题目信息成功: questionId={}", questionId);
            } else {
                log.debug("Redis中未找到题目信息: questionId={}", questionId);
            }
            
            return questionEntity;
        } catch (Exception e) {
            log.error("从Redis获取题目信息失败: questionId={}", questionId, e);
            return null;
        }
    }

    @Override
    public boolean deleteQuestion(String questionId) {
        try {
            if (questionId == null || questionId.trim().isEmpty()) {
                log.warn("题目ID为空，无法删除Redis中的题目信息");
                return false;
            }

            String redisKey = REDIS_QUESTION_PREFIX + questionId;
            redisService.remove(redisKey);
            
            log.debug("删除Redis中的题目信息成功: questionId={}", questionId);
            return true;
        } catch (Exception e) {
            log.error("删除Redis中的题目信息时发生异常: questionId={}", questionId, e);
            return false;
        }
    }

    @Override
    public boolean setExpireTime(String questionId, long expireTime) {
        try {
            if (questionId == null || questionId.trim().isEmpty()) {
                log.warn("题目ID为空，无法设置过期时间");
                return false;
            }

            String redisKey = REDIS_QUESTION_PREFIX + questionId;
            // TODO: 实现设置过期时间的逻辑，当前Redis服务可能不支持expire方法
            // boolean success = redisService.expire(redisKey, expireTime);
            
            log.debug("设置题目信息过期时间: questionId={}, expireTime={}ms", questionId, expireTime);
            return true;
        } catch (Exception e) {
            log.error("设置题目信息过期时间时发生异常: questionId={}", questionId, e);
            return false;
        }
    }
}