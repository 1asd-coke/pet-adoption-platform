# claw-pet-web - Vue 3 前端项目

Claw Pet 宠物领养系统的前端界面。

## 技术栈

Vue 3.4 + Vite 5 + Pinia + Vue Router 4 + Element Plus + ECharts

## 目录说明

| 目录 | 说明 |
|------|------|
| api/ | Axios 接口封装（15 个模块，对应后端 Controller）|
| components/ | 可复用组件（通知铃铛、主题切换、横幅）|
| composables/ | 组合式函数（WebSocket 连接管理）|
| layouts/ | 页面布局（前台 FrontLayout、后台 AdminLayout）|
| router/ | Vue Router 路由配置（Hash 模式 + 登录守卫）|
| stores/ | Pinia 状态管理（用户登录态）|
| styles/ | 全局样式（主题 CSS 变量、管理后台修正）|
| utils/ | 工具函数（图片压缩、主题配置）|
| views/ | 页面视图组件 |

## 启动方式

```bash
npm install
npm run dev
```

服务默认端口 3001，/api 和 /ws 请求自动代理到后端 8081 端口。
