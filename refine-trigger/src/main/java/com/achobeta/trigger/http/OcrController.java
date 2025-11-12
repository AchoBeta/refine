package com.achobeta.trigger.http;

import com.achobeta.api.dto.QuestionInfoResponseDTO;
import com.achobeta.api.dto.UserInfoRequestDTO;
import com.achobeta.domain.auth.service.AuthService;
import com.achobeta.domain.ocr.model.entity.QuestionEntity;
import com.achobeta.domain.ocr.service.IOcrService;
import com.achobeta.types.Response;
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
    private final AuthService authService;

    /**
     * 抽取第一个问题
     *
     * @param file     文件（最大为 5MB）
     * @param fileType 文件类型
     * @return 第一个问题
     */
    @PostMapping(value = "extract-first", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<QuestionInfoResponseDTO> extractFirst(@RequestPart("file") MultipartFile file,
                                                          @RequestParam(value = "fileType", required = false) String fileType,
                                                          @RequestHeader("Authorization") String token) {
        try {
            // token 验证
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<QuestionInfoResponseDTO>builder()
                        .code(Response.SERVICE_ERROR().getCode())
                        .info(Response.SERVICE_ERROR().getInfo())
                        .build();
            }

            // 解析token获取用户ID
            String userId = authService.openid(token);
            assert userId != null;

            String ft = fileType;

            // 如果文件类型未指定，则尝试从原始文件名中获取
            if (ft == null || ft.isEmpty()) {
                ft = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
            }

            // 调用OCR服务提取文件中的第一个题目
            QuestionEntity questionEntity = ocrService.extractQuestionContent(file.getBytes(), ft);
            questionEntity.setUserId(userId);

            // TODO 将错题数据保存到数据库中

            // 返回响应
            return Response.<QuestionInfoResponseDTO>builder()
                    .code(Response.SYSTEM_SUCCESS().getCode())
                    .info(Response.SYSTEM_SUCCESS().getInfo())
                    .data(QuestionInfoResponseDTO.builder()
                            .questionText(questionEntity.getQuestionText())
                            .questionId(questionEntity.getQuestionId())
                            .build())
                    .build();
        } catch (Exception e) {
            // 记录OCR提取失败的日志并返回错误响应
            log.error("OCR 提取题目失败", e);
            return Response.<QuestionInfoResponseDTO>builder()
                    .code(Response.SERVICE_ERROR().getCode())
                    .info(Response.SERVICE_ERROR().getInfo())
                    .build();
        }
    }
}



