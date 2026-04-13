package com.achobeta.domain.overview.service.extendbiz;

import com.achobeta.domain.overview.adapter.repository.IUserOverviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserOverviewService {
    @Autowired
    private IUserOverviewRepository userOverviewRepository;

    @Scheduled(cron="0 */3 * * * *")
    public void handleUserOverview() {
        List<String> userIds = userOverviewRepository.getUserIds();
        for (String userId : userIds) {
            //查询用户易错知识点数
            int hardQuestions = userOverviewRepository.getHardQuestions(userId);
            //查询用户总错题数
            int questionsNum = userOverviewRepository.getQuestionsNum(userId);
            //查询用户已掌握错题数
            int hardQuestionsNum = userOverviewRepository.getHardQuestionsNum(userId);

            double reviewRate = questionsNum == 0 ? 0 : hardQuestionsNum * 1.0 / questionsNum;

            userOverviewRepository.updateUserOverview(userId, hardQuestions, questionsNum, reviewRate);
        }
    }

    public void updateUserDuration(String userId, int duration) {
        userOverviewRepository.updateUserDuration(userId, duration);
    }
}
