package com.achobeta.infrastructure.adapter.repository;

import cn.hutool.core.lang.UUID;
import com.achobeta.domain.question.adapter.repository.IMistakeRepository;
import com.achobeta.domain.question.model.entity.MistakeQuestionEntity;
import com.achobeta.domain.question.model.po.MistakeKnowledgePO;
import com.achobeta.api.dto.MistakeQuestionDTO;
import com.achobeta.infrastructure.dao.IMistakeQuestionMapper;
import com.achobeta.infrastructure.dao.po.MistakePO;
import com.achobeta.domain.IRedisService;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class MistakeRepository implements IMistakeRepository {

    @Resource
    private IRedisService redis;

    private final IMistakeQuestionMapper mistakeQuestionMapper;

    @Override
    public void save(MistakeQuestionEntity mistakeEntity) {
        MistakePO mistakePO = new MistakePO();
        BeanUtils.copyProperties(mistakeEntity, mistakePO);
        mistakePO.setUpdateTime(LocalDateTime.now()); // 补充领域层未定义的字段
        mistakeQuestionMapper.insert(mistakePO);
    }

    @Override
    public MistakeKnowledgePO findSubjectAndKnowledgeIdById(Integer mistakeQuestionId) {
        MistakeKnowledgePO po = mistakeQuestionMapper.findSubjectAndKnowledgeIdById(mistakeQuestionId);
        if (null == po) {
            throw new AppException("可能是数据库一致性问题,mistakeQuestionId:"+mistakeQuestionId);
        }
        return po;
    }

    @Override
    public void setValue(String s, MistakeQuestionDTO mistakeQuestionDTO, Long expired) {
        redis.setValue(s, mistakeQuestionDTO, expired);
    }

    @Override
    public MistakeQuestionDTO getValue(String s) {
        MistakeQuestionDTO value = redis.getValue(s);
        if (null == value) {
            throw new AppException(GlobalServiceStatusCode.QUESTION_IS_EXPIRED);
        }
        return value;
    }

    @Override
    public void remove(String s) {
        redis.remove(s);
    }


}