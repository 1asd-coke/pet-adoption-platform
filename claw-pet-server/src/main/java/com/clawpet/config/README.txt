# config/ — 配置类

该目录存放 Spring Boot 的应用配置类，负责框架集成和全局配置。

| 文件名 | 作用 |
|--------|------|
| SecurityConfig.java | Spring Security 安全配置，定义接口访问权限、白名单路径、密码编码器等 |
| WebMvcConfig.java | Spring MVC 配置，包含 CORS 跨域设置、静态资源映射路径 |
| WebSocketConfig.java | WebSocket 配置，注册 WebSocket 处理器和握手拦截器 |
| RedisConfig.java | Redis 缓存配置，设定 Key/Value 序列化方式及连接池参数 |
| MyBatisPlusConfig.java | MyBatis-Plus 分页插件配置，启用 MyBatis-Plus 分页功能 |
| MyMetaObjectHandler.java | MyBatis-Plus 自动填充处理器，自动填充创建时间、更新时间等通用字段 |
