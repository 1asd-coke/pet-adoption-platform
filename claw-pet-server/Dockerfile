# ============================================================
# Claw Pet 后端镜像
#
# 多阶段构建：用 Maven 镜像编译，再只把 jar 拷进 JRE 镜像，
# 最终镜像里没有 Maven、没有源码、没有 Maven 仓库（省 600MB+）。
#
# ⚠️ 构建上下文必须是【仓库根目录】，因为这个 Dockerfile 要用到
#    同级目录的 demo-data/（把演示图打进镜像）。
#    在 docker-compose.yml 里对应：
#      build:
#        context: .
#        dockerfile: claw-pet-server/Dockerfile
# ============================================================

# ---------- 阶段一：编译 ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# 先只拷 pom.xml 单独跑一次依赖解析。
# 这样只要 pom.xml 没变，改 Java 源码时这一层就能命中缓存，不用重新下依赖。
COPY claw-pet-server/pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY claw-pet-server/src ./src
RUN mvn -B -q -DskipTests package


# ---------- 阶段二：运行 ----------
FROM eclipse-temurin:17-jre-alpine

# tzdata 用于时区（日志/数据库时间都要 Asia/Shanghai），
# 其余用 busybox 自带的 wget 做健康检查，不额外装 curl
RUN apk add --no-cache tzdata \
 && ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# jar
COPY --from=build /build/target/*.jar /app/app.jar

# 演示图片：容器首次启动时由 entrypoint 拷进 upload/pet/
# 注意只拷 pet/（真正要用的 1.1MB），不拷 originals/（素材源，运行时用不到）
COPY demo-data/images/pet/ /app/demo-images/

# 上传目录：用户上传的图片落在这里，docker-compose 会给它挂持久化卷
RUN mkdir -p /app/upload/pet /app/upload/avatar

COPY claw-pet-server/docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# 上传接口单文件上限 30MB，健康检查用轻量的 wget 探公开接口
HEALTHCHECK --interval=10s --timeout=5s --start-period=90s --retries=12 \
  CMD wget -q --spider http://127.0.0.1:8081/api/category/list || exit 1

EXPOSE 8081

ENTRYPOINT ["/app/docker-entrypoint.sh"]
