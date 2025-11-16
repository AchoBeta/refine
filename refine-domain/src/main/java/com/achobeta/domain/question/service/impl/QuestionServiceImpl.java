package com.achobeta.domain.question.service.impl;

import com.achobeta.domain.question.adapter.port.AiGenerationService;
import com.achobeta.domain.question.adapter.repository.IKnowledgeRepository;
import com.achobeta.domain.question.adapter.repository.IMistakeRepository;
import com.achobeta.domain.question.model.entity.MistakeQuestionEntity;
import com.achobeta.domain.question.model.po.MistakeKnowledgePO;
import com.achobeta.domain.question.model.valobj.MistakeQuestionVO;
import com.achobeta.domain.question.model.valobj.QuestionResponseVO;
import com.achobeta.domain.question.service.IQuestionService;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import com.achobeta.types.support.postprocessor.AbstractPostProcessor;
import com.achobeta.types.support.postprocessor.PostContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.achobeta.types.common.Constants.QUESTION_GENERATION_EXPIRED_SECONDS;
import static com.achobeta.types.common.Constants.QUESTION_GENERATION_ID_KEY;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends AbstractPostProcessor<QuestionResponseVO> implements IQuestionService {

    private final IMistakeRepository mistakeRepository;

    private final IKnowledgeRepository knowledgeRepository;

    private final AiGenerationService aiGenerationService;


    /**
     * 题目生成
     */
    public QuestionResponseVO questionGeneration(String userId, Integer mistakeQuestionId) {

        MistakeKnowledgePO po = mistakeRepository.findSubjectAndKnowledgeIdById(mistakeQuestionId);
        String subject = po.getSubject();
        Integer knowledgeId = po.getKnowledgeId();
        String knowledgePointName = knowledgeRepository.findKnowledgeNameById(po.getKnowledgeId());

        String toAi = "你是一位" + subject + "老师，请根据\"" + knowledgePointName + "\"知识点出一道题目";

        // 调用外部接口生成新题目
        QuestionResponseVO questionResult = aiGenerationService.Generation(toAi);
        if (null == questionResult.getQuestionContent() || null == questionResult.getAnswer() || null == questionResult.getAnalysis()) {
            throw new AppException(GlobalServiceStatusCode.QUESTION_GENERATION_FAIL);
        }

        // 构建错题实体
        String questionId = String.valueOf(mistakeQuestionId + System.currentTimeMillis());

        MistakeQuestionVO vo = new MistakeQuestionVO();
        vo.setUserId(userId);
        vo.setQuestionId(questionId);
        vo.setQuestionContent(questionResult.getQuestionContent());
        vo.setSubject(subject);
        vo.setKnowledgePointId(knowledgeId);

        // 暂存题目到redis
        mistakeRepository.setValue(QUESTION_GENERATION_ID_KEY + questionId, vo, QUESTION_GENERATION_EXPIRED_SECONDS);

        questionResult.setQuestionId(questionId);

        return questionResult;
    }

    /**
     * 把错题存入数据库
     */
    @Override
    public void recordMistakeQuestion(String userId, String questionId) {

        MistakeQuestionVO vo = mistakeRepository.getValue(QUESTION_GENERATION_ID_KEY + questionId);
        if (null == vo) {
            throw new AppException(GlobalServiceStatusCode.QUESTION_IS_EXPIRED);
        }
        mistakeRepository.save(MistakeQuestionEntity.builder()
                .userId(userId)
                .questionContent(vo.getQuestionContent())
                .subject(vo.getSubject())
                .knowledgePointId(vo.getKnowledgePointId())
                .questionStatus(0)
                .createTime(LocalDateTime.now())
                .build());


    }

    @Override
    public PostContext<QuestionResponseVO> doMainProcessor(PostContext<QuestionResponseVO> postContext) {
        return null;
    }

    public void removeQuestionCache(String questionId) {
        mistakeRepository.remove(QUESTION_GENERATION_ID_KEY + questionId);
    }
}