package com.achobeta.domain.Feetback.adapter.repository;

import com.achobeta.domain.Feetback.model.valobj.TrickyKnowledgePointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ITrickyKnowledgeRepository {
    /**
     * 获取用户近期出错多的知识点
     * @param userId
     * @return
     */
    @Select("select m.knowledge_point_id, k.knowledge_desc from MistakeQuestion m" +
            " join knowledgePoint k on m.knowledge_point_id = k.knowledge_point_id " +
            "where m.user_id = #{userId} and question_status = 0 and m.create_time >= now() - interval 14 day " +
            "group by m.knowledge_point_id, k.knowledge_desc " +
            "having count(m.knowledge_point_id) >= 3")
    List<TrickyKnowledgePointVO> getTrickyKnowledgePoints(String userId);

}
