package com.achobeta.domain.keypoints_explanation.adapter.repository;

import cn.hutool.core.date.DateTime;
import com.achobeta.domain.keypoints_explanation.model.valobj.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KeyPointsMapper {
    @Select("select knowledge_point_id as id, knowledge_point_name as keyPoints from knowledgePoint" +
            " where user_id = #{userId} and parent_knowledge_point_id = #{knowledgeId}")
    List<KeyPointsVO> getSonKeyPoints(@Param("knowledgeId") String knowledgeId, @Param("userId") String userId);

    @Select("select knowledge_point_id as id, knowledge_point_name as keyPoints from knowledgePoint" +
            " where user_id = #{userId} and parent_knowledge_point_id = #{subjectId} ")
    List<KeyPointsVO> getKeyPoints(int subjectId, String userId);

    @Select("select knowledge_desc from knowledgePoint" +
            " where user_id = #{userId} and knowledge_point_id = #{knowledgeId} ")
    String getKnowledgedescById(String knowledgeId, String userId);

    @Select("select count(id) as updateCount, count(update_time >= now() - interval 7 day) as reviewCount from MistakeQuestion" +
            " where user_id = #{userId} and knowledge_point_id = #{knowledgeId}")
    WrongQuestionVO getRelatedWrongQuecstions(String knowledgeId, String userId);

    @Select("select k.parent_knowledge_point_id as id, k1.knowledge_point_name as keyPoints from knowledgePoint k " +
            "join knowledgePoint k1 on k.parent_knowledge_point_id = k1.knowledge_point_id" +
            " where k.user_id = #{userId} and k.knowledge_point_id = #{knowledgeId}")
    KeyPointsVO getParentKeyPoints(String knowledgeId, String userId);
    
    @Select("update knowledgePoint set note = #{note} where user_id = #{userId} and knowledge_point_id = #{knowledgeId}")
    void savedNote(String note, String knowledgeId, String userId);

    @Select("update knowledgePoint set status = 1  where user_id = #{userId} and knowledge_point_id = #{knowledgeId}")
    void markAsMastered(String knowledgeId, String userId);

    @Select("update knowledgePoint set knowledge_point_name = #{newName} where user_id = #{userId} and knowledge_point_id = #{knowledgeId}")
    void renameNode(String knowledgeId, String newName, String userId);

    @Select("select count(id) from MistakeQuestion where user_id = #{userId} and knowledge_point_id = #{knowledgeId}")
    int getTotalById(String knowledgeId, String userId);

    @Select("select count(id) from MistakeQuestion where knowledge_point_id = #{knowledgeId} and user_id = #{userId} and question_status = 0")
    int getCountById(String knowledgeId, String userId);

    @Select("select max(update_time) from MistakeQuestion where knowledge_point_id = #{knowledgeId} and user_id = #{userId}")
    String getLastReviewTimeById(String knowledgeId, String userId);

    @Select("select question_id as id, question_content as question from MistakeQuestion" +
            " where knowledge_point_id = #{knowledgeId} and user_id = #{userId} limit 3")
    List<QuestionVO> getRelatedQuestions(String knowledgeId, String userId);

    @Select("select note from knowledgePoint" +
            " where knowledge_point_id = #{knowledgeId} and user_id = #{userId}")
    String getNoteById(String knowledgeId, String userId);

    @Select("insert into knowledgePoint(knowledge_point_id, knowledge_point_name, knowledge_desc, parent_knowledge_point_id, user_id)" +
            " values(#{node.pointId}, #{node.pointName}, #{node.pointDesc}, #{parentId}, #{userId})")
    void saveMindMapTree(String userId, SonPointVO node, String parentId);

    @Insert("insert into knowledgePoint(knowledge_point_id, knowledge_point_name, user_id)" +
            " values(#{knowledgePointId}, #{knowledgePointName}, #{userId})")
    void insertNewPoint4MistakeQuestion(String userId, String knowledgePointId, String knowledgePointName);

}
