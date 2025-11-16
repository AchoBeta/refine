package com.achobeta.infrastructure.adapter.repository;

import com.achobeta.domain.question.adapter.repository.IMistakeRepository;
import com.achobeta.domain.question.model.entity.MistakeQuestionEntity;
import com.achobeta.domain.question.model.po.MistakeKnowledgePO;
import com.achobeta.domain.question.model.valobj.MistakeQuestionVO;
import com.achobeta.infrastructure.dao.MistakeQuestionMapper;
import com.achobeta.infrastructure.dao.po.MistakePO;
import com.achobeta.infrastructure.redis.IRedisService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class MistakeRepositoryImpl implements IMistakeRepository {

    @Resource
    private IRedisService redis;

    private final MistakeQuestionMapper mistakeQuestionMapper;

    @Override
    public void save(MistakeQuestionEntity mistakeEntity) {
        MistakePO mistakePO = new MistakePO();
        BeanUtils.copyProperties(mistakeEntity, mistakePO);
        mistakePO.setUpdateTime(LocalDateTime.now()); // 补充领域层未定义的字段
        mistakeQuestionMapper.insert(mistakePO);
    }

    @Override
    public MistakeKnowledgePO findSubjectAndKnowledgeIdById(Integer mistakeQuestionId) {
        return mistakeQuestionMapper.findSubjectAndKnowledgeIdById(mistakeQuestionId);
    }

    @Override
    public void setValue(String s, MistakeQuestionVO mistakeQuestionDTO, Long expired) {
        redis.setValue(s, mistakeQuestionDTO, expired);
    }

    @Override
    public MistakeQuestionVO getValue(String s) {
        return redis.getValue(s);
    }

    @Override
    public void remove(String s) {
        redis.remove(s);
    }


}