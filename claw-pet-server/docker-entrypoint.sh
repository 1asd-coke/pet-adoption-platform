#!/bin/sh
# ============================================================
# Claw Pet 后端容器启动入口
#
# 只做一件事：首次启动时把演示图片放进上传目录，然后交给 java。
# 之所以要在这里做，是因为 upload/ 是持久化卷 ——
# 卷是空的，而演示图打在了镜像里，得有个时机把它搬过去。
# ============================================================
set -e

DEMO_DIR=/app/demo-images
UPLOAD_PET_DIR=/app/upload/pet

count_images() {
    # 数一下目录里已有的图片（排除 README/.gitkeep 这类占位文件）
    find "$1" -maxdepth 1 -type f \
        \( -name '*.jpg' -o -name '*.jpeg' -o -name '*.png' \
           -o -name '*.gif' -o -name '*.webp' -o -name '*.bmp' \) \
        2>/dev/null | wc -l | tr -d ' '
}

if [ -d "$DEMO_DIR" ]; then
    mkdir -p "$UPLOAD_PET_DIR"

    if [ "$(count_images "$UPLOAD_PET_DIR")" -eq 0 ]; then
        cp "$DEMO_DIR"/*.jpg "$UPLOAD_PET_DIR"/ 2>/dev/null || true
        echo "[entrypoint] 首次启动，已放入演示图片 $(count_images "$UPLOAD_PET_DIR") 张"
    else
        echo "[entrypoint] 上传目录已有 $(count_images "$UPLOAD_PET_DIR") 张图片，跳过演示图还原"
    fi
else
    echo "[entrypoint] 未找到演示图目录 $DEMO_DIR，跳过"
fi

echo "[entrypoint] 启动后端（上传目录 /app/upload）..."
exec java ${JAVA_OPTS:-} -jar /app/app.jar "$@"
