#!/usr/bin/env bash
# ============================================================
#  Claw Pet 一键启动（macOS / Linux）
#
#  用法（在仓库根目录执行）：
#     chmod +x scripts/start.sh     # 第一次需要给执行权限
#     ./scripts/start.sh
#
#  停掉服务：./scripts/stop.sh
#
#  和 Windows 的 start.ps1 做同一件事：
#     读 .env → 检查环境 → 检查 MySQL/Redis → 还原演示图片
#     → 安装前端依赖 → 起后端 → 起前端 → 打印地址
# ============================================================

set -uo pipefail

# 本脚本位于 scripts/ 下，往上退一级才是仓库根目录
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND="$ROOT/claw-pet-server"
FRONTEND="$ROOT/claw-pet-web"
RUN_DIR="$ROOT/.run"
LOG_DIR="$ROOT/.run/logs"

mkdir -p "$RUN_DIR" "$LOG_DIR"

c_info()  { printf '\033[36m%s\033[0m\n' "$*"; }
c_ok()    { printf '\033[32m%s\033[0m\n' "$*"; }
c_warn()  { printf '\033[33m警告: %s\033[0m\n' "$*"; }
c_err()   { printf '\033[31m错误: %s\033[0m\n' "$*"; }

port_busy() {
    # 优先用 nc / ss / lsof，退回到 bash 自带的 /dev/tcp
    if command -v nc >/dev/null 2>&1; then
        nc -z 127.0.0.1 "$1" >/dev/null 2>&1
    elif command -v ss >/dev/null 2>&1; then
        ss -ltn 2>/dev/null | grep -q ":$1 "
    elif command -v lsof >/dev/null 2>&1; then
        lsof -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
    else
        (exec 3<>"/dev/tcp/127.0.0.1/$1") >/dev/null 2>&1
    fi
}

wait_http() {
    # wait_http <url> <最多等多少秒>
    local url="$1" limit="$2" i=0
    while [ "$i" -lt "$((limit * 2))" ]; do
        if curl -fsS -o /dev/null --max-time 2 "$url" 2>/dev/null; then
            return 0
        fi
        sleep 0.5
        i=$((i + 1))
    done
    return 1
}

echo "===== ClawPet Launcher ====="

# ---------- 0. 读取 .env ----------
if [ ! -f "$ROOT/.env" ]; then
    if [ -f "$ROOT/.env.example" ]; then
        cp "$ROOT/.env.example" "$ROOT/.env"
        c_info "已根据 .env.example 生成 .env"
        c_warn "请打开 .env 填好 DB_PASSWORD（MySQL 密码），然后重新运行 ./scripts/start.sh"
    else
        c_warn "未找到 .env，将使用代码里的默认配置（数据库大概率连不上）"
    fi
fi

if [ -f "$ROOT/.env" ]; then
    while IFS= read -r line || [ -n "$line" ]; do
        line="${line#"${line%%[![:space:]]*}"}"   # 去首部空白
        line="${line%"${line##*[![:space:]]}"}"   # 去尾部空白
        case "$line" in ''|'#'*) continue ;; esac
        key="${line%%=*}"
        value="${line#*=}"
        # 留空的一律跳过：设成空串会把 application.yml 里的默认值顶掉
        [ -z "$value" ] && continue
        export "$key=$value"
    done < "$ROOT/.env"
    echo "已加载 .env"
fi

