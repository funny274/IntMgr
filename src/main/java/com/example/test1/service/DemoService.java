package com.example.test1.service;

import com.example.test1.model.dto.GreetingRequestDTO;
import com.example.test1.model.vo.GreetingVO;

/**
 * ====================================================================================
 * 【业务层接口】DemoService —— 定义"对外能做什么"
 * ====================================================================================
 *
 * ▌1. 为什么要先写接口，再写实现？
 *   - 面向接口编程：Controller 依赖接口而非实现，解耦。
 *   - 好处 1：换实现不影响调用方。比如换成从缓存读的实现，Controller 不动。
 *   - 好处 2：单元测试可以 Mock 这个接口，不依赖真实数据库。
 *   - 好处 3：接口就是"对外契约"，团队协作时先定接口再各自实现。
 *
 * ▌2. 接口里该放什么？
 *   - 只放业务能力（动词：能做什么），不放技术细节。
 *   - 入参/出参用 DTO/VO（接口层的契约模型），不用领域实体。
 *
 * ▌3. 调用链路（接 Controller 之后的流程）：
 *   DemoController.greet()
 *        │  调 demoService.greet(request)
 *        ▼
 *   DemoServiceImpl.greet()            ← 实际跑的是这里
 *        │  但本实现没有自己重写 greet()，而是继承自 AbstractGreetingService
 *        ▼
 *   AbstractGreetingService.greet()     ← 模板方法在这里编排流程
 *        │  preCheck → resolveScene → doBuildGreeting → assemble
 *        ▼
 *   DemoServiceImpl.doBuildGreeting()   ← 子类只实现"生成问候语"这一步
 *        │  调 greetingStrategyFactory.getStrategy(scene)
 *        ▼
 *   GreetingStrategyFactory 返回具体策略 → strategy.buildGreeting(name)
 *        ▼
 *   返回问候语字符串，一路回传包装成 VO
 * ====================================================================================
 */
public interface DemoService {

    /**
     * 简单问候：固定返回字符串。
     * <p>
     * 给 Controller 接口 1 调用，没有入参也没有场景。
     *
     * @return 问候语
     */
    String getSimpleGreeting();

    /**
     * 按场景 + 称呼生成个性化问候。
     * <p>
     * 给 Controller 接口 2 调用。注意：
     *   - 入参是 DTO（已被 Controller 层 @Valid 校验过基本格式）
     *   - 出参是 VO（不泄露领域模型）
     *   - 方法内部若发现业务异常（如不支持的场景），抛 BusinessException 即可，
     *     会被全局异常处理器统一转成错误响应，无需 try-catch。
     *
     * @param request 请求入参（已通过 @Valid 校验）
     * @return 问候响应 VO
     */
    GreetingVO greet(GreetingRequestDTO request);

}
