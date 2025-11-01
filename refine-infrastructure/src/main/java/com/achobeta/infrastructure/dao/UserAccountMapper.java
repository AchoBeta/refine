package com.achobeta.infrastructure.dao;

import com.achobeta.domain.user.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountMapper {

    UserEntity selectByAccount(String userAccount);

    void insert(UserEntity user);

    void update(UserEntity user, UserEntity query);

}