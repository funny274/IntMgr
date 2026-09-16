package com.example.test1.strategy.impl;

import com.example.test1.enums.GreetingSceneEnum;
import com.example.test1.strategy.GreetingStrategy;
import org.springframework.stereotype.Component;

/**
 * 【策略实现】夜晚问候。结构同 MorningGreetingStrategy，只是措辞和场景不同。
 */
@Component
public class EveningGreetingStrategy implements GreetingStrategy {

    @Override
    public GreetingSceneEnum supportScene() {
        return GreetingSceneEnum.EVENING;
    }

    @Override
    public String buildGreeting(String name) {
        return "晚上好，" + name + "！";
    }

}
