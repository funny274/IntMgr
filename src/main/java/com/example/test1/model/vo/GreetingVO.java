package com.example.test1.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * ====================================================================================
 * 【视图对象】GreetingVO —— 接口出参的"展示卡"
 * ====================================================================================
 *
 * ▌1. 什么是 VO（View Object）？
 *   专门给前端"看"的对象。字段由"前端需要展示什么"决定，
 *   而不是"内部领域模型长什么样"。
 *
 * ▌2. 为什么要和 DTO / Entity 分开？
 *   - 接口契约稳定：内部模型改字段不影响前端；
 *   - 安全：可以隐藏内部敏感字段（如密码、成本价），只挑该露的放进 VO；
 *   - 灵活：同一个领域对象可以"渲染"出多个不同 VO 给不同场景。
 *   简单理解：DTO=收进来，VO=发出去。
 *
 * ▌3. 这里三个字段：
 *   - greeting：最终问候语
 *   - sceneDesc：场景中文描述（前端展示用）
 *   - serverTime：服务端时间戳，便于前端做时序对齐/缓存判断
 *
 * ▌4. 注解说明见 GreetingRequestDTO，这里相同不再赘述。
 * ====================================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "问候响应出参")
public class GreetingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "问候语", example = "早上好，张三！")
    private String greeting;

    @Schema(description = "场景描述", example = "清晨问候")
    private String sceneDesc;

    @Schema(description = "服务端生成时间戳(ms)", example = "1718000000000")
    private Long serverTime;

}
