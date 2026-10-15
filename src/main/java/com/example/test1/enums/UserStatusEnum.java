package com.example.test1.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 【枚举】用户状态。
 * <p>
 * 与数据库存储的关系：
 *   - 数据库里 status 存的是字符串 code（"ACTIVE"/"INACTIVE"），不是枚举本身；
 *   - 枚举负责提供 desc 给前端展示、做类型安全的取值。
 * 这是一种常见折中：DB 存码、代码用枚举、VO 返回码+中文描述。
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    ACTIVE("ACTIVE", "启用"),
    INACTIVE("INACTIVE", "停用");

    /** 存储到数据库的码 */
    private final String code;

    /** 给前端展示的中文 */
    private final String desc;

    /**
     * 按码解析，不存在返回 null（不抛异常，把决策留给调用方）。
     */
    public static UserStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }

}