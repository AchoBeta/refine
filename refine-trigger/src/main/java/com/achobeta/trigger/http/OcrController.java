package com.achobeta.trigger.http;

import cn.hutool.core.lang.UUID;
import com.achobeta.api.dto.QuestionInfoResponseDTO;
import com.achobeta.domain.keypoints_explanation.adapter.repository.KeyPointsMapper;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import com.achobeta.domain.ocr.service.IMistakeQuestionService;
import com.achobeta.domain.ocr.service.IOcrService;
import com.achobeta.domain.ocr.adapter.port.redis.IQuestionRedisRepository;
import com.achobeta.domain.question.adapter.port.AiGenerationService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Auth : Malog
 * @Desc : OCR 识别接口
 * @Time : 2025/10/31 17:29
 */
@Slf4j
@Validated
@RestController()
@CrossOrigin("${app.config.cross-origin}:*")
@RequestMapping("/api/${app.config.api-version}/ocr/")
@RequiredArgsConstructor
public class OcrController {

    // 通用异步线程池
    private final ThreadPoolTaskExecutor threadPoolExecutor;

    private final KeyPointsMapper keyPointsMapper;

    private final IOcrService ocrService;
    private final IMistakeQuestionService mistakeQuestionService;
    private final IQuestionRedisRepository questionRedisRepository;
    private final AiGenerationService aiGenerationService;

    /**
     * 抽取第一个问题
     *
     * @param file     文件（最大为 5MB）
     * @param fileType 文件类型
     * @return 第一个问题
     */
    @GlobalInterception
    @PostMapping(value = "extract-first", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<QuestionInfoResponseDTO> extractFirst(@RequestPart("file") MultipartFile file,
                                                          @RequestParam(value = "fileType", required = false) String fileType) {
        try {
            // 获取用户ID
            String userId = UserContext.getUserId();
            if (userId == null) {
                log.info("用户id为空！");
                return Response.<QuestionInfoResponseDTO>builder()
                        .code(GlobalServiceStatusCode.USER_ID_IS_NULL.getCode())
                        .info(GlobalServiceStatusCode.USER_ID_IS_NULL.getMessage())
                        .build();
            }

            String ft = fileType;

            // 如果文件类型未指定，则尝试从原始文件名中获取
            if (ft == null || ft.isEmpty()) {
                ft = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
            }

            // 调用OCR服务提取文件中的第一个题目
            QuestionEntity questionEntity = ocrService.extractQuestionContent(userId, file.getBytes(), ft);

            //TODO
            // ai根据向量库（和mysql知识点表里的完全一致）（新建一个表）查询有没有相似知识点，再返回知识点名称
            String knowledgeName = aiGenerationService.knowledgeAnalysis(questionEntity.getQuestionText());
            if (knowledgeName == null || knowledgeName.isEmpty()) {
                log.warn("ai生成的知识点为空，请检查ai生成知识点的逻辑");
            }
            String knowledgePointId = UUID.fastUUID().toString();
            //把新知识点录入数据库中
            threadPoolExecutor.execute(() -> {
                try {
                    keyPointsMapper.insertNewPoint4MistakeQuestion(userId, knowledgePointId, knowledgeName);
                    log.info("ai生成知识点录入表成功, knowledgeName:{} 题目id:{}", knowledgeName, questionEntity.getQuestionId());
                } catch (Exception e) {
                    log.error("ai生成知识点录入表失败, knowledgeName:{} 题目id:{}", knowledgeName, questionEntity.getQuestionId(), e);
                }
            });

            // 将错题数据保存到数据库中
            questionEntity.setKnowledgePointId(knowledgePointId);
            boolean saveSuccess = mistakeQuestionService.saveMistakeQuestion(questionEntity);
            if (!saveSuccess) {
                log.warn("错题保存失败，但继续返回OCR识别结果: userId={}, questionId={}",
                        userId, questionEntity.getQuestionId());
            }

            // 将题目信息保存到Redis中，用于后续对话查询
            boolean redisSaveSuccess = questionRedisRepository.saveQuestion(questionEntity);
            if (!redisSaveSuccess) {
                log.warn("题目信息保存到Redis失败，但继续返回OCR识别结果: userId={}, questionId={}",
                        userId, questionEntity.getQuestionId());
            }

            // 返回响应
            return Response.<QuestionInfoResponseDTO>builder()
                    .code(GlobalServiceStatusCode.SYSTEM_SUCCESS.getCode())
                    .info(GlobalServiceStatusCode.SYSTEM_SUCCESS.getMessage())
                    .data(QuestionInfoResponseDTO.builder()
                            .questionText(questionEntity.getQuestionText())
                            .questionId(questionEntity.getQuestionId())
                            .build())
                    .build();
        } catch (Exception e) {
            // 记录OCR提取失败的日志并返回错误响应
            log.error("OCR 提取题目失败", e);
            return Response.<QuestionInfoResponseDTO>builder()
                    .code(GlobalServiceStatusCode.OCR_ERROR.getCode())
                    .info(GlobalServiceStatusCode.OCR_ERROR.getMessage())
                    .build();
        }
    }
}



