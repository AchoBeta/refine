package com.achobeta.domain.question.service;

import com.achobeta.domain.question.model.valobj.QuestionResponseVO;

public interface IQuestionService {

    QuestionResponseVO questionGeneration(String userId, Integer knowledgePointId);

    void recordMistakeQuestion(String userId, String questionId);

    void removeQuestionCache(String questionId);

}
