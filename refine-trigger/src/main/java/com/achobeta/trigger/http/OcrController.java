package com.achobeta.trigger.http;

import com.achobeta.domain.ocr.service.IOcrService;
import com.achobeta.domain.ocr.service.QuestionItem;
import com.achobeta.types.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController()
@CrossOrigin("${app.config.cross-origin}:*")
@RequestMapping("/api/${app.config.api-version}/ocr/")
@RequiredArgsConstructor
public class OcrController {

    private final IOcrService ocrService;

    @PostMapping(value = "extract-first", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<QuestionItem> extractFirst(@RequestPart("file") MultipartFile file,
                                               @RequestParam(value = "fileType", required = false) String fileType) {
        try {
            String ft = fileType;
            // 如果文件类型未指定，则尝试从原始文件名中获取
            if (ft == null || ft.isEmpty()) {
                ft = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
            }
            // 调用OCR服务提取文件中的第一个题目
            QuestionItem item = ocrService.extractFirstQuestion(file.getBytes(), ft);
            return Response.SYSTEM_SUCCESS(item);
        } catch (Exception e) {
            // 记录OCR提取失败的日志并返回错误响应
            log.error("OCR 提取题目失败", e);
            return Response.SERVICE_ERROR(e.getMessage());

        }
    }
}



