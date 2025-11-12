package com.achobeta.domain.user.adapter.repository;

import com.achobeta.domain.user.model.entity.UserEntity;

/**
 * @author liangchaowen
 * @description 用户仓储接口（领域层定义，基础设施层实现）
 * @date 2025/10/29
 */
public interface IUserRepository {

    /**
     * 按ID查询用户
     */
    UserEntity findById(String userId);

    /**
     * 按账号查询用户
     */
    UserEntity findByAccount(String account);

    /**
     * 新增用户
     */
    void save(UserEntity user);

    /**
     * 更新用户
     */
    void updateByUserAccount(UserEntity user, String account);

    /**
     * redis set操作
     */
    void setValue(String key, String value, long timeout);

    /**
     * redis get操作
     */
    String getValue(String key);

    /**
     * redis del操作
     */
    void delValue(String key);

}