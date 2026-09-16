package com.example.test1.strategy.impl;

import com.example.test1.enums.GreetingSceneEnum;
import com.example.test1.strategy.GreetingStrategy;
import org.springframework.stereotype.Component;

/**
 * 【策略实现】清晨问候。
 * <p>
 * 只关心两件事：
 *   1. supportScene() 声明"我处理 MORNING 场景"；
 *   2. buildGreeting(name) 拼出"早上好，xxx！"。
 * <p>
 * @Component：让 Spring 把它注册成 Bean，工厂启动时自动收集到。
 */
@Component
public class MorningGreetingStrategy implements GreetingStrategy {

    @Override
    public GreetingSceneEnum supportScene() {
        return GreetingSceneEnum.MORNING;
    }

    @Override
    public String buildGreeting(String name) {
        return "早上好，" + name + "！";
    }

}
