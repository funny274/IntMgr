package com.example.test1.common.exception;

import com.example.test1.common.response.Result;
import com.example.test1.common.response.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ====================================================================================
 * 【全局异常处理器】GlobalExceptionHandler —— 所有异常的"总收发室"
 * ====================================================================================
 *
 * ▌1. 它解决什么问题？
 *   没有它，每个 Controller 方法都得这样写：
 *     try { ... } catch(参数错误 e) {...} catch(业务异常 e) {...} catch(Exception e) {...}
 *   代码被 try-catch 淹没。有了全局处理器：
 *   - 业务代码只管在合适处 throw，或正常 return；
 *   - 所有异常自动汇聚到这里，统一转成 Result JSON。
 *
 * ▌2. @RestControllerAdvice 怎么生效？
 *   = @ControllerAdvice + @ResponseBody。
 *   它会让本类中所有 @ExceptionHandler 标注的方法"全局生效"：
 *   任何 Controller 抛出的异常都会被按类型匹配到对应方法处理。
 *   返回值会被 @ResponseBody 转 JSON 写回响应体。
 *
 * ▌3. @ExceptionHandler(X.class)
 *   声明"这个方法负责处理 X 类型的异常"。
 *   Spring 会按异常类型精确匹配，找最贴切的那个方法。
 *
 * ▌4. @ResponseStatus(HttpStatus.BAD_REQUEST)
 *   设置 HTTP 状态码。虽然 body 是统一 Result，
 *   但状态码仍按 HTTP 语义返回 400/500，便于运维监控/网关判断。
 *
 * ▌5. 异常处理优先级（从具体到兜底）：
 *   ① BusinessException       → 业务异常（200，前端按 code 处理）
 *   ② MethodArgumentNotValid… → 参数校验（400）
 *   ③ ConstraintViolation…    → query/path 参数校验（400）
 *   ④ Missing…Parameter        → 缺 query 参数（400）
 *   ⑤ HttpMessageNotReadable  → JSON 体格式错（400）
 *   ⑥ HttpRequestMethodNot…   → 方法不对（405）
 *   ⑦ Exception               → 兜底（500，打印完整堆栈）
 *
 *   为什么业务异常返回 200 而不是 4xx/5xx？
 *   业务约定：HTTP 200 表示"请求被服务端正确处理了"，业务成败看 code。
 *   这样前端只需一种处理路径：先看 code，不为 100000 就走错误分支。
 * ====================================================================================
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：返回 200 + 业务错误码（前端按 code 分支处理）。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        // warn 级别：业务异常是可预期的，不必当成系统错误
        log.warn("业务异常: code={}, msg={}", ex.getResultCode().getCode(), ex.getMessage());
        return Result.fail(ex.getResultCode(), ex.getMessage());
    }

    /**
     * 参数校验失败（@Valid 标注在 Bean 上）。
     * <p>
     * 触发场景：POST 请求体 + DTO 上 @NotBlank 等校验不通过。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        // 把所有字段错误信息用分号拼起来，一次返回给前端
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return Result.fail(ResultCode.BAD_REQUEST, msg);
    }

    /**
     * 参数绑定失败（表单形式）。
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBind(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.BAD_REQUEST, msg);
    }

    /**
     * 参数校验失败（@RequestParam / @PathVariable 上的校验）。
     * <p>
     * 触发场景：方法参数直接标 @Pattern/@Min 等（需要类上加 @Validated）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.BAD_REQUEST, msg);
    }

    /**
     * 缺少必填 query 参数。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        return Result.fail(ResultCode.BAD_REQUEST, "缺少必填参数: " + ex.getParameterName());
    }

    /**
     * 请求体不可读（JSON 格式错误）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        return Result.fail(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    /**
     * 请求方法不被允许（比如对只支持 POST 的接口发 GET）。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    /**
     * 兜底：未预期的系统异常。
     * <p>
     * 打印完整堆栈便于排查，但不把堆栈信息返回给前端（安全考虑）。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknown(Exception ex) {
        log.error("未预期异常", ex);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }

}
