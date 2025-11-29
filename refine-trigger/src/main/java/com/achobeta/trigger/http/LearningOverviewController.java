package com.achobeta.trigger.http;


import com.achobeta.api.dto.StudyOverviewDTO;
import com.achobeta.domain.IRedisService;
import com.achobeta.domain.overview.model.valobj.LearningDynamicVO;
import com.achobeta.domain.overview.service.ILearningOverviewService;
import com.achobeta.domain.overview.model.valobj.StudyOverviewVO;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.Constants;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.achobeta.types.enums.GlobalServiceStatusCode.*;

/**
 * 学习概览接口
 */
@Slf4j
@Validated
@CrossOrigin("${app.config.cross-origin}:*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/${app.config.api-version}/overview")
public class LearningOverviewController {
    private final ILearningOverviewService service;

    /**
     * 获取学习概览
     * @return 学习概览
     */
    @GetMapping("/get_overview")
    @GlobalInterception
    public Response<StudyOverviewDTO> getOverview() {
        String userId = UserContext.getUserId();
        StudyOverviewVO vo = null;
        try {
            log.info("用户获取学习概览，userId:{}", userId);
            vo = service.getOverview(userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(REQUEST_NOT_VALID);
        }
        return Response.SYSTEM_SUCCESS(StudyOverviewDTO.builder()
                .questionsNum(vo.getQuestionsNum())
                .reviewRate(vo.getReviewRate())
                .hardQuestions(vo.getHardQuestions())
                .studyTime(vo.getStudyTime())
                .build());

    }

    /**
     * 获取学习动态
     * @return 学习动态
     */
    @GetMapping("/get_study_dynamic")
    @GlobalInterception
    public Response<LearningDynamicVO> getStudyDynamic(){
        String userId = UserContext.getUserId();
        LearningDynamicVO result = null;
        try {
            log.info("用户获取学习动画，userId:{}", userId);
            result = service.getStudyDynamic(userId);
        } catch (Exception e) {
            log.error("getStudyDynamic error", e);
            return Response.CUSTOMIZE_ERROR(GET_STUDY_DYNAMIC_FAIL);
        }
        return Response.SYSTEM_SUCCESS(result);
    }
}
