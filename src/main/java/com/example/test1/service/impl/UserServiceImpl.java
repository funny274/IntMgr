package com.example.test1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.test1.common.exception.BusinessException;
import com.example.test1.common.response.ResultCode;
import com.example.test1.converter.UserConverter;
import com.example.test1.mapper.UserMapper;
import com.example.test1.model.dto.UserCreateDTO;
import com.example.test1.model.dto.UserQueryDTO;
import com.example.test1.model.dto.UserUpdateDTO;
import com.example.test1.model.entity.UserEntity;
import com.example.test1.model.vo.UserVO;
import com.example.test1.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ====================================================================================
 * 【业务实现】UserServiceImpl —— 用户 CRUD + 事务 + 分页的完整示例
 * ====================================================================================
 *
 * ▌1. 事务 @Transactional 什么时候加？
 *   - 读操作（查询）不加：只读，无需事务。
 *   - 写操作（增/删/改）要加：保证要么全成功、要么全回滚。
 *   - rollbackFor = Exception.class：默认只对 RuntimeException 回滚，
 *     显式声明后，任何异常（含受检异常）都回滚，行为更可控、更符合直觉。
 *
 * ▌2. 为什么方法里不 try-catch？
 *   业务异常直接 throw，交给全局异常处理器（GlobalExceptionHandler）统一转 Result。
 *   事务注解会感知异常并回滚，然后异常继续向上抛——两个机制各司其职，不打架。
 *
 * ▌3. 逻辑删除的"隐形"效果：
 *   @TableLogic 让 deleteById 变成 UPDATE deleted=1；
 *   并且所有 select 查询自动追加 deleted=0 条件。所以下面的 userId 查询、
 *   重名检查查不到"已被逻辑删除"的数据——这正是我们想要的。
 * ====================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 新增用户。
     * <p>
     * 唯一性校验是"应用层检查 + 数据库唯一约束"双保险：
     *   应用层检查给用户友好报错；数据库 UK_T_USER_USERNAME 兜底防并发穿透。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        checkUsernameUnique(dto.getUsername());

        UserEntity entity = UserConverter.toEntity(dto);
        // insert 时 MyMetaObjectHandler 自动填 createdTime/updatedTime，主键雪花自动生成
        userMapper.insert(entity);
        log.info("创建用户成功: id={}, username={}", entity.getId(), entity.getUsername());
        return entity.getId();
    }

    /**
     * 部分更新：只改 DTO 里非空的字段。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO dto) {
        UserEntity entity = requireEntity(id);
        UserConverter.applyUpdate(entity, dto);
        // updateById 默认只更新"非空"字段，天然支持部分更新
        userMapper.updateById(entity);
    }

    /**
     * 逻辑删除：数据仍在，deleted 置 1。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        requireEntity(id);
        userMapper.deleteById(id);
    }

    @Override
    public UserVO getUserById(Long id) {
        return UserConverter.toVO(requireEntity(id));
    }

    /**
     * 分页 + 动态条件查询。
     * <p>
     * LambdaQueryWrapper 是类型安全的条件构造器：用方法引用写列名（UserEntity::getUsername），
     * 避免手写字符串列名拼错。第一个参数为 boolean 条件：为 false 时该条件自动忽略，
     * 从而优雅地实现"有 keyword 才模糊查、有 status 才过滤"。
     */
    @Override
    public IPage<UserVO> pageUsers(UserQueryDTO query) {
        Page<UserEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        String kw = query.getKeyword();
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                // and(...) 把多个 or 条件括在一起，避免污染后面的 status 条件
                .and(StringUtils.hasText(kw), w -> w
                        .like(UserEntity::getUsername, kw)
                        .or().like(UserEntity::getNickname, kw)
                        .or().like(UserEntity::getEmail, kw))
                .eq(StringUtils.hasText(query.getStatus()), UserEntity::getStatus, query.getStatus())
                .orderByDesc(UserEntity::getCreatedTime);

        // selectPage 返回实体分页，用 convert() 把每页实体流式转成 VO，不写循环
        IPage<UserEntity> result = userMapper.selectPage(page, wrapper);
        return result.convert(UserConverter::toVO);
    }

    /**
     * 批量新增，演示事务回滚：循环里某一条失败，前面已插入的会全部回滚。
     * <p>
     * 注意：这里没有调 this.createUser() 而是复用 checkUsernameUnique + insert。
     * 原因是"自调用"（同类的 this.xxx）不经过 Spring 代理，@Transactional 不会生效，
     * 但本方法自己已处于事务中，直接复用私有步骤即可——这也顺带避开了自调用的坑。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<UserCreateDTO> list) {
        for (UserCreateDTO dto : list) {
            checkUsernameUnique(dto.getUsername());
            userMapper.insert(UserConverter.toEntity(dto));
        }
        log.info("批量创建用户成功: 数量={}", list.size());
    }

    /* ---------------- 私有辅助方法 ---------------- */

    /**
     * 校验用户名是否已存在（含逻辑删除自动过滤）。
     */
    private void checkUsernameUnique(String username) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS, "用户名已存在: " + username);
        }
    }

    /**
     * 按 id 取实体，不存在抛业务异常（复用"取不到就报错"的通用逻辑）。
     */
    private UserEntity requireEntity(Long id) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在: id=" + id);
        }
        return entity;
    }

}