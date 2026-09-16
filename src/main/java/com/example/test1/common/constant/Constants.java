package com.example.test1.common.constant;

/**
 * ====================================================================================
 * 【常量】Constants —— 项目级"魔法值"集中地
 * ====================================================================================
 *
 * ▌1. 为什么要抽常量？
 *   代码里出现 "/api"、"20" 这种字面量叫"魔法值"——
 *   三个月后没人知道它什么意思，改的时候要全文搜索还可能漏。
 *   提成常量后：① 有名字能自解释 ② 改一处全局生效 ③ 编译期类型检查。
 *
 * ▌2. 为什么构造器抛异常？
 *   这是工具类/常量类的标准做法：禁止被实例化。
 *   有人手滑 new Constants() 会直接报错，避免无意义对象。
 * ====================================================================================
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }

    /** API 统一前缀：所有 Controller 的 @RequestMapping 都拼这个 */
    public static final String API_PREFIX = "/api";

    /** 默认分页页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 请求头：链路追踪 ID（以后接 SkyWalking/Sleuth 时用） */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

}
