package com.achobeta.domain.rag.service.impl;

import com.achobeta.domain.rag.service.ILearningDynamicsService;
import com.achobeta.domain.rag.model.valobj.LearningDynamicVO;
import com.achobeta.domain.rag.model.valobj.LearningStatisticsVO;
import com.achobeta.domain.rag.model.entity.LearningVectorEntity;
import com.achobeta.domain.rag.adapter.port.ILearningDataRepository;
import com.achobeta.domain.ai.service.IAiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LearningDynamicsServiceImpl implements ILearningDynamicsService {

    @Autowired
    private ILearningDataRepository learningDataRepository;

    @Autowired
    private IAiService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<LearningDynamicVO> analyzeUserLearningDynamics(String userId) {
        try {
            log.info("开始分析用户学习动态，userId:{}", userId);

            // 1. 获取用户最近7天的学习数据
            String learningDataSummary = getUserLearningDataSummary(userId, 7);

            if (learningDataSummary.isEmpty()) {
                log.info("用户最近7天无学习数据，userId:{}", userId);
                return Collections.emptyList();
            }

            // 2. 构建AI分析提示词
            String analysisPrompt = buildAnalysisPrompt(learningDataSummary);

            // 3. 使用AI分析学习动态
            List<LearningDynamicVO> dynamics = new ArrayList<>();
            // 异步执行AI分析任务，解析题目并生成学习动态
            CompletableFuture<Void> aiAnalysis = CompletableFuture.runAsync(() -> {
                aiService.aiChat(analysisPrompt, response -> {
                    try {
                        // 解析AI返回的JSON格式学习动态
                        List<LearningDynamicVO> parsedDynamics = parseAIResponse(response);
                        dynamics.addAll(parsedDynamics);
                    } catch (Exception e) {
                        log.error("解析AI分析结果失败", e);
                    }
                });
            });


            // 等待AI分析完成，最多等待30秒
            try {
                aiAnalysis.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("AI分析超时或失败", e);
                // 如果AI分析失败，返回基于规则的分析结果
                return generateRuleBasedDynamics(userId);
            }
            // 4. 限制返回最多3条动态
            List<LearningDynamicVO> result = dynamics.stream()
                    .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                    .limit(3)
                    .collect(Collectors.toList());

            log.info("用户学习动态分析完成，userId:{} 动态数量:{}", userId, result.size());
            return result;

        } catch (Exception e) {
            log.error("分析用户学习动态失败，userId:{}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getUserLearningDataSummary(String userId, int days) {
        try {
            // 获取基础统计数据
            LearningStatisticsVO statistics = learningDataRepository.getUserLearningStatistics(userId, days);

            // 获取按科目分组的数据
            List<Map<String, Object>> subjectData = learningDataRepository.getUserLearningDataBySubject(userId, days);

            // 获取按行为类型分组的数据
            List<Map<String, Object>> actionData = learningDataRepository.getUserLearningDataByActionType(userId, days);

            // 获取详细的学习记录
            List<LearningVectorEntity> recentData = learningDataRepository.getUserRecentLearningData(userId, days);

            // 构建学习数据摘要
            StringBuilder summary = new StringBuilder();
            summary.append("用户最近").append(days).append("天学习数据摘要：\n\n");

            // 基础统计
            if (statistics != null) {
                summary.append("总体统计：\n");
                summary.append("- 总学习活动次数：").append(statistics.getTotalActivities()).append("\n");
                summary.append("- 涉及科目数量：").append(statistics.getSubjectsCount()).append("\n");
                summary.append("- 活跃天数：").append(statistics.getActiveDays()).append("\n");
                summary.append("- 学习行为类型数：").append(statistics.getActionTypesCount()).append("\n\n");
            }

            // 科目分析
            if (!subjectData.isEmpty()) {
                summary.append("科目学习情况：\n");
                for (Map<String, Object> subject : subjectData) {
                    summary.append("- ").append(subject.get("subject"))
                            .append("：").append(subject.get("activity_count")).append("次活动")
                            .append("，行为类型：").append(subject.get("action_list")).append("\n");
                }
                summary.append("\n");
            }

            // 行为类型分析
            if (!actionData.isEmpty()) {
                summary.append("学习行为分析：\n");
                for (Map<String, Object> action : actionData) {
                    summary.append("- ").append(getActionDescription(action.get("action_type").toString()))
                            .append("：").append(action.get("activity_count")).append("次")
                            .append("，涉及").append(action.get("subjects_count")).append("个科目\n");
                }
                summary.append("\n");
            }

            // 最近学习记录
            if (!recentData.isEmpty()) {
                summary.append("最近学习记录（最新5条）：\n");
                recentData.stream().limit(5).forEach(record -> {
                    summary.append("- ").append(record.getCreatedAt().toString())
                            .append(" ").append(getActionDescription(record.getActionType()))
                            .append(" ").append(record.getSubject() != null ? record.getSubject() : "未分类")
                            .append("题目\n");
                });
            }

            return summary.toString();

        } catch (Exception e) {
            log.error("获取用户学习数据摘要失败，userId:{}", userId, e);
            return "";
        }
    }

    /**
     * 构建AI分析提示词
     */
    private String buildAnalysisPrompt(String learningDataSummary) {
        return String.format("""
                请基于以下用户学习数据，分析用户的学习动态，并返回3条最重要的学习动态信息。
                
                学习数据：
                %s
                
                请按照以下JSON格式返回分析结果，每条动态都要有明确的类型、标题、描述和建议：
                
                [
                  {
                    "type": "progress|weakness|achievement",
                    "title": "动态标题",
                    "description": "详细描述",
                    "subject": "相关科目",
                    "priority": 1-5,
                    "suggestion": "建议行动",
                    "relatedQuestionCount": 数量
                  }
                ]
                
                动态类型说明：
                - progress: 学习进步，如某科目练习增加、错误率下降等
                - weakness: 发现薄弱点，如某科目错误较多、学习频率低等
                - achievement: 学习成就，如连续学习天数、完成学习目标等
                
                请确保返回的是有效的JSON格式，优先级高的动态排在前面。
                """, learningDataSummary);
    }

    /**
     * 解析AI返回的分析结果
     */
    private List<LearningDynamicVO> parseAIResponse(String aiResponse) {
        try {
            // 提取JSON部分（AI可能返回额外的文本）
            String jsonPart = extractJsonFromResponse(aiResponse);

            // 解析JSON
            List<Map<String, Object>> dynamicMaps = objectMapper.readValue(
                    jsonPart, new TypeReference<List<Map<String, Object>>>() {
                    });

            List<LearningDynamicVO> dynamics = new ArrayList<>();
            for (Map<String, Object> map : dynamicMaps) {
                LearningDynamicVO dynamic = LearningDynamicVO.builder()
                        .type(map.get("type").toString())
                        .title(map.get("title").toString())
                        .description(map.get("description").toString())
                        .subject(map.get("subject") != null ? map.get("subject").toString() : null)
                        .priority(Integer.parseInt(map.get("priority").toString()))
                        .suggestion(map.get("suggestion") != null ? map.get("suggestion").toString() : null)
                        .relatedQuestionCount(map.get("relatedQuestionCount") != null ?
                                Integer.parseInt(map.get("relatedQuestionCount").toString()) : 0)
                        .build();
                dynamics.add(dynamic);
            }

            return dynamics;

        } catch (Exception e) {
            log.error("解析AI分析结果失败：{}", aiResponse, e);
            return Collections.emptyList();
        }
    }

    /**
     * 从AI响应中提取JSON部分
     */
    private String extractJsonFromResponse(String response) {
        // 查找JSON数组的开始和结束
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');

        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }
        throw new IllegalArgumentException("无法从AI响应中提取有效的JSON");
    }

    /**
     * 基于规则生成学习动态（AI分析失败时的备选方案）
     */
    private List<LearningDynamicVO> generateRuleBasedDynamics(String userId) {
        try {
            List<LearningDynamicVO> dynamics = new ArrayList<>();

            // 获取统计数据
            LearningStatisticsVO statistics = learningDataRepository.getUserLearningStatistics(userId, 7);
            List<Map<String, Object>> subjectData = learningDataRepository.getUserLearningDataBySubject(userId, 7);
            List<Map<String, Object>> actionData = learningDataRepository.getUserLearningDataByActionType(userId, 7);

            // 规则1：学习活跃度分析
            if (statistics != null) {
                int totalActivities = statistics.getTotalActivities();
                int activeDays = statistics.getActiveDays();

                if (activeDays >= 5) {
                    dynamics.add(LearningDynamicVO.builder()
                            .type("achievement")
                            .title("学习坚持性优秀")
                            .description(String.format("最近7天中有%d天进行了学习，共完成%d次学习活动", activeDays, totalActivities))
                            .priority(4)
                            .suggestion("继续保持良好的学习习惯")
                            .relatedQuestionCount(totalActivities)
                            .build());
                } else if (activeDays <= 2) {
                    dynamics.add(LearningDynamicVO.builder()
                            .type("weakness")
                            .title("学习频率偏低")
                            .description(String.format("最近7天仅有%d天进行了学习", activeDays))
                            .priority(5)
                            .suggestion("建议制定每日学习计划，提高学习频率")
                            .relatedQuestionCount(totalActivities)
                            .build());
                }
            }

            // 规则2：科目分析
            if (!subjectData.isEmpty()) {
                Map<String, Object> mostActiveSubject = subjectData.get(0);
                int activityCount = Integer.parseInt(mostActiveSubject.get("activity_count").toString());
                dynamics.add(LearningDynamicVO.builder()
                        .type("progress")
                        .title("重点科目学习进展")
                        .description(String.format("%s是您最活跃的学习科目，完成了%d次学习活动",
                                mostActiveSubject.get("subject"), activityCount))
                        .subject(mostActiveSubject.get("subject").toString())
                        .priority(3)
                        .suggestion("可以将这种学习热情扩展到其他科目")
                        .relatedQuestionCount(activityCount)
                        .build());
            }

            // 规则3：学习行为分析
            if (!actionData.isEmpty()) {
                for (Map<String, Object> action : actionData) {
                    String actionType = action.get("action_type").toString();
                    int count = Integer.parseInt(action.get("activity_count").toString());

                    if ("mistake".equals(actionType) && count > 3) {
                        dynamics.add(LearningDynamicVO.builder()
                                .type("weakness")
                                .title("错题较多需要关注")
                                .description(String.format("最近7天出现了%d次错题，需要加强相关知识点的学习", count))
                                .priority(4)
                                .suggestion("建议重点复习错题，总结错误原因")
                                .relatedQuestionCount(count)
                                .build());
                        break;
                    }
                }
            }

            // 限制返回3条，按优先级排序
            return dynamics.stream()
                    .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                    .limit(3)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("生成基于规则的学习动态失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取行为类型描述
     */
    private String getActionDescription(String actionType) {
        switch (actionType) {
            case "upload":
                return "上传";
            case "review":
                return "复习";
            case "qa":
                return "问答";
            case "mistake":
                return "错题";
            default:
                return "学习";
        }
    }
}
