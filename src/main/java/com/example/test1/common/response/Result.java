package com.example.test1.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * ====================================================================================
 * 【统一响应】Result —— 所有接口的"标准回信格式"
 * ====================================================================================
 *
 * ▌1. 为什么所有接口都包一层 Result？
 *   想象前端拿到三种原始响应：
 *     接口 A: "Hello"          ← 字符串
 *     接口 B: {"name":"张三"}  ← 对象
 *     接口 C: 404 page        ← 报错页
 *   前端要为每个接口写不同的解析逻辑，很痛苦。
 *   统一成 Result 后都是：{code, message, data, timestamp}
 *   前端只看 code 判断成败，data 取业务数据，一套逻辑通吃所有接口。
 *
 * ▌2. 字段含义：
 *   - code：业务码，100000=成功，其他=各类失败（见 ResultCode）
 *   - message：提示语，可给用户直接展示
 *   - data：真正的业务数据，泛型 T 让每种接口返回不同类型
 *   - timestamp：服务端生成时间，便于排查时序/缓存问题
 *
 * ▌3. 设计要点：
 *
 *   构造器私有 + 静态工厂：
 *     外部不能 new Result(...)，必须通过 Result.success(...)/Result.fail(...)。
 *     好处：调用点语义清晰，不会传错参数；创建逻辑集中可控。
 *
 *   @JsonInclude(NON_NULL)：
 *     data 为 null 时，JSON 里直接不出现 data 字段，省带宽。
 *
 *   Serializable：
 *     支持序列化（虽然走 JSON 时不强依赖，但作为规范保留）。
 *
 * ▌4. 典型用法：
 *   return Result.success(vo);              // 成功带数据
 *   return Result.success();                 // 成功无数据
 *   throw new BusinessException(code);       // 失败——由全局异常处理器转成 Result.fail(...)
 *
 * ▌5. 建造者模式体现在哪？
 *   内部用 private 构造 + 显式赋值；对外暴露静态工厂方法。
 *   如果以后想支持链式 setResult(...).setData(...).setMsg(...)，
 *   也可以扩展为标准 Builder，目前简洁版已满足需求。
 * ====================================================================================
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 时间戳，便于客户端排查时序问题 */
    private Long timestamp;

    /**
     * 内部构造：用 ResultCode 初始化码与默认提示，并填入数据。
     * 私有：禁止外部直接 new，统一走静态工厂。
     */
    private Result(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /* ===================== 静态工厂方法 ===================== */

    /** 成功，无数据 */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS, null);
    }

    /** 成功，带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    /** 失败，使用枚举默认提示 */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode, null);
    }

    /** 失败，覆盖默认提示语（业务运行期才知道具体原因时用） */
    public static <T> Result<T> fail(ResultCode resultCode, String overrideMessage) {
        Result<T> result = new Result<>(resultCode, null);
        result.message = overrideMessage;
        return result;
    }

}
