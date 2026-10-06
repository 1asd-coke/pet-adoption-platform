# ============================================================
#  Claw Pet 一键启动（Windows / PowerShell）
#
#  双击仓库根目录的 start.bat 即可。或者右键本文件 → 使用 PowerShell 运行。
#
#  它会依次完成：
#    读 .env → 检查环境 → 启动 MySQL → 启动 Redis → 检查项目结构
#    → 首次运行自动还原演示图片 → 安装前端依赖 → 启动后端 → 启动前端 → 打开浏览器
#
#  按 Ctrl+C 或直接关掉窗口可中断。日志同时写入 start.log。
# ============================================================

$ErrorActionPreference = 'Continue'
# 本脚本位于 scripts/ 下，往上退一级才是仓库根目录
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$log = Join-Path $root 'start.log'
$envFile = Join-Path $root '.env'
$envSample = Join-Path $root '.env.example'

function Log($m) {
    $line = ('[{0}] {1}' -f (Get-Date -Format 'HH:mm:ss'), $m)
    Write-Host $line
    Add-Content -Path $log -Value $line -Encoding UTF8
}

function Warn($m) { Log ("警告: " + $m) }

function Test-Port($port) {
    [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

# 在若干候选目录里找可执行文件，返回第一个命中的完整路径（找不到返回 $null）
function Find-Exe($name, $candidates) {
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    return $null
}

Set-Content -Path $log -Value '===== ClawPet Launcher =====' -Encoding UTF8
Log '脚本开始'

# ---------- 0. 读取 .env ----------
if (-not (Test-Path $envFile)) {
    if (Test-Path $envSample) {
        Copy-Item $envSample $envFile
        Log '已根据 .env.example 生成 .env'
        Warn '请打开 .env 填好 DB_PASSWORD（MySQL 密码），然后重新运行本脚本'
    } else {
        Warn '未找到 .env，将直接使用代码里的默认配置（数据库大概率连不上）'
    }
}

if (Test-Path $envFile) {
    Get-Content $envFile -Encoding UTF8 | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        $i = $line.IndexOf('=')
        if ($i -lt 1) { return }
        $k = $line.Substring(0, $i).Trim()
        $v = $line.Substring($i + 1).Trim()
        # 留空的一律跳过：设成空串会把 application.yml 里的默认值顶掉
        if ($v -eq '') { return }
        [Environment]::SetEnvironmentVariable($k, $v, 'Process')
    }
    Log '已加载 .env'
}

# ---------- 1. 检查基础环境 ----------
$missing = @()
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { $missing += 'JDK 17+（java 命令找不到）' }
if (-not (Get-Command mvn  -ErrorAction SilentlyContinue)) { $missing += 'Maven 3.6+（mvn 命令找不到）' }
if (-not (Get-Command npm  -ErrorAction SilentlyContinue)) { $missing += 'Node.js 18+（npm 命令找不到）' }
if ($missing.Count -gt 0) {
    Warn ('缺少必要环境: ' + ($missing -join ' / '))
    Warn '装好之后再运行本脚本。JDK/Maven/Node 装完记得重开一个窗口让 PATH 生效。'
    Read-Host '回车退出'
    exit 1
}
Log '基础环境 OK (java / mvn / npm)'

# ---------- 2. MySQL ----------
if (Test-Port 3306) {
    Log 'MySQL 已在运行 (3306)'
} else {
    $svc = Get-Service -Name 'MySQL*' -ErrorAction SilentlyContinue |
           Where-Object { $_.Status -ne 'Running' } | Select-Object -First 1
    if ($svc) {
        Log ('启动 MySQL 服务 (' + $svc.Name + ')...')
        try { Start-Service -Name $svc.Name -ErrorAction Stop } catch { }
        for ($i = 0; $i -lt 30; $i++) {
            if (Test-Port 3306) { break }
            Start-Sleep -Milliseconds 500
        }
    }
    if (Test-Port 3306) {
        Log 'MySQL 启动成功'
    } else {
        Warn 'MySQL 3306 未就绪。请用【管理员身份】运行: net start MySQL80'
        Warn '（或者用 Navicat / 服务面板手动把 MySQL 起来）'
    }
}

# ---------- 3. Redis（可选，不影响核心功能）----------
if (Test-Port 6379) {
    Log 'Redis 已在运行 (6379)'
} else {
    $redis = Find-Exe 'redis-server' @(
        (Join-Path $root 'redis\redis-server.exe'),
        'C:\Redis\redis-server.exe',
        'D:\Redis\redis-server.exe'
    )
    if ($redis) {
        Log '启动 Redis...'
        Start-Process -FilePath $redis -WindowStyle Minimized
        for ($i = 0; $i -lt 20; $i++) {
            if (Test-Port 6379) { break }
            Start-Sleep -Milliseconds 500
        }
        if (Test-Port 6379) { Log 'Redis 启动成功' } else { Warn 'Redis 启动失败' }
    } else {
        Warn '没找到 redis-server，跳过。不影响浏览/领养/评论，只是登录验证码不可用。'
    }
}

# ---------- 4. 项目结构 ----------
$backend  = Join-Path $root 'claw-pet-server'
$frontend = Join-Path $root 'claw-pet-web'
if (-not (Test-Path (Join-Path $backend 'pom.xml'))) {
    Warn '找不到 claw-pet-server/pom.xml，请确认仓库结构完整（本脚本需放在 scripts/ 下）'
    Read-Host '回车退出'; exit 1
}
if (-not (Test-Path (Join-Path $frontend 'package.json'))) {
    Warn '找不到 claw-pet-web/package.json'
    Read-Host '回车退出'; exit 1
}
Log '项目结构 OK'

# ---------- 5. 首次运行：还原演示图片 ----------
# 演示图片放在 demo-data/images/pet/（upload/pet/ 被 gitignore，里面不能存版本化的文件），
# 首次运行自动拷过去
$uploadPet = Join-Path $backend 'upload\pet'
$demoImages = Join-Path $root 'demo-data\images\pet'
if ((Test-Path $demoImages) -and (Test-Path $uploadPet)) {
    $existing = Get-ChildItem $uploadPet -File -ErrorAction SilentlyContinue |
                Where-Object { $_.Extension -in '.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp' }
    if (-not $existing) {
        Copy-Item (Join-Path $demoImages '*.jpg') $uploadPet -ErrorAction SilentlyContinue
        $n = (Get-ChildItem $uploadPet -File -Filter '*.jpg' -ErrorAction SilentlyContinue).Count
        Log ("首次运行，已放入演示图片 $n 张")
    }
}

# ---------- 6. 前端依赖 ----------
if (-not (Test-Path (Join-Path $frontend 'node_modules'))) {
    Log '前端依赖缺失，执行 npm install（第一次会慢，耐心等）...'
    Push-Location $frontend
    npm install --no-audit --no-fund
    if (-not (Test-Path (Join-Path $frontend 'node_modules\.package-lock.json'))) {
        Warn 'npm install 未完成，改用 --ignore-scripts 重试'
        npm install --ignore-scripts --no-audit --no-fund
    }
    Pop-Location
}
Log '前端依赖 OK'

# ---------- 7. 端口占用检查 ----------
if (Test-Port 8081) { Warn '8081 已被占用，后端可能启动失败' }
if (Test-Port 3001) { Warn '3001 已被占用，前端可能启动失败' }

# ---------- 8. 后端 ----------
if (Test-Port 8081) {
    Log '后端已在运行 (8081)'
} else {
    Log '启动后端 (Spring Boot, 8081)...'
    # 显式传 --server.port，优先级最高，避免环境变量把端口带跑
    $backendCmd = 'cd /d "' + $backend + '" && mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"'
    Start-Process -FilePath 'cmd.exe' -ArgumentList @('/k', $backendCmd) -WorkingDirectory $backend
    Log '等待后端就绪（首次启动 Maven 要下载依赖，可能要几分钟）...'
    $backendOk = $false
    for ($i = 0; $i -lt 360; $i++) {
        try {
            $r = Invoke-WebRequest -Uri 'http://localhost:8081/api/category/list' -UseBasicParsing -TimeoutSec 1 -ErrorAction Stop
            if ($r.StatusCode -eq 200) { $backendOk = $true; break }
        } catch { }
        Start-Sleep -Milliseconds 500
    }
    if ($backendOk) {
        Log '后端启动成功 (8081)'
    } else {
        Warn '后端 8081 未就绪，请看新开那个 Maven 窗口的报错。'
        Warn '常见原因：① .env 里 DB_PASSWORD 没填对 ② MySQL 没启动 ③ 8081 被占用'
    }
}

# ---------- 9. 前端 ----------
if (Test-Port 3001) {
    Log '前端已在运行 (3001)'
} else {
    Log '启动前端 (http://localhost:3001) ...'
    Start-Process -FilePath 'cmd.exe' -ArgumentList @('/k', "cd /d `"$frontend`" && npm run dev") -WorkingDirectory $frontend
    for ($i = 0; $i -lt 120; $i++) {
        try {
            $r = Invoke-WebRequest -Uri 'http://localhost:3001' -UseBasicParsing -TimeoutSec 1 -ErrorAction Stop
            if ($r.StatusCode -eq 200) { break }
        } catch { }
        Start-Sleep -Milliseconds 500
    }
}

# ---------- 10. 打开浏览器 ----------
$chromePaths = @(
    (Join-Path $env:ProgramFiles 'Google\Chrome\Application\chrome.exe'),
    (Join-Path ${env:ProgramFiles(x86)} 'Google\Chrome\Application\chrome.exe'),
    (Join-Path $env:LOCALAPPDATA 'Google\Chrome\Application\chrome.exe')
) | Where-Object { $_ -and (Test-Path $_) }

if ($chromePaths.Count -gt 0) {
    Start-Process $chromePaths[0] -ArgumentList 'http://localhost:3001'
    Log '已用 Chrome 打开'
} else {
    Start-Process 'http://localhost:3001'
    Log '未找到 Chrome，已用默认浏览器打开'
}

Log '全部完成！后端 8081 / 前端 3001 / Redis 6379'
Write-Host ''
Write-Host '默认账号：admin / admin123 （管理员）' -ForegroundColor Cyan
Write-Host '         user  / admin123 （普通用户）' -ForegroundColor Cyan
Read-Host '回车退出'
