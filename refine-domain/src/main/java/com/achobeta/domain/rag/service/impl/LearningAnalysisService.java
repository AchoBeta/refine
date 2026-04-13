package com.achobeta.domain.rag.service.impl;

import com.achobeta.domain.rag.service.IVectorService;
import com.achobeta.domain.rag.service.ILearningDynamicsService;
import com.achobeta.domain.rag.model.valobj.LearningInsightVO;
import com.achobeta.domain.rag.model.valobj.LearningDynamicVO;
import com.achobeta.domain.ai.service.IAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LearningAnalysisService {

    @Autowired
    private IVectorService vectorService;

    @Autowired
    private ILearningDynamicsService learningDynamicsService;

    /**
     * 用户登录时触发的学习动态分析
     *
     * @param userId 用户ID
     */
    @Async
    public void onUserLogin(String userId) {
        try {
            log.info("用户登录触发学习动态分析，userId:{}", userId);

            // 分析用户最近7天的学习动态
            List<LearningDynamicVO> dynamics = learningDynamicsService.analyzeUserLearningDynamics(userId);

            log.info("用户登录学习动态分析完成，userId:{} 动态数量:{}", userId, dynamics.size());

        } catch (Exception e) {
            log.error("用户登录学习动态分析失败，userId:{}", userId, e);
        }
    }

    /**
     * 获取用户学习洞察
     */
    public List<LearningInsightVO> getUserLearningInsights(String userId) {
        try {
            log.info("获取用户学习洞察，userId:{}", userId);

            // 先触发实时分析
            vectorService.analyzeUserLearningPatterns(userId);

            // 获取薄弱点分析
            List<LearningInsightVO> weaknesses = vectorService.getUserWeaknesses(userId);

            // 获取学习推荐
            List<LearningInsightVO> recommendations = vectorService.getUserRecommendations(userId);

            // 合并结果
            List<LearningInsightVO> allInsights = new java.util.ArrayList<>();
            allInsights.addAll(weaknesses);
            allInsights.addAll(recommendations);

            log.info("获取用户学习洞察完成，userId:{} 洞察数量:{}", userId, allInsights.size());
            return allInsights;

        } catch (Exception e) {
            log.error("获取用户学习洞察失败，userId:{}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户学习动态
     */
    public List<LearningDynamicVO> getUserLearningDynamics(String userId) {
        try {
            log.info("获取用户学习动态，userId:{}", userId);
            return learningDynamicsService.analyzeUserLearningDynamics(userId);
        } catch (Exception e) {
            log.error("获取用户学习动态失败，userId:{}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户薄弱点分析
     */
    public List<LearningInsightVO> getUserWeaknesses(String userId) {
        try {
            log.info("获取用户薄弱点分析，userId:{}", userId);
            return vectorService.getUserWeaknesses(userId);
        } catch (Exception e) {
            log.error("获取用户薄弱点分析失败，userId:{}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户学习推荐
     */
    public List<LearningInsightVO> getUserRecommendations(String userId) {
        try {
            log.info("获取用户学习推荐，userId:{}", userId);
            return vectorService.getUserRecommendations(userId);
        } catch (Exception e) {
            log.error("获取用户学习推荐失败，userId:{}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 手动触发用户学习分析
     */
    public boolean triggerUserAnalysis(String userId) {
        try {
            log.info("手动触发用户学习分析，userId:{}", userId);
            vectorService.analyzeUserLearningPatterns(userId);
            return true;
        } catch (Exception e) {
            log.error("手动触发用户学习分析失败，userId:{}", userId, e);
            return false;
        }
    }
}