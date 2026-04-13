package com.achobeta.infrastructure.dao;

import com.achobeta.domain.question.model.po.MistakeKnowledgePO;
import com.achobeta.infrastructure.dao.po.MistakePO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 错题数据访问接口
 */
@Mapper
public interface IMistakeQuestionMapper {

    @Insert("INSERT INTO MistakeQuestion (user_id, question_id, question_content, subject, other_reason, knowledge_point_id,create_time, update_time) " +
            "VALUES (#{userId}, #{questionId}, #{questionContent}, #{subject}, #{otherReason}, #{knowledgePointId}, #{createTime}, #{updateTime})")
    void insert(MistakePO mistakePO);

    @Select("SELECT subject, knowledge_point_id AS knowledgeId FROM MistakeQuestion WHERE id = #{mistakeQuestionId}")
    MistakeKnowledgePO findSubjectAndKnowledgeIdById(Integer mistakeQuestionId);
}