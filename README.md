<div align="center">

# 🐾 Claw Pet

### 宠物领养信息管理平台

*用爱给流浪动物一个温暖的家*

[![Java](https://img.shields.io/badge/Java-17-blue?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?style=flat-square&logo=vuedotjs)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.4-red?style=flat-square&logo=redis)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](./LICENSE)

</div>

---

## 📖 项目概述

**Claw Pet** 是一个面向流浪动物救助机构的宠物领养信息管理平台。采用前后端分离架构，提供从宠物信息发布、用户浏览申请、领养审核、回访跟进到数据统计的**全链路业务闭环**。

### 解决了什么问题

我国每年约有数千万只流浪动物，救助站容量有限、领养率低。传统救助站依赖 Excel + 微信群管理，存在以下痛点：

| 业务痛点 | 传统做法 | Claw Pet 方案 |
|---------|---------|--------------|
| 🐾 流浪动物信息零散，难触达潜在领养人 | 朋友圈/微信群零散发布 | 统一线上展示平台，多维度筛选精准匹配 |
| 📞 领养流程不透明 | 电话沟通，进度黑箱 | 在线申请 + 状态追踪 + WebSocket 实时通知 |
| 📋 回访追踪缺失 | 送出即失联，二次遗弃频发 | 领养记录 + 回访跟进，建立长期档案 |
| 📊 数据沉淀空白 | 纸质/Excel 记录，易丢失 | MySQL 持久化 + ECharts 看板辅助决策 |
| 🔒 隐私安全无保障 | 公开网盘散落敏感信息 | JWT + BCrypt + 三级文件校验 |

### 核心数据

- **79** 个 RESTful API
- **17** 个后端 Controller
- **15** 张数据库表
- **8** 大业务模块
- **3** 层权限体系（游客 / 用户 / 管理员）

### 两种跑法，各自只要一条命令

| 场景 | 命令 | 本机需要装什么 |
|------|------|---------------|
| **零基础上手** | `quick-start.bat`（Windows 双击） | 脚本自动检查并安装 Docker / WSL2 / 镜像加速 |
| **本地开发** | `start.bat`（Windows 双击）/ `./scripts/start.sh` | JDK 17 · Maven · Node · MySQL 8 · Redis |
| **容器部署** | `deploy.bat`（Windows 双击）/ `./scripts/deploy.sh` | **只要 Docker** |

- `quick-start.bat` —— 面向「刚 clone 下来什么都没装」的用户：7 步检查（系统 / Docker / WSL2 / 引擎 / 国内镜像 / 端口），能自动修就自动修，最后调`deploy` 完成部署
- `start.*` —— 用你本机的环境直接跑，改代码即时生效，适合开发调试
- `deploy.*` —— 把 MySQL / Redis / 后端 / 前端打包成 **4 个容器**，一条命令全起，适合交付和上线

三个脚本互不干扰，可以共存。`start.*` 还会自动读 `.env` → 起 MySQL/Redis →
还原演示图片 → 装前端依赖 → 起前后端 → 开浏览器；`./scripts/stop.sh` 用来停。

> 脚本本体在 `scripts/` 下，根目录只留三个 Windows 双击入口。所有脚本都会自己往上退一级
> 定位仓库根目录，所以 `.env` / `docker-compose.yml` / `demo-data/` 的相对路径不受位置影响。
> 各脚本的参数与排错见 [`scripts/README.txt`](./scripts/README.txt)。

### 代表功能

| 领养申请（资料自动带入） | 后台数据仪表盘 |
|:---:|:---:|
| ![领养申请审核](screenshots/24-admin-approval.png) | ![后台数据仪表盘](screenshots/14-admin-dashboard.png) |
| **用户侧：填资料 → 提交 → 实时收到审核结果** | **管理侧：ECharts 趋势 + 分类占比 + 待审统计** |

---

<details>
<summary><b>🖼 全部页面截图（29 张）— 点击展开</b></summary>


> 共 29 张实机截图，原图在 [`screenshots/`](./screenshots) 目录

**前台 · 浏览与发现**

| 主页 | 宠物列表 |
|:---:|:---:|
| ![主页](screenshots/01-home.png) | ![宠物列表](screenshots/02-pet-list.png) |
| **宠物详情** | **关键词分类** |
| ![宠物详情](screenshots/21-pet-detail.png) | ![关键词分类](screenshots/03-category-filter.png) |
| **关键词搜索** | **明暗主题切换** |
| ![关键词搜索](screenshots/04-search.png) | ![明暗主题切换](screenshots/13-theme.png) |

**前台 · 内容资讯**

| 领养须知 | 领养小常识 |
|:---:|:---:|
| ![领养须知](screenshots/05-adoption-guide.png) | ![领养小常识](screenshots/06-tips.png) |
| **领养故事** | |
| ![领养故事](screenshots/28-adoption-stories.png) | |

**前台 · 注册登录**

| 登录页 | 注册页 |
|:---:|:---:|
| ![登录页](screenshots/07-login.png) | ![注册页](screenshots/08-register.png) |
| **Redis 图形验证码** | **个人中心** |
| ![Redis 图形验证码](screenshots/09-captcha.png) | ![个人中心](screenshots/12-profile.png) |
| **普通用户菜单** | **管理员菜单** |
| ![普通用户菜单](screenshots/10-user-menu.png) | ![管理员菜单](screenshots/11-admin-menu.png) |

**前台 · 互动与消息**

| 发表评论 | 收藏宠物 |
|:---:|:---:|
| ![发表评论](screenshots/22-comment.png) | ![收藏宠物](screenshots/23-favorite.png) |
| **我的收藏** | **消息中心** |
| ![我的收藏](screenshots/27-my-favorites.png) | ![消息中心](screenshots/25-message-center.png) |
| **申请消息** | **我的消息** |
| ![申请消息](screenshots/26-application-notice.png) | ![我的消息](screenshots/29-my-messages.png) |

**后台 · 管理端**

| 数据可视化仪表盘 | 宠物管理 |
|:---:|:---:|
| ![数据可视化仪表盘](screenshots/14-admin-dashboard.png) | ![宠物管理](screenshots/15-admin-pets.png) |
| **宠物分类管理** | **领养管理** |
| ![宠物分类管理](screenshots/16-admin-categories.png) | ![领养管理](screenshots/17-admin-adoptions.png) |
| **领养记录管理** | **领养申请审核** |
| ![领养记录管理](screenshots/18-admin-records.png) | ![领养申请审核](screenshots/24-admin-approval.png) |
| **领养须知编辑器（多章节）** | **收容所信息管理** |
| ![领养须知编辑器](screenshots/19-guide-manage.png) | ![收容所信息管理](screenshots/20-admin-shelter.png) |


</details>

<details>
<summary><b>🎬 完整功能演示动图（27 段· 1920×1080 录屏）— 点击展开</b></summary>


以下 **27 段**均为真实操作录屏（Playwright 驱动真实浏览器，**1920×1080** 拍摄，自动循环播放），覆盖从前台浏览到后台审核的完整业务闭环。

> 动图尺寸 1120×630，在 Retina 屏上也能看清文字。
> 想看**原始画质**？同目录下 `screenshots/clips/` 存放着对应的 MP4（1920×1080，H.264 crf20）。

#### A. 账号体系

<table>
<tr>
  <th>① 登录 ⇄ 注册 双栏滑动切换</th>
  <th>② 登录（Redis 算术验证码）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/01-auth-switch.gif" alt="登录注册滑动切换" width="100%" /></td>
  <td><img src="screenshots/gifs/02-login.gif" alt="登录流程" width="100%" /></td>
</tr>
<tr>
  <th>③ 注册（可设置密保问题）</th>
  <th>④ 找回密码（验证 → 密保 → 重置）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/03-register.gif" alt="注册流程" width="100%" /></td>
  <td><img src="screenshots/gifs/04-forgot-password.gif" alt="找回密码" width="100%" /></td>
</tr>
<tr>
  <th>⑤ 主题一键切换（10 套配色）</th>
  <th></th>
</tr>
<tr>
  <td><img src="screenshots/gifs/05-theme-switch.gif" alt="主题切换" width="100%" /></td>
  <td></td>
</tr>
</table>

#### B. 前台浏览

<table>
<tr>
  <th>⑥ 首页（动态光晕 + 实时统计）</th>
  <th>⑦ 宠物列表（分类筛选 + 关键词搜索）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/06-home.gif" alt="首页" width="100%" /></td>
  <td><img src="screenshots/gifs/07-pet-list.gif" alt="宠物列表筛选" width="100%" /></td>
</tr>
<tr>
  <th>⑧ 宠物详情（图片轮播 + 收藏切换）</th>
  <th>⑨ 领养须知（多章节折叠）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/08-pet-detail.gif" alt="宠物详情" width="100%" /></td>
  <td><img src="screenshots/gifs/09-guide.gif" alt="领养须知" width="100%" /></td>
</tr>
<tr>
  <th>⑩ 领养小常识（分类 → 详情）</th>
  <th></th>
</tr>
<tr>
  <td><img src="screenshots/gifs/10-tip.gif" alt="领养小常识" width="100%" /></td>
  <td></td>
</tr>
</table>

#### C. 互动与消息

<table>
<tr>
  <th>⑪ 发表评论与回复</th>
  <th>⑫ 消息中心（系统通知 / 我的评论 / 被回复）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/11-comment.gif" alt="评论与回复" width="100%" /></td>
  <td><img src="screenshots/gifs/13-message-tabs.gif" alt="消息中心三Tab" width="100%" /></td>
</tr>
<tr>
  <th>⑬ 我的收藏（取消收藏 → 空状态）</th>
  <th>⑭ 发布领养故事（富文本编辑器）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/14-favorite.gif" alt="收藏夹" width="100%" /></td>
  <td><img src="screenshots/gifs/15-story.gif" alt="发布领养故事" width="100%" /></td>
</tr>
</table>

#### ⚡ D. 实时通知（WebSocket）

<table>
<tr>
  <th>⑮ 用户发评论 → 管理员回复 → 铃铛实时亮起</th>
  <th>⑯ 管理员端收到新领养申请</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/12-comment-reply-notify.gif" alt="评论回复实时通知" width="100%" /></td>
  <td><img src="screenshots/gifs/18-realtime-admin.gif" alt="管理员实时收申请" width="100%" /></td>
</tr>
<tr>
  <th>⑰ 用户端实时收到审核结果</th>
  <th></th>
</tr>
<tr>
  <td><img src="screenshots/gifs/20-realtime-user.gif" alt="用户实时收审核结果" width="100%" /></td>
  <td></td>
</tr>
</table>

> **业务规则**（`CommentServiceImpl.add()`）：只有**回复**（`parentId > 0`）才会给对方建`COMMENT_REPLY` 通知，顶级评论不打扰任何人，且**不通知自己**。
> ⑮ 中用户正停留在宠物详情页，管理员的回复一提交，铃铛徽标立刻从空跳到 1，下拉里就是刚收到的那条。

#### E. 领养业务闭环

<table>
<tr>
  <th>⑱ 提交领养申请（自动带入个人中心资料）</th>
  <th>⑲ 我的申请（进度追踪 + 取消）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/16-adoption-apply.gif" alt="提交领养申请" width="100%" /></td>
  <td><img src="screenshots/gifs/17-my-applications.gif" alt="我的申请" width="100%" /></td>
</tr>
<tr>
  <th>⑳ 管理员审核通过（状态机流转）</th>
  <th></th>
</tr>
<tr>
  <td><img src="screenshots/gifs/19-review-approve.gif" alt="审核通过" width="100%" /></td>
  <td></td>
</tr>
</table>

> ⑯ → ⑳ → ⑰ 是**同一条申请**的完整时序：申请提交后管理员端铃铛实时亮起，审核通过后用户端立刻收到通知，全程无需刷新页面。
> ⑱ 弹窗会自动从个人中心带入手机号和地址（`PetDetail.vue` 读`userStore.userInfo` 预填），不需要重复填写。

#### F. 后台管理

<table>
<tr>
  <th>㉑ 数据仪表盘（ECharts 趋势 + 分类占比）</th>
  <th>㉒ 分类管理（新增 / 拖拽排序 / 编辑 / 删除）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/21-admin-dashboard.gif" alt="数据仪表盘" width="100%" /></td>
  <td><img src="screenshots/gifs/22-admin-category.gif" alt="分类管理" width="100%" /></td>
</tr>
<tr>
  <th>㉓ 新增宠物</th>
  <th>㉔ 领养记录与回访登记</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/23-admin-pet-add.gif" alt="新增宠物" width="100%" /></td>
  <td><img src="screenshots/gifs/24-admin-record.gif" alt="回访登记" width="100%" /></td>
</tr>
<tr>
  <th>㉕ 内容管理（须知 / 常识 / 收容所）</th>
  <th>㉖ 领养故事审核（标记展示）</th>
</tr>
<tr>
  <td><img src="screenshots/gifs/25-admin-content.gif" alt="内容管理" width="100%" /></td>
  <td><img src="screenshots/gifs/27-admin-story.gif" alt="故事审核" width="100%" /></td>
</tr>
</table>

#### G. 个人中心

<table>
<tr>
  <th>㉗ 个人信息 / 密保问题 / 修改密码（两种验证方式）</th>
  <th></th>
</tr>
<tr>
  <td><img src="screenshots/gifs/26-profile.gif" alt="个人中心" width="100%" /></td>
  <td></td>
</tr>
</table>

> 修改密码支持两种身份验证：**当前密码**或**密保验证**（后者需先在「密保问题」Tab 添加至少一个问题，否则按钮置灰）。

---


</details>
## 📁 项目结构

```
Claw-Pet/
│
├── demo-data/                             # ⭐ 测试数据全部集中在这里（见 demo-data/README.md）
│   ├── README.md                          #    逐文件说明、对应关系、怎么重新生成
│   ├── sql/
│   │   ├── 01-init.sql                    #    建表（15 张）+ 默认管理员 + 收容所信息
│   │   └── 02-demo-pets.sql               #    演示数据（5 分类 + 15 只宠物 + 图片记录）
│   ├── images/
│   │   ├── manifest.json                  #    图片 ↔ 宠物的对应关系（机器可读）
│   │   ├── pet/                           #    实际被前端加载的演示图（16 张 / 1.0 MB）
│   │   └── originals/                     #    素材源（20 张 / 1.9 MB），只在重新生成时才用
│   └── tools/
│       └── build-demo-images.py           #    从原图生成演示图（也可从外部照片导入）
│
├── claw-pet-server/                       # 后端（Spring Boot 3.2）
│   ├── pom.xml                            # Maven 依赖
│   ├── Dockerfile                         # 容器镜像（多阶段：Maven 编译 → JRE 运行）
│   ├── docker-entrypoint.sh               # 容器启动入口（首次自动还原演示图片）
│   ├── upload/                            # 文件存储（内容 gitignore）
│   │   ├── pet/                           # 宠物图片
│   │   └── avatar/                        # 用户头像
│   └── src/main/java/com/clawpet/
│       ├── ClawPetApplication.java        # 启动类
│       ├── common/                        # 统一响应 + 全局异常
│       ├── config/                        # Security / Redis / MyBatis / MVC / WebSocket
│       ├── security/                      # JWT 工具 + 认证过滤器
│       ├── entity/                        # 15 个实体类
│       ├── mapper/                        # MyBatis-Plus Mapper
│       ├── service/                       # 业务逻辑层 + impl
│       ├── controller/                    # 17 个 API 控制器
│       ├── dto/                           # 数据传输对象
│       ├── task/                          # 定时任务（孤儿文件清理）
│       ├── util/                          # 验证码生成 / 上传路径解析
│       └── websocket/                     # WebSocket 推送处理器
│
├── claw-pet-web/                          # 前端（Vue 3 + Vite 5）
│   ├── package.json
│   ├── Dockerfile                         # 构建 + Nginx
│   ├── nginx.conf                         # ⭐ 上线时必须和 vite.config.js 的代理规则一致
│   ├── vite.config.js                     # 开发代理（/api + /ws + /profile）
│   └── src/
│       ├── main.js                        # 应用入口
│       ├── App.vue
│       ├── styles/theme.css               # 设计系统 + 主题变量
│       ├── styles/admin.css               # 后台样式修正
│       ├── api/                           # Axios 接口封装（15 个模块）
│       ├── stores/user.js                 # Pinia 用户状态
│       ├── router/index.js                # 路由 + 登录守卫
│       ├── layouts/                       # FrontLayout / AdminLayout
│       ├── components/                    # HeroBanner / AuroraBg / DeveloperCard / ThemeSwitcher ...
│       ├── composables/                   # useWebSocket 等组合式函数
│       ├── utils/                         # compressImage / themes 工具
│       └── views/
│           ├── auth/                      # 登录 / 注册 / 忘记密码
│           ├── home/                      # 首页
│           ├── pet/                       # 宠物列表 / 详情
│           ├── adopt/                     # 我的领养申请
│           ├── guide/                     # 领养须知
│           ├── tip/                       # 小常识列表 / 详情
│           ├── story/                     # 领养故事 / 编辑器
│           ├── message/                   # 消息中心
│           ├── comment/                   # 我的评论（旧，已重定向至消息中心）
│           ├── notification/              # 通知（旧，已重定向至消息中心）
│           ├── favorite/                  # 我的收藏
│           ├── profile/                   # 个人中心
│           └── admin/                     # 后台管理（10 个页面）
│
├── scripts/                               # ⭐ 启动与部署脚本（详见 scripts/README.txt）
│   ├── quick-start.ps1                    #    一键引导：检查 Docker/WSL2/网络 → 自动修复 → 部署
│   ├── start.ps1 / start.sh               #    本地开发：读 .env → 起 MySQL/Redis → 起前后端
│   ├── stop.sh                            #    本地开发：停掉前后端（macOS / Linux）
│   └── deploy.ps1 / deploy.sh             #    容器部署：构建镜像 + 起 4 个容器
│
├── start.bat                              # ⭐ Windows 双击：本地开发一键启动（转调 scripts/start.ps1）
├── deploy.bat                             # ⭐ Windows 双击：容器一键部署（转调 scripts/deploy.ps1）
├── quick-start.bat                        # ⭐ Windows 双击：零基础一键引导（检查环境 → 引导安装 → 部署）
├── docker-compose.yml                     # ⭐ 4 容器编排（mysql / redis / backend / frontend）
├── .env.example                           # 环境变量模板
├── .dockerignore
├── LICENSE                                # MIT 许可证
├── .gitattributes                         # 行尾策略（仓库统一存 LF，.bat / .ps1 例外）
├── .gitignore
└── README.md                              # 本文件
```

> **`docker-compose.yml` 和 `.dockerignore` 为什么留在根目录？**
> 不是偷懒 —— compose 以**自己所在目录**为项目目录，`.env` 的查找位置和各服务的相对路径
> （`context: .`、`./demo-data/sql`、`./claw-pet-web`）全都跟着它走。挪进子目录会让这些路径全错，
> 除非每次调用都额外传 `--project-directory`，手动敲 `docker compose up -d` 的人一定会踩坑。
>
> 数据库脚本已经从 `claw-pet-server/sql/` 挪到了顶层的 `demo-data/sql/` ——
> 这样「测试数据」和「代码」彻底分开：不想要测试数据，删掉 `demo-data/` 一个目录即可。

---

## 🏗️ 技术架构

### 系统架构图

```
                    ┌─────────────────────────────────────────┐
                    │              浏览器 / 客户端               │
                    └──────────┬─────────────────┬────────────┘
                          HTTP │ REST API    WS  │ WebSocket
                               ▼                 ▼
┌──────────────────────────────────────────────────────────────────┐
│                 Spring Boot 3.2 后端（容器内 :8081）              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Security │  │ Controller│  │ Service  │  │  WebSocket Push  │  │
│  │  Filter  │→ │  Layer   │→ │  Layer   │  │  (Notification)  │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘  │
│                      │                                            │
│  ┌──────────────────┴───────────────────┐                        │
│  │       MyBatis-Plus ORM (零 XML)       │                        │
│  └──────────────────┬───────────────────┘                        │
└─────────────────────┼────────────────────────────────────────────┘
                      │ JDBC
        ┌─────────────┴─────────────┐
        ▼                           ▼
┌───────────────┐          ┌────────────────┐
│   MySQL 8.0   │          │   Redis 7.4    │
│  (15 tables)  │          │ (验证码/缓存)   │
└───────────────┘          └────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                 Vue 3 前端（对外 :3000 / 本地开发 :3001）         │
│   ┌────────┐  ┌──────────┐  ┌────────┐  ┌────────────────────┐  │
│   │ Router │  │  Stores  │  │  Axios │  │  WebSocket Client  │  │
│   │ (Hash) │  │ (Pinia)  │  │ 拦截器  │  │   (自动重连)        │  │
│   └────────┘  └──────────┘  └────────┘  └────────────────────┘  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │      Element Plus + ECharts + 10 套预设主题设计系统       │   │
│   └─────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.0 | 基础框架 |
| Spring Security | 6.x | 安全认证 + CORS |
| Spring WebSocket | - | 实时通知推送 |
| MyBatis-Plus | 3.5.5 | ORM 框架（零 XML 配置） |
| MySQL Connector/J | 8.x | 数据库驱动 |
| JJWT | 0.12.3 | JWT Token 签发与验证 |
| Redis (Lettuce) | 7.4 | 验证码存储、缓存 |
| Hutool | 5.8.25 | 工具类库 |
| Lombok | - | 代码简化 |
| Maven | 3.6+ | 构建管理 |

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | 前端框架（Composition API） |
| Vite | 5.0 | 构建工具（HMR 热更新） |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.2 | 路由管理（Hash 模式） |
| Element Plus | 2.4 | UI 组件库 |
| Axios | 1.6 | HTTP 请求封装 |
| ECharts | 6.1 | 统计图表可视化 |
| @element-plus/icons-vue | 2.3 | 图标库 |

---

## 🖥️ 功能总览

### 前台功能（普通用户）

| 模块 | 功能描述 |
|------|---------|
| **首页** | 收容所背景图、数据总览、精选宠物展示、联系方式 |
| **宠物列表** | 多维筛选（分类/状态/性别/健康）+ 关键字搜索 + 分页 |
| **宠物详情** | 多图轮播、基本信息、健康/疫苗状况、收藏、评论区 |
| **领养申请** | 提交申请（电话/地址/理由）、进度追踪、取消申请 |
| **收藏** | 收藏/取消收藏、收藏列表 |
| **消息中心** | 系统通知 + 我的评论 + 被回复 三合一 |
| **通知** | 评论回复、审核结果、未读红点、全部已读、删除 |
| **个人中心** | 头像上传、资料修改、密保管理、修改密码 |
| **忘记密码** | 3 步流程：用户名 → 密保问题 → 新密码 |

### 后台功能（管理员）

| 模块 | 功能描述 |
|------|---------|
| **仪表盘** | 4 个统计卡片 + 领养趋势图 + 分类分布图 + 审核状态图 |
| **宠物管理** | CRUD、多图上传、分类/状态编辑、分页搜索 |
| **分类管理** | 增删改、拖拽排序、启用/禁用 |
| **领养审核** | 申请列表、详情查看、通过/驳回（含原因） |
| **领养记录** | 已完成记录管理、回访记录增删 |
| **用户管理** | 列表、搜索、删除 |
| **收容所设置** | 名称/地址/电话/邮箱/背景图编辑 |
| **系统管理** | 重置数据、加载演示数据、清理孤儿文件（均需密码确认） |

---

## 🧠 业务设计

### 宠物状态机

```
                    ┌─────────────┐
                    │  available  │ ← 可领养
                    └──────┬──────┘
                           │ 用户提交申请
                           ▼
                    ┌─────────────┐
        ┌──────────►│  adopting   │ ← 领养中（锁定，他人不可申请）
        │           └──────┬──────┘
        │                  │
        │           ┌──────┴──────┐
        │           │             │
        │      审核拒绝      审核通过
        │           │             │
        │           ▼             ▼
        │     ┌──────────┐  ┌─────────┐
        └─────│ (恢复)   │  │ adopted │ ← 已领养
              └──────────┘  └─────────┘

管理员可随时操作：available ⇄ offline（下架）
```

一只宠物被申请后自动锁定为 `adopting`，其他用户无法同时申请；审核拒绝后自动恢复为 `available`。

### 核心业务流程

```
[游客]
  │
  ├──► 注册 ──► [普通用户]
  │                 │
  │                 ├──► 浏览宠物 ──► 收藏 / 评论
  │                 │
  │                 ├──► 提交领养申请 ──────────────────┐
  │                 │                                   │
  │                 └──► 接收通知 ◄── 管理员审核 ◄──────┘
  │                                      │
  │                                 通过 / 拒绝
  │                                      │
  │                                 生成领养记录
  │                                      │
  │                                 定期回访跟进
  │
  └──► 管理员登录 ──► [后台管理]
                        │
                        ├──► 宠物 CRUD + 图片上传
                        ├──► 领养审核
                        ├──► 领养记录 + 回访管理
                        ├──► 用户管理
                        ├──► 分类管理（拖拽排序）
                        ├──► 收容所信息编辑
                        └──► 数据统计看板
```

### 关键业务规则

- **申请锁定**：一只宠物被申请后状态变为 `adopting`，其他用户无法再申请
- **状态回退**：审核拒绝后宠物自动恢复为 `available`
- **评论嵌套**：通过 `parent_id` + `reply_to_user_id` 双字段实现评论树，回复时自动通知被回复人
- **密码重置**：通过密保问题验证，无需管理员介入
- **文件清理**：每日凌晨扫描磁盘，删除未被数据库引用的孤儿文件（1 天宽限期）
- **禁用分类过滤**：分类设为 `disabled` 时自动从前台导航栏、筛选栏、宠物列表中隐藏

---

<details>
<summary><b>🗃️ 数据模型（15 张表 · 字段说明）— 点击展开</b></summary>

数据库 `claw_pet`（MySQL 8.0，utf8mb4），共 **15 张表**：

```
users ────── user_security_question    (密保问题，一对多)
  │
  ├── pet_favorite         (收藏)
  ├── pet_comment          (评论，parent_id 嵌套回复)
  ├── adopt_application    (领养申请)
  │     └── adopt_record   (领养记录，一对一)
  │           └── followup_record  (回访记录，一对多)
  └── notification         (通知)

pet_info ──── pet_image     (宠物图片，一对多)
       └──── pet_category   (分类)

shelter_info     (收容所信息，单行配置)
adoption_guide   (领养须知)
pet_tip          (养宠小常识)
adoption_story   (领养故事)
```

### 核心表字段说明

| 表名 | 关键字段 | 说明 |
|------|---------|------|
| `users` | id, username, password(BCrypt), role, avatar, deleted | 用户表，支持软删除 |
| `user_security_question` | id, user_id, question, answer(BCrypt) | 密保问题，一对多 |
| `pet_info` | id, name, category_id, breed, status, view_count | 宠物信息，状态机驱动 |
| `pet_image` | id, pet_id, url, is_cover, sort | 宠物图片，多图 + 封面 |
| `pet_category` | id, name, icon, sort, status | 分类（支持禁用） |
| `adopt_application` | id, pet_id, user_id, status, reason, review_user_id | 领养申请 |
| `adopt_record` | id, application_id, pet_id, user_id, adopt_time | 领养记录 |
| `followup_record` | id, record_id, content, images, followup_time | 回访记录 |
| `notification` | id, user_id, type, title, content, related_id, is_read | 通知 |
| `pet_comment` | id, pet_id, user_id, content, parent_id, reply_to_user_id | 评论（嵌套） |
| `pet_favorite` | id, user_id, pet_id (唯一索引) | 收藏 |
| `shelter_info` | id, name, address, phone, email, image | 收容所信息 |
| `adoption_guide` | id, title, content | 领养须知 |
| `pet_tip` | id, title, content, sort | 养宠小常识 |
| `adoption_story` | id, user_id, title, content, status | 领养故事 |

---
</details>

<details>
<summary><b>🔌 API 接口清单（79 个 · 按权限分层）— 点击展开</b></summary>

后端共 **79 个 RESTful API**，按权限分三层：

### 🔓 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录（含验证码校验） |
| POST | `/api/auth/register` | 用户注册 |
| GET | `/api/auth/captcha` | 获取算术验证码图片 |
| GET | `/api/auth/forgot-password` | 获取密保问题列表 |
| POST | `/api/auth/verify-answer` | 验证密保答案 |
| POST | `/api/auth/reset-password` | 重置密码 |
| GET | `/api/pet/list` | 宠物列表（分类/状态/性别/健康筛选） |
| GET | `/api/pet/{id}` | 宠物详情 |
| GET | `/api/category/list` | 分类列表 |
| GET | `/api/stats/home` | 首页统计数据 |
| GET | `/api/shelter` | 收容所信息 |

### 🔐 需登录接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/upload/pet-image` | 上传宠物图片（三级校验） |
| POST | `/api/upload/avatar` | 上传用户头像 |
| POST | `/api/adopt/apply` | 提交领养申请 |
| GET | `/api/adopt/my` | 我的领养申请列表 |
| DELETE | `/api/adopt/{id}` | 取消申请 |
| GET | `/api/adopt/my-pets` | 我的已领养宠物 |
| POST | `/api/comment` | 发表评论 / 回复 |
| DELETE | `/api/comment/{id}` | 删除评论 |
| GET | `/api/comment/my` | 我的评论 |
| GET | `/api/comment/replied` | 被回复列表 |
| POST | `/api/favorite` | 收藏宠物 |
| DELETE | `/api/favorite/{id}` | 取消收藏 |
| GET | `/api/favorite/list` | 我的收藏列表 |
| GET | `/api/notification/list` | 通知列表（分页） |
| GET | `/api/notification/unread-count` | 未读通知数 |
| PUT | `/api/notification/read/{id}` | 标记已读 |
| PUT | `/api/notification/read-all` | 全部标记已读 |
| DELETE | `/api/notification/{id}` | 删除通知 |
| PUT | `/api/profile` | 更新个人资料 |
| POST | `/api/profile/delete` | 注销账号 |
| POST | `/api/auth/change-password` | 修改密码 |
| POST | `/api/auth/verify-password` | 验证当前密码 |
| POST | `/api/auth/security-question` | 添加密保问题 |
| DELETE | `/api/auth/security-question/{id}` | 删除密保问题 |

### 🔑 管理员接口（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/pet` | 新增宠物 |
| PUT | `/api/pet` | 修改宠物信息 |
| DELETE | `/api/pet/{id}` | 删除宠物 |
| POST | `/api/category` | 新增分类 |
| PUT | `/api/category/{id}` | 修改分类 |
| DELETE | `/api/category/{id}` | 删除分类 |
| GET | `/api/adopt/list` | 领养申请列表 |
| PUT | `/api/adopt/review` | 审核申请（通过/拒绝 + 原因） |
| GET | `/api/record/list` | 领养记录列表 |
| GET | `/api/user/list` | 用户列表 |
| DELETE | `/api/user/{id}` | 删除用户 |
| GET | `/api/admin/stats/dashboard` | 仪表盘统计 |
| GET | `/api/admin/stats/trend` | 领养趋势（12 个月） |
| PUT | `/api/shelter` | 更新收容所信息 |
| POST | `/api/admin/cleanup/reset-all` | 重置全部数据 |
| POST | `/api/admin/cleanup/reload-demo` | 加载演示数据 |
| POST | `/api/admin/cleanup/orphans` | 清理孤儿文件 |

> **鉴权方式**：请求头 `Authorization: Bearer <JWT_TOKEN>`，Token 有效期 24 小时。

---
</details>

## ✨ 技术亮点

### 1. 安全体系

- **JWT 无状态认证**：Token 鉴权 + Spring Security 角色守卫，前后端分离标准方案
- **三级 API 权限**：`@PreAuthorize` 注解实现公开 / 需登录 / 管理员三层隔离
- **三级文件上传校验**：扩展名白名单 → MIME Type 检测 → Magic Number 文件头验证，拦截 100% 非法文件；前端 Canvas resize 压缩至 1200px
- **算术验证码**：Java2D 纯后端绘制，Redis 一次性校验，防自动化攻击与凭证填充
- **BCrypt 加密**：密码与密保答案均采用不可逆加密存储

### 2. 实时通信

- **WebSocket 双向推送**：通知（评论回复、审核结果）即时送达，无需刷新
- **自动重连**：前端 `useWebSocket` 组合式函数封装断线重连逻辑，Token 过期主动清理

### 3. 业务工程化

- **宠物状态机**：`available → adopting → adopted`，申请冲突自动锁定，拒绝后自动回退
- **嵌套评论系统**：`parent_id` + `reply_to_user_id` 双字段实现评论树，回复时通知被回复人
- **多密保找回密码**：支持设置多个密保问题，找回时选一答对即可，无需管理员介入
- **定时清理孤儿文件**：每日凌晨扫描磁盘，删除未被数据库引用的图片（1 天宽限期防误删）
- **逻辑删除**：用户与评论支持软删除（`deleted` 字段），数据可恢复

### 4. 数据可视化

- **领养趋势折线图**：12 个月领养数量趋势
- **分类分布环形图**：各宠物分类占比
- **审核状态饼图**：申请通过率、拒绝率分布
- **统计卡片组**：宠物总数、用户数、待审核数、领养中数、已领养数

### 5. 前端工程化

- **Axios 统一拦截器**：Token 自动注入、401 自动跳转登录、全局错误处理
- **路由守卫**：登录态校验 + 角色权限校验（前台/后台路由隔离）
- **多主题系统**：暖橙、焦糖、樱花粉、莫兰迪粉、自然绿、墨绿、海洋蓝、商务蓝、简约白、薰衣草紫，共 10 套 CSS 变量预设主题，一键切换
- **组合式 API**：`useWebSocket` 等可复用逻辑封装
- **毛玻璃设计系统**：`rgba(255,255,255,0.55)` + `backdrop-filter: blur(20px)` 统一玻璃风格

---

## 🚀 快速开始

### 环境要求

**只想把项目跑起来看效果** → 只装 Docker 就够了，跳到「方式 A」。

**要改代码开发** → 需要下面这一整套：

| 软件 | 版本 | 必选 |
|------|------|:----:|
| JDK | 17+ | ✅ |
| Maven | 3.6+ | ✅ |
| MySQL | 8.0+ | ✅ |
| Node.js | 18+ | ✅ |
| Redis | 7.x | ⬜（可选，不装则登录验证码不可用） |
| Docker | 24+ | ⬜（只有容器部署才需要） |

---

### 方式 A：容器部署（最省事，只要 Docker）

> 🪟 **Windows 新手直接双击根目录的 `quick-start.bat`** —— 脚本会自动检查 Docker /
> WSL2 / 引擎状态，没装的手把手引导安装，国内网络还会自动配置镜像加速，
> 部署完自动打开浏览器。下面手动命令适合已经熟悉命令行的用户。

整套环境（MySQL + Redis + 后端 + 前端）打包成 4 个容器，一条命令全起。
**本机不用装 JDK / Maven / Node / MySQL / Redis。**

```bash
# macOS / Linux
./scripts/deploy.sh

# Windows：双击 deploy.bat，或者
.\scripts\deploy.ps1
```

首次会自动生成 `.env`（数据库密码、Redis 密码、JWT 密钥都是随机的），
然后构建镜像 → 起容器 → 等前端就绪 → 打印访问地址。

| 场景 | 命令 |
|------|------|
| 启动 | `./scripts/deploy.sh` |
| 看状态 | `./scripts/deploy.sh ps` |
| 看日志 | `./scripts/deploy.sh logs backend` |
| 停止（数据保留） | `./scripts/deploy.sh down` |
| 清空数据重来 | `./scripts/deploy.sh clean` |

> ⏱ 首次构建要下 Maven / Node 依赖，大约 3~10 分钟；之后启动只要十几秒。
>
> 🗄 **数据库是自动初始化的** —— 容器首次启动时会自动执行 `demo-data/sql/` 里的建表脚本和演示数据，
> 不需要你手动 import 任何东西。

---

### 方式 B：本地一键启动（开发用）

#### Windows

1. 双击 **`start.bat`**
2. 首次运行会自动根据 `.env.example` 生成 `.env`，然后**停下来提示你填 MySQL 密码**
3. 打开 `.env`，把 `DB_PASSWORD=` 后面填上你的 MySQL 密码，保存
4. 再双击一次 `start.bat` —— 剩下的交给它

#### macOS / Linux

```bash
chmod +x scripts/start.sh
./scripts/start.sh
```

脚本会自动完成：读 `.env` → 起 MySQL/Redis → 首次运行还原演示图片 → `npm install`
→ 起后端（等它真正能响应才继续）→ 起前端 → 打开浏览器。

停止：`./scripts/stop.sh`（macOS/Linux）；Windows 直接关掉弹出的两个命令行窗口。

> ⚠️ **数据库还是空的？** 一键脚本不负责建库导数据，第一次仍然需要跑一遍下面的「Step 1 初始化数据库」。

---

<details>
<summary><b>方式 C：手动启动（不用脚本，逐条命令）— 点击展开</b></summary>

### 方式 C：手动启动

#### Step 1：初始化数据库

**做法一：Navicat / DataGrip（推荐）**

1. 新建数据库 `claw_pet`，字符集 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`
2. 执行 `demo-data/sql/01-init.sql`（建 15 张表 + 默认管理员/分类/收容所）
3. 执行 `demo-data/sql/02-demo-pets.sql`（导入 15 只宠物等演示数据）
4. **把演示图片放进去**（这一步别漏，漏了宠物列表全是裂图）：

```powershell
# Windows
Copy-Item demo-data\images\pet\*.jpg claw-pet-server\upload\pet\
```
```bash
# macOS / Linux
cp demo-data/images/pet/*.jpg claw-pet-server/upload/pet/
```

> 演示图片为什么不直接放在 `upload/pet/`？因为那里被 `.gitignore` 忽略了
> （用户上传的图片不该进仓库），所以单独放在 `demo-data/images/pet/` 随仓库分发。
> 里面放什么、怎么重新生成，见 [`demo-data/README.md`](demo-data/README.md)。

**做法二：命令行**

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS claw_pet CHARACTER SET utf8mb4;"
mysql -u root -p claw_pet < demo-data/sql/01-init.sql
mysql -u root -p claw_pet < demo-data/sql/02-demo-pets.sql
cp demo-data/images/pet/*.jpg claw-pet-server/upload/pet/
```

---

#### Step 2：配置并启动后端

1. 用 IDEA 打开 `claw-pet-server` 目录，等待 Maven 依赖下载完成
2. 配置数据库密码。**两种方式任选一种**：

   方式一（推荐）：项目根目录建 `.env`，内容写

   ```ini
   DB_PASSWORD=你的MySQL密码
   ```

   方式二：改 `src/main/resources/application.yml`

   ```yaml
   spring:
     datasource:
       password: ${DB_PASSWORD:}
   ```

   或直接设环境变量 `DB_PASSWORD`。

3. 运行 `ClawPetApplication.java` 主类
4. 服务地址：**http://localhost:8081**

> 💡 **不配也能起**：`DB_PASSWORD` 留空时按「本机 root 无密码」去连（有密码就填上）；
> `JWT_SECRET` 留空时启动会**随机生成**一个临时密钥 —— 项目照常跑，代码里也没有任何
> 写死的默认密钥可供伪造，只是重启后需要重新登录。生产环境建议显式配置 `JWT_SECRET`。

> 💡 **Redis 可选**：未启动 Redis 时后端仍可运行，仅验证码功能降级。

---

#### Step 3：启动前端

```bash
cd claw-pet-web
npm install
npm run dev
```

服务地址：**http://localhost:3001**

---

#### Step 4：体验系统

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 👑 管理员 | `admin` | `admin123` | 全部后台管理权限 |
| 👤 普通用户 | `user` | `admin123` | 浏览、领养、收藏、评论 |

> 也可在登录页注册新账号，注册后默认为普通用户角色。

> 🔐 这两个是**演示账号**，正式部署请登录后立刻改密码（个人中心 → 修改密码）。

---

#### SQL 文件说明

脚本和图片都在顶层的 `demo-data/` 里，和后端代码分开存放：

| 文件 | 用途 | 使用场景 |
|------|------|---------|
| `demo-data/sql/01-init.sql` | 建库 + 建表（15 表）+ 默认数据（管理员/收容所/须知/常识） | **首次部署**，必选 |
| `demo-data/sql/02-demo-pets.sql` | 15 只演示宠物 + 图片记录（5 分类全部覆盖） | **首次部署**，推荐 |
| `demo-data/images/pet/*.jpg` | 16 张演示图（15 张宠物封面 + 1 张首页收容所大图） | 拷到 `claw-pet-server/upload/pet/`，见上面的 Step 1 |

> 容器部署不用管最后一步：`docker-compose.yml` 会把 `demo-data/images/pet/` 挂进后端镜像，
> 后端首次启动自动拷进上传目录。手动部署才需要自己拷。
> 想换图或重新生成，看 `demo-data/README.md`。

> 管理后台提供"重置数据"和"加载演示数据"按钮（需密码确认），可直接在 UI 中重置业务数据并重新导入演示宠物
> —— "加载演示数据"就是让你选上面这个 `02-demo-pets.sql`。
>
> 同一页的"清理孤儿文件"按钮会**先跑一次预演**，把将要删除的清单列给你确认，确认后才真删，
> 并且会显示能释放多少空间。详见下面的常见问题。


---

</details>

## ⚙️ 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8081

spring:
  datasource:
    # 三个都可用环境变量覆盖，方便换机器、换端口部署
    url: ${DB_URL:jdbc:mysql://localhost:3306/claw_pet?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
  servlet:
    multipart:
      max-file-size: 30MB
      max-request-size: 30MB
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}     # 没设密码就留空
      database: 0

jwt:
  # 留空则启动时随机生成临时密钥（代码里没有任何写死的默认值）；
  # 生产环境建议显式配置，否则每次重启都会把在线用户踢下线
  secret: ${JWT_SECRET:}
  expiration: 86400000              # Token 24 小时有效

upload:
  # 相对路径的基准是「后端模块根目录」，与启动时的工作目录无关
  path: ./upload

cleanup:
  cron: "0 0 0 * * ?"              # 每天凌晨 0 点执行
  orphan-grace-period-days: 1      # 孤儿文件宽限期
  enabled: true
```

### 可配置的环境变量

`.env.example` 里就是这份清单，复制成 `.env` 即可。

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_URL` | 本机 3306 / `claw_pet` | 数据库连接串 |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | 空 | 数据库密码 |
| `JWT_SECRET` | 空（启动时随机生成） | 签名密钥，**生产建议配置**固定值（≥32 字节） |
| `REDIS_PASSWORD` | 空 | Redis 密码，没设就留空 |

### 前端代理 (vite.config.js)

```javascript
server: {
  port: 3001,
  proxy: {
    '/api': { target: 'http://localhost:8081', changeOrigin: true },
    '/ws':  { target: 'ws://localhost:8081', ws: true },
    // 图片走这里，别漏。bypass 是为了让前端自己的 /profile 个人中心路由不被代理走
    '/profile': { target: 'http://localhost:8081', changeOrigin: true,
      bypass: (req) => { if (req.url === '/profile') return '/index.html' } }
  }
}
```

> 生产部署时，Nginx 也必须把 `/profile` 转发到后端，否则所有图片 404。

---

## 🎨 设计规范

采用 **毛玻璃（Glassmorphism）** 设计系统，支持 10 套预设主题：

| 设计元素 | 值 | 应用场景 |
|---------|-----|---------|
| 卡片背景 | `rgba(255,255,255,0.55)` | 毛玻璃卡片 |
| 页面背景 | 渐变 `bg-page → brand-primary-light → bg-page` | 前后台统一背景 |
| 导航背景 | `rgba(255,255,255,0.92)` + `blur(20px)` | 毛玻璃导航栏 |
| 按钮圆角 | 12-24px | 圆润按钮 |
| 卡片圆角 | 12-20px | 卡片阴影 |

### 预设主题

| 主题名称 | 主色 | 浅色 | 页面底色 |
|---------|------|------|---------|
| 暖橙 | #e8956a | #fef0e8 | #fdf4ee |
| 焦糖 | #a0623a | #fdf6f0 | #fdf8f3 |
| 樱花粉 | #e88aa0 | #fef0f4 | #fef5f8 |
| 莫兰迪粉 | #c898aa | #fdf6f0 | #faf6f3 |
| 自然绿 | #5fa87f | #e8f5ee | #f0f7f3 |
| 墨绿 | #3a7a5c | #e8f0ec | #f0f5f2 |
| 海洋蓝 | #5b9bd5 | #e8f0fa | #eef4fa |
| 商务蓝 | #2c5282 | #eef4fa | #f5f7fa |
| 简约白 | #2a2a2a | #f5f5f5 | #fafafa |
| 薰衣草紫 | #9665d2 | #ede4f7 | #f3edf8 |

> 定义在 `claw-pet-web/src/utils/themes.js`，改配色只改这一个文件。
> 组件里**不要写死颜色**，一律用 `var(--brand-primary)` 这类变量，否则换主题不生效。

---

## ❓ 常见问题

<details>
<summary><b>Q: clone 下来后怎么最快跑起来？</b></summary>

**装了 Docker 的话（最省事）：**

```bash
./scripts/deploy.sh
```

完事。数据库会自动初始化，不用手动 import 任何东西。
Windows 双击 `deploy.bat`。

**没装 Docker：**

Windows 双击 `start.bat`，macOS/Linux 跑 `./scripts/start.sh`。
第一次它会让你填 MySQL 密码（在生成的 `.env` 里填），填完再跑一次就行。

但这一条要手动做 —— **建库导数据**：新建 `claw_pet` 库 → 执行 `demo-data/sql/01-init.sql`
→ 执行 `demo-data/sql/02-demo-pets.sql` → 把 `demo-data/images/pet/*.jpg` 拷到
`claw-pet-server/upload/pet/`。（第二次以后 `start.*` 会自己还原图片）
</details>

<details>
<summary><b>Q: 用 Docker 部署，端口被占了 / 想换端口怎么办？</b></summary>

改项目根目录 `.env` 里的两个值，然后 `./scripts/deploy.sh restart`：

```ini
WEB_PORT=3000        # 前端端口，改这个就行
BACKEND_PORT=8081    # 后端端口，只是方便调接口，可以不管
```

如果连 `BACKEND_PORT` 都不想暴露，把 `docker-compose.yml` 里 backend 服务的
`ports:` 两行注释掉 —— 前端是通过容器内网 `http://backend:8081` 访问后端的，
不依赖这个对外映射。
</details>

<details>
<summary><b>Q: 数据存在哪？重建容器会丢吗？</b></summary>

用命名卷存的，`down` 不会丢，只有 `clean`（即 `docker compose down -v`）才会清：

| 卷名 | 存什么 |
|------|--------|
| `claw-pet-mysql-data` | 数据库全部内容 |
| `claw-pet-upload-data` | 用户上传的图片 |
| `claw-pet-redis-data` | Redis AOF |

想重置演示数据：`./scripts/deploy.sh clean` 然后重新 `./scripts/deploy.sh`
（数据库会重新执行一遍 `demo-data/sql/` 里的脚本，演示图片也会自动还原）。

> ⚠️ `clean` 不可逆：数据库里的申请、评论、用户上传的图片都会没。
</details>

<details>
<summary><b>Q: 打包上线后宠物图片全 404，但开发时正常？</b></summary>

**多半是 Nginx 少了 `/profile` 转发。**

开发时靠 `vite.config.js` 的 dev server 代理，上线后这个角色由 Nginx 接手。
前端要转发 **三条**路径，少一条就是"开发正常、上线 404"：

| 路径 | 用途 |
|------|------|
| `/api` | 后端接口 |
| `/ws` | WebSocket（必须带 `Upgrade` / `Connection` 头） |
| `/profile` | 上传的图片（宠物封面、头像） |

本仓库的 `claw-pet-web/nginx.conf` 已经配好了，可以直接参考或复制。

另外别忘了 `client_max_body_size` —— Nginx 默认只有 1MB，而后端允许上传 30MB，
不放开的话传图会返回 **413**，而且**后端日志里什么都看不到**（请求根本没到后端）。
</details>

<details>
<summary><b>Q: 登录时提示"验证码不能为空" / 验证码图片不显示？</b></summary>

验证码存在 Redis 里。Redis 没启动就会出现这种情况。
`start.bat` / `scripts/start.sh` 会自动尝试拉起 Redis；容器部署的话 Redis 是必起的。

Redis 起不来也不影响浏览、领养、评论，只是登录验证码这一环不可用。
</details>

### 更多问题

<details>
<summary><b>Q: 启动日志里有 JWT 密钥告警，要紧吗？</b></summary>

```
⚠️  未配置环境变量 JWT_SECRET，本次启动已随机生成临时签名密钥。
⚠️  该密钥重启即失效（需重新登录）；生产环境请配置 JWT_SECRET（≥32 字节）。
```

**本地开发不用管** —— 密钥是随机生成的，只存在于这次进程的内存里，别人伪造不了。

**部署到公网时建议配一个固定的**，否则每次重启都会把所有在线用户踢下线。

改法：在 `.env` 里设置 `JWT_SECRET`（长度 ≥ 32 字节）。生成办法见 `.env.example` 末尾。
设好之后再启动，这条告警就不会出现了（可以用这个来判断配置有没有真的生效）。
</details>

<details>
<summary><b>Q: 启动后端报数据库连接失败？</b></summary>

三种改法任选一种：

1. 项目根目录的 `.env` 里填 `DB_PASSWORD=你的密码`（推荐，`start.bat` / `scripts/start.sh` 会自动读）
2. 设环境变量 `DB_PASSWORD`
3. 直接改 `application.yml` 的 `spring.datasource.password`

数据库不在本机 / 端口不是 3306 的，连 `DB_URL` 一起改。
</details>

<details>
<summary><b>Q: 宠物列表全是裂图 / 头像显示不出来？</b></summary>

图片不在数据库里，在磁盘上。两个可能：

1. **演示数据没放图**：把 `demo-data/images/pet/*.jpg` 拷到 `claw-pet-server/upload/pet/`
   （`start.bat` / `scripts/start.sh` 首次运行会自动做这件事，手动导数据的话要自己拷）
2. **上传目录定位错了**：后端启动日志里有一行 `静态资源目录: xxx`，确认它指向 `claw-pet-server/upload`
   而不是仓库根目录的 `upload`。现在相对路径是锚定后端模块根的，正常不会错；如果错了就把它改成绝对路径。
</details>

<details>
<summary><b>Q: 前端页面白屏或接口 404？</b></summary>

1. 确认后端已启动成功（控制台无报错）
2. 确认前端 `npm run dev` 正常运行
3. 前端的 `/api` 请求会自动代理到 8081 端口，无需手动修改

**控制台报 `ECONNREFUSED 127.0.0.1:8081`、页面接口全 500 —— 那是后端没起来，不是前端的问题。**
去后端那个命令行窗口看报错。
</details>

<details>
<summary><b>Q: 上传图片失败？</b></summary>

系统支持 JPG / JPEG / PNG / GIF / WebP / BMP / HEIC 格式，最大 30MB，前端自动压缩至 1200px。

提示"文件实际是 XX 格式，但扩展名是 YY"是因为后端做了魔数校验（读文件头），
把文件另存为它真实的格式再传即可。
</details>

<details>
<summary><b>Q: 不启动 Redis 可以吗？</b></summary>

可以。Redis 是可选的，不启动不影响核心功能，仅验证码校验功能不可用（前端会降级为不校验）。
</details>

<details>
<summary><b>Q: 后台"清理孤儿文件"会误删东西吗？</b></summary>

它是按"磁盘上有、数据库里没人引用"来判断的，所以**风险在误删，不在漏删**。现在已经上了三层保护：

1. **预演**：点按钮后先跑一次 `?dryRun=true`，把要删的文件清单弹出来给你看，确认了才真删
2. **并集收集引用**：宠物图、用户头像、**收容所首页大图**、回访记录里的图片，四处都会查。
   （之前漏了收容所那处，首页大图会被当孤儿删掉 —— 已修）
3. **安全阀**：如果一条被引用的图片都查不到，直接中止不删。这是防止数据库连错之后把整个上传目录清空

另外它只处理"最后修改超过 1 天"的文件，不会误伤刚上传、还没写库的图片。

> 新增了存图片的字段时，记得同步改 `FileCleanupTask.collectReferencedUrls()`，
> 否则那个字段引用的图片会被当成孤儿。
</details>

<details>
<summary><b>Q: 演示数据乱了如何重置？</b></summary>

管理员登录后台 → 仪表盘 → 系统管理 → "重置数据"（清空全部业务数据 + 宠物）→ 再点"加载演示数据"，会弹出文件选择框，选 `demo-data/sql/02-demo-pets.sql` 即可。所有操作需输入密码确认。

> 导入是白名单的：只执行文件里对 `pet_category` / `pet_info` / `pet_image` 的 INSERT 和对 `shelter_info.image` 的 UPDATE，其它语句一律忽略。
> 所以直接把这个文件整个丢进去就行，不用事先裁剪。
>
> 重置只会重建数据库记录，不会动 `upload/pet/` 里的图片文件 —— 演示图片是 `demo-*.jpg`，
> 不受影响，重置后照样能显示。
</details>

<details>
<summary><b>Q: 部署时拉镜像报 <code>failed to fetch oauth token ... Bad Gateway</code> / 一直卡住？</b></summary>

国内网络访问 Docker Hub 不稳定导致的。给 Docker 配一个镜像加速即可：

**Windows（Docker Desktop）**：设置 → Docker Engine，在 JSON 里加一段后点 Apply & Restart：

```json
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://dockerproxy.net"
  ]
}
```

**Linux**：编辑 `/etc/docker/daemon.json` 加入同样的 `registry-mirrors` 字段，然后
`sudo systemctl restart docker`。

> Windows 用户可以直接双击 `quick-start.bat`，脚本检测到连不上 Docker Hub 会自动帮你配好。
</details>

---

## 📝 更新日志

### v2.1.2 (2026-10-06)

**安全加固（面向「公开作品集」场景的收尾）：**

- 🔒 **`jwt.secret` 不再有任何写死的默认值**：以前不配环境变量时会退回到一个写死在源码里的
  密钥 —— 那个值随仓库一起公开，任何人都能拿它签一个管理员 Token。现在不配时改为
  **启动随机生成**（`Jwts.SIG.HS256.key().build()`）：既保住「clone 下来不配也能跑」这个便利，
  又让外人无从伪造。代价是重启后需重新登录（本地开发无所谓）。
  连带同步了 `.env.example`、`claw-pet-server/README.md` 里的相关说明
- 🔒 **`/api/upload/**` 由 `permitAll` 收紧为「必须登录」**：原先是匿名可传、一次 30MB、不限次数，
  别人可以拿你的服务器当免费图床或塞满硬盘。**注意这里传不了 webshell** ——
  文件名是 `UUID.randomUUID()`（构造不出路径穿越）、扩展名/MIME/魔数三层校验、图片只当图片读，
  所以是「滥用」而非「入侵」。前端 4 个上传点本来就都带着 `Authorization` 头，因此没动前端
- 🔒 **登录框不再预填 `admin/admin123`**：以前任何人打开站点点一下「登录」就是管理员。
  演示账号改为只写在 README 里
- 📝 顺带澄清一个被高估的点：CORS 配的是 `allowedOrigins("*")`，看着吓人，但本项目的凭证是
  前端手动加到请求头上的、**不会随跨站请求自动携带**，所以实际危害很小，未做改动

### v2.1.1 (2026-10-06)

**仓库根目录整理：**

- 🗂 **启动 / 部署脚本收进 `scripts/`**：根目录原来是 7 个脚本平铺，没法一眼看出哪两个是一对。
  现在按功能分族 —— 真正干活的 5 个进 `scripts/`，根目录只留 `quick-start.bat` / `start.bat` / `deploy.bat` 三个双击入口。
  根目录条目从 17 项降到 13 项（9 个文件 + 4 个目录），每一项要么是入口、要么是配置、要么是文档
- 🔧 **脚本内部改成「向上退一级」定位仓库根目录**：`$ROOT` 从「脚本所在目录」改为「脚本所在目录的上一级」，
  所以 `.env`、`docker-compose.yml`、`demo-data/` 这些相对路径仍然锚定根目录，调用方式不受影响
- 📝 **`docker-compose.yml` 明确留在根目录**：compose 以自己所在目录为项目目录，`.env` 查找位置和各服务
  的相对路径都跟着它走，挪进子目录会让这些路径全错。README 里补了说明，免得后人再搬一次
- 🔧 **`.dockerignore` 排除 `scripts/`**：后端镜像的构建上下文是仓库根，那些脚本镜像里用不到
  （后端容器有自己的 `docker-entrypoint.sh`），没必要每次都塞进构建上下文
- 📝 同步修正 `.env.example`、`claw-pet-server/README.md`、`demo-data/README.md`、
  `demo-data/sql/02-demo-pets.sql`、`docker-compose.yml` 注释里全部脚本路径引用
- 📝 新增 `scripts/README.txt`，把两个族的脚本、用法、以及「行尾/BOM 不能乱动」的注意事项写在一处
- 📝 **修正 README 结构树里对 `.gitattributes` 的描述**：原来写「仓库存 CRLF、本地 LF」，
  正好说反了 —— 实际策略是 `* text=auto eol=lf`，即**仓库里统一存 LF**，检出时按平台转换
  （`.bat` / `.ps1` 是例外，强制 CRLF）

### 更早版本（v2.1.0 及以前） (2026-10-06)

<details>
<summary><b>点击展开 v2.1.0 / v2.0.0 / v1.0.0 的详细记录</b></summary>


**部署体验（主要目的：让 clone 下来的人能直接跑起来）：**
- 🔧 **补上一键启动脚本**：`start.bat` / `start.ps1`（Windows）、`start.sh` + `stop.sh`（macOS/Linux）。
  自动读 `.env`、起 MySQL/Redis、还原演示图片、装依赖、起前后端、开浏览器
- 🔧 **补上 `.env.example`** 环境变量模板
- 🔧 **`JWT_SECRET` 补默认值**：以前不配就直接启动失败，现在开箱能跑（同时加了启动告警，提醒上线要换）
- 🔧 **`upload.path` 不再依赖启动时的工作目录**：新增 `UploadPathResolver`，
  IDE 跑和 jar 跑解析出的目录一致，修掉"上传成功但图片打不开"
- 🔧 **演示图片随仓库分发**：图片原来被 gitignore 挡在仓库外，新人导完演示数据全是裂图。
  现在放在 `demo-data/images/pet/` 并首次启动自动还原
- 🔧 **`demo-pets.sql` 修正**：图片路径补上前导斜杠，且换成真实存在的图片
- 🔧 **演示图从 PNG 转 JPEG**：8.5MB → 0.8MB，肉眼无差别
- 📝 README 全面对齐实现（表数量 12→15、配置片段、代理说明、常见问题）

**结构整理与容器化：**
- 🗂 **测试数据集中到顶层 `demo-data/`**：SQL 脚本、演示原图、成品图、生成工具从后端目录里搬出来，
  「代码」和「测试数据」彻底分开。不想要演示数据，删掉这一个目录即可。详见 [`demo-data/README.md`](demo-data/README.md)
- 🔧 **演示图改为两步压缩**：原图（q92，留档）→ 演示图（q82 + 最长边 1200px）。
  原图 11.27 MB 全量入库 → 合计压到 3.0 MB。附带确认了那批 PNG 的 alpha 通道全是 255（等于白占体积），
  转 JPEG 零损失
- 🐳 **全面容器化**：新增 `docker-compose.yml`（MySQL + Redis + 后端 + 前端 4 个服务）、
  两个 `Dockerfile`、前端 `nginx.conf`、`.dockerignore`。一条 `docker compose up -d` 起全套，
  数据库和演示数据自动初始化，不用本机装 MySQL/Redis/JDK/Node
- 🚀 **新增部署脚本**：`deploy.sh` / `deploy.ps1` / `deploy.bat`，放在仓库根目录。
  子命令 `up` / `down` / `restart` / `ps` / `logs` / `clean` / `rebuild` / `pull`，
  首次运行自动生成随机数据库密码与 `JWT_SECRET`
- 📛 **前端目录改名**：`claw-pet-vue` → `claw-pet-web`，与后端 `claw-pet-server` 命名对称
- 🔐 **删掉文档里泄露的真实数据库密码**（`claw-pet-server/README.md` 里那行明文）
- 🐛 **修掉孤儿图片清理会误删首页大图的问题**：清理任务只保护宠物图和用户头像，
  漏了收容所图片，会把首页那张大图当"孤儿"删掉。改成并集收集全部 4 处引用，
  另外加了安全阀（一条引用都查不到就中止，防止连错库清空上传目录）和预演模式（`?dryRun=true` 只看清单不真删）
- 📝 后端配置改为全部可被环境变量覆盖（端口、Redis 地址），容器里才能连对服务

### v2.0.0 (2026-07-01)

**新增功能：**
- ✨ 通知系统：WebSocket 实时推送 + 通知列表 + 未读红点提醒
- ✨ 评论增强：嵌套回复 + @用户 + 锚点定位 + 消息中心三合一
- ✨ 多密保问题：设置多个 + 注册可选 + 密码重置 + 修改密码验证
- ✨ 算术验证码：Java2D 后端绘制 + Redis 一次性校验 + 弹窗模式
- ✨ 忘记密码流程：3 步找回（用户名 → 密保 → 新密码）
- ✨ ECharts 统计看板：领养趋势图 + 分类分布图 + 审核状态图
- ✨ 多主题系统（6 套预设 CSS 变量主题）
- ✨ 前端图片压缩 + 后端三级文件校验
- ✨ 管理后台 UI 重构（玻璃风格 + 动画进场）
- ✨ 定时清理孤儿文件 + 演示数据加载

**优化改进：**
- ♻️ 导航栏改为分类子菜单（el-sub-menu）
- ♻️ MyBatis-Plus 零 XML 配置
- ♻️ README 全面重写

### v1.0.0 (2026-06-28)

- ✨ 完整的宠物领养 CRUD
- ✨ Spring Boot 3 + Vue 3 前后端分离架构
- ✨ JWT 认证 + 角色权限分离
- ✨ 三态文件上传安全校验
- ✨ 评论、收藏、领养申请全流程
- ✨ 管理员仪表盘数据统计
- ✨ Redis 缓存加速

---

</details>

## 📄 许可证

[MIT License](./LICENSE)

---

## 🙏 致谢

感谢以下开源项目为本项目提供基础：

- [Spring Boot](https://spring.io/projects/spring-boot) — 后端框架
- [Vue.js](https://vuejs.org/) — 前端框架
- [MyBatis-Plus](https://baomidou.com/) — ORM 框架
- [Element Plus](https://element-plus.org/) — UI 组件库
- [ECharts](https://echarts.apache.org/) — 数据可视化

---

<div align="center">

**Claw Pet** — 用爱给流浪动物一个温暖的家 🐱🐶

Made with ❤️ by [贾敬涛](https://github.com/1asd-coke) · [Gitee](https://gitee.com/jia-jingtao1) / [GitHub](https://github.com/1asd-coke)

</div>
