package com.example.test1.common.exception;

import com.example.test1.common.response.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * ====================================================================================
 * 【业务异常】BusinessException —— 可预期业务错误的统一表达
 * ====================================================================================
 *
 * ▌1. 为什么不直接 return Result.fail(...) 而要抛异常？
 *   - 业务方法嵌套很深时，层层返回错误码会让代码充斥 if/return；
 *   - 抛异常可以"一跳到底"，由最外层的 GlobalExceptionHandler 统一接住转 Result；
 *   - 业务代码保持"正常路径"风格，只在异常处 throw，可读性更好。
 *
 * ▌2. 为什么继承 RuntimeException 而不是 Exception？
 *   RuntimeException 是"非受检异常"，方法签名上不必声明 throws，
 *   不会污染业务接口；如果用 Exception，每个方法都得 throws，太啰嗦。
 *
 * ▌3. 字段：
 *   - resultCode：关联的错误码枚举，由全局处理器取出填到 Result.code
 *
 * ▌4. 使用示例：
 *   if (scene == null) {
 *       throw new BusinessException(ResultCode.GREETING_SCENE_NOT_SUPPORTED, "不支持的场景: " + code);
 *   }
 *   GlobalExceptionHandler 捕获后返回：
 *   {"code":200001, "message":"不支持的场景: XXX", "timestamp":...}
 * ====================================================================================
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 关联的错误码枚举 */
    private final ResultCode resultCode;

    /**
     * 用枚举自带的默认 message。
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 覆盖默认 message，运行时拼具体原因。
     * <p>
     * 比如"不支持的问候场景: ABC"——code 仍是枚举的，但 message 更具体。
     */
    public BusinessException(ResultCode resultCode, String overrideMessage) {
        super(overrideMessage);
        this.resultCode = resultCode;
    }

}
