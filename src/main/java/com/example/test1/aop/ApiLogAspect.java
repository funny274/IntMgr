package com.example.test1.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * ====================================================================================
 * 【AOP 切面】ApiLogAspect —— 接口日志自动打印器
 * ====================================================================================
 *
 * ▌1. 什么是 AOP（面向切面编程）？
 *   "横切关注点"：多个类都要做的事（日志/权限/事务/限流）。
 *   如果在每个 Controller 方法里都写 log.info(...)，代码重复且侵入业务。
 *   AOP 把这些逻辑抽成"切面"，在方法执行前后自动"织入"。
 *
 *   类比：高速公路收费站。不管你开什么车，过站都得交费。
 *   切面就是那个收费站，所有车（方法）路过都自动被处理。
 *
 * ▌2. 关键概念（对照本类）：
 *   - 切面 Aspect        = 带 @Aspect 的类（本类）
 *   - 切入点 Pointcut    = "在哪些方法上生效"（@Pointcut 定义 controller 包下所有方法）
 *   - 通知 Advice        = "做什么"（@Around 环绕通知：方法前后都干）
 *   - 连接点 JoinPoint   = 被拦截到的那一次方法调用（含参数等信息）
 *
 * ▌3. 注解解释：
 *
 *   @Aspect       声明这是个切面，Spring 会代理被它拦截的方法。
 *   @Component     注册成 Bean，让 Spring 发现并生效。
 *   @RequiredArgsConstructor 注入 ObjectMapper（Spring 容器里的 JSON 工具）。
 *   @Slf4j         打日志用。
 *
 *   @Pointcut("execution(public * com.example.test1.controller..*.*(..))")
 *     定义"切入点表达式"：
 *       execution( 修饰符 返回类型 包..类.方法(参数) )
 *       .. 表示"当前包及子包"，* 表示"任意"
 *     整句意思：com.example.test1.controller 包（含子包）下所有 public 方法。
 *
 *   @Around("controllerPointcut()")
 *     "环绕通知"：最强大的一种，可以在方法执行前/后/异常时都插入逻辑。
 *     参数 ProceedingJoinPoint 比普通 JoinPoint 多一个 proceed() 方法，
 *     调用它才会真正执行被拦截的方法——你掌控"是否放行"。
 *
 * ▌4. 这个切面做了什么（执行顺序）：
 *   请求进来
 *     ↓ [before] 打印 [API-REQ] 方法/URI/入参
 *     ↓ joinPoint.proceed()  ← 真正执行 Controller 方法（含 Service 调用）
 *     ↓ [after]  打印 [API-RES] 返回值/耗时
 *   如果 proceed() 抛异常：
 *     ↓ [throw] 打印 [API-ERR] 异常信息/耗时，再把异常抛出（不吞异常）
 *
 * ▌5. 为什么要"截断"日志？
 *   一个接口可能返回几 MB 数据（如导出列表），全打印会撑爆日志。
 *   MAX_LOG_LEN 限制单条最大 2000 字符，超了截断 + 标记 (truncated)。
 *
 * ▌6. 为什么要 try-catch 序列化？
 *   有些对象不能被 Jackson 序列化（如 HttpServletRequest/流/循环引用），
 *   如果让异常抛出会影响主流程。失败时降级打印类名+hashCode 即可。
 * ====================================================================================
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    /** 复用 Spring 容器中的 ObjectMapper（自动注入） */
    private final ObjectMapper objectMapper;

    /** 单次打印的最大字符数，防止超大 payload 刷屏 */
    private static final int MAX_LOG_LEN = 2000;

    /**
     * 切入点：controller 包下所有方法。
     * 这个方法本身是空的，只是给 @Pointcut 当"挂载点"用。
     */
    @Pointcut("execution(public * com.example.test1.controller..*.*(..))")
    public void controllerPointcut() {
        // 切点占位方法，方法体留空即可
    }

    /**
     * 环绕通知：在切入点方法前后插入日志逻辑。
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 取当前 HTTP 请求（用于打印方法和 URI）
        HttpServletRequest request = currentRequest();
        String method = joinPoint.getSignature().getDeclaringTypeName() + "#" + joinPoint.getSignature().getName();
        String httpMethod = request == null ? "-" : request.getMethod();
        String uri = request == null ? "-" : request.getRequestURI();

        long start = System.currentTimeMillis();
        // 【前置】打印入参日志
        log.info("[API-REQ] {} {} | method={} | args={}", httpMethod, uri, method, shortenArgs(joinPoint.getArgs()));

        Object result;
        try {
            // ★ 关键：proceed() 才会真正执行被拦截的 Controller 方法
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            // 【异常】打印异常日志后把异常继续抛出（不能吞掉，否则全局异常处理器接不到）
            long cost = System.currentTimeMillis() - start;
            log.warn("[API-ERR] {} {} | cost={}ms | err={}: {}", httpMethod, uri, cost,
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }

        // 【后置】打印出参和耗时
        long cost = System.currentTimeMillis() - start;
        log.info("[API-RES] {} {} | cost={}ms | resp={}", httpMethod, uri, cost, shorten(result));
        return result;
    }

    /**
     * 从 RequestContextHolder 取当前线程绑定的 HttpServletRequest。
     * <p>
     * Spring 把请求对象存在 ThreadLocal 里，任何位置都能拿到。
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    /**
     * 把方法参数数组逐个序列化拼接，方便打印。
     */
    private String shortenArgs(Object[] args) {
        return Arrays.stream(args)
                .map(this::shorten)
                .reduce((a, b) -> a + "," + b)
                .orElse("()");
    }

    /**
     * 安全序列化并截断，避免序列化异常影响主流程。
     * <p>
     * 特殊对象（HttpServletRequest/Response/MultipartFile）不能 JSON 序列化，
     * 单独识别后只打印类名。
     */
    private String shorten(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof HttpServletRequest
                || obj instanceof jakarta.servlet.http.HttpServletResponse
                || obj instanceof org.springframework.web.multipart.MultipartFile) {
            return obj.getClass().getSimpleName();
        }
        try {
            String json = objectMapper.writeValueAsString(obj);
            return json.length() > MAX_LOG_LEN ? json.substring(0, MAX_LOG_LEN) + "...(truncated)" : json;
        } catch (Exception ex) {
            // 序列化失败时降级：打印类名+hashCode，不抛异常
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
        }
    }

}
