resources/ — 配置文件和静态资源
===============================
Spring Boot 的配置目录。目前只有一个文件：

- application.yml：数据库、Redis、JWT、文件上传路径等全部核心配置

配置项一律写成 `${环境变量:默认值}` 的形式，好处有两个：
1. 不配也能在本地跑起来
2. 容器部署时靠环境变量覆盖（见仓库根目录的 .env.example 与 docker-compose.yml）

改配置项时记得同步检查三处要不要跟着改：
  .env.example  ·  docker-compose.yml 的 backend.environment  ·  各 Dockerfile
（这三个文件里的变量名必须完全对得上，漏一个就会「配了但没生效」）
