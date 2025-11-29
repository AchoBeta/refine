package com.achobeta.infrastructure.adapter.repository;

import com.achobeta.domain.ocr.adapter.port.IMistakeQuestionRepository;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import com.achobeta.infrastructure.dao.IMistakeQuestionDao;
import com.achobeta.infrastructure.dao.po.MistakeQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auth : Malog
 * @Desc : 错题仓储实现
 * @Time : 2025/11/10
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MistakeQuestionRepository implements IMistakeQuestionRepository {

    private final IMistakeQuestionDao mistakeQuestionDao;

    @Override
    public boolean save(QuestionEntity questionEntity) {
        try {
            if (questionEntity == null) {
                log.warn("题目实体为空，无法保存错题");
                return false;
            }

            // 转换为数据库实体
            MistakeQuestion mistakeQuestion = new MistakeQuestion();
            mistakeQuestion.setUserId(questionEntity.getUserId());
            mistakeQuestion.setQuestionId(questionEntity.getQuestionId());
            mistakeQuestion.setQuestionContent(questionEntity.getQuestionText());
            mistakeQuestion.setKnowledgePointId(questionEntity.getKnowledgePointId());

            // 设置默认值
            mistakeQuestion.setSubject("未分类"); // 默认学科
            mistakeQuestion.setIsCareless(0);
            mistakeQuestion.setIsUnfamiliar(0);
            mistakeQuestion.setIsCalculateErr(0);
            mistakeQuestion.setIsTimeShortage(0);
            mistakeQuestion.setOtherReason("");
            mistakeQuestion.setStudyNote("");
            mistakeQuestion.setQuestionStatus(0); // 默认未理解状态

            // 设置时间
            Date now = new Date();
            mistakeQuestion.setCreateTime(now);
            mistakeQuestion.setUpdateTime(now);

            // 执行插入操作
            int result = mistakeQuestionDao.insert(mistakeQuestion);

            boolean success = result > 0;
            if (success) {
                log.info("错题保存成功: userId={}, questionId={}",
                    questionEntity.getUserId(), questionEntity.getQuestionId());
            } else {
                log.error("错题保存失败: userId={}, questionId={}",
                    questionEntity.getUserId(), questionEntity.getQuestionId());
            }

            return success;
        } catch (Exception e) {
            log.error("保存错题数据时发生异常: userId={}, questionId={}",
                questionEntity != null ? questionEntity.getUserId() : null,
                questionEntity != null ? questionEntity.getQuestionId() : null, e);
            return false;
        }
    }

    @Override
    public QuestionEntity findByQuestionId(String questionId) {
        try {
            if (questionId == null || questionId.isEmpty()) {
                log.warn("题目ID为空，无法查询错题");
                return null;
            }

            MistakeQuestion mistakeQuestion = mistakeQuestionDao.selectByQuestionId(questionId);
            if (mistakeQuestion == null) {
                log.info("未找到题目ID为 {} 的错题", questionId);
                return null;
            }

            // 转换为领域实体
            QuestionEntity questionEntity = new QuestionEntity();
            questionEntity.setUserId(mistakeQuestion.getUserId());
            questionEntity.setQuestionId(mistakeQuestion.getQuestionId());
            questionEntity.setQuestionText(mistakeQuestion.getQuestionContent());

            return questionEntity;
        } catch (Exception e) {
            log.error("根据题目ID查询错题时发生异常: questionId={}", questionId, e);
            return null;
        }
    }

    @Override
    public List<QuestionEntity> findByUserId(String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                log.warn("用户ID为空，无法查询错题列表");
                return List.of();
            }

            List<MistakeQuestion> mistakeQuestions = mistakeQuestionDao.selectByUserId(userId);
            if (mistakeQuestions == null || mistakeQuestions.isEmpty()) {
                log.info("未找到用户ID为 {} 的错题", userId);
                return List.of();
            }

            // 转换为领域实体列表
            return mistakeQuestions.stream()
                .map(mistakeQuestion -> {
                    QuestionEntity questionEntity = new QuestionEntity();
                    questionEntity.setUserId(mistakeQuestion.getUserId());
                    questionEntity.setQuestionId(mistakeQuestion.getQuestionId());
                    questionEntity.setQuestionText(mistakeQuestion.getQuestionContent());
                    return questionEntity;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据用户ID查询错题列表时发生异常: userId={}", userId, e);
            return List.of();
        }
    }
}
