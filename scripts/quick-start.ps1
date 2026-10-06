<#
============================================================
 Claw Pet 一键引导（Windows）· quick-start
============================================================
 面向"刚 clone 仓库、什么都没装"的用户，从零把项目跑起来：

   [1/7] 检查操作系统（Win10 1809+ / Win11）
   [2/7] 检查 Docker（没装 → winget 自动装 / 打开官网）
   [3/7] 检查 WSL2（缺失 → 自动 wsl --install，装完需重启电脑）
   [4/7] 检查 Docker 引擎（没运行 → 自动启动 Docker Desktop）
   [5/7] 检查国内网络（拉不动 Docker Hub → 自动配置镜像加速）
   [6/7] 端口预检（3000 / 后端端口被占会提前提醒）
   [7/7] 调用 scripts\deploy.ps1 up 完成部署 → 打开浏览器

 用法：
   双击根目录 quick-start.bat
   或：powershell -ExecutionPolicy Bypass -File scripts\quick-start.ps1
   参数：-NoBrowser 部署后不自动开浏览器；-Yes 警告时自动继续；
         -SkipWsl 跳过 WSL2 检查（使用 Hyper-V 后端的用户）

 Mac / Linux 用户请直接用 scripts/deploy.sh，本脚本仅面向 Windows。
============================================================
#>
param(
    [switch]$NoBrowser,
    [switch]$Yes,
    [switch]$SkipWsl
)

$ErrorActionPreference = 'Continue'
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Set-Location $Root

# ---------------------------------------------------------- 输出 helpers
function Write-Ok   ($m) { Write-Host "  [OK] $m"   -ForegroundColor Green }
function Write-Warn ($m) { Write-Host "  [!]  $m"   -ForegroundColor Yellow }
function Write-Err  ($m) { Write-Host "  [X]  $m"   -ForegroundColor Red }
function Write-Info ($m) { Write-Host "  ->   $m"   -ForegroundColor Cyan }
function Write-Step ($m) { Write-Host ""; Write-Host $m -ForegroundColor White }

function Confirm-Continue ([string]$Question) {
    if ($Yes) { return $true }
    $ans = Read-Host "  $Question (Y=继续 / N=退出)"
    return ($ans -match '^[Yy]')
}

# ==========================================================
# [1/7] 操作系统
# ==========================================================
Write-Step '[1/7] 检查操作系统'
if ($env:OS -ne 'Windows_NT') {
    Write-Err '本脚本仅支持 Windows。Mac / Linux 用户请直接运行 scripts/deploy.sh'
    exit 1
}
$v = [Environment]::OSVersion.Version
if ($v.Major -lt 10 -or ($v.Major -eq 10 -and $v.Build -lt 17763)) {
    Write-Err "Windows 版本过低（Build $($v.Build)），WSL2 需要 Win10 1809+ 或 Win11"
    exit 1
}
Write-Ok "Windows $($v.Major).$($v.Minor)（Build $($v.Build)）"

# ==========================================================
# [2/7] Docker 是否安装
# ==========================================================
Write-Step '[2/7] 检查 Docker 是否已安装'
$dockerExe = $null
$cmd = Get-Command docker -ErrorAction SilentlyContinue
if ($cmd) { $dockerExe = $cmd.Source }
else {
    foreach ($p in @(
        (Join-Path $env:ProgramFiles  'Docker\Docker\resources\bin\docker.exe'),
        (Join-Path $env:LOCALAPPDATA  'Programs\DockerDesktop\resources\bin\docker.exe')
    )) { if (Test-Path $p) { $dockerExe = $p; break } }
}

while (-not $dockerExe) {
    Write-Err '没有检测到 Docker Desktop'
    $hasWinget = [bool](Get-Command winget -ErrorAction SilentlyContinue)
    if ($hasWinget) {
        Write-Info '检测到 winget，可以自动安装 Docker Desktop（过程中会弹 UAC 授权窗口）'
    } else {
        Write-Info '本机没有 winget，将打开官网下载页（默认安装即可，一路 Next）'
    }
    $ans = Read-Host '  [W] winget 自动安装 / [D] 打开官网手动下载 / [R] 我装好了，重新检查'
    switch ($ans) {
        'W' {
            if ($hasWinget) {
                & winget install -e --id Docker.DockerDesktop --accept-source-agreements --accept-package-agreements
            } else {
                Write-Warn '本机没有 winget，改为打开官网'
                Start-Process 'https://www.docker.com/products/docker-desktop/'
            }
        }
        'D' { Start-Process 'https://www.docker.com/products/docker-desktop/' }
        'R' {
            $cmd = Get-Command docker -ErrorAction SilentlyContinue
            if ($cmd) { $dockerExe = $cmd.Source }
            else {
                foreach ($p in @(
                    (Join-Path $env:ProgramFiles  'Docker\Docker\resources\bin\docker.exe'),
                    (Join-Path $env:LOCALAPPDATA  'Programs\DockerDesktop\resources\bin\docker.exe')
                )) { if (Test-Path $p) { $dockerExe = $p; break } }
            }
        }
        default { Write-Info '请输入 W / D / R' }
    }
}