# ---------- 1. 检查基础环境 ----------
missing=()
command -v java >/dev/null 2>&1 || missing+=("JDK 17+（java 命令找不到）")
command -v mvn  >/dev/null 2>&1 || missing+=("Maven 3.6+（mvn 命令找不到）")
command -v npm  >/dev/null 2>&1 || missing+=("Node.js 18+（npm 命令找不到）")
if [ ${#missing[@]} -gt 0 ]; then
    for m in "${missing[@]}"; do c_err "$m"; done
    echo
    echo "装好之后再运行本脚本。"
    echo "  macOS : brew install openjdk@17 maven node"
    echo "  Ubuntu: sudo apt install openjdk-17-jdk maven nodejs npm"
    exit 1
fi
c_ok "基础环境 OK (java / mvn / npm)"

# ---------- 2. MySQL ----------
if port_busy 3306; then
    echo "MySQL 已在运行 (3306)"
else
    c_warn "MySQL 3306 没在监听。"
    echo "  macOS : brew services start mysql"
    echo "  Ubuntu: sudo systemctl start mysql"
    echo "  没装 MySQL 8：https://dev.mysql.com/downloads/mysql/"
fi

# ---------- 3. Redis（可选）----------
if port_busy 6379; then
    echo "Redis 已在运行 (6379)"
elif command -v redis-server >/dev/null 2>&1; then
    echo "启动 Redis..."
    nohup redis-server > "$LOG_DIR/redis.log" 2>&1 &
    disown 2>/dev/null || true
    sleep 2
    if port_busy 6379; then echo "Redis 启动成功"; else c_warn "Redis 启动失败"; fi
else
    c_warn "没找到 redis-server，跳过。不影响浏览/领养/评论，只是登录验证码不可用。"
fi

# ---------- 4. 项目结构 ----------
if [ ! -f "$BACKEND/pom.xml" ]; then
    c_err "找不到 claw-pet-server/pom.xml，请确认仓库结构完整（本脚本需放在 scripts/ 下）"; exit 1
fi
if [ ! -f "$FRONTEND/package.json" ]; then
    c_err "找不到 claw-pet-web/package.json"; exit 1
fi
c_ok "项目结构 OK"

# ---------- 5. 首次运行：还原演示图片 ----------
# 演示图片放在 demo-data/images/pet/（upload/pet/ 被 gitignore，里面不能存版本化的文件）
if [ -d "$ROOT/demo-data/images/pet" ] && [ -d "$BACKEND/upload/pet" ]; then
    img_count=$(find "$BACKEND/upload/pet" -maxdepth 1 -type f \
        \( -name '*.jpg' -o -name '*.jpeg' -o -name '*.png' -o -name '*.webp' \) 2>/dev/null | wc -l | tr -d ' ')
    if [ "$img_count" -eq 0 ]; then
        cp "$ROOT/demo-data/images/pet/"*.jpg "$BACKEND/upload/pet/" 2>/dev/null
        n=$(find "$BACKEND/upload/pet" -maxdepth 1 -type f -name '*.jpg' | wc -l | tr -d ' ')
        echo "首次运行，已放入演示图片 $n 张"
    fi
fi

# ---------- 6. 前端依赖 ----------
if [ ! -d "$FRONTEND/node_modules" ]; then
    c_info "前端依赖缺失，执行 npm install（第一次会慢）..."
    (cd "$FRONTEND" && npm install --no-audit --no-fund)
    if [ ! -f "$FRONTEND/node_modules/.package-lock.json" ]; then
        c_warn "npm install 未完成，改用 --ignore-scripts 重试"
        (cd "$FRONTEND" && npm install --ignore-scripts --no-audit --no-fund)
    fi
fi
c_ok "前端依赖 OK"

# ---------- 7. 端口检查 ----------
port_busy 8081 && c_warn "8081 已被占用，后端可能启动失败"
port_busy 3001 && c_warn "3001 已被占用，前端可能启动失败"

# ---------- 8. 后端 ----------
if port_busy 8081; then
    echo "后端已在运行 (8081)"
else
    c_info "启动后端 (Spring Boot, 8081)..."
    # 显式传 --server.port，优先级最高，避免环境变量把端口带跑
    (cd "$BACKEND" && nohup mvn spring-boot:run \
        -Dspring-boot.run.arguments=--server.port=8081 \
        > "$LOG_DIR/backend.log" 2>&1 & echo $! > "$RUN_DIR/backend.pid")
    disown 2>/dev/null || true
    echo "等待后端就绪（首次启动 Maven 要下载依赖，可能要几分钟）..."
    echo "  实时日志: tail -f .run/logs/backend.log"
    if wait_http "http://localhost:8081/api/category/list" 300; then
        c_ok "后端启动成功 (8081)"
    else
        c_warn "后端 8081 未就绪，请看 .run/logs/backend.log"
        echo "  常见原因：① .env 里 DB_PASSWORD 没填对 ② MySQL 没启动 ③ 8081 被占用"
    fi
fi

# ---------- 9. 前端 ----------
if port_busy 3001; then
    echo "前端已在运行 (3001)"
else
    c_info "启动前端 (http://localhost:3001) ..."
    (cd "$FRONTEND" && nohup npm run dev > "$LOG_DIR/frontend.log" 2>&1 & echo $! > "$RUN_DIR/frontend.pid")
    disown 2>/dev/null || true
    if wait_http "http://localhost:3001" 120; then
        c_ok "前端启动成功"
    else
        c_warn "前端 3001 未就绪，请看 .run/logs/frontend.log"
    fi
fi

# ---------- 10. 打开浏览器 ----------
if command -v open >/dev/null 2>&1; then
    open "http://localhost:3001"      # macOS
elif command -v xdg-open >/dev/null 2>&1; then
    xdg-open "http://localhost:3001" >/dev/null 2>&1   # Linux
fi

echo
c_ok "全部完成！后端 8081 / 前端 3001"
echo "  默认账号：admin / admin123 （管理员）"
echo "            user  / admin123 （普通用户）"
echo "  停掉服务：./scripts/stop.sh"
