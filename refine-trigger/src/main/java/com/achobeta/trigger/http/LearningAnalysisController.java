package com.achobeta.trigger.http;

import com.achobeta.domain.rag.service.impl.LearningAnalysisService;
import com.achobeta.domain.rag.model.valobj.LearningDynamicVO;
import com.achobeta.domain.rag.model.valobj.LearningInsightVO;
import com.achobeta.domain.rag.model.valobj.SimilarQuestionVO;
import com.achobeta.domain.rag.service.IVectorService;
import com.achobeta.api.dto.LearningDynamicResponseDTO;
import com.achobeta.api.dto.LearningInsightResponseDTO;
import com.achobeta.api.dto.SimilarQuestionResponseDTO;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auth : Malog
 * @Desc : 学习分析控制器
 * @Time : 2025/11/25
 */
@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/learning-analysis")
@CrossOrigin("${app.config.cross-origin}")
public class LearningAnalysisController {

    @Autowired
    private LearningAnalysisService learningAnalysisService;

    @Autowired
    private IVectorService vectorService;

    /**
     * 获取用户学习洞察
     */
    @GetMapping("/insights")
    @GlobalInterception
    public Response<List<LearningInsightResponseDTO>> getUserLearningInsights() {
        try {
            String userId = UserContext.getUserId();
            log.info("获取用户学习洞察开始，userId:{}", userId);

            List<LearningInsightVO> insights = learningAnalysisService.getUserLearningInsights(userId);

            List<LearningInsightResponseDTO> response = insights.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            log.info("获取用户学习洞察成功，userId:{} 洞察数量:{}", userId, response.size());
            return Response.SYSTEM_SUCCESS(response);

        } catch (Exception e) {
            log.error("获取用户学习洞察失败", e);
            return Response.SERVICE_ERROR("获取学习洞察失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户薄弱点分析
     */
    @GetMapping("/weaknesses")
    @GlobalInterception
    public Response<List<LearningInsightResponseDTO>> getUserWeaknesses() {
        try {
            String userId = UserContext.getUserId();
            log.info("获取用户薄弱点分析开始，userId:{}", userId);

            List<LearningInsightVO> weaknesses = learningAnalysisService.getUserWeaknesses(userId);

            List<LearningInsightResponseDTO> response = weaknesses.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            log.info("获取用户薄弱点分析成功，userId:{} 薄弱点数量:{}", userId, response.size());
            return Response.SYSTEM_SUCCESS(response);

        } catch (Exception e) {
            log.error("获取用户薄弱点分析失败", e);
            return Response.SERVICE_ERROR("获取薄弱点分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户学习推荐
     */
    @GetMapping("/recommendations")
    @GlobalInterception
    public Response<List<LearningInsightResponseDTO>> getUserRecommendations() {
        try {
            String userId = UserContext.getUserId();
            log.info("获取用户学习推荐开始，userId:{}", userId);
            List<LearningInsightVO> recommendations = learningAnalysisService.getUserRecommendations(userId);

            List<LearningInsightResponseDTO> response = recommendations.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            log.info("获取用户学习推荐成功，userId:{} 推荐数量:{}", userId, response.size());
            return Response.SYSTEM_SUCCESS(response);

        } catch (Exception e) {
            log.error("获取用户学习推荐失败", e);
            return Response.SERVICE_ERROR("获取学习推荐失败: " + e.getMessage());
        }
    }

    /**
     * 搜索相似题目
     */
    @GetMapping("/similar-questions")
    @GlobalInterception
    public Response<List<SimilarQuestionResponseDTO>> searchSimilarQuestions(
            @RequestParam String queryText,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            String userId = UserContext.getUserId();
            log.info("搜索相似题目开始，userId:{} queryText:{} limit:{}", userId, queryText, limit);

            List<SimilarQuestionVO> similarQuestions = vectorService.searchSimilarQuestions(userId, queryText, limit);

            List<SimilarQuestionResponseDTO> response = similarQuestions.stream()
                    .map(this::convertToSimilarQuestionResponseDTO)
                    .collect(Collectors.toList());
            log.info("搜索相似题目成功，userId:{} 结果数量:{}", userId, response.size());
            return Response.SYSTEM_SUCCESS(response);

        } catch (Exception e) {
            log.error("搜索相似题目失败", e);
            return Response.SERVICE_ERROR("搜索相似题目失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发学习分析
     */
    @PostMapping("/trigger-analysis")
    @GlobalInterception
    public Response<Boolean> triggerAnalysis() {
        try {
            String userId = UserContext.getUserId();
            log.info("手动触发学习分析开始，userId:{}", userId);

            boolean success = learningAnalysisService.triggerUserAnalysis(userId);

            if (success) {
                log.info("手动触发学习分析成功，userId:{}", userId);
                return Response.SYSTEM_SUCCESS(true);
            } else {
                log.warn("手动触发学习分析失败，userId:{}", userId);
                return Response.SERVICE_ERROR("触发学习分析失败");
            }

        } catch (Exception e) {
            log.error("手动触发学习分析失败", e);
            return Response.SERVICE_ERROR("触发学习分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户学习动态
     */
    @GetMapping("/dynamics")
    @GlobalInterception
    public Response<List<LearningDynamicResponseDTO>> getUserLearningDynamics() {
        try {
            String userId = UserContext.getUserId();
            log.info("获取用户学习动态开始，userId:{}", userId);
            List<LearningDynamicVO> dynamics = learningAnalysisService.getUserLearningDynamics(userId);

            List<LearningDynamicResponseDTO> response = dynamics.stream()
                    .map(this::convertToLearningDynamicResponseDTO)
                    .collect(Collectors.toList());
            log.info("获取用户学习动态成功，userId:{} 动态数量:{}", userId, response.size());
            return Response.SYSTEM_SUCCESS(response);

        } catch (Exception e) {
            log.error("获取用户学习动态失败", e);
            return Response.SERVICE_ERROR("获取学习动态失败: " + e.getMessage());
        }
    }

    /**
     * 转换为响应DTO
     */
    private LearningInsightResponseDTO convertToResponseDTO(LearningInsightVO vo) {
        return LearningInsightResponseDTO.builder()
                .type(vo.getType())
                .title(vo.getTitle())
                .description(vo.getDescription())
                .confidenceScore(vo.getConfidenceScore())
                .relatedQuestions(vo.getRelatedQuestions())
                .createdAt(vo.getCreatedAt() != null ? vo.getCreatedAt().toString() : null)
                .isActive(vo.getIsActive())
                .build();
    }

    /**
     * 转换为相似题目响应DTO
     */
    private SimilarQuestionResponseDTO convertToSimilarQuestionResponseDTO(SimilarQuestionVO vo) {
        return SimilarQuestionResponseDTO.builder()
                .questionId(vo.getQuestionId())
                .questionContent(vo.getQuestionContent())
                .actionType(vo.getActionType())
                .subject(vo.getSubject())
                .similarity(vo.getSimilarity())
                .createdAt(vo.getCreatedAt())
                .build();
    }

    /**
     * 转换为学习动态响应DTO
     */
    private LearningDynamicResponseDTO convertToLearningDynamicResponseDTO(LearningDynamicVO vo) {
        return LearningDynamicResponseDTO.builder()
                .type(vo.getType())
                .title(vo.getTitle())
                .description(vo.getDescription())
                .subject(vo.getSubject())
                .priority(vo.getPriority())
                .suggestion(vo.getSuggestion())
                .relatedQuestionCount(vo.getRelatedQuestionCount())
                .build();
    }
}