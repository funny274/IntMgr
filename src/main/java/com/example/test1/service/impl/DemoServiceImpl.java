package com.example.test1.service.impl;

import com.example.test1.enums.GreetingSceneEnum;
import com.example.test1.model.dto.GreetingRequestDTO;
import com.example.test1.strategy.GreetingStrategy;
import com.example.test1.strategy.GreetingStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ====================================================================================
 * 【业务实现】DemoServiceImpl —— 问候服务的具体实现
 * ====================================================================================
 *
 * ▌1. 类上的注解：
 *
 *   @Service
 *     告诉 Spring"这是一个业务层 Bean，请把它注册进容器"。
 *     等价于 @Component，但语义更清晰（一眼看出是 Service 层）。
 *     注册后，Controller 那边 @RequiredArgsConstructor 才能自动注入到 demoService 字段。
 *     注意：容器里 DemoService 类型只能有一个实现，否则 Spring 不知道注入哪个。
 *
 *   @RequiredArgsConstructor
 *     Lombok 注解：为 final 字段 greetingStrategyFactory 生成构造器。
 *     Spring 通过这个构造器把工厂注入进来。
 *
 *   @Slf4j
 *     自动生成 log 对象，方便打日志。
 *
 * ▌2. 为什么 extends AbstractGreetingService？
 *   父类用模板方法定义了流程（preCheck→resolveScene→doBuildGreeting→assemble），
 *   本类只需实现"会变化"的那一步 doBuildGreeting()，其余流程复用父类。
 *   这样新增一种"问候生成方式"时，再写个子类即可，不用改父类（开闭原则）。
 *
 * ▌3. doBuildGreeting 的执行逻辑：
 *   从工厂按场景编码取出对应策略 → 调用策略的 buildGreeting。
 *   举个例子：scene="MORNING"
 *     → factory.getStrategy("MORNING") 返回 MorningGreetingStrategy
 *     → strategy.buildGreeting("张三") 返回 "早上好，张三！"
 *   这就是策略模式：把"变化的部分"抽成独立类，用工厂分发，避免 if-else。
 *
 * ▌4. 整体调用顺序（接 Controller 之后）：
 *   DemoController.greet(request)
 *      → demoService.greet(request)   ← 实际是父类 AbstractGreetingService.greet()
 *         → preCheck(request)
 *         → resolveScene("MORNING") → GreetingSceneEnum.MORNING
 *         → doBuildGreeting(...)     ← 子类（本类）实现
 *            → greetingStrategyFactory.getStrategy("MORNING")
 *               → MorningGreetingStrategy.buildGreeting("张三")
 *                  → "早上好，张三！"
 *         → assemble("早上好，张三！", MORNING) → GreetingVO
 *      → Result.success(vo)
 * ====================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoServiceImpl extends AbstractGreetingService {

    /** 策略工厂：通过构造器注入，final 保证不可变 */
    private final GreetingStrategyFactory greetingStrategyFactory;

    /**
     * 第一个接口的业务实现：简单返回固定问候语。
     */
    @Override
    public String getSimpleGreeting() {
        return "Hello, Spring Boot!";
    }

    /**
     * 模板方法基类要求的步骤：根据场景策略生成问候语。
     * <p>
     * 注意：这里我们不写 if("MORNING".equals(scene))... 这种分支，
     * 而是把"每种场景怎么问候"交给具体的策略类去实现。
     * 新增场景时只需：① 新增一个策略类 ② 在枚举里加一项，本类和工厂都不用改。
     */
    @Override
    protected String doBuildGreeting(GreetingRequestDTO request, GreetingSceneEnum scene) {
        // 步骤 1：从工厂按场景取策略（不支持的场景会抛业务异常）
        GreetingStrategy strategy = greetingStrategyFactory.getStrategy(scene.getCode());
        // 步骤 2：让策略生成问候语
        return strategy.buildGreeting(request.getName());
    }

}
