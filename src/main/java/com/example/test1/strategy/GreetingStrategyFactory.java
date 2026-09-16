package com.example.test1.strategy;

import com.example.test1.common.exception.BusinessException;
import com.example.test1.common.response.ResultCode;
import com.example.test1.enums.GreetingSceneEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ====================================================================================
 * 【工厂模式】GreetingStrategyFactory —— 策略的"自动注册中心"
 * ====================================================================================
 *
 * ▌1. 为什么需要工厂？
 *   如果不用工厂，Service 里就得写：
 *     if ("MORNING".equals(scene)) return new MorningStrategy().build(...);
 *     if ("AFTERNOON".equals(scene)) return ...;
 *   场景一多，if-else 越堆越长，加新场景要改 Service——违反开闭原则。
 *
 *   工厂的做法：启动时把所有策略按场景收集到 Map，运行时 O(1) 取出。
 *   新增场景：只需新建一个策略 Bean，工厂和 Service 都不用改！
 *
 * ▌2. 它是怎么自动收集所有策略的？
 *   构造器注入 List&lt;GreetingStrategy&gt; strategies：
 *   Spring 看到这个参数，会把容器里所有 GreetingStrategy 类型的 Bean
 *   装进 List 传进来。这就是 Spring 的"集合注入"特性。
 *
 * ▌3. @PostConstruct 的作用：
 *   标在 init() 上，表示"Bean 构造完成后自动调用一次"。
 *   时机：依赖注入完成之后、Bean 正式可用之前。
 *   我们在这里遍历 strategies，按 supportScene() 注册到 strategyMap。
 *
 * ▌4. 为什么用 EnumMap 而不是 HashMap？
 *   EnumMap 以枚举为 key，内部用数组实现，性能比 HashMap 略好，
 *   且不会有 hash 冲突。枚举做 key 时首选。
 *
 * ▌5. 注册时的"重复检查"：
 *   如果两个策略类都声明 supportScene()=MORNING，会被发现并抛异常，
 *   避免运行时一个场景对应两个策略导致歧义。
 *
 * ▌6. 调用顺序：
 *   DemoServiceImpl.doBuildGreeting() 调 getStrategy("MORNING")
 *      → 从 strategyMap 取出 MorningGreetingStrategy 返回
 *      → Service 调 strategy.buildGreeting("张三")
 * ====================================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GreetingStrategyFactory {

    /** Spring 自动注入的所有策略实现（集合注入） */
    private final List<GreetingStrategy> strategies;

    /** 按场景枚举索引，O(1) 查找 */
    private final Map<GreetingSceneEnum, GreetingStrategy> strategyMap = new EnumMap<>(GreetingSceneEnum.class);

    /**
     * 启动时把所有策略按 supportScene() 注册到 Map。
     * <p>
     * 注意：此方法在 Spring 容器初始化阶段被调用一次，之后不再执行。
     */
    @PostConstruct
    public void init() {
        for (GreetingStrategy strategy : strategies) {
            GreetingSceneEnum scene = strategy.supportScene();
            // putIfAbsent 等效写法：发现已有同场景策略就直接报错
            GreetingStrategy existed = strategyMap.put(scene, strategy);
            if (existed != null) {
                throw new IllegalStateException("场景 " + scene + " 存在重复策略: "
                        + existed.getClass().getName() + " vs " + strategy.getClass().getName());
            }
            log.info("注册问候策略: scene={} -> {}", scene, strategy.getClass().getSimpleName());
        }
    }

    /**
     * 根据场景编码获取策略。
     * <p>
     * 不支持的场景抛 BusinessException，会被全局异常处理器转成统一错误响应。
     * 这就是为什么 Service 里不需要 try-catch 的原因。
     *
     * @param sceneCode 场景编码（字符串）
     * @return 策略实现
     * @throws BusinessException 场景不被支持时抛出
     */
    public GreetingStrategy getStrategy(String sceneCode) {
        // 步骤 1：字符串 → 枚举（不存在返回 null）
        GreetingSceneEnum scene = GreetingSceneEnum.of(sceneCode);
        if (scene == null) {
            throw new BusinessException(ResultCode.GREETING_SCENE_NOT_SUPPORTED,
                    "不支持的问候场景: " + sceneCode);
        }
        // 步骤 2：枚举 → 策略（如果某个场景没实现策略也会报错）
        GreetingStrategy strategy = strategyMap.get(scene);
        if (strategy == null) {
            throw new BusinessException(ResultCode.GREETING_SCENE_NOT_SUPPORTED,
                    "场景暂未实现策略: " + sceneCode);
        }
        return strategy;
    }

}
