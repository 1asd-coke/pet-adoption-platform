# common/ - 通用工具与统一响应

提供全局统一的 API 响应体和异常处理机制，所有控制器的返回格式由此包统一。

## 文件说明

| 文件 | 作用 |
|------|------|
| Result.java | 统一 API 响应封装类。泛型结构 `{code, data, message}`，提供 success()、error()、badRequest() 等静态工厂方法。所有控制器的返回值均使用此类。 |
| GlobalExceptionHandler.java | 全局异常处理器。通过 @ControllerAdvice 拦截参数校验异常、业务异常（IllegalArgumentException）、运行时异常，统一转换为 Result 格式返回，避免直接将异常堆栈暴露给前端。 |
