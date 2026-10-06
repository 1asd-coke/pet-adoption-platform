# ============================================================
# Claw Pet 前端镜像
#
# 两阶段：Node 构建出 dist/，再塞进 Nginx 提供静态文件 + 反向代理。
# 构建上下文是 claw-pet-web/ 自己（不需要仓库根），对应：
#   build:
#     context: ./claw-pet-web
# ============================================================

# ---------- 阶段一：构建 ----------
FROM node:22-alpine AS build

WORKDIR /app

# 先只拷依赖清单，依赖没变就能命中缓存
COPY package.json package-lock.json ./

# npm ci 严格按 lock 文件装，最可复现；
# 万一 lock 和 package.json 有出入就退回 npm install，别让构建挂死
RUN npm ci --no-audit --no-fund || npm install --no-audit --no-fund

COPY . .
RUN npm run build


# ---------- 阶段二：运行 ----------
FROM nginx:alpine

# 默认配置里那个 80 端口的 server 块会被我们的替换掉
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=6 \
  CMD wget -q --spider http://127.0.0.1/ || exit 1
