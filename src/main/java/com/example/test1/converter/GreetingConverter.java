package com.example.test1.converter;

import com.example.test1.enums.GreetingSceneEnum;
import com.example.test1.model.vo.GreetingVO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ====================================================================================
 * 【转换器】GreetingConverter —— 领域结果 → VO 的"翻译官"
 * ====================================================================================
 *
 * ▌1. 为什么需要 Converter？
 *   业务层处理的是"领域结果"（这里就是一段问候语字符串 + 场景枚举），
 *   对外要的是"VO"。两者字段、结构往往不一致，需要一个翻译。
 *
 * ▌2. 把转换逻辑集中在一个工具类的好处：
 *   - 转换规则只有一处定义，所有调用点一致；
 *   - 改转换逻辑（比如 VO 加字段）只改这里，不碰 Service；
 *   - 静态方法调用方便，不必注入 Spring Bean。
 *
 * ▌3. 为什么是 final + 私有构造？
 *   这是"工具类"的标准写法：
 *   - final 防止被子类继承破坏一致性；
 *   - 私有构造 + 抛异常防止被 new 出来（工具类不需要实例）。
 *
 * ▌4. 为什么不让 Service 直接 new VO？
 *   如果组装逻辑简单时确实可以 new，但稍微复杂一点
 *   （字段映射、单位换算、脱敏）就该抽出来，避免散落各处难维护。
 * ====================================================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GreetingConverter {

    /**
     * 将问候语 + 场景枚举转换为响应 VO。
     *
     * @param greeting 问候语
     * @param scene    场景枚举
     * @return VO
     */
    public static GreetingVO toVO(String greeting, GreetingSceneEnum scene) {
        // Builder 模式组装，比一连串 setXxx 更直观
        return GreetingVO.builder()
                .greeting(greeting)
                .sceneDesc(scene == null ? null : scene.getDesc())
                .serverTime(System.currentTimeMillis())
                .build();
    }

}
