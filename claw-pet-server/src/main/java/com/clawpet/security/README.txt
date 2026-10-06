# security/ — 安全/权限模块

负责用户认证和授权，基于 JWT 令牌实现无状态认证。
Spring Security 的全局配置定义在 config/SecurityConfig.java 中。

| 文件名 | 作用 |
|--------|------|
| JwtUtil.java | JWT 令牌工具类，负责生成和解析 JWT Token（含过期时间校验） |
| JwtAuthenticationFilter.java | JWT 认证过滤器，从请求头提取 Token 并验证用户身份，注入安全上下文 |
