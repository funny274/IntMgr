package com.example.test1.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.test1.common.constant.Constants;
import com.example.test1.common.response.Result;
import com.example.test1.model.dto.UserCreateDTO;
import com.example.test1.model.dto.UserQueryDTO;
import com.example.test1.model.dto.UserUpdateDTO;
import com.example.test1.model.vo.UserVO;
import com.example.test1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ====================================================================================
 * 【控制层】UserController —— 用户 CRUD + 分页接口
 * ====================================================================================
 *
 * ▌RESTful 设计（方法是"动词"、URL 是"资源名"）：
 *   POST   /api/users       新增
 *   PUT    /api/users/{id}  按 id 更新
 *   DELETE /api/users/{id}  按 id 删除
 *   GET    /api/users/{id}  查单个
 *   GET    /api/users       分页列表（query 参数）
 *   POST   /api/users/batch 批量新增（事务演示）
 *
 * ▌@Validated 的作用：
 *   处理"方法参数级"校验——@PathVariable/@RequestParam 直接标 @NotNull 等注解时，
 *   需要类上加 @Validated 才生效（区别于 @RequestBody 上的 @Valid）。
 * ====================================================================================
 */
@Validated
@RestController
@RequestMapping(Constants.API_PREFIX + "/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户 CRUD / 分页 / 事务示例（MyBatis-Plus + Oracle）")
public class UserController {

    private final UserService userService;

    @Operation(summary = "新增用户", description = "用户名唯一，返回新用户 ID")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

    @Operation(summary = "更新用户", description = "只更新请求体中非空字段（部分更新）")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @NotNull Long id,
                               @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "逻辑删除（deleted 置 1）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable @NotNull Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "分页查询用户", description = "支持 keyword 模糊匹配、status 过滤")
    @GetMapping
    public Result<IPage<UserVO>> page(@Valid UserQueryDTO query) {
        return Result.success(userService.pageUsers(query));
    }

    @Operation(summary = "批量新增用户", description = "演示事务：任一失败整体回滚")
    @PostMapping("/batch")
    public Result<Void> batchCreate(@RequestBody @NotEmpty(message = "批量列表不能为空")
                                    List<@Valid UserCreateDTO> list) {
        userService.batchCreate(list);
        return Result.success();
    }

}