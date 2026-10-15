package com.example.test1.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 【入参 DTO】更新用户的请求体。
 * <p>
 * 与新增不同：所有字段都"可空"——只更新前端传了值的字段（部分更新）。
 * 用户名不允许改（它是唯一身份），要改得走专门的"改用户名"接口并校验唯一性。
 * id 从 URL 路径传入，不在请求体里（见 UserController）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户入参")
public class UserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称", example = "张三三")
    @Size(max = 100, message = "昵称最长 100 字符")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长 100 字符")
    private String email;

    @Schema(description = "年龄", example = "29")
    @Min(value = 1, message = "年龄最小为 1")
    @Max(value = 150, message = "年龄最大为 150")
    private Integer age;

    @Schema(description = "状态：ACTIVE=启用/INACTIVE=停用", example = "ACTIVE")
    private String status;

}