package com.example.test1.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 【入参 DTO】分页查询用户的请求参数（GET query 参数）。
 * <p>
 * 分页三要素：pageNum(第几页)、pageSize(每页几条)，外加可选的筛选条件 keyword/status。
 *
 * ▌重点：@Builder.Default 的坑
 *   注释字段给了默认值 1 和 10，但 Lombok 的 @Builder 默认"无视"字段初始化值：
 *   不调 .pageNum() 时，builder 会给 0（基本类型默认值），而不是 1。
 *   所以必须加 @Builder.Default，让 builder 也采用字段默认值。
 *   这是一个新手高频踩坑点。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户分页查询入参")
public class UserQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码（从 1 开始）", example = "1")
    @Builder.Default
    @Min(value = 1, message = "页码最小为 1")
    private long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    @Builder.Default
    @Min(value = 1, message = "每页至少 1 条")
    @Max(value = 100, message = "每页最多 100 条")
    private long pageSize = 10L;

    @Schema(description = "关键字（模糊匹配用户名/昵称/邮箱）", example = "zhang")
    private String keyword;

    @Schema(description = "状态筛选：ACTIVE/INACTIVE", example = "ACTIVE")
    private String status;

}