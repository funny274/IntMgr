package com.example.test1;

import com.example.test1.common.exception.BusinessException;
import com.example.test1.strategy.GreetingStrategy;
import com.example.test1.strategy.GreetingStrategyFactory;
import com.example.test1.strategy.impl.AfternoonGreetingStrategy;
import com.example.test1.strategy.impl.EveningGreetingStrategy;
import com.example.test1.strategy.impl.MorningGreetingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 【单元测试】策略工厂。
 * <p>
 * 纯单元测试：不启动 Spring 容器，直接 new 工厂和策略，快、稳、聚焦。
 * 重点覆盖：① 自动注册 ② 按场景取策略 ③ 重复场景启动报错 ④ 未知场景抛业务异常。
 */
class GreetingStrategyFactoryTest {

    private GreetingStrategyFactory newFactory(List<GreetingStrategy> strategies) {
        GreetingStrategyFactory factory = new GreetingStrategyFactory(strategies);
        factory.init(); // 模拟 Spring 的 @PostConstruct
        return factory;
    }

    @Test
    void shouldRegisterAllStrategiesAndGetByScene() {
        GreetingStrategyFactory factory = newFactory(List.of(
                new MorningGreetingStrategy(),
                new AfternoonGreetingStrategy(),
                new EveningGreetingStrategy()));

        assertThat(factory.getStrategy("MORNING").buildGreeting("张三")).isEqualTo("早上好，张三！");
        assertThat(factory.getStrategy("afternoon").buildGreeting("张三")).isEqualTo("下午好，张三！");
        assertThat(factory.getStrategy("EVENING").buildGreeting("张三")).isEqualTo("晚上好，张三！");
    }

    @Test
    void shouldThrowWhenDuplicateSceneRegistered() {
        // 两个策略都声明 MORNING -> 启动时应报 IllegalStateException，避免运行时歧义
        GreetingStrategyFactory factory = new GreetingStrategyFactory(List.of(
                new MorningGreetingStrategy(),
                new MorningGreetingStrategy()));

        assertThatThrownBy(factory::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowBusinessExceptionOnUnknownScene() {
        GreetingStrategyFactory factory = newFactory(List.of(new MorningGreetingStrategy()));

        assertThatThrownBy(() -> factory.getStrategy("NIGHT"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NIGHT");
    }

}