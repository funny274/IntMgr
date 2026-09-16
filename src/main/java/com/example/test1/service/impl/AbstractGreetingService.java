package com.example.test1.service.impl;

import com.example.test1.common.exception.BusinessException;
import com.example.test1.common.response.ResultCode;
import com.example.test1.converter.GreetingConverter;
import com.example.test1.enums.GreetingSceneEnum;
import com.example.test1.model.dto.GreetingRequestDTO;
import com.example.test1.model.vo.GreetingVO;
import com.example.test1.service.DemoService;
import lombok.extern.slf4j.Slf4j;

/**
 * ====================================================================================
 * 【模板方法模式】AbstractGreetingService —— 问候流程的"骨架"
 * ====================================================================================
 *
 * ▌1. 什么是模板方法模式？
 *   在父类定义一个算法的"骨架"（先做啥、再做啥、最后做啥），
 *   把"可变步骤"留给子类去实现。
 *   类比：做菜的菜谱——"洗→切→炒→装盘"是骨架，具体炒什么菜由子类决定。
 *
 * ▌2. 这里怎么用的？
 *   问候流程被拆成 4 步（固定顺序）：
 *     ① preCheck(request)         —— 前置业务校验（兜底，防止 DTO 校验被绕过）
 *     ② resolveScene(sceneCode)  —— 字符串场景编码 → 枚举
 *     ③ doBuildGreeting(...)     —— 生成问候语【抽象，子类必须实现】
 *     ④ assemble(...)             —— 组装 VO【固定，走 Converter】
 *
 *   其中 greet() 方法把这些步骤串起来，用 final 修饰，
 *   子类不能改流程顺序，只能改"生成问候语"这一步——这就是模板方法的精髓。
 *
 * ▌3. 为什么这么做？
 *   - 把通用流程集中在一处，避免每个子类都重复写"校验→组装"代码。
 *   - 以后改流程（比如加埋点）只改父类一个方法，所有子类自动生效。
 *   - 子类只关注"做什么"而非"怎么做"，代码更聚焦。
 *
 * ▌4. 为什么是抽象类而不是接口？
 *   接口只能定义抽象方法，不能有"已实现的骨架方法"。
 *   抽象类可以既有具体方法（greet/preCheck/assemble）又有抽象方法（doBuildGreeting），
 *   正好符合模板方法模式的需要。
 *
 * ▌5. 注意几个 protected/final 的设计：
 *   - greet() 是 final：禁止子类覆写，保证流程顺序稳定。
 *   - doBuildGreeting() 是 protected abstract：子类必须实现，是它"发挥"的地方。
 *   - assemble() 是 final：组装逻辑固定，不让子类乱改。
 *   - preCheck/resolveScene 是 protected：子类可以"选择"覆写，但通常不必要。
 * ====================================================================================
 */
@Slf4j
public abstract class AbstractGreetingService implements DemoService {

    /**
     * 模板方法：编排问候流程。声明为 final，防止子类打乱流程顺序。
     * <p>
     * 这是整个流程的"总调度"，Controller 调 demoService.greet() 实际进的就是这里。
     */
    @Override
    public final GreetingVO greet(GreetingRequestDTO request) {
        // 步骤 1：业务侧二次校验（@Valid 已挡了格式，这里再挡业务规则）
        preCheck(request);
        // 步骤 2：字符串 → 枚举（不合法直接抛异常，由全局异常处理器接住）
        GreetingSceneEnum scene = resolveScene(request.getScene());
        // 步骤 3：子类负责生成问候语（这是抽象步骤，必须由子类实现）
        String greeting = doBuildGreeting(request, scene);
        // 步骤 4：组装为对外 VO（固定逻辑，集中走 Converter）
        return assemble(greeting, scene);
    }

    /* ---------- 可被子类覆写的钩子（hook） ---------- */

    /**
     * 前置业务校验。DTO 已由 @Valid 把关，此处做业务侧二次保险。
     * <p>
     * 子类可以覆写加更严的规则，但通常不必。
     */
    protected void preCheck(GreetingRequestDTO request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            // 抛业务异常而不是返回 null/ false —— 让全局处理器统一兜底
            throw new BusinessException(ResultCode.GREETING_NAME_BLANK);
        }
    }

    /**
     * 解析场景编码为枚举，校验不通过抛业务异常。
     * <p>
     * 把"字符串编码"转成"枚举"的好处：
     *   - 编译期就能限制取值范围，避免魔法字符串散落；
     *   - 枚举自带描述、可加方法；
     *   - 后续策略工厂用枚举做 key 查找高效。
     */
    protected GreetingSceneEnum resolveScene(String sceneCode) {
        GreetingSceneEnum scene = GreetingSceneEnum.of(sceneCode);
        if (scene == null) {
            throw new BusinessException(ResultCode.GREETING_SCENE_NOT_SUPPORTED,
                    "不支持的问候场景: " + sceneCode);
        }
        return scene;
    }

    /**
     * 子类必须实现的步骤：生成问候语。
     * <p>
     * 这是模板里唯一"变化"的部分，不同实现可以走不同策略。
     *
     * @param request 请求入参
     * @param scene   解析后的场景枚举
     * @return 问候语
     */
    protected abstract String doBuildGreeting(GreetingRequestDTO request, GreetingSceneEnum scene);

    /**
     * 组装响应 VO，统一走 Converter，避免在子类散落组装逻辑。
     * <p>
     * final 修饰：组装规则是项目级规范，禁止子类各自发挥。
     */
    protected final GreetingVO assemble(String greeting, GreetingSceneEnum scene) {
        GreetingVO vo = GreetingConverter.toVO(greeting, scene);
        log.debug("问候组装完成: greeting={}, scene={}", greeting, scene);
        return vo;
    }

}
