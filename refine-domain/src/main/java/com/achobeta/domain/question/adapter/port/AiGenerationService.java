package com.achobeta.domain.question.adapter.port;

import com.achobeta.domain.question.model.valobj.QuestionResponseVO;
import dev.langchain4j.service.SystemMessage;

/**
 * 语言模型接口
 */
public interface AiGenerationService {

    @SystemMessage(fromResource = "AiGeneration.txt")
    QuestionResponseVO Generation(String message);

}
