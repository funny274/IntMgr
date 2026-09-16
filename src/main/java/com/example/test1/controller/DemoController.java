package com.example.test1.controller;

import com.example.test1.common.constant.Constants;
import com.example.test1.common.response.Result;
import com.example.test1.model.dto.GreetingRequestDTO;
import com.example.test1.model.vo.GreetingVO;
import com.example.test1.service.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ====================================================================================
 * 【控制层】DemoController —— 接收 HTTP 请求的"前台接待"
 * ====================================================================================
 *
 * ▌1. Controller 的职责（务必记住这个分工）
 *   Controller 只做三件事：接参数 → 调 Service → 包结果。
 *   不要在 Controller 里写 SQL、写复杂业务、调用多个 Service 互相组合。
 *   一句话：Controller 是"薄"的，业务逻辑在 Service 里。
 *
 * ▌2. 类上的注解逐个解释：
 *
 *   @RestController
 *     = @Controller + @ResponseBody 的组合。
 *     - @Controller：告诉 Spring"这是一个处理请求的类，请把它注册成 Bean"
 *     - @ResponseBody：方法的返回值不要跳页面，而是直接作为响应体写出（默认转成 JSON）
 *     所以前端拿到的是 JSON，不是网页。
 *
 *   @RequestMapping("/api/demo")
 *     给这个类的所有方法定一个"公共前缀"。
 *     下面 hello() 上的 @GetMapping("/hello") 拼起来就是 /api/demo/hello。
 *     好处：同一业务的接口集中在一类，URL 整齐；改前缀只改一处。
 *     这里用 Constants.API_PREFIX 常量而不是硬编码 "/api"，方便全局统一改。
 *
 *   @RequiredArgsConstructor
 *     Lombok 注解：为类里所有 final 字段自动生成构造器。
 *     这里的 demoService 是 final 的，所以会生成
 *       public DemoController(DemoService demoService) { this.demoService = demoService; }
 *     Spring 看到这个唯一构造器，会自动从容器里找 DemoService 实现注入进来。
 *     这叫"构造器注入"，是官方推荐的方式（对比 @Autowired 字段注入更利于测试）。
 *
 *   @Slf4j
 *     Lombok 注解：自动注入一个名为 log 的日志对象，可直接 log.info(...)。
 *     不用再手写 private static final Logger log = LoggerFactory.getLogger(...)。
 *
 *   @Tag(name = "问候服务", ...)
 *     OpenAPI 注解：在 Swagger 文档页把这个 Controller 显示为"问候服务"分组。
 *
 * ▌3. 为什么依赖的是 DemoService 接口而不是 DemoServiceImpl？
 *   面向接口编程：Controller 只知道"有个问候服务"，不关心是谁实现。
 *   好处：换实现类或单元测试 Mock，Controller 一行都不用改。
 *
 * ▌4. 新增接口的"四步走"流程（写在类注释里方便常看）：
 *   ① 在 DemoService 接口加方法签名（决定对外能力）；
 *   ② 在 DemoServiceImpl 实现该方法（写具体业务）；
 *   ③ 在本 Controller 加一个方法，标注 @GetMapping/@PostMapping + URL；
 *   ④ 入参用 DTO + @Valid，出参用 VO，统一用 Result 包装返回。
 * ====================================================================================
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_PREFIX + "/demo")
@RequiredArgsConstructor
@Tag(name = "问候服务", description = "对外提供问候相关接口")
public class DemoController {

    /** 通过构造器注入的服务（final 保证不可变，更安全） */
    private final DemoService demoService;

    /**
     * 接口 1：简单问候。
     * <p>
     * 浏览器访问 GET http://localhost:8080/api/demo/hello 即可触发。
     *
     * 注解解释：
     *   @GetMapping("/hello")
     *     表示"用 GET 方法访问 /hello 才会进这个方法"。
     *     等价于 @RequestMapping(value="/hello", method=RequestMethod.GET)
     *     GET 适合"取数据"，参数一般在 URL 上，幂等（重复请求结果一致）。
     *
     *   @Operation(summary="...")
     *     OpenAPI 注解，文档页会显示这一行作为接口标题。
     *
     * 为什么不传参数？
     *   这就是个"自检接口"——给运维判断服务是否活着，不需要任何入参。
     *
     * 返回 Result&lt;String&gt;：
     *   不直接 return "Hello..."，而是包成 Result.success(...)，
     *   这样所有接口的响应结构统一为 {code, message, data, timestamp}，
     *   前端不用为每个接口单独写解析逻辑。
     *
     * @return 统一响应包装的问候语
     */
    @Operation(summary = "简单问候", description = "返回固定问候语，用于连通性自检")
    @GetMapping("/hello")
    public Result<String> hello() {
        // 第 1 步：调 Service 拿业务结果（Controller 不写业务逻辑）
        String greeting = demoService.getSimpleGreeting();
        // 第 2 步：包成统一响应返回（让 Jackson 自动转 JSON）
        return Result.success(greeting);
    }

    /**
     * 接口 2：按场景个性化问候。
     * <p>
     * 用 Postman/curl 发 POST：
     *   POST http://localhost:8080/api/demo/greet
     *   Content-Type: application/json
     *   Body: {"name":"张三","scene":"MORNING"}
     *
     * 注解解释：
     *   @PostMapping("/greet")
     *     表示"用 POST 方法访问 /greet"。POST 适合"提交数据"，参数在请求体。
     *
     *   @RequestBody
     *     告诉 Spring："把 HTTP 请求体的 JSON 反序列化成 GreetingRequestDTO 对象"。
     *     没有 @RequestBody，Spring 会把参数当 query/form 处理，对不上 JSON 体。
     *
     *   @Valid
     *     触发 DTO 上的校验注解（@NotBlank/@Size/@Pattern）：
     *     - 合法 → 进入方法体
     *     - 不合法 → 抛 MethodArgumentNotValidException，
     *       被 GlobalExceptionHandler 接住，返回 400 + 错误信息，不进方法体
     *     这就是"参数校验自动化"，不用在方法里 if(name==null) 手写校验。
     *
     * 返回 Result&lt;GreetingVO&gt;：
     *   data 是 VO（视图对象），只暴露前端需要的字段，不把内部领域模型泄出去。
     *
     * @param request 问候请求入参
     * @return 问候响应 VO
     */
    @Operation(summary = "个性化问候", description = "按场景与称呼生成问候语")
    @PostMapping("/greet")
    public Result<GreetingVO> greet(@Valid @RequestBody GreetingRequestDTO request) {
        // 调 Service 拿到 VO，包成统一响应返回
        GreetingVO vo = demoService.greet(request);
        return Result.success(vo);
    }

}
