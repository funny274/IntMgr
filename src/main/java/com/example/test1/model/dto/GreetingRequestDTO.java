package com.example.test1.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * ====================================================================================
 * 【数据传输对象】GreetingRequestDTO —— 接口入参的"快递盒"
 * ====================================================================================
 *
 * ▌1. 什么是 DTO（Data Transfer Object）？
 *   专门用来在"网络/层之间"传递数据的对象。和数据库实体（Entity）区分开：
 *   - Entity 对应数据库表，字段类型/名称受表结构约束；
 *   - DTO 对应接口契约，字段由前端需要传什么决定。
 *   两者分开后，改表结构不会影响接口，反之亦然。
 *
 * ▌2. 为什么用 POJO 而不是直接传 String？
 *   接口入参一般不止一个字段，用对象封装 + JSON 传输最自然。
 *   Spring 收到请求体后，会自动按字段名匹配反序列化。
 *
 * ▌3. 类上的注解：
 *
 *   @Data  Lombok：一键生成 getter/setter/toString/equals/hashCode。
 *
 *   @Builder  Lombok：生成建造者模式 API，可以这样用：
 *     GreetingRequestDTO.builder().name("张三").scene("MORNING").build();
 *   测试和内部构造时比一连串 setter 更清晰。
 *
 *   @NoArgsConstructor / @AllArgsConstructor
 *     生成无参和全参构造器。
 *     Spring/Jackson 反序列化需要无参构造器；
 *     全参构造器方便测试时一行 new 出对象。
 *
 *   @Schema  OpenAPI 注解：Swagger 文档里显示字段说明 + 示例。
 *
 * ▌4. 字段上的校验注解（jakarta.validation）：
 *
 *   @NotBlank  不能为 null、空串、纯空白；仅适用于字符串。
 *   @Size(min,max)  字符串长度/集合大小范围。
 *   @Pattern(regexp)  必须匹配正则；这里限制只能传 MORNING/AFTERNOON/EVENING。
 *
 *   这些注解本身不生效，需要 Controller 方法参数上加 @Valid 触发：
 *   不合法 → 抛 MethodArgumentNotValidException → 被 GlobalExceptionHandler 接住。
 *
 * ▌5. implements Serializable
 *   可序列化标记。虽然走 JSON 不强依赖 Java 原生序列化，
 *   但在缓存（Redis）、RPC 等场景可能用到，作为规范保留。
 * ====================================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "问候请求入参")
public class GreetingRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "被问候者称呼", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "称呼不能为空")
    @Size(min = 1, max = 20, message = "称呼长度需在 1-20 之间")
    private String name;

    @Schema(description = "问候场景编码", example = "MORNING", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "场景编码不能为空")
    @Pattern(regexp = "MORNING|AFTERNOON|EVENING", message = "场景编码必须为 MORNING/AFTERNOON/EVENING")
    private String scene;

}
