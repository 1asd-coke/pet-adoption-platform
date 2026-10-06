<#
============================================================
 Claw Pet 一键部署（容器化）· Windows 版
============================================================
 把整套环境（MySQL + Redis + 后端 + 前端）打包成 4 个容器跑起来。

 用法（在仓库根目录）：
    .\scripts\deploy.ps1              构建并启动（首次会拉镜像 + 编译，比较慢）
    .\scripts\deploy.ps1 up           同上
    .\scripts\deploy.ps1 down         停止并删除容器（数据保留）
    .\scripts\deploy.ps1 restart      重启
    .\scripts\deploy.ps1 ps           查看状态
    .\scripts\deploy.ps1 logs backend 看某个服务的日志
    .\scripts\deploy.ps1 clean        连数据一起删，会清空数据库和已上传的图片
    .\scripts\deploy.ps1 rebuild      强制重新构建镜像

 第一次跑之前不用做任何事 —— 脚本会自动生成 .env 和随机密码。

 注意：如果 PowerShell 提示脚本被禁止运行，用这个绕过：
    powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1
 或者直接双击 deploy.bat。
============================================================
#>
param(
    [Parameter(Position = 0)]
    [string]$Action = 'up',

    [Parameter(Position = 1, ValueFromRemainingArguments = $true)]
    [string[]]$Rest,

    # 部署成功后不自动打开浏览器（脚本/自动化场景用）
    [switch]$NoBrowser
)

$ErrorActionPreference = 'Stop'

# 本脚本位于 scripts/ 下，往上退一级才是仓库根目录
# （.env 和 docker-compose.yml 都在根目录，compose 的相对路径也锚定根目录）
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Set-Location $Root

# ---------------------------------------------------------- 输出helpers
function Write-Ok   ($m) { Write-Host "  [OK] $m"   -ForegroundColor Green }
function Write-Warn ($m) { Write-Host "  [!]  $m"   -ForegroundColor Yellow }
function Write-Err  ($m) { Write-Host "  [X]  $m"   -ForegroundColor Red }
function Write-Info ($m) { Write-Host "  ->   $m"   -ForegroundColor Cyan }
function Write-Step ($m) { Write-Host ""; Write-Host $m -ForegroundColor White }

# ---------------------------------------------------------- 随机串
function New-Secret {
    param([int]$Length = 32)
    $chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    $bytes = New-Object 'System.Byte[]' $Length
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $rng.Dispose()
    -join ($bytes | ForEach-Object { $chars[$_ % $chars.Length] })
}

# ---------------------------------------------------------- docker compose
$script:DcCmd = $null

function Test-Docker {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        return 'missing'
    }
    # 先试 v2 的 `docker compose`，再退回 v1 的 `docker-compose`
    $null = & docker compose version 2>&1
    if ($LASTEXITCODE -eq 0) {
        $script:DcCmd = @('docker', 'compose')
        return 'ok'
    }
    if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
        $script:DcCmd = @('docker-compose')
        return 'ok'
    }
    return 'missing'
}

function Invoke-Dc {
    param([string[]]$DcArgs)
    # 注意别写成 $DcCmd[1..($DcCmd.Count-1)] —— 当数组只有一个元素时，
    # PowerShell 的降序区间 [1..0] 会算出 @($null, 元素) 这种鬼东西。
    if ($script:DcCmd.Count -gt 1) {
        & $script:DcCmd[0] $script:DcCmd[1] @DcArgs
    }
    else {
        & $script:DcCmd[0] @DcArgs
    }
}

function Require-Docker {
    if ((Test-Docker) -eq 'ok') { return }

    Write-Err '没有检测到 Docker'
    Write-Host ''
    Write-Host '  这套部署方式要求先装 Docker Desktop（自带 compose）：'
    Write-Host ''
    Write-Host '    https://www.docker.com/products/docker-desktop/'
    Write-Host ''
    Write-Host '  装好后重启一次终端，执行 docker compose version 能输出版本号即可。'
    Write-Host ''
    Write-Host '  不想装 Docker 也可以 —— 用本地开发方式启动：' -ForegroundColor Cyan
    Write-Host '    本机装好 JDK 17 / Maven / Node / MySQL / Redis 后，双击 start.bat 即可。'
    Write-Host '    两者不冲突：start.bat 用本机环境，deploy 用容器。'
    Write-Host ''
    exit 1
}

