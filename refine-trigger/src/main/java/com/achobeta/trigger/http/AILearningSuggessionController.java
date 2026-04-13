package com.achobeta.trigger.http;

import com.achobeta.api.dto.KeyPointDTO;
import com.achobeta.domain.aisuggession.model.valobj.KeyPointVO;
import com.achobeta.domain.aisuggession.service.IAILearningSuggessionService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI学习建议接口
 */
@Slf4j
@Validated
@CrossOrigin("${app.config.cross-origin}:*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/${app.config.api-version}/ai_suggession")
public class AILearningSuggessionController {
    private final IAILearningSuggessionService service;

    @RequestMapping("/get_key_point")
    @GlobalInterception
    public Response<List<KeyPointDTO>> getKeyPoint() {
        String userId = UserContext.getUserId();
        List<KeyPointVO> keyPointVOS = null;
        try {
            log.info("用户获取AI学习建议，userId:{}", userId);
            keyPointVOS = service.getKeyPoint(userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(GlobalServiceStatusCode.AI_RESPONSE_TIMEOUT);
        }
        List<KeyPointDTO> keyPointDTOS = keyPointVOS.stream()
                .map(keyPointVO -> KeyPointDTO.builder()
                        .knowledgePoint(keyPointVO.getKnowledgePoint())
                        .reviewReason(keyPointVO.getReviewReason())
                        .build()).toList();
        return Response.SYSTEM_SUCCESS(keyPointDTOS);
    }
}
