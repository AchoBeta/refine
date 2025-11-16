package com.achobeta.domain.question.adapter.repository;

import com.achobeta.domain.question.model.entity.MistakeQuestionEntity;
import com.achobeta.domain.question.model.po.MistakeKnowledgePO;
import com.achobeta.domain.question.model.valobj.MistakeQuestionVO;

/**
 * 错题仓储接口（领域层定义，基础设施层实现）
 */
public interface IMistakeRepository {
    // 保存错题
    void save(MistakeQuestionEntity mistakeEntity);

    MistakeKnowledgePO findSubjectAndKnowledgeIdById(Integer mistakeQuestionId);

    void setValue(String s, MistakeQuestionVO mistakeQuestionDTO, Long expired);

    MistakeQuestionVO getValue(String s);

    void remove(String s);
}