package com.example.test1.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 【入参 DTO】创建用户的请求体。
 * <p>
 * 字段与校验：
 *   - username 必填、长度 2~50；创建时用户名是唯一身份，必须校验。
 *   - nickname/email/age 选填，有就做格式/范围校验。
 *   - status 不在这里传——创建时统一默认 ACTIVE（见 UserConverter.toEntity）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建用户入参")
public class UserCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "登录名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度需在 2-50 之间")
    private String username;

    @Schema(description = "昵称", example = "张三")
    @Size(max = 100, message = "昵称最长 100 字符")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长 100 字符")
    private String email;

    @Schema(description = "年龄", example = "28")
    @Min(value = 1, message = "年龄最小为 1")
    @Max(value = 150, message = "年龄最大为 150")
    private Integer age;

}