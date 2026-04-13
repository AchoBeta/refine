package com.achobeta.domain.overview.adapter.repository;

import com.achobeta.domain.Feetback.model.valobj.TrickyKnowledgePointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface IUserOverviewRepository {
    @Select("select count(distinct knowledge_point_id) from MistakeQuestion " +
            "where user_id = #{userId} and create_time >= subdate(now(), 14) and question_status = 0 " +
            "group by knowledge_point_id " +
            "having count(knowledge_point_id) >= 3")
    int getHardQuestions(String userId);

    @Select("select count(id) from MistakeQuestion " +
            "where user_id = #{userId}")
    int getQuestionsNum(String userId);

    @Select("select count(id) from MistakeQuestion " +
            "where user_id = #{userId} and question_status = 1")
    int getHardQuestionsNum(String userId);

    @Update("update UserData set hard_questions = #{hardQuestions}, " +
                                "questions_num = #{questionsNum}, " +
                                "review_rate = #{reviewRate} " +
            "where user_id = #{userId}")
    void updateUserOverview(String userId, int hardQuestions, int questionsNum, double reviewRate);

    @Select("select user_id from UserInformation where user_status = 1")
    List<String> getUserIds();

    @Update("update UserData set study_time = study_time + #{duration} " +
            "where user_id = #{userId}")
    void updateUserDuration(String userId, int duration);
}