# ---------------------------------------------------------- .env
function Read-EnvValue {
    param([string]$Key)
    if (-not (Test-Path '.env')) { return '' }
    $line = Select-String -Path '.env' -Pattern "^$Key=" -Encoding UTF8 |
            Select-Object -First 1
    if (-not $line) { return '' }
    return ($line.Line -replace "^$Key=", '').Trim()
}

function Initialize-Env {
    Write-Step '[1/4] 检查环境变量'

    if (-not (Test-Path '.env')) {
        if (-not (Test-Path '.env.example')) {
            Write-Err '找不到 .env.example，仓库不完整？'
            exit 1
        }
        Write-Info '.env 不存在，自动生成一份（含随机密码）'

        # 以 .env.example 为模板，替换掉三处空值
        $content = Get-Content '.env.example' -Raw -Encoding UTF8
        $dbPass    = New-Secret
        $redisPass = New-Secret
        $jwt       = (New-Secret 32) + (New-Secret 32)

        $content = $content -replace '(?m)^DB_PASSWORD=$',    "DB_PASSWORD=$dbPass"
        $content = $content -replace '(?m)^REDIS_PASSWORD=$', "REDIS_PASSWORD=$redisPass"
        $content = $content -replace '(?m)^JWT_SECRET=$',     "JWT_SECRET=$jwt"

        # .env 统一写成 UTF-8 无 BOM：docker compose 读带 BOM 的文件会把
        # 第一个变量名连 BOM 一起读进去（表现为第一个变量神秘失效）
        [System.IO.File]::WriteAllText(
            (Join-Path $Root '.env'),
            $content,
            (New-Object System.Text.UTF8Encoding $false)
        )
        Write-Ok '已生成 .env（数据库密码 / Redis 密码 / JWT 密钥都是随机的）'
    }
    else {
        Write-Ok '.env 已存在，沿用现有配置'
    }

    # compose 里这三个是必填，空着会直接报错。这里提前查，提示更清楚。
    $missing = @()
    foreach ($v in @('DB_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET')) {
        if ([string]::IsNullOrWhiteSpace((Read-EnvValue $v))) { $missing += $v }
    }
    if ($missing.Count -gt 0) {
        Write-Err ('.env 里这几项还是空的：' + ($missing -join ', '))
        Write-Host '  它们不能为空（数据库密码 / Redis 密码 / JWT 签名密钥）。'
        Write-Host '  随机值生成办法见 .env.example 末尾。'
        Write-Host '  或者删掉 .env 让本脚本重新生成一份。'
        exit 1
    }
    Write-Ok '必填项都已填写'
}

# ---------------------------------------------------------- 等待就绪
function Wait-Frontend {
    $port = Read-EnvValue 'WEB_PORT'
    if ([string]::IsNullOrWhiteSpace($port)) { $port = '3000' }
    $url = "http://localhost:$port"

    Write-Step '[4/4] 等待服务就绪'
    Write-Info "探测 $url ..."

    for ($i = 1; $i -le 60; $i++) {
        try {
            $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3
            if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 400) {
                Write-Ok '前端已就绪'
                # 部署成功后自动打开浏览器（-NoBrowser 可关闭）
                if (-not $NoBrowser) {
                    try {
                        Start-Process $url
                        Write-Ok "已自动在浏览器打开 $url"
                    } catch { }
                }
                return $true
            }
        }
        catch {
            # 还没起来，继续等
        }
        Start-Sleep -Seconds 2
    }

    Write-Warn '等了 2 分钟前端还没起来，下面是当前状态：'
    Invoke-Dc @('ps')
    Write-Host ''
    Write-Warn '查看日志：.\scripts\deploy.ps1 logs frontend'
    return $false
}

