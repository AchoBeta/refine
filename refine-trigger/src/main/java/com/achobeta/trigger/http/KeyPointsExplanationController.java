package com.achobeta.trigger.http;


import com.achobeta.api.dto.*;
import com.achobeta.domain.IRedisService;
import com.achobeta.domain.keypoints_explanation.model.valobj.*;
import com.achobeta.domain.keypoints_explanation.service.IKeyPointsExplanationService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.Constants;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.achobeta.types.enums.GlobalServiceStatusCode.*;

/**
 * 知识点解释接口
 */
@Slf4j
@Validated
@CrossOrigin("${app.config.cross-origin}:*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/${app.config.api-version}/keypoints_explanation")
public class KeyPointsExplanationController {
    private final IKeyPointsExplanationService keyPointsExplanationService;

    /**
     * 根据学科获取中心知识点
     * @param subject
     * @return
     */
    @GetMapping("/get_key_points")
    @GlobalInterception
    public Response<List<KeyPointsDTO>> getKeyPoints(@Param("subject") String subject) {
        String userId = UserContext.getUserId();
        List<KeyPointsVO> keyPoints = null;
        try {
            log.info("用户获取中心知识点，subject:{}", subject);
            keyPoints = keyPointsExplanationService.getKeyPoints(subject, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(GET_KEY_POINTS_FAIL);
        }
        if (keyPoints == null || keyPoints.isEmpty()) {
            return Response.SYSTEM_SUCCESS(new ArrayList<>());
        }
        return Response.SYSTEM_SUCCESS(keyPoints.stream()
                .map(keyPointsVO -> KeyPointsDTO.builder()
                        .id(keyPointsVO.getId())
                        .keyPoints(keyPointsVO.getKeyPoints())
                        .build())
                .toList());
    }
    /**
     * 获取子知识点
     * @param knowledgeId 父知识点id
     * @return
     */
    @GetMapping("/get_son_key_points")
    @GlobalInterception
    public Response<List<KeyPointsDTO>> getSonKeyPoints(@Param("knowledgeId") String knowledgeId) {
        String userId = UserContext.getUserId();
        List<KeyPointsVO> keyPointsVOs = null;
        try {
            log.info("用户获取子知识点，knowledgeId:{}, userId:{}", knowledgeId, userId);
            keyPointsVOs = keyPointsExplanationService.getSonKeyPoints(knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(GET_SON_KEY_POINTS_FAIL);
        }
        //空值判断
        if (keyPointsVOs == null || keyPointsVOs.isEmpty()) {
            return Response.SYSTEM_SUCCESS(new ArrayList<>());
        }
        return Response.SYSTEM_SUCCESS(keyPointsVOs.stream()
                .map(keyPointsVO -> KeyPointsDTO.builder()
                        .id(keyPointsVO.getId())
                        .keyPoints(keyPointsVO.getKeyPoints())
                        .build())
                .toList());
    }

    /**
     * 获取知识点详情
     */
    @GetMapping("/{knowledgeId}")
    @GlobalInterception
    public Response<String> getKnowledgePoint(@PathVariable String knowledgeId ) {
        String userId = UserContext.getUserId();
        String point = null;
        try {
            point = keyPointsExplanationService.getKnowledgedescById(knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(GET_KNOWLEDGE_POINT_DESC_FAIL);
        }
        if (point == null || point.isEmpty()) {
            return Response.SYSTEM_SUCCESS("暂无知识点详情");
        }
        return Response.SYSTEM_SUCCESS(point);
    }

    /**
     * 获取相关错题统计数量
     * todo 完善学习反馈模块获取错题内容
     */
    @GetMapping("/{knowledgeId}/related-questions-statistic")
    @GlobalInterception
    public Response<String> getRelatedWrongQuestionsStatistic(
            @PathVariable String knowledgeId ) {
        String userId = UserContext.getUserId();
        WrongQuestionVO questions = null;

        try {
            questions = keyPointsExplanationService.getRelatedWrongQuestionsStatistic(knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(GET_RELATED_MESSAGES_FAIL);
        }
        if(questions.getUpdateCount() == 0){
            String res = "暂无相关错题";
            return Response.SYSTEM_SUCCESS(res);
        }
        String res = "该知识点上传了" + questions.getUpdateCount() + "道题，其中本周复习了" + questions.getReviewCount() + "道题";
        return Response.SYSTEM_SUCCESS(res);
    }

    /**
     * 获取相关错题或笔记
     *
     */
    @GetMapping("/{knowledgeId}/related-questions")
    @GlobalInterception
    public Response<RelateQeustionDTO> getRelatedWrongQuestions(
            @PathVariable String knowledgeId ) {
        String userId = UserContext.getUserId();
        RelateQuestionVO relatedQuestions = null;
        try {
            relatedQuestions = keyPointsExplanationService.getRelatedWrongQuestions(knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(GET_RELATED_MESSAGES_FAIL);
        }
        if (relatedQuestions == null){
            return Response.SYSTEM_SUCCESS(RelateQeustionDTO.builder()
                    .questions(new ArrayList<>())
                    .note("")
                    .build());
        }
        return Response.SYSTEM_SUCCESS(RelateQeustionDTO.builder()
                .questions(relatedQuestions.getQestions().stream()
                        .map(questionVO -> QuestionDTO.builder()
                                .id(questionVO.getId())
                                .question(questionVO.getQuestion())
                                .build())
                        .toList())
                .note(relatedQuestions.getNote())
                .build());
    }

    /**
     * 标记知识点已掌握
     */
    @PostMapping("/{knowledgeId}/mark-as-mastered")
    @GlobalInterception
    public Response<String> markAsMastered(@PathVariable String knowledgeId ) {
        String userId = UserContext.getUserId();
        try {
            keyPointsExplanationService.markAsMastered(knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(MARK_AS_MASTERED_FAIL);
        }
        return Response.SYSTEM_SUCCESS("已修改成功");
    }

    /**
     * 获取相关知识点
     */
    @GetMapping("/{knowledgeId}/related-points")
    @GlobalInterception
    public Response<List<KeyPointsDTO>> getRelatedKnowledgePoints(
            @PathVariable String knowledgeId ) {
        String userId = UserContext.getUserId();
        List<KeyPointsVO> relatedPoints = null;
        try {
            relatedPoints = keyPointsExplanationService.getRelatedKnowledgePoints(knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(GET_RELATED_POINTS_FAIL);
        }
        if (relatedPoints == null || relatedPoints.isEmpty()){
            return Response.SYSTEM_SUCCESS(new ArrayList<>());
        }
        return Response.SYSTEM_SUCCESS(relatedPoints.stream()
                .map(relatedPoint -> KeyPointsDTO.builder()
                        .id(relatedPoint.getId())
                        .keyPoints(relatedPoint.getKeyPoints())
                        .build())
                .toList());
    }

    /**
     * 保存或更新学生笔记
     */
    @PostMapping("/{knowledgeId}/notes")
    @GlobalInterception
    public Response<String> saveOrUpdateStudentNote(@PathVariable String knowledgeId, @RequestBody String note ) {
        String userId = UserContext.getUserId();
        try {
            keyPointsExplanationService.savedNote(note, knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(SAVE_OR_UPDATE_NOTE_FAIL);
        }
        return Response.SYSTEM_SUCCESS("笔记更新成功");
    }

    /**
     * 节点重命名
     */
    @PostMapping("/{knowledgeId}/rename")
    @GlobalInterception
    public Response<String> renameNode(@PathVariable String knowledgeId, @RequestBody String newName ) {
        String userId = UserContext.getUserId();
        try {
            keyPointsExplanationService.renameNode(knowledgeId, newName, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(RENAME_NODE_FAIL);
        }
        return Response.SYSTEM_SUCCESS("重命名成功");
    }

    /**
     * 显示tooltip
     */
    @GetMapping("/{knowledgeId}/show-tooltip")
    @GlobalInterception
    public Response<?> showTooltip(@PathVariable String knowledgeId ) {
        String userId = UserContext.getUserId();
        ToolTipVO tooltip = null;
        try {
            tooltip = keyPointsExplanationService.gettooltipById(knowledgeId, userId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(SHOW_TOOLTIP_FAIL);
        }
        if(tooltip == null || tooltip.getTotal() == 0){
            ToolTipDTO tooltipDTO = ToolTipDTO.builder()
                    .count(0)
                    .lastReviewTime("")
                    .degreeOfProficiency(0)
                    .build();
            return Response.SYSTEM_SUCCESS(tooltipDTO);
        }
        double degreeOfProficiency = 1.0 * tooltip.getCount() / tooltip.getTotal();
        return Response.SYSTEM_SUCCESS(ToolTipDTO.builder()
                .count(tooltip.getCount())
                .lastReviewTime(tooltip.getLastReviewTime())
                .degreeOfProficiency(degreeOfProficiency)
                .build());
    }

    /**
     * 添加子知识点
     */
    @PostMapping("/{knowledgeId}/add-son-point")
    @GlobalInterception
    public Response<String> addSonPoint(@PathVariable String knowledgeId, @RequestBody SonPointVO sonPoints ) {
        String userId = UserContext.getUserId();
        try {
            keyPointsExplanationService.addSonPoint(sonPoints, userId, knowledgeId);
        } catch (Exception e) {
            return Response.CUSTOMIZE_ERROR(ADD_SON_POINT_FAIL);
        }
        return Response.SYSTEM_SUCCESS("添加成功");
    }

}
