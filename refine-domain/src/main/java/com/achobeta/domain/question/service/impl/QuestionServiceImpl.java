package com.achobeta.domain.question.service.impl;

import cn.hutool.core.lang.UUID;
import com.achobeta.domain.question.adapter.port.AiGenerationService;
import com.achobeta.domain.question.adapter.repository.IKnowledgeRepository;
import com.achobeta.domain.question.adapter.repository.IMistakeRepository;
import com.achobeta.domain.question.model.entity.MistakeQuestionEntity;
import com.achobeta.domain.question.model.po.MistakeKnowledgePO;
import com.achobeta.api.dto.MistakeQuestionDTO;
import com.achobeta.api.dto.QuestionResponseDTO;
import com.achobeta.domain.question.service.IQuestionService;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import com.achobeta.types.support.postprocessor.AbstractPostProcessor;
import com.achobeta.types.support.postprocessor.PostContext;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

import static com.achobeta.types.common.Constants.QUESTION_GENERATION_EXPIRED_SECONDS;
import static com.achobeta.types.common.Constants.QUESTION_GENERATION_ID_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl extends AbstractPostProcessor<QuestionResponseDTO> implements IQuestionService {

    private final IMistakeRepository mistakeRepository;

    private final IKnowledgeRepository knowledgeRepository;

    private final AiGenerationService aiGenerationService;

    @Resource(name = "mistakeExecutor")
    private Executor mistakeExecutor;

    @Resource(name = "aiExclusiveThreadPool")
    private Executor aiExclusiveThreadPool;


    /**
     * 题目生成
     */
    public QuestionResponseDTO questionGeneration(String userId, Integer mistakeQuestionId) {

        MistakeKnowledgePO po = mistakeRepository.findSubjectAndKnowledgeIdById(mistakeQuestionId);
        String subject = po.getSubject();
        Integer knowledgeId = po.getKnowledgeId();
        String knowledgePointName = knowledgeRepository.findKnowledgeNameById(knowledgeId);

        if (null == subject) {
            throw new AppException("（ai出题）找不到题目所属科目,mistakeQuestionId" + mistakeQuestionId);
        }
        if (null == knowledgePointName) {
            throw new AppException("（ai出题）找不到该题知识点名称,mistakeQuestionId" + mistakeQuestionId);
        }

        // 调用外部接口生成新题目
        String toAi;
        QuestionResponseDTO question;
        if (subject.equals("未分类")) {
            toAi = "你是一位专业出题老师，请根据\"" + knowledgePointName + "\"知识点出一道题目";
            question = aiGenerationService.Generation(toAi);
        } else {
            toAi = "你是一位" + subject + "老师，请根据\"" + knowledgePointName + "\"知识点出一道题目";
            question = aiGenerationService.Generation(subject, toAi);
        }

        if (null == question.getContent() || null == question.getAnswer()) {
            throw new AppException(GlobalServiceStatusCode.QUESTION_GENERATION_FAIL);
        }

        // 构建错题实体
        String questionId = UUID.fastUUID().toString().substring(0, 19)+ System.currentTimeMillis();

        MistakeQuestionDTO dto = new MistakeQuestionDTO();
        dto.setUserId(userId);
        dto.setQuestionId(questionId);
        dto.setQuestionContent(question.getContent());
        dto.setAnswer(question.getAnswer());
        dto.setSubject(subject);
        dto.setKnowledgePointId(knowledgeId);

        // 暂存题目到redis
        mistakeRepository.setValue(QUESTION_GENERATION_ID_KEY + questionId, dto, QUESTION_GENERATION_EXPIRED_SECONDS);

        return QuestionResponseDTO.builder()
                .questionId(questionId)
                .content(question.getContent())
                .build();
    }

    /**
     * 把redis的错题存入数据库
     */
    @Override
    public void recordMistakeQuestion(String userId, String questionId) {
        MistakeQuestionDTO value = mistakeRepository.getValue(QUESTION_GENERATION_ID_KEY + questionId);

        if (null == value) {
            throw new AppException(GlobalServiceStatusCode.QUESTION_IS_EXPIRED);
        }
        if (!userId.equals(value.getUserId())) {
            throw new AppException(GlobalServiceStatusCode.PARAM_FAILED_VALIDATE);
        }

        mistakeRepository.save(MistakeQuestionEntity.builder()
                .userId(userId)
                .questionId(questionId)
                .questionContent(value.getQuestionContent())
                .subject(value.getSubject())
                .knowledgePointId(value.getKnowledgePointId())
                .questionStatus(0)
                .createTime(LocalDateTime.now())
                .build());

        log.info("用户 {} 错题录入完成，题目id: {}", userId, questionId);

        this.removeQuestionCache(questionId);
    }

    @Override
    public PostContext<QuestionResponseDTO> doMainProcessor(PostContext<QuestionResponseDTO> postContext) {
        return null;
    }

    public void removeQuestionCache(String questionId) {
        mistakeRepository.remove(QUESTION_GENERATION_ID_KEY + questionId);
        log.info("已删除redis题目缓存，题目id：{}", questionId);
    }

    public Flux<ServerSentEvent<String>> aiJudge(String userId, String questionId, String userAnswer) {
        MistakeQuestionDTO value = mistakeRepository.getValue(QUESTION_GENERATION_ID_KEY + questionId);

        if (null == value) {
            throw new AppException(GlobalServiceStatusCode.QUESTION_IS_EXPIRED);
        }
        if (!userId.equals(value.getUserId())) {
            throw new AppException(GlobalServiceStatusCode.PARAM_FAILED_VALIDATE);
        }
        String correctAnswer = value.getAnswer();

        Integer auto = 0;    // 用户可选是否自动录入TODO
        if (auto == 1) {
            autoRecord(userId, questionId, userAnswer, correctAnswer);
        }

        String questionContent = value.getQuestionContent();
        String chat = "请根据题目:\"" + questionContent + "\"以及正确答案:\"" + correctAnswer + "\"，判断答案:\"" + correctAnswer + "\"是否正确，并给出解析。";

        // 将ai流式调用提交到自定义线程池
        String subject = value.getSubject();
        if (subject.equals("未分类")) {
            return Flux.defer(() -> aiGenerationService.aiJudgeStream(chat))
                    .subscribeOn(Schedulers.fromExecutor(aiExclusiveThreadPool))
                    .doOnError(e -> log.error("流式聊天异常：用户Id={},题目id={}, 用户判题回答={}", userId, questionId, userAnswer, e))
                    .map(chunk -> ServerSentEvent.<String>builder()
                            .data(chunk)
                            .build());
        } else {
            return Flux.defer(() -> aiGenerationService.aiJudgeStream(subject, chat))
                    .subscribeOn(Schedulers.fromExecutor(aiExclusiveThreadPool))
                    .doOnError(e -> log.error("流式聊天异常：用户Id={},题目id={}, 用户判题回答={}", userId, questionId, userAnswer, e))
                    .map(chunk -> ServerSentEvent.<String>builder()
                            .data(chunk)
                            .build());
        }

    }

    private void autoRecord(String userId, String questionId, String answer, String correctAnswer) {
        // 错误则异步录入错题，不阻塞主线程
        if (!correctAnswer.equalsIgnoreCase(answer.trim())) {
            log.info("用户 {} 答题错误，准备自动录入错题，题目id: {}", userId, questionId);
            // 提交到线程池异步执行
            mistakeExecutor.execute(() -> {
                try {
                    recordMistakeQuestion(userId, questionId);
                } catch (Exception e) {
                    log.error("用户 {} 错题异步录入失败，题目id: {}", userId, questionId, e);
                }
            });
        }
    }

}