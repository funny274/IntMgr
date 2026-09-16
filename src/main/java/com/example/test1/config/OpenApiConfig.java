package com.example.test1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ====================================================================================
 * 【配置类】OpenApiConfig —— Swagger 接口文档的"门面信息"
 * ====================================================================================
 *
 * ▌1. 什么是 @Configuration？
 *   标注这是个"配置类"，相当于以前的 XML 配置文件。
 *   里面的 @Bean 方法返回的对象会被注册进 Spring 容器。
 *
 * ▌2. 这个类做了什么？
 *   配置 OpenAPI 文档的元信息（标题、版本、作者、许可证）。
 *   访问 http://localhost:8080/swagger-ui.html 时，页面顶部会显示这些信息。
 *
 * ▌3. @Bean 是什么意思？
 *   把方法的返回值注册成 Bean 放进容器。
 *   Spring 启动时会调这个方法，把返回的 OpenAPI 对象存起来，
 *   springdoc 库会从容器里取这个对象用于生成文档。
 *
 * ▌4. 为什么不直接在 yml 里配？
 *   yml 适合简单配置；涉及对象构建（嵌套的 Info/Contact/License）时，
 *   Java 代码比一长串 yml 更清晰、可调试。
 * ====================================================================================
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("test1 接口服务")
                        .description("Spring Boot 分层架构示例接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("test1-team"))
                        .license(new License().name("Apache 2.0")));
    }

}
