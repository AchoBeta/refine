package com.achobeta.domain.keypoints_explanation.service;

import com.achobeta.domain.keypoints_explanation.model.valobj.*;

import java.util.Collection;
import java.util.List;

public interface IKeyPointsExplanationService {
    List<KeyPointsVO> getSonKeyPoints(String knowledgeId, String userId);

    List<KeyPointsVO> getKeyPoints(String subject, String userId);

    String getKnowledgedescById(String knowledgeId, String userId);

    WrongQuestionVO getRelatedWrongQuestionsStatistic(String knowledgeId, String userId);

    List<KeyPointsVO> getRelatedKnowledgePoints(String knowledgeId, String userId);

    void savedNote(String note, String knowledgeId, String userId);

    void markAsMastered(String knowledgeId, String userId);

    void renameNode(String knowledgeId, String newName, String userId);

    ToolTipVO gettooltipById(String knowledgeId, String userId);

    RelateQuestionVO getRelatedWrongQuestions(String knowledgeId, String userId);

    void addSonPoint(SonPointVO sonPointVOs, String userId, String parentId);
}
