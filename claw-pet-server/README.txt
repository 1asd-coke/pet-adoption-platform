# claw-pet-server - Spring Boot 后端项目

Claw Pet 宠物领养系统的后端服务。

## 技术栈

Java 17 + Spring Boot 3.2 + MyBatis-Plus 3.5 + MySQL + Redis

## 目录结构

| 目录/文件 | 说明 |
|-----------|------|
| pom.xml | Maven 项目配置，定义所有 Java 依赖 |
| Dockerfile | 容器镜像构建（多阶段：Maven 编译 → JRE 运行） |
| docker-entrypoint.sh | 容器启动入口（首次启动自动还原演示图片） |
| src/main/java/ | Java 源码，按业务分包 |
| src/main/resources/ | 配置文件（application.yml） |
| upload/ | 用户上传的图片文件（已 gitignore） |

## 数据库脚本在哪

不在这个目录里 —— 已统一挪到仓库顶层的 `demo-data/sql/`：

    demo-data/sql/01-init.sql       建表 + 默认管理员 + 收容所信息
    demo-data/sql/02-demo-pets.sql  演示数据（15 只宠物）

演示图片在 `demo-data/images/`，完整说明见 `demo-data/README.md`。

## 启动方式

```bash
mvn spring-boot:run
```

或直接运行 `ClawPetApplication.java` 主类。服务默认端口 8081。

配置（数据库地址、Redis 地址、JWT 密钥等）全部走环境变量，
见仓库根目录的 `.env.example`。
