package com.example.test1.strategy.impl;

import com.example.test1.enums.GreetingSceneEnum;
import com.example.test1.strategy.GreetingStrategy;
import org.springframework.stereotype.Component;

/**
 * 【策略实现】午后问候。结构同 MorningGreetingStrategy，只是措辞和场景不同。
 */
@Component
public class AfternoonGreetingStrategy implements GreetingStrategy {

    @Override
    public GreetingSceneEnum supportScene() {
        return GreetingSceneEnum.AFTERNOON;
    }

    @Override
    public String buildGreeting(String name) {
        return "下午好，" + name + "！";
    }

}
