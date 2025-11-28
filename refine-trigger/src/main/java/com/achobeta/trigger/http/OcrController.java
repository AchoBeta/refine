package com.achobeta.trigger.http;

import com.achobeta.api.dto.QuestionInfoResponseDTO;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import com.achobeta.domain.ocr.service.IMistakeQuestionService;
import com.achobeta.domain.ocr.service.IOcrService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    private final IOcrService ocrService;
    private final IMistakeQuestionService mistakeQuestionService;

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

            // 将错题数据保存到数据库中
            boolean saveSuccess = mistakeQuestionService.saveMistakeQuestion(questionEntity);
            if (!saveSuccess) {
                log.warn("错题保存失败，但继续返回OCR识别结果: userId={}, questionId={}",
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



