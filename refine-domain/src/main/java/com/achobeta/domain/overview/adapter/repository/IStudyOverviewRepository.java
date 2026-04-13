package com.achobeta.domain.overview.adapter.repository;

import com.achobeta.domain.overview.model.valobj.StudyOverviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface IStudyOverviewRepository {

    @Select("select questions_num, review_rate, hard_questions, study_time" +
            " from UserData where user_id = #{userId}")
    StudyOverviewVO queryStudyOverview(String userId);

    @Select("select count(*) from MistakeQuestion where user_id = #{userId} and create_time > #{localDateTime}")
    Integer countByUserIdAndCreateTimeAfter(@Param("userId") String userId, LocalDateTime localDateTime);

    @Select("select count(*) from MistakeQuestion where user_id = #{userId} and update_time between #{localDateTime} and #{localDateTime1}")
    Integer countByUserIdAndUpdateTimeBetween(@Param("userId") String userId, LocalDateTime localDateTime, LocalDateTime localDateTime1);


}
