package com.achobeta.infrastructure.dao;

import com.achobeta.domain.user.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDataMapper {

    void insert(String userId);
}
