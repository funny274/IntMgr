package com.example.test1;

import com.example.test1.common.exception.BusinessException;
import com.example.test1.enums.GreetingSceneEnum;
import com.example.test1.model.dto.GreetingRequestDTO;
import com.example.test1.model.vo.GreetingVO;
import com.example.test1.service.impl.AbstractGreetingService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 【单元测试】模板方法流程。
 * <p>
 * AbstractGreetingService 是抽象类，测试时用一个匿名子类实现"可变步骤" doBuildGreeting，
 * 从而验证模板方法 greet() 的骨架逻辑（校验 → 解析场景 → 生成 → 组装）是否正确串起来。
 */
class AbstractGreetingServiceTest {

    /** 测试专用子类：实现基类要求的两处抽象点（getSimpleGreeting 与模板方法可变步骤 doBuildGreeting） */
    private final AbstractGreetingService service = new AbstractGreetingService() {
        @Override
        public String getSimpleGreeting() {
            return "测试问候";
        }

        @Override
        protected String doBuildGreeting(GreetingRequestDTO request, GreetingSceneEnum scene) {
            return "测试·" + request.getName();
        }
    };

    @Test
    void shouldFlowThroughAllTemplateSteps() {
        GreetingRequestDTO dto = GreetingRequestDTO.builder()
                .name("李四").scene("MORNING").build();

        GreetingVO vo = service.greet(dto);

        assertThat(vo.getGreeting()).isEqualTo("测试·李四");
        assertThat(vo.getSceneDesc()).isEqualTo("清晨问候");
        assertThat(vo.getServerTime()).isNotNull();
    }

    @Test
    void shouldResolveSceneIgnoringCase() {
        GreetingRequestDTO dto = GreetingRequestDTO.builder()
                .name("王五").scene("evening").build();

        GreetingVO vo = service.greet(dto);

        assertThat(vo.getSceneDesc()).isEqualTo("夜晚问候");
    }

    @Test
    void shouldThrowWhenNameBlank() {
        GreetingRequestDTO dto = GreetingRequestDTO.builder()
                .name("  ").scene("MORNING").build();

        assertThatThrownBy(() -> service.greet(dto)).isInstanceOf(BusinessException.class);
    }

}