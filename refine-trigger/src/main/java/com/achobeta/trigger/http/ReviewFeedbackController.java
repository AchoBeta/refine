package com.achobeta.trigger.http;


import com.achobeta.api.dto.ReviewTrendDTO;
import com.achobeta.api.dto.TrickyKnowledgePointDTO;
import com.achobeta.api.dto.OverdueReviewDTO;
import com.achobeta.api.dto.StatsDTO;
import com.achobeta.domain.Feetback.model.entity.MistakeQuestionEntity;
import com.achobeta.domain.Feetback.model.valobj.*;
import com.achobeta.domain.Feetback.service.feedback.IReviewFeedbackService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.enums.TimeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.achobeta.types.enums.GlobalServiceStatusCode.*;

/**
 * 待复习题目反馈接口
 */
@Slf4j
@Validated
@CrossOrigin("${app.config.cross-origin}:*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/${app.config.api-version}/feedback/review")
public class ReviewFeedbackController {


    private final IReviewFeedbackService reviewFeedbackService;


    /**
     * 获取用户超过一周未复习题目数量
     * @return
     */
    @GetMapping("/overdue-count")
    @GlobalInterception
    public Response<OverdueReviewDTO> getOverdueReviewCount() {
        String userId = UserContext.getUserId();
        OverdueCountVO overdueCountVO = null;
        try{
            log.info("用户获取待复习题目数量开始，userId:{}", userId);
            overdueCountVO = reviewFeedbackService.getOverdueCount(userId);
            log.info("用户获取待复习题目数量结束，userId:{} count:{}", userId, overdueCountVO.getCount());
        }catch (Exception e){
            log.error("用户获取待复习题目数量失败！userId:{}", userId, e);
            return Response.CUSTOMIZE_ERROR(GET_OVERDUE_REVIEW_COUNT_FAIL);
        }
        return Response.SYSTEM_SUCCESS(OverdueReviewDTO.builder()
                .count(overdueCountVO.getCount())
                .description(overdueCountVO.getDescription())
                .build());

    }

    /**
     * 获取用户近期出错多(>= 3)的知识点
     */
    @GetMapping("/tricky_knowledge")
    @GlobalInterception
    public Response<List<TrickyKnowledgePointDTO>> getTrickyKnowledgePoint(){
        String userId = UserContext.getUserId();
        List<TrickyKnowledgePointVO> trickyKnowledgePointVOS = null;
        try{
            log.info("用户获取近期出错最多的知识点，userId:{}", userId);
            trickyKnowledgePointVOS = reviewFeedbackService.getTrickyKnowledgePoint(userId);
        }catch (Exception e){
            log.error("用户获取近期出错最多的知识点失败！userId:{}", userId, e);
            return Response.CUSTOMIZE_ERROR(GET_TRICKY_KNOWLEDGE_POINT_FAIL);
        }

        List<TrickyKnowledgePointDTO> trickyKnowledgePointDTOS = trickyKnowledgePointVOS.stream()
                .map(trickyKnowledgePointVO -> {
                    if (trickyKnowledgePointVO == null) {
                        return TrickyKnowledgePointDTO.builder()
                                .knowledgeId("")
                                .knowledgeName("未知")
                                .build();
                    }
                    return TrickyKnowledgePointDTO.builder()
                            .knowledgeId(trickyKnowledgePointVO.getKnowledgeId())
                            .knowledgeName(trickyKnowledgePointVO.getKnowledgeName())
                            .build();
                }).toList();
        return Response.SYSTEM_SUCCESS(trickyKnowledgePointDTOS);

    }

