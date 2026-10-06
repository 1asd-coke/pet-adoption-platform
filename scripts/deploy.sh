#!/usr/bin/env bash
# ============================================================
#  Claw Pet 一键部署（容器化）
# ============================================================
#  把整套环境（MySQL + Redis + 后端 + 前端）打包成 4 个容器跑起来。
#
#  用法：
#     ./scripts/deploy.sh              构建并启动（首次会拉镜像 + 编译，比较慢）
#     ./scripts/deploy.sh up           同上
#     ./scripts/deploy.sh down         停止并删除容器（数据保留）
#     ./scripts/deploy.sh restart      重启
#     ./scripts/deploy.sh ps           查看状态
#     ./scripts/deploy.sh logs [服务]  看日志（如 ./scripts/deploy.sh logs backend）
#     ./scripts/deploy.sh clean        连数据一起删 ⚠️ 会清空数据库和已上传的图片
#     ./scripts/deploy.sh rebuild      强制重新构建镜像
#
#  第一次跑之前不用做任何事 —— 脚本会自动生成 .env 和随机密码。
# ============================================================
set -euo pipefail

# 本脚本位于 scripts/ 下，往上退一级才是仓库根目录
# （.env、docker-compose.yml、demo-data/ 都在根目录，compose 的相对路径也锚定根目录）
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# ---------- 颜色 ----------
if [ -t 1 ]; then
    C_OK=$'\033[32m'; C_WARN=$'\033[33m'; C_ERR=$'\033[31m'
    C_INFO=$'\033[36m'; C_BOLD=$'\033[1m'; C_OFF=$'\033[0m'
else
    C_OK=; C_WARN=; C_ERR=; C_INFO=; C_BOLD=; C_OFF=
fi

ok()   { echo "${C_OK}✓${C_OFF} $*"; }
warn() { echo "${C_WARN}!${C_OFF} $*"; }
err()  { echo "${C_ERR}✗${C_OFF} $*" >&2; }
info() { echo "${C_INFO}→${C_OFF} $*"; }
step() { echo; echo "${C_BOLD}$*${C_OFF}"; }

# ---------- 找一个能生成随机串的办法 ----------
gen_secret() {
    if command -v openssl >/dev/null 2>&1; then
        openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | cut -c1-32
    elif [ -r /dev/urandom ]; then
        LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 32
    else
        printf 'ChangeMe%s%s' "$RANDOM" "$RANDOM"
    fi
}

# ---------- 探测 docker compose ----------
DC=""
detect_compose() {
    if ! command -v docker >/dev/null 2>&1; then
        return 1
    fi
    if docker compose version >/dev/null 2>&1; then
        DC="docker compose"
        return 0
    fi
    if command -v docker-compose >/dev/null 2>&1; then
        DC="docker-compose"
        return 0
    fi
    return 2
}

require_docker() {
    detect_compose && return 0
    local rc=$?
    err "没有检测到 Docker${rc:-}"
    echo
    echo "  这套部署方式要求先装 Docker（含 compose 插件）："
    echo
    echo "    Windows / macOS :  https://www.docker.com/products/docker-desktop/"
    echo "    Linux           :  https://docs.docker.com/engine/install/"
    echo
    echo "  装好后执行 'docker compose version' 能输出版本号即可。"
    echo
    echo "  ${C_INFO}不想装 Docker 也可以${C_OFF} —— 直接用本地开发方式启动："
    echo "    本机装好 JDK 17 / Maven / Node / MySQL / Redis 后，跑 ./scripts/start.sh 即可。"
    echo "    两者不冲突：scripts/start.sh 用本机环境，scripts/deploy.sh 用容器。"
    exit 1
}

