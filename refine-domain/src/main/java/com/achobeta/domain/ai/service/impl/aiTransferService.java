package com.achobeta.domain.ai.service.impl;

import com.achobeta.domain.ai.service.IAiTransferService;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @Auth : Malog
 * @Desc : 抽取第一个问题
 * @Time : 2025/11/2 14:34
 */
@Slf4j
@Service
@ConfigurationProperties(prefix = "dashscope")
public class aiTransferService implements IAiTransferService {

    /**
     * API密钥，用于访问大模型服务。
     */
    @Setter
    private String apiKey;

    /**
     * 使用的大模型名称，默认为 "qwen3-max"。
     */
    @Setter
    private String MODEL_NAME = "qwen3-max";

    @Override
    public String extractTheFirstQuestion(String content) {
        // 检查输入是否为空
        if (content == null || content.trim().isEmpty()) {
            log.warn("输入内容为空，无法提取问题");
            return "";
        }

        try {
            Generation gen = new Generation();
            // 构造提示词，明确提取规则和示例
            String prompt = """
                    请从以下大段文本中准确识别并提取第一个完整且可识别的题目。提取规则如下：
                    
                    如果该题目是选择题（包括单选或多选），请完整提取题干和所有选项（如 A、B、C、D 等），确保格式清晰、内容连贯；
                    如果该题目是非选择题（如填空题、简答题、编程题等），则仅提取完整的题干信息，不要包含答案、解析或后续无关内容；
                    忽略试卷标题、页眉页脚、考生信息、承诺声明、得分栏等与题目本身无关的内容；
                    以题目编号（如“1.”、“2.”、“一、”等）作为判断起点，但必须确保提取的是一个语义完整、结构清晰的问题；
                    若识别为填空题，需要填的空用四个下划线“____”占位置
                    若无法识别任何有效题目，请返回空字符串。
                    请严格遵循上述规则，仅输出提取结果，不要添加解释或额外内容。
                    
                    示例 1（选择题）：
                    输入文本包含：
                    
                    1.一个C程序的基本结构是(
                    (A)一个主函数和若干个非主函数
                    (B)若干个主函数和若干个非主函数
                    (C)一个主函数和最多一个非主函数
                    (D)若干个主函数和最多一个非主函数\s
                    
                    输出应为：
                    1.一个C程序的基本结构是(
                    (A)一个主函数和若干个非主函数
                    (B)若干个主函数和若干个非主函数
                    (C)一个主函数和最多一个非主函数
                    (D)若干个主函数和最多一个非主函数
                    
                    示例 2（非选择题）：
                    输入文本包含：
                    
                    编写一个C语言函数，实现输入一个整数n，输出其阶乘值。要求使用递归方法实现。
                    输出应为：
                    
                    编写一个C语言函数，实现输入一个整数n，输出其阶乘值。要求使用递归方法实现。
                    """;

            // 构建系统角色消息，定义助手的行为目标
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是一个题目题干提取助手，善于从大段文字中提取出第一个出现的完整题目。")
                    .build();

            // 构建用户消息，将提示词与实际内容拼接传入
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt + "\n" + "请从以下文本中提取第一个问题：\n\n" + content)
                    .build();

            // 设置调用参数，包括API Key、模型名及上下文消息列表
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(MODEL_NAME)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            // 调用大模型接口获取结果
            GenerationResult result = gen.call(param);

            // 判断返回结果的有效性并提取内容
            if (result != null && result.getOutput() != null
                    && !result.getOutput().getChoices().isEmpty()
                    && result.getOutput().getChoices().get(0).getMessage() != null) {
                return result.getOutput().getChoices().get(0).getMessage().getContent().trim();
            } else {
                log.warn("模型返回结果为空或格式异常");
                return "";
            }

        } catch (Exception e) {
            // 记录异常日志便于排查问题
            log.error("调用大模型提取问题失败，输入内容: {}", content, e);
            return "";
        }
    }
}

