package com.achobeta.domain.overview.service.extendbiz;

import com.achobeta.api.dto.TrendDataDTO;
import com.achobeta.domain.Feetback.service.feedback.IReviewFeedbackService;
import com.achobeta.domain.Feetback.service.feedback.extendbiz.ReviewFeedbackService;
import com.achobeta.domain.overview.adapter.repository.IStudyOverviewRepository;
import com.achobeta.domain.overview.model.valobj.LearningDynamicVO;
import com.achobeta.domain.overview.service.ILearningOverviewService;
import com.achobeta.domain.overview.model.valobj.StudyOverviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class LearningOverviewService implements ILearningOverviewService{
    @Autowired
    private IStudyOverviewRepository repository;
    @Autowired
    private IReviewFeedbackService reviewFeedbackService;
    @Override
    public StudyOverviewVO getOverview(String userId) {
        StudyOverviewVO vo = repository.queryStudyOverview(userId);
        if(vo == null || vo.getQuestionsNum() == 0){
            vo = StudyOverviewVO.builder()
                    .questionsNum(0)
                    .reviewRate(0.0)
                    .hardQuestions(0)
                    .studyTime(0)
                    .build();
        }
        return vo;
    }

    @Override
    public LearningDynamicVO getStudyDynamic(String userId) {
        LearningDynamicVO vo = new LearningDynamicVO();

        // 1. 最近错题数
        vo.setQuestionsCount(repository.countByUserIdAndCreateTimeAfter(userId, LocalDateTime.now().minusDays(7)));

        // 2. 本周复盘数
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);
        vo.setWeeklyReviewCount(repository.countByUserIdAndUpdateTimeBetween(userId, startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59)));

        // 3. 学习趋势折线图数据（过去6个月，每月一次）
        vo.setTrendData(getTrendData(userId));

        return vo;
    }

    private List<TrendDataDTO> getTrendData(String userId) {
        List<TrendDataDTO> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Integer studyCount = repository.countByUserIdAndUpdateTimeBetween(
                    userId,
                    month.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(),
                    month.with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay()
            );
            Integer reviewCount = repository.countByUserIdAndUpdateTimeBetween(
                    userId,
                    month.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(),
                    month.with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay()
            );
            result.add(new TrendDataDTO(monthStr, studyCount, reviewCount));
        }
        return result;
    }
}