# ---------------------------------------------------------- 完成提示
function Show-Done {
    $port = Read-EnvValue 'WEB_PORT'
    if ([string]::IsNullOrWhiteSpace($port)) { $port = '3000' }
    $bport = Read-EnvValue 'BACKEND_PORT'
    if ([string]::IsNullOrWhiteSpace($bport)) { $bport = '8081' }

    Write-Host ''
    Write-Host '  ========================================' -ForegroundColor Green
    Write-Host '    Claw Pet 部署完成' -ForegroundColor Green
    Write-Host '  ========================================' -ForegroundColor Green
    Write-Host ''
    Write-Host "    前台首页   " -NoNewline; Write-Host "http://localhost:$port/" -ForegroundColor Cyan
    Write-Host "    后台管理   " -NoNewline; Write-Host "http://localhost:$port/#/admin" -ForegroundColor Cyan
    Write-Host "    后端接口   http://localhost:$bport/api"
    Write-Host ''
    Write-Host '    演示账号   admin / admin123'
    Write-Host '               user  / admin123'
    Write-Host ''
    Write-Host '    数据库和 Redis 的端口没有对外暴露，只在容器内网可达。' -ForegroundColor Yellow
    Write-Host '    常用命令：'
    Write-Host '      .\scripts\deploy.ps1 logs backend    看后端日志'
    Write-Host '      .\scripts\deploy.ps1 ps              查看容器状态'
    Write-Host '      .\scripts\deploy.ps1 down            停止（数据保留）'
    Write-Host '      .\scripts\deploy.ps1 clean           停止并清空数据（危险）'
    Write-Host ''
}

# ========================================================== 主流程
switch ($Action.ToLower()) {

    { $_ -in 'up', '' } {
        Require-Docker
        Initialize-Env

        Write-Step '[2/4] 构建镜像'
        Write-Info '首次构建要下 Maven / Node 依赖，大约 3~10 分钟，请耐心等'
        Invoke-Dc @('build')
        if ($LASTEXITCODE -ne 0) { Write-Err '镜像构建失败'; exit 1 }

        Write-Step '[3/4] 启动容器'
        Invoke-Dc @('up', '-d')
        if ($LASTEXITCODE -ne 0) { Write-Err '容器启动失败'; exit 1 }
        Write-Ok '容器已启动'

        $null = Wait-Frontend
        Invoke-Dc @('ps')
        Show-Done
    }

    'rebuild' {
        Require-Docker
        Initialize-Env
        Write-Step '[1/2] 强制重新构建（不用缓存）'
        Invoke-Dc @('build', '--no-cache')
        if ($LASTEXITCODE -ne 0) { Write-Err '镜像构建失败'; exit 1 }
        Write-Step '[2/2] 重启容器'
        Invoke-Dc @('up', '-d')
        $null = Wait-Frontend
        Invoke-Dc @('ps')
        Show-Done
    }

    'down' {
        Require-Docker
        Write-Info '停止并删除容器（数据卷保留）'
        Invoke-Dc @('down')
        Write-Ok '已停止。下次 .\scripts\deploy.ps1 会接着用原来的数据'
    }

    'restart' {
        Require-Docker
        Invoke-Dc @('restart')
        $null = Wait-Frontend
        Invoke-Dc @('ps')
    }

    { $_ -in 'ps', 'status' } {
        Require-Docker
        Invoke-Dc @('ps')
    }

    'logs' {
        Require-Docker
        if ($Rest -and $Rest.Count -gt 0) {
            Invoke-Dc @('logs', '-f', '--tail=200', $Rest[0])
        }
        else {
            Invoke-Dc @('logs', '-f', '--tail=100')
        }
    }

    'clean' {
        Require-Docker
        Write-Host ''
        Write-Warn '即将删除全部数据 —— 数据库内容、用户上传的图片都会没。'
        Write-Warn '（演示图片会在下次启动时自动还原，但用户上传的东西恢复不了）'
        Write-Host ''
        $ans = Read-Host '确认要删吗？输入 yes 继续'
        if ($ans -ne 'yes') {
            Write-Info '已取消'
            exit 0
        }
        Invoke-Dc @('down', '-v')
        Write-Ok '容器和数据卷都已删除'
    }

    'pull' {
        Require-Docker
        Invoke-Dc @('pull')
    }

    { $_ -in 'help', '-h', '--help' } {
        Get-Help $MyInvocation.MyCommand.Definition -Detailed
    }

    default {
        Write-Err "未知命令：$Action"
        Write-Host '可用命令：up / down / restart / ps / logs / clean / rebuild / pull'
        exit 1
    }
}
