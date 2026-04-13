package com.achobeta.domain.Feetback.adapter.repository;

import com.achobeta.domain.Feetback.model.valobj.CountByTypeVO;
import com.achobeta.domain.Feetback.model.valobj.ReviewTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface IReviewQuestionRepository {


    @Select("SELECT COUNT(id) FROM MistakeQuestion " +
            "WHERE user_id = #{userId} AND now()- interval 7 day >= update_time")
    int queryReviewQuestions(@Param("userId") String userId);

    void deleteBatch(@Param("userId") String userId, @Param("questionIds") List<Integer> questionIds);

    List<CountByTypeVO> countBySubject(String userId);

    List<CountByTypeVO> countByKnowledge(@Param("userId")String userId);

    List<ReviewTrendVO> calculateReviewRate(@Param("userId")String userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
