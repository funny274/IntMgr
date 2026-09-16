package com.example.test1.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * ====================================================================================
 * 【枚举】GreetingSceneEnum —— 问候场景的可选值
 * ====================================================================================
 *
 * ▌1. 为什么用枚举而不用 String？
 *   - String 可以传任意值（"HELLO"、"XXX"），运行到深处才报错；
 *   - 枚举把可选值固定在代码里，传错了编译期/解析阶段就发现；
 *   - 还能附带描述（desc）、自定义方法（of、getCode）。
 *   一句话：枚举是"类型安全的常量集合"。
 *
 * ▌2. 字段含义：
 *   - code：对外约定的字符串编码（前端传的就是它，如 "MORNING"）
 *   - desc：给人看的中文描述，会回传给前端展示
 *
 * ▌3. of(...) 方法：把前端传来的字符串解析成枚举实例。
 *   不存在返回 null，由调用方决定是抛异常还是给默认值。
 * ====================================================================================
 */
@Getter
@AllArgsConstructor
public enum GreetingSceneEnum {

    MORNING("MORNING", "清晨问候"),
    AFTERNOON("AFTERNOON", "午后问候"),
    EVENING("EVENING", "夜晚问候");

    /** 场景编码（前端传的字符串就是它） */
    private final String code;

    /** 场景描述（中文，给前端展示用） */
    private final String desc;

    /**
     * 按编码解析枚举，忽略大小写。
     * <p>
     * 为什么不抛异常而返回 null？把异常处理留给业务层（Service），
     * 这样枚举本身更通用（也可用于"找默认值"等场景）。
     *
     * @param code 编码
     * @return 枚举；不存在时返回 null
     */
    public static GreetingSceneEnum of(String code) {
        if (code == null) {
            return null;
        }
        // values() 返回所有枚举实例；流式过滤找第一个匹配的
        return Arrays.stream(values())
                .filter(e -> e.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

}
