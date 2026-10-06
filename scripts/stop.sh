#!/usr/bin/env bash
# ============================================================
#  Claw Pet 停止脚本（macOS / Linux）
#
#  做两件事：
#    1. 杀掉 start.sh 起的后端/前端进程（按 .run/*.pid）
#    2. 如果 8081 / 3001 还在监听，再按端口兜底杀一次
#
#  不会动 MySQL 和 Redis —— 那是系统服务，不该由项目脚本乱关。
# ============================================================

set -uo pipefail

# 本脚本位于 scripts/ 下，往上退一级才是仓库根目录（.run/*.pid 在根目录）
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT/.run"

kill_pidfile() {
    local name="$1" pidfile="$RUN_DIR/$1.pid"
    [ -f "$pidfile" ] || return 0
    local pid
    pid="$(cat "$pidfile" 2>/dev/null)"
    if [ -n "${pid:-}" ] && kill -0 "$pid" 2>/dev/null; then
        echo "停止 $name (pid $pid) ..."
        # mvn / npm 会派生 java、node 子进程，整组一起收掉
        kill -TERM -- "-$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null
        sleep 1
        kill -0 "$pid" 2>/dev/null && kill -KILL "$pid" 2>/dev/null
    fi
    rm -f "$pidfile"
}

kill_port() {
    local port="$1" label="$2"
    command -v lsof >/dev/null 2>&1 || return 0
    local pids
    pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null)"
    if [ -n "$pids" ]; then
        echo "端口 $port 仍被占用，结束 $label 进程: $pids"
        echo "$pids" | xargs -r kill -TERM 2>/dev/null
        sleep 1
        pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null)"
        [ -n "$pids" ] && echo "$pids" | xargs -r kill -KILL 2>/dev/null
    fi
}

kill_pidfile backend
kill_pidfile frontend
kill_port 8081 后端
kill_port 3001 前端

echo "已停止。（MySQL / Redis 保持运行，需要的话自行停）"