    /**
     * 获取用户待复习题目列表
     */
    @GetMapping("/list")
    @GlobalInterception
    public Response<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> subject,
            @RequestParam(required = false) List<String> errorType,
            @RequestParam(required = false) String timeRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = UserContext.getUserId();
        Page<MistakeQuestionEntity> result = null;
        try {
            MistakeQueryParamsVO params = new MistakeQueryParamsVO();
            params.setUserId(userId);
            params.setKeyword(keyword);
            params.setSubject(subject);
            params.setErrorType(errorType);
            // 添加空值检查
            if (timeRange != null && !timeRange.isEmpty()) {
                params.setTimeRange(TimeRange.valueOf(timeRange.toUpperCase()));
            } else {
                // 设置默认值或处理空值情况
                params.setTimeRange(TimeRange.MORE_THAN_ONE_WEEK); // 或其他合适的默认值
            }
            params.setPage(page);
            params.setSize(size);

            log.info("用户筛选获取待复习题目列表开始，userId:{}", userId);
            result = reviewFeedbackService.searchAndFilter(params);
        } catch (IllegalArgumentException e) {
            log.error("用户筛选获取待复习题目列表失败！userId:{}", userId, e);
            return Response.CUSTOMIZE_ERROR(GET_REVIEW_QUESTION_LIST_FAIL);
        }
        return Response.SYSTEM_SUCCESS(result);
    }

    /**
     * 删除用户待复习题目
     */
    @DeleteMapping("/deleteBatch")
    @GlobalInterception
    public Response<String> deleteBatch(@RequestParam List<Integer> questionIds) {
        String userId = UserContext.getUserId();
        try {
            log.info("用户删除待复习题目开始，userId:{}", userId);
            reviewFeedbackService.deleteBatch(userId, questionIds);
        } catch (Exception e) {
            log.error("用户删除待复习题目失败！userId:{}", userId, e);
            return Response.CUSTOMIZE_ERROR(DELETE_REVIEW_QUESTION_FAIL);
        }
        return Response.SYSTEM_SUCCESS();
    }

    /**
     * 获取待复习题目统计信息
     */
    @GetMapping("/statistics")
    @GlobalInterception
    public Response<StatsDTO> getStatistics() {
        String userId = UserContext.getUserId();
        StatsVO statsVO = null;
        try {
            log.info("获取待复习题目统计信息开始");
            statsVO = reviewFeedbackService.getStatistics(userId);
        } catch (Exception e) {
            log.error("获取待复习题目统计信息失败！", e);
            return Response.CUSTOMIZE_ERROR(GET_STATISTICS_FAIL);
        }
        List<ReviewTrendDTO> reviewTrendDTOS = new ArrayList<>();
        if(statsVO.getReviewTrend() == null || statsVO.getReviewTrend().isEmpty()){
            reviewTrendDTOS.add(ReviewTrendDTO.builder()
                    .month("0")
                    .total(0)
                    .reviewed(0)
                    .completionRate(0.0)
                    .build());
        }else{
            reviewTrendDTOS = statsVO.getReviewTrend().stream().map(reviewTrendVO -> {
                if(reviewTrendVO == null || reviewTrendVO.getTotal() == 0){
                    return ReviewTrendDTO.builder()
                            .month(reviewTrendVO.getMonth())
                            .total(0)
                            .reviewed(0)
                            .completionRate(0.0)
                            .build();
                }
                return ReviewTrendDTO.builder()
                        .month(reviewTrendVO.getMonth())
                        .total(reviewTrendVO.getTotal())
                        .reviewed(reviewTrendVO.getReviewed())
                        .completionRate(reviewTrendVO.getReviewed()*1.0/reviewTrendVO.getTotal())
                        .build();
            }).toList();
        }

        StatsDTO statsDTO = StatsDTO.builder()
                .subjectDistribution(statsVO.getSubjectDistribution().stream().map(count -> {
                    Map<String, Integer> map = new HashMap<>();
                    map.put(count.getName(), count.getCount());
                    return map;
                }).collect(Collectors.toList()))
                .knowledgeDistribution(statsVO.getKnowledgeDistribution().stream().map(count -> {
                    Map<String, Integer> map = new HashMap<>();
                    map.put(count.getName(), count.getCount());
                    return map;
                }).collect(Collectors.toList()))
                .reviewTrend(reviewTrendDTOS).build();
        return Response.SYSTEM_SUCCESS(statsDTO);
    }

}