# 把 docker.exe 所在目录借给 PATH，保证后面调用的 deploy.ps1 也能找到 docker
$dockerBin = Split-Path $dockerExe
if (($env:PATH -split ';') -notcontains $dockerBin) { $env:PATH = "$dockerBin;$env:PATH" }
Write-Ok "Docker CLI：$dockerExe"

# Docker Desktop 主程序路径（启动引擎用）
$Launchers = @(
    (Join-Path $env:ProgramFiles  'Docker\Docker\Docker Desktop.exe'),
    (Join-Path $env:LOCALAPPDATA  'Programs\DockerDesktop\Docker Desktop.exe')
)

function Test-Engine {
    $ver = & $dockerExe version --format '{{.Server.Version}}' 2>$null
    return $ver
}

# ==========================================================
# [3/7] WSL2
# ==========================================================
Write-Step '[3/7] 检查 WSL2'
if ($SkipWsl) {
    Write-Info '已按参数跳过 WSL2 检查（使用 Hyper-V 后端的用户）'
} else {
$wslOk = $false
try { $null = & wsl.exe --status 2>&1; if ($LASTEXITCODE -eq 0) { $wslOk = $true } } catch { }
if (-not $wslOk) {
    try { $null = & wsl.exe -l -v 2>&1; if ($LASTEXITCODE -eq 0) { $wslOk = $true } } catch { }
}
if ($wslOk) {
    Write-Ok 'WSL2 已就绪'
} else {
    Write-Warn '未检测到 WSL2（Docker Desktop 默认依赖它跑 Linux 容器）'
    Write-Info '可以帮你执行 wsl --install --no-distribution（弹 UAC 授权；首次启用需要重启电脑）'
    $ans = Read-Host '  现在自动安装 WSL2 吗？(Y/N)'
    if ($ans -match '^[Yy]') {
        try {
            Start-Process -FilePath 'wsl.exe' -ArgumentList '--install', '--no-distribution' -Verb RunAs -Wait
            Write-Warn '如果这是首次启用 WSL，请【重启电脑】后重新双击 quick-start.bat'
        } catch {
            Write-Err "自动安装失败：$($_.Exception.Message)"
            Write-Info '手动安装方法：https://learn.microsoft.com/windows/wsl/install'
        }
        if (-not (Confirm-Continue '没重启的话后续步骤可能失败，仍要继续吗？')) { exit 1 }
    } else {
        Write-Info '已跳过。手动安装参考：https://learn.microsoft.com/windows/wsl/install'
        if (-not (Confirm-Continue '没有 WSL2 也可能装不了 Docker 容器，仍要继续吗？')) { exit 1 }
    }
}
}

# ==========================================================
# [4/7] Docker 引擎是否运行
# ==========================================================
Write-Step '[4/7] 检查 Docker 引擎'
$engine = Test-Engine
if ($engine) {
    Write-Ok "引擎运行中（Server $engine）"
} else {
    Write-Warn 'Docker 引擎没有运行，尝试自动启动 Docker Desktop...'
    $launched = $false
    foreach ($l in $Launchers) {
        if (Test-Path $l) {
            Start-Process -FilePath $l -WorkingDirectory (Split-Path $l)
            $launched = $true
            break
        }
    }
    if (-not $launched) {
        Write-Err '找不到 Docker Desktop 主程序，请从开始菜单手动启动后重跑本脚本'
        exit 1
    }
    Write-Info '已发出启动命令，等待引擎就绪（首次启动会弹许可协议，请点同意）...'
    $ok = $false
    for ($i = 0; $i -lt 18; $i++) {
        Start-Sleep -Seconds 5
        $engine = Test-Engine
        if ($engine) { $ok = $true; break }
        Write-Host "  等待引擎... $((($i + 1) * 5))s"
    }
    if (-not $ok) {
        Write-Err '等了 90 秒引擎还没就绪。请手动打开 Docker Desktop，左下角变绿后重跑本脚本'
        exit 1
    }
    Write-Ok "引擎运行中（Server $engine）"
}

# ==========================================================
# [5/7] 国内网络 / 镜像加速
# ==========================================================
Write-Step '[5/7] 检查镜像拉取网络（国内环境）'
$daemonJson   = Join-Path $env:USERPROFILE '.docker\daemon.json'
$NeededMirrors = @('https://docker.1ms.run', 'https://docker.m.daocloud.io', 'https://dockerproxy.net')

function Test-DockerHubDirect {
    # 只要能收到任意 HTTP 响应（哪怕 4xx）就算通；超时/连接重置才算不通
    try {
        $req = [System.Net.WebRequest]::Create('https://auth.docker.io/token?service=registry.docker.io&scope=repository:library/alpine:pull')
        $req.Timeout = 6000
        $req.ReadWriteTimeout = 6000
        $resp = $req.GetResponse()
        $resp.Close()
        return $true
    } catch {
        return ($null -ne $_.Exception.Response)
    }
}

