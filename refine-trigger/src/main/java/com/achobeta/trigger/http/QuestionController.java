package com.achobeta.trigger.http;

import com.achobeta.api.dto.QuestionResponseDTO;
import com.achobeta.domain.question.service.impl.QuestionServiceImpl;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.exception.AppException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionServiceImpl questionService;


    /*
        生成出来的题目先放入redis，
        如果用户答案正确，则直接移除题目；否则录入错题数据库，再移除redis
     */


    /**
     * 1.题目生成，目前支持判分的只有填空题和选择题
     *
     * @return 题目id，题目内容（含答案,简单解析）
     */
    @GlobalInterception
    @PostMapping("/generation")
    public Response<QuestionResponseDTO> questionGeneration(@NotNull Integer mistakeQuestionId) {
        String userId = UserContext.getUserId();
        try {
            QuestionResponseDTO responseDTO = questionService.questionGeneration(userId, mistakeQuestionId);
            log.info("用户id: {} 生成题目成功，题目redis id: {}", userId, responseDTO.getQuestionId());
            return Response.SYSTEM_SUCCESS(responseDTO);
        } catch (AppException e) {
            throw new AppException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("用户id: {} 生成题目失败，题目id: {}", userId, mistakeQuestionId, e);
            throw new AppException(e.getMessage());
        }
    }


    /**
     * 2.ai判题
     */
    @GlobalInterception
    @PostMapping("/judge")
    public Flux<ServerSentEvent<String>> aiJudge(@NotNull String questionId, @NotNull String answer) {
        String userId = UserContext.getUserId();
        try {
            log.info("用户id: {} 调用ai判题，题目id: {}", userId, questionId);
            return questionService.aiJudge(userId, questionId, answer);
        } catch (AppException e) {
            throw new AppException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("用户id: {} 调用ai判题失败，题目id: {}", userId, questionId, e);
            throw new AppException(e.getMessage());
        }
    }


    /**
     * 3.记录错题
     */
    @GlobalInterception
    @PostMapping("/record")
    public Response recordMistakeQuestion(@NotNull String questionId) {
        String userId = UserContext.getUserId();
        try {
            log.info("用户 {} 记录错题，题目id: {}", userId, questionId);
            questionService.recordMistakeQuestion(userId, questionId);
            return Response.SYSTEM_SUCCESS("已加入错题");
        } catch (AppException e) {
            throw new AppException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 记录错题失败，题目id: {}", userId, questionId, e);
            throw new AppException(e.getMessage());
        }
    }


}