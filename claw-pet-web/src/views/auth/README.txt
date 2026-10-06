# 认证页面

用户登录、注册及密码找回相关页面。

## 文件列表

| 文件 | 功能 |
|------|------|
| `AuthView.vue` | 登录 + 注册 — **同一个组件**，靠 `route.path`（`/login` 或 `/register`）区分，左右分栏滑动切换 |
| `ForgotPassword.vue` | 忘记密码 — 3 步找回：验证用户名 → 回答密保问题 → 设置新密码 |

## 改动时注意

- `AuthView.vue` 的左右分栏宽度是 `.sliding-panel` 45% / `.content-panel` 55%，
  滑动位移 `translateX(122.22%)` 里的 **122.22% = 55 ÷ 45**，改任一栏宽度都要重新算
- `.auth-card` 是**固定高度 + `overflow: hidden`**，往里加内容必须实测高度，否则会被裁掉
- 这两个页面**不套 FrontLayout**（路由里单独配的），所以页面级样式要自己写全
- 登录/注册成功后是路由跳转而不是刷新，注意 `route.path` 变化时 `isRegister` 要跟着变