$existingMirrors = @()
if (Test-Path $daemonJson) {
    try {
        $j = Get-Content $daemonJson -Raw | ConvertFrom-Json
        if ($j.'registry-mirrors') { $existingMirrors = @($j.'registry-mirrors') }
    } catch { }
}

if ($existingMirrors.Count -gt 0) {
    Write-Ok "已配置镜像加速（$($existingMirrors[0]) ...），跳过"
} elseif (Test-DockerHubDirect) {
    Write-Ok 'Docker Hub 直连正常，无需加速'
} else {
    Write-Warn 'Docker Hub 直连失败（国内网络常见问题），自动配置镜像加速...'
    $obj = $null
    if (Test-Path $daemonJson) { try { $obj = Get-Content $daemonJson -Raw | ConvertFrom-Json } catch { } }
    if ($null -eq $obj) { $obj = New-Object PSObject }
    $all = @($existingMirrors + $NeededMirrors) | Select-Object -Unique
    $obj | Add-Member -NotePropertyName 'registry-mirrors' -NotePropertyValue @($all) -Force
    $json = ConvertTo-Json $obj -Depth 10
    New-Item -ItemType Directory -Path (Split-Path $daemonJson) -Force | Out-Null
    [System.IO.File]::WriteAllText($daemonJson, $json, (New-Object System.Text.UTF8Encoding $false))
    Write-Ok "已写入 $daemonJson"

    Write-Info '重启 Docker 使配置生效（容器会自动恢复）...'
    & $dockerExe desktop restart 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Warn 'docker desktop 命令不可用，改为重启 Docker Desktop 进程...'
        Get-Process 'Docker Desktop' -ErrorAction SilentlyContinue | Stop-Process -Force
        Start-Sleep -Seconds 3
        foreach ($l in $Launchers) { if (Test-Path $l) { Start-Process -FilePath $l; break } }
    }
    $engineOk = $false
    for ($i = 0; $i -lt 24; $i++) {
        Start-Sleep -Seconds 5
        if (Test-Engine) { $engineOk = $true; break }
    }
    if ($engineOk) { Write-Ok '引擎已恢复运行' }
    else { Write-Err '重启后引擎没起来，请手动打开 Docker Desktop 后重跑本脚本'; exit 1 }
}

# ==========================================================
# [6/7] 端口预检
# ==========================================================
Write-Step '[6/7] 端口预检'
function Read-EnvValue([string]$Key) {
    if (-not (Test-Path '.env')) { return '' }
    $line = Select-String -Path '.env' -Pattern "^$Key=" | Select-Object -First 1
    if (-not $line) { return '' }
    return ($line.Line -replace "^$Key=", '').Trim()
}
$webPort = Read-EnvValue 'WEB_PORT';      if ([string]::IsNullOrWhiteSpace($webPort)) { $webPort = '3000' }
$bePort  = Read-EnvValue 'BACKEND_PORT';  if ([string]::IsNullOrWhiteSpace($bePort))  { $bePort  = '8081' }

$busyByForeign = @()
foreach ($port in @($webPort, $bePort)) {
    $conns = $null
    try { $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue } catch { }
    if ($conns) {
        $names = @()
        foreach ($c in $conns) {
            $pn = (Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue).ProcessName
            $names += "$pn"
        }
        # docker 自己（com.docker.backend / wslrelay 等）占着 = 我们的容器在跑，不算冲突
        $foreign = $names | Where-Object { $_ -notmatch 'docker|wslrelay|vmmem|backend' }
        if ($foreign) { $busyByForeign += "$port（被 $($foreign -join ',') 占用）" }
    }
}
if ($busyByForeign.Count -gt 0) {
    Write-Warn "以下端口被其他程序占用：$($busyByForeign -join '；')"
    Write-Info '可编辑 .env 修改 WEB_PORT / BACKEND_PORT 后重跑，或停掉占用程序'
    if (-not (Confirm-Continue '仍要继续部署吗？')) { exit 1 }
} else {
    Write-Ok "端口 $webPort（前端）与 $bePort（后端）可用"
}

# ==========================================================
# [7/7] 部署
# ==========================================================
Write-Step '[7/7] 开始部署（复用 scripts\deploy.ps1，成功后它会自动打开浏览器）'
$deployArgs = @('up')
if ($NoBrowser) { $deployArgs += '-NoBrowser' }
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'scripts\deploy.ps1') @deployArgs
if ($LASTEXITCODE -ne 0) {
    Write-Err '部署失败。排查指引：'
    Write-Info '  看后端日志   .\scripts\deploy.ps1 logs backend'
    Write-Info '  看容器状态   .\scripts\deploy.ps1 ps'
    exit 1
}
Write-Ok "部署完成：http://localhost:$webPort/"
Write-Host ''
Write-Host '  演示账号   admin / admin123   user / admin123'
Write-Host '  常用命令   .\scripts\deploy.ps1 ps | logs backend | down'
