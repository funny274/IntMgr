package com.example.test1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.test1.model.dto.UserCreateDTO;
import com.example.test1.model.dto.UserQueryDTO;
import com.example.test1.model.dto.UserUpdateDTO;
import com.example.test1.model.vo.UserVO;

import java.util.List;

/**
 * 【业务层接口】UserService —— 用户"能做什么"的对外契约。
 * <p>
 * 只声明能力，不写实现，与 DemoService 同一套分层理念。
 * 入参用 DTO、出参用 VO、分页统一返回 IPage（接口而非具体实现 Page，便于替换）。
 */
public interface UserService {

    /** 新增用户，返回新主键 ID */
    Long createUser(UserCreateDTO dto);

    /** 按 id 部分更新用户 */
    void updateUser(Long id, UserUpdateDTO dto);

    /** 按 id 逻辑删除用户 */
    void deleteUser(Long id);

    /** 按 id 查询用户详情 */
    UserVO getUserById(Long id);

    /** 分页 + 条件查询用户 */
    IPage<UserVO> pageUsers(UserQueryDTO query);

    /** 批量新增（演示事务：任一失败整体回滚） */
    void batchCreate(List<UserCreateDTO> list);

}