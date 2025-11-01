package com.achobeta.infrastructure.adapter.repository;

import com.achobeta.domain.user.adapter.repository.IUserRepository;
import com.achobeta.domain.user.model.entity.UserEntity;
import com.achobeta.infrastructure.dao.UserAccountMapper;
import com.achobeta.types.support.id.SnowflakeIdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** @author liangchaowen
 * @description 用户仓储实现（依赖MyBatis操作数据库）
 * @date 2025/10/29
 */
@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

    private final UserAccountMapper userAccountMapper;

    /**
     * 这里简化了，分布式部署的话要从配置文件读入workerId(每台机器设置唯一不同)
     */
    private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(0);


    @Override
    public UserEntity findByAccount(String account) {
        return userAccountMapper.selectByAccount(account);
    }

    @Override
    public void save(UserEntity user) {
        // 生成唯一用户ID
        user.setUserId(INSTANCE.nextIdStr());
        userAccountMapper.insert(user);
    }

    @Override
    public void update(UserEntity user, UserEntity query) {
        userAccountMapper.update(user, query);
    }

}