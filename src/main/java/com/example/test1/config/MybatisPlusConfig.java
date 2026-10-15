package com.example.test1.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ====================================================================================
 * 【配置类】MybatisPlusConfig —— MyBatis-Plus 的全局装配
 * ====================================================================================
 *
 * ▌1. @MapperScan("com.example.test1.mapper")
 *   批量把 mapper 包下的所有 Mapper 接口注册成 Spring Bean（不用每个 Mapper 挨个加 @Mapper）。
 *   一个工程扫一次即可。
 *
 * ▌2. 分页插件为什么必须手动注册？
 *   MyBatis-Plus 的"物理分页"不是自动开启的——需要往拦截器链里塞一个
 *   PaginationInnerInterceptor，它会在 SQL 执行前把 `LIMIT`/`ROWNUM` 拼进去。
 *   不注册的话，Page 分页查询会退化成"全表查出再内存截取"，数据一大就爆内存。
 *
 * ▌3. DbType.ORACLE 的作用？
 *   告诉分页插件按 Oracle 方言生成分页 SQL（Oracle 没有 MySQL 的 LIMIT，
 *   它是用 ROWNUM 或 12c+ 的 OFFSET...FETCH 实现），必须指明，否则分页 SQL 拼错。
 *
 * ▌4. setMaxLimit(500) 干嘛的？
 *   防御性限制：防止有人传 pageSize=999999 一次拉爆数据库。超过 500 会被强制压到 500。
 * ====================================================================================
 */
@Configuration
@MapperScan("com.example.test1.mapper")
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器链，并挂上 Oracle 分页插件。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件：指定 Oracle 方言 + 单页上限
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.ORACLE);
        pagination.setMaxLimit(500L);
        interceptor.addInnerInterceptor(pagination);

        return interceptor;
    }

}