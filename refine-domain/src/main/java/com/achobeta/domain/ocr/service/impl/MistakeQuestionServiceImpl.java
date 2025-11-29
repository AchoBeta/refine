package com.achobeta.domain.ocr.service.impl;

import com.achobeta.domain.ocr.adapter.port.IMistakeQuestionRepository;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import com.achobeta.domain.ocr.service.IMistakeQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Auth : Malog
 * @Desc : 错题领域服务实现
 * @Time : 2025/11/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MistakeQuestionServiceImpl implements IMistakeQuestionService {

    private final IMistakeQuestionRepository mistakeQuestionRepository;

    @Override
    public boolean saveMistakeQuestion(QuestionEntity questionEntity) {
        try {
            if (questionEntity == null) {
                log.warn("题目实体为空，无法保存错题");
                return false;
            }

            if (questionEntity.getQuestionId() == null || questionEntity.getQuestionId().isEmpty()) {
                log.warn("题目ID为空，无法保存错题");
                return false;
            }

            if (questionEntity.getUserId() == null || questionEntity.getUserId().isEmpty()) {
                log.warn("用户ID为空，无法保存错题");
                return false;
            }

            if (questionEntity.getQuestionText() == null || questionEntity.getQuestionText().isEmpty()) {
                log.warn("题目内容为空，无法保存错题");
                return false;
            }

            if (questionEntity.getKnowledgePointId() == null || questionEntity.getKnowledgePointId().isEmpty()) {
                log.warn("知识点id为空，无法保存错题");
                return false;
            }

            // 保存错题数据
            boolean success = mistakeQuestionRepository.save(questionEntity);

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
}