# ---------- 准备 .env ----------
prepare_env() {
    step "[1/4] 检查环境变量"

    if [ ! -f .env ]; then
        if [ ! -f .env.example ]; then
            err "找不到 .env.example，仓库不完整？"
            exit 1
        fi
        info ".env 不存在，自动生成一份（含随机密码）"
        cp .env.example .env

        local db_pass redis_pass jwt
        db_pass="$(gen_secret)"
        redis_pass="$(gen_secret)"
        jwt="$(gen_secret)$(gen_secret)"

        # 就地替换三处空值（用 # 做分隔符避免密码里的 / 冲突）
        sed -i.bak \
            -e "s|^DB_PASSWORD=$|DB_PASSWORD=${db_pass}|" \
            -e "s|^REDIS_PASSWORD=$|REDIS_PASSWORD=${redis_pass}|" \
            -e "s|^JWT_SECRET=$|JWT_SECRET=${jwt}|" \
            .env
        rm -f .env.bak
        ok "已生成 .env（数据库密码 / Redis 密码 / JWT 密钥都是随机的）"
    else
        ok ".env 已存在，沿用现有配置"
    fi

    # compose 里这三个是必填（:? 语法），空着会直接报错。
    # 这里提前检查，给出比 compose 更清楚的提示。
    local missing=()
    for v in DB_PASSWORD REDIS_PASSWORD JWT_SECRET; do
        local val
        val="$(grep -E "^${v}=" .env | head -1 | cut -d= -f2- || true)"
        if [ -z "$val" ]; then
            missing+=("$v")
        fi
    done
    if [ ${#missing[@]} -gt 0 ]; then
        err ".env 里这几项还是空的：${missing[*]}"
        echo "  它们不能为空（数据库密码 / Redis 密码 / JWT 签名密钥）。"
        echo "  随机值生成办法见 .env.example 末尾。"
        echo "  或者删掉 .env 让本脚本重新生成一份。"
        exit 1
    fi
    ok "必填项都已填写"
}

# ---------- 读一个 .env 的值 ----------
env_get() {
    grep -E "^$1=" .env 2>/dev/null | head -1 | cut -d= -f2- || true
}

# ---------- 等前端起来 ----------
wait_frontend() {
    local port url i
    port="$(env_get WEB_PORT)"; port="${port:-3000}"
    url="http://localhost:${port}"

    step "[4/4] 等待服务就绪"
    info "探测 ${url} ..."
    for i in $(seq 1 60); do
        if command -v curl >/dev/null 2>&1; then
            curl -fsS -o /dev/null "$url" 2>/dev/null && { ok "前端已就绪"; return 0; }
        elif command -v wget >/dev/null 2>&1; then
            wget -q -O /dev/null "$url" 2>/dev/null && { ok "前端已就绪"; return 0; }
        else
            # 没有 curl/wget 就没法探活，直接放过
            warn "没有 curl / wget，跳过就绪探测"
            return 0
        fi
        sleep 2
    done
    warn "等了 2 分钟前端还没起来，下面是当前状态："
    $DC ps
    echo
    warn "查看日志：./scripts/deploy.sh logs frontend"
    return 1
}

print_done() {
    local port bport
    port="$(env_get WEB_PORT)";  port="${port:-3000}"
    bport="$(env_get BACKEND_PORT)"; bport="${bport:-8081}"

    echo
    echo "${C_OK}${C_BOLD}========================================${C_OFF}"
    echo "${C_OK}${C_BOLD}  Claw Pet 部署完成${C_OFF}"
    echo "${C_OK}${C_BOLD}========================================${C_OFF}"
    echo
    echo "  前台首页   ${C_INFO}http://localhost:${port}/${C_OFF}"
    echo "  后台管理   ${C_INFO}http://localhost:${port}/#/admin${C_OFF}"
    echo "  后端接口   http://localhost:${bport}/api"
    echo
    echo "  演示账号   admin / admin123"
    echo "             user  / admin123"
    echo
    echo "  ${C_WARN}数据库和 Redis 的端口没有对外暴露${C_OFF}，只在容器内网可达。"
    echo "  需要从外面连的话，给 docker-compose.yml 的 mysql 服务加 ports 映射。"
    echo
    echo "  常用命令："
    echo "    ./scripts/deploy.sh logs backend     看后端日志"
    echo "    ./scripts/deploy.sh ps               查看容器状态"
    echo "    ./scripts/deploy.sh down             停止（数据保留）"
    echo "    ./scripts/deploy.sh clean            停止并清空数据 ⚠️"
    echo
}

# ============================================================
#  主流程
# ============================================================
CMD="${1:-up}"
shift || true

case "$CMD" in

    up)
        require_docker
        prepare_env

        step "[2/4] 构建镜像"
        info "首次构建要下 Maven / Node 依赖，大约 3~10 分钟，请耐心等"
        $DC build

        step "[3/4] 启动容器"
        $DC up -d
        ok "容器已启动"

        wait_frontend || true
        $DC ps
        print_done
        ;;

    rebuild)
        require_docker
        prepare_env
        step "[1/2] 强制重新构建（不用缓存）"
        $DC build --no-cache
        step "[2/2] 重启容器"
        $DC up -d
        wait_frontend || true
        $DC ps
        print_done
        ;;

    down)
        require_docker
        info "停止并删除容器（数据卷保留）"
        $DC down
        ok "已停止。下次 ./scripts/deploy.sh 会接着用原来的数据"
        ;;

    restart)
        require_docker
        $DC restart
        wait_frontend || true
        $DC ps
        ;;

    ps|status)
        require_docker
        $DC ps
        ;;

    logs)
        require_docker
        if [ $# -gt 0 ]; then
            $DC logs -f --tail=200 "$1"
        else
            $DC logs -f --tail=100
        fi
        ;;

    clean)
        require_docker
        echo
        warn "${C_ERR}${C_BOLD}即将删除全部数据${C_OFF} —— 数据库内容、用户上传的图片都会没。"
        warn "（演示图片会在下次启动时自动还原，但用户上传的东西恢复不了）"
        echo
        printf "确认要删吗？输入 yes 继续："
        read -r ans
        if [ "$ans" != "yes" ]; then
            info "已取消"
            exit 0
        fi
        $DC down -v
        ok "容器和数据卷都已删除"
        ;;

    pull)
        require_docker
        $DC pull
        ;;

    -h|--help|help)
        sed -n '2,18p' "$0" | sed 's/^# \{0,1\}//'
        ;;

    *)
        err "未知命令：$CMD"
        echo "可用命令：up / down / restart / ps / logs / clean / rebuild / pull"
        exit 1
        ;;
esac
