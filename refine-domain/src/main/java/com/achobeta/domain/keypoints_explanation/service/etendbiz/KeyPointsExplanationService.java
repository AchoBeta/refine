package com.achobeta.domain.keypoints_explanation.service.etendbiz;


import com.achobeta.domain.keypoints_explanation.adapter.repository.KeyPointsMapper;
import com.achobeta.domain.keypoints_explanation.adapter.repository.SubjectTransportMappeer;
import com.achobeta.domain.keypoints_explanation.model.valobj.*;
import com.achobeta.domain.keypoints_explanation.service.IKeyPointsExplanationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class KeyPointsExplanationService implements IKeyPointsExplanationService {
    @Autowired
    private KeyPointsMapper keyPointsMapper;

    @Autowired
    private MindMapService mindMapService;

    /**
     * 获取子知识点
     * @param knowledgeId 父知识点id
     * @return
     */
    @Override
    public List<KeyPointsVO> getSonKeyPoints(String knowledgeId, String userId) {
        List<KeyPointsVO> keyPointsVOList = keyPointsMapper.getSonKeyPoints(knowledgeId, userId);
        return keyPointsVOList == null ? new ArrayList<>() : keyPointsVOList;
    }

    /**
     * 获取中心知识点
     * @param subject
     * @return
     */
    @Override
    public List<KeyPointsVO> getKeyPoints(String subject, String userId) {
        int subjectId;
        subjectId = SubjectTransportMappeer.FIELD_MAP.get(subject);
        return keyPointsMapper.getKeyPoints(subjectId, userId);

    }

    /**
     * 获取知识点详情
     */
    @Override
    public String getKnowledgedescById(String knowledgeId, String userId) {
        return keyPointsMapper.getKnowledgedescById(knowledgeId, userId);
    }

    /**
     * 获取知识点错误题数
     * @param knowledgeId
     * @return
     */
    @Override
    public WrongQuestionVO getRelatedWrongQuestionsStatistic(String knowledgeId, String userId) {
        WrongQuestionVO wrongQuestionVO = keyPointsMapper.getRelatedWrongQuecstions(knowledgeId, userId);
        return wrongQuestionVO == null ? new WrongQuestionVO() : wrongQuestionVO;
    }

    /**
     * 获取知识点相关知识点
     * @param knowledgeId
     * @return
     */
    @Override
    public List<KeyPointsVO> getRelatedKnowledgePoints(String knowledgeId, String userId) {
        List<KeyPointsVO> keyPointsVOList = new ArrayList<>();

        // 获取父知识点
        KeyPointsVO parentKeyPoints = keyPointsMapper.getParentKeyPoints(knowledgeId, userId);
        if(parentKeyPoints != null){
            keyPointsVOList.add(parentKeyPoints);
            knowledgeId = parentKeyPoints.getId();

            // 获取同级知识点
            keyPointsVOList.addAll(keyPointsMapper.getSonKeyPoints(knowledgeId, userId));
            return keyPointsVOList;
        }
        return null;
    }

    /**
     * 保存或更新知识点笔记
     * @param note
     * @param knowledgeId
     * @return
     */
    @Override
    public void savedNote(String note, String knowledgeId, String userId) {
        keyPointsMapper.savedNote(note, knowledgeId, userId);
    }

    /**
     * 标记为已掌握
     * @param knowledgeId
     * @return
     */
    @Override
    public void markAsMastered(String knowledgeId, String userId) {
        keyPointsMapper.markAsMastered(knowledgeId, userId);
    }

    /**
     * 重命名知识点
     * @param knowledgeId
     * @param newName
     * @return
     */
    @Override
    public void renameNode(String knowledgeId, String newName, String userId) {
        keyPointsMapper.renameNode(knowledgeId, newName, userId);
    }

    /**
     * 获取知识点提示信息
     * @param knowledgeId
     * @return
     */
    @Override
    public ToolTipVO gettooltipById(String knowledgeId, String userId) {
        // 获取总错题数
        int total = keyPointsMapper.getTotalById(knowledgeId, userId);

        // 获取状态为0的错题数
        int count = keyPointsMapper.getCountById(knowledgeId, userId);

        // 获取最后一次复习时间
        String lastReviewTime = keyPointsMapper.getLastReviewTimeById(knowledgeId, userId);

        return new ToolTipVO(total, count, lastReviewTime);
    }

    @Override
    public RelateQuestionVO getRelatedWrongQuestions(String knowledgeId, String userId) {
        List<QuestionVO> qestions = keyPointsMapper.getRelatedQuestions(knowledgeId, userId);
        String note = keyPointsMapper.getNoteById(knowledgeId, userId);

        return new RelateQuestionVO(qestions, note);
    }

    @Override
    public void addSonPoint(SonPointVO sonPointVOs, String userId, String parentId) {
        saveMindMapNode(userId, sonPointVOs, parentId);
    }

    private void saveMindMapNode(String userId, SonPointVO node, String parentId) {
        node.setPointId(mindMapService.generateUUID());
        keyPointsMapper.saveMindMapTree(userId, node, parentId);
        for (SonPointVO child : node.getSonPoints()) {
            saveMindMapNode(userId, child, node.getPointId());
        }
    }
}
