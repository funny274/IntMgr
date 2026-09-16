package com.example.test1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ====================================================================================
 * 【程序入口】Test1Application —— 整个 Spring Boot 项目的"点火开关"
 * ====================================================================================
 *
 * ▌1. 这是什么？为什么是入口？
 *   每个普通 Java 程序的入口都是 main 方法。Spring Boot 也不例外：
 *   运行这个类的 main 方法，就会启动内嵌的 Tomcat，让程序变成一个"小服务器"。
 *   也就是说：双击运行 / IDE 点绿色三角 / java -jar xxx.jar，最终都来到这里。
 *
 * ▌2. main 方法里那一行做了什么？
 *   SpringApplication.run(Test1Application.class, args) 会做一大堆事：
 *     ① 创建 Spring 容器（ApplicationContext，一个管理所有对象的"大管家"）
 *     ② 扫描本类所在包 com.example.test1 及其所有子包，
 *        把带 @RestController / @Service / @Component 等注解的类，
 *        全部 new 出来放进容器（专业叫法：注册成 Bean）
 *     ③ 启动内嵌 Tomcat，监听 8080 端口
 *     ④ 等待请求进来
 *   所以你写完一个 Controller，不用手动 new，Spring 会自动帮你创建实例。
 *
 * ▌3. @SpringBootApplication 注解含义
 *   这是一个"组合注解"，相当于同时加了三个：
 *     - @SpringBootConfiguration  → 本类自己也是一个配置类（可在这里定义 Bean）
 *     - @EnableAutoConfiguration   → 按依赖自动装配：发现 web 依赖就配 MVC+Tomcat
 *     - @ComponentScan              → 扫描包，自动注册带注解的类（见第 2 点）
 *
 * ▌4. 扫描范围是怎么定的？
 *   默认从"主启动类所在的包"开始向下扫描。本类在 com.example.test1，
 *   所以 controller / service / strategy / aop / common / config 子包都会被扫到。
 *   注意：如果把某个 Service 写到 com.example.test2 包下，默认扫不到，会注入失败。
 *
 * ▌5. 启动后能看到什么？
 *   控制台会打印 Spring 的 banner，最后一行通常长这样：
 *     "Started Test1Application in 2.345 seconds (process running for 3.1)"
 *   看到这行说明成功了，这时你就可以用浏览器/Postman 访问接口了。
 *
 * ▌6. 整体调用链路（一个请求从进到出的完整路径）：
 *   浏览器 GET http://localhost:8080/api/demo/hello
 *        │
 *        ▼
 *   Tomcat 接收 HTTP 请求，转交给 Spring MVC 的 DispatcherServlet
 *        │
 *        ▼
 *   DispatcherServlet 按 URL(/api/demo/hello) 找到 DemoController#hello()
 *        │  ← 在这一层之前，AOP 的 ApiLogAspect 会先"拦一下"打印入参日志
 *        ▼
 *   DemoController.hello() 调用 demoService.getSimpleGreeting()
 *        │
 *        ▼
 *   DemoServiceImpl.getSimpleGreeting() 返回 "Hello, Spring Boot!"
 *        │
 *        ▼
 *   Controller 用 Result.success(...) 把字符串包装成统一响应体
 *        │
 *        ▼
 *   Jackson 把 Result 对象序列化成 JSON
 *        │  ← AOP 在这里再"拦一下"打印出参和耗时
 *        ▼
 *   Tomcat 把 JSON 通过 HTTP 响应回浏览器
 *
 *   你看：这个 main 方法只是"点火"，真正的业务在它扫描出来的那些 Bean 里。
 * ====================================================================================
 */
@SpringBootApplication
public class Test1Application {

    /**
     * 程序入口方法。args 是命令行参数，一般用不到。
     */
    public static void main(String[] args) {
        // 这一行启动整个 Spring Boot 应用，返回的 ConfigurableApplicationContext
        // 就是那个"大管家"容器，可以从中按类型取出任意 Bean。
        SpringApplication.run(Test1Application.class, args);
    }

}
