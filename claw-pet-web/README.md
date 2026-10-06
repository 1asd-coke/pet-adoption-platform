# Claw Pet — 前端

宠物领养平台前端，基于 Vue 3 + Vite + Pinia + Vue Router + Element Plus。

## 技术栈

| 技术 | 说明 |
|------|------|
| Vue 3 | Composition API + `<script setup>` |
| Vite | 构建工具（开发服务器端口 3001） |
| Pinia | 状态管理 |
| Vue Router | 路由（Hash 模式） |
| Element Plus | UI 组件库 |
| Axios | HTTP 请求封装 |

## 项目结构

```
src/
├── main.js                      # 入口文件
├── App.vue
├── styles/
│   └── theme.css                # 全局设计系统主题
├── api/                         # API 接口封装
├── stores/
│   └── user.js                  # 用户状态管理
├── router/
│   └── index.js                 # 路由配置 + 登录守卫
├── layouts/
│   ├── FrontLayout.vue          # 前台布局（导航栏 + 页脚）
│   └── AdminLayout.vue          # 后台布局（侧边栏 + 顶栏）
├── components/
│   └── HeroBanner.vue           # 首页 Hero 文字组件
└── views/
    ├── auth/                    # 登录/注册（滑动切换）
    ├── home/                    # 首页
    ├── pet/                     # 宠物列表/详情
    ├── adopt/                   # 我的领养申请
    ├── favorite/                # 我的收藏
    ├── comment/                 # 我的评论
    ├── message/                 # 站内消息
    ├── notification/            # 通知
    ├── guide/                   # 领养须知
    ├── story/                   # 领养故事
    ├── tip/                     # 养护小贴士
    ├── profile/                 # 个人中心
    └── admin/                   # 管理后台（10 个页面）
```

## 路由

| 路径 | 布局 | 说明 |
|------|------|------|
| `/login` / `/register` | 无 | 登录/注册（滑动切换页面） |
| `/home` | FrontLayout | 首页 |
| `/pet` | FrontLayout | 宠物列表 |
| `/pet/detail/:id` | FrontLayout | 宠物详情 |
| `/adopt` | FrontLayout | 我的领养申请 |
| `/favorite` | FrontLayout | 我的收藏 |
| `/profile` | FrontLayout | 个人中心 |
| `/admin/dashboard` | AdminLayout | 仪表盘 |
| `/admin/pet-manage` | AdminLayout | 宠物管理 |
| `/admin/adopt-manage` | AdminLayout | 领养管理 |
| `/admin/record-manage` | AdminLayout | 领养记录 |
| `/admin/user-manage` | AdminLayout | 用户管理 |
| `/admin/shelter-edit` | AdminLayout | 收容所设置 |

## 设计主题

项目内置 **10 套主题**，不是写死一种配色：

暖橙 · 焦糖 · 樱花粉 · 莫兰迪粉 · 自然绿 · 墨绿 · 海洋蓝 · 商务蓝 · 简约白 · 薰衣草紫

- 定义在 `src/utils/themes.js`
- 切换时 `applyTheme()` 把配色写成 CSS 变量挂到 `document.documentElement`
- 用户选择存在 `localStorage['claw-pet-theme']`，默认「简约白」

> ⚠️ **写样式时不要写死颜色**，一律用 `var(--brand-primary)` / `var(--brand-primary-light)`
> 这类变量，否则换主题时那部分不会跟着变。

## 启动方式

```bash
cd claw-pet-web
npm install
npm run dev
```

开发服务器运行在 `http://localhost:3001`，API 请求自动代理到 `http://localhost:8081`。

## 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | admin123 |
