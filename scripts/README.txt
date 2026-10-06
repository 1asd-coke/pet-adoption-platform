scripts/ — 启动与部署脚本
==========================

仓库根目录只留两个双击入口（start.bat / deploy.bat），真正干活的脚本都放在这里，
按「本地开发」和「容器部署」两族分开。

本地开发（用本机装的 JDK / Maven / Node / MySQL / Redis）
----------------------------------------------------------
  start.ps1    Windows      读 .env → 起 MySQL/Redis → 还原演示图片 → 装依赖 → 起前后端 → 开浏览器
  start.sh     macOS/Linux  同上
  stop.sh      macOS/Linux  停掉 start.sh 起的前后端

  注：stop.sh 不会动 MySQL 和 Redis —— 那是系统服务，不该由项目脚本乱关。

容器部署（本机只要装 Docker）
------------------------------
  deploy.ps1   Windows      构建镜像 → 起 4 个容器 → 等前端就绪 → 打印访问地址
  deploy.sh    macOS/Linux  同上

  两者都支持子命令：up / down / restart / ps / logs / clean / rebuild / pull，
  首次运行会自动生成 .env（数据库密码、Redis 密码、JWT 密钥都是随机的）。

⚠️ 这几个脚本都会自己往上退一级定位仓库根目录（ROOT），
   所以 .env、docker-compose.yml、demo-data/ 这些相对路径仍然锚定在根目录 ——
   从根目录调用 ./scripts/start.sh 能跑对，cd 进 scripts/ 再跑也对。
   改动路径解析那几行时务必保住这个性质。

⚠️ 行尾和 BOM 不能乱动，见仓库根目录的 .gitattributes：
     *.ps1   必须是 CRLF + UTF-8 BOM（丢了 BOM 中文会乱码）
     *.bat   必须是 CRLF、且不能有 BOM（带 BOM 会被 cmd 解析出错）
     *.sh    必须是 LF（CRLF 会让 shebang 失效）
   用记事本「另存为」或某些格式化工具处理这几个文件，很容易把它们改坏。

用法见仓库根目录 README.md 的「快速开始」一节。
