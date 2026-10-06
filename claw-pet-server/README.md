# Claw Pet — 后端服务

宠物领养平台后端，基于 Spring Boot 3.2 + MyBatis-Plus + Spring Security + JWT。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.2 |
| MyBatis-Plus | 最新 |
| Spring Security | 6.x |
| JWT | jjwt |
| MySQL | 8.0+ |
| Redis | 7.4（可选，用于缓存） |

## 项目结构

```
src/main/java/com/clawpet/
├── ClawPetApplication.java      # 启动类
├── config/                       # 配置类
│   ├── SecurityConfig.java       # Spring Security + CORS
│   ├── RedisConfig.java          # Redis 缓存配置
│   ├── MyBatisPlusConfig.java    # 分页插件
│   ├── MyMetaObjectHandler.java  # 自动填充
│   └── WebMvcConfig.java         # 静态资源映射
├── security/                     # JWT 认证
│   ├── JwtUtil.java              # Token 生成/解析
│   └── JwtAuthenticationFilter.java
├── common/                       # 公共响应
│   └── Result.java
├── entity/                       # 实体类 (15个，对应 15 张表)
├── mapper/                       # Mapper 接口 (15个)
├── service/                      # 业务逻辑 (13个接口 + 13个实现)
├── controller/                   # API 控制器 (17个)
└── dto/                          # 数据传输对象
```

> 数据库脚本不在这个目录里 —— 已统一挪到仓库顶层的 `demo-data/sql/`，
> 详见 [../demo-data/README.md](../demo-data/README.md)。

## 数据库

- 数据库名：`claw_pet`
- 字符集：`utf8mb4`
- 建表脚本：`demo-data/sql/01-init.sql`
- 演示数据：`demo-data/sql/02-demo-pets.sql`

### 表结构（15 张）

| 表名 | 说明 |
|------|------|
| `users` | 用户表（支持 admin/user 角色） |
| `user_security_question` | 密保问题（用于找回密码） |
| `pet_category` | 宠物分类 |
| `pet_info` | 宠物信息 |
| `pet_image` | 宠物图片（字段是 `url`，值是 `/profile/pet/xxx.jpg`） |
| `pet_favorite` | 收藏 |
| `pet_comment` | 评论 |
| `adopt_application` | 领养申请 |
| `adopt_record` | 领养记录 |
| `followup_record` | 回访记录（`images` 存回访照片） |
| `notification` | 站内通知 |
| `shelter_info` | 收容所信息（`image` 是首页 hero 大图） |
| `adoption_guide` | 领养须知 |
| `adoption_story` | 领养故事 |
| `pet_tip` | 宠物养护小贴士 |

## 接口概览

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 登录/注册/当前用户 |
| 宠物 | `/api/pet` | 宠物 CRUD + 列表筛选 |
| 分类 | `/api/category` | 分类 CRUD |
| 领养 | `/api/adopt` | 申请/审核/取消 |
| 记录 | `/api/record` | 领养记录 + 回访 |
| 收藏 | `/api/favorite` | 收藏/取消/检查 |
| 评论 | `/api/comment` | 评论 CRUD |
| 用户管理 | `/api/users` | 用户列表/删除（管理员） |
| 个人资料 | `/api/profile` | 修改资料/删除账号 |
| 统计 | `/api/stats` | 首页/仪表盘统计数据 |
| 文件上传 | `/api/upload` | 图片/头像上传（三级安全校验） |
| 收容所 | `/api/shelter` | 收容所信息读取/编辑 |

## 配置说明

所有可能因机器而异的配置都走环境变量，`application.yml` 里只放默认值：

```yaml
server.port: ${SERVER_PORT:8081}

spring.datasource.url:      ${DB_URL:jdbc:mysql://localhost:3306/claw_pet?...}
spring.datasource.username: ${DB_USERNAME:root}
spring.datasource.password: ${DB_PASSWORD:}

spring.data.redis.host:     ${REDIS_HOST:localhost}
spring.data.redis.port:     ${REDIS_PORT:6379}
spring.data.redis.password: ${REDIS_PASSWORD:}

jwt.secret:                 ${JWT_SECRET:（留空则启动时随机生成，见下）}
upload.path:                ./upload
```

完整说明见仓库根目录的 `.env.example`。

> ⚠️ **`jwt.secret` 建议在部署前显式设置。**
> 不设置的话，启动时会**随机生成**一个临时密钥 —— 代码里已经没有任何写死的默认值，
> 所以别人伪造不了登录态；但随机密钥重启即失效，会把所有在线用户踢下线。
> 这种情况启动日志里会有 WARN 提醒。

> 上传目录（`upload.path`）虽然是相对路径，但**不依赖启动时的工作目录**：
> 由 `com.clawpet.util.UploadPathResolver` 统一解析成绝对路径，
> 所以在仓库根目录 `java -jar claw-pet-server/target/xxx.jar` 也不会把图片放错地方。

## 启动方式

### 方式一：脚本（推荐）

Windows 在仓库根目录双击 `start.bat`，macOS/Linux 跑 `./scripts/start.sh`，
脚本会检查环境、还原演示图片、装依赖、起前后端。

建库导数据这一步脚本不做，手动执行一次：

```bash
mysql -u root -p -e "CREATE DATABASE claw_pet DEFAULT CHARSET utf8mb4;"
mysql -u root -p claw_pet < demo-data/sql/01-init.sql
mysql -u root -p claw_pet < demo-data/sql/02-demo-pets.sql
```

### 方式二：容器（不用装 JDK/Maven/MySQL）

Windows 双击仓库根目录的 `deploy.bat`，或跑 `./scripts/deploy.sh`；四个容器一键起。

### 方式三：IDE 里跑

打开项目，运行 `ClawPetApplication.java`。服务启动在 `http://localhost:8081`。

> 可选：启动 Redis（端口 6379）启用缓存与验证码；不启动的话登录验证码不可用。
