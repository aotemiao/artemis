# 通过 Nacos Open API 将 config/nacos 目录下的配置文件上传到 Nacos
# 用法: .\upload-configs.ps1 [-NacosServer "http://127.0.0.1:8848"] [-NamespaceId ""]
# 需先启动 Nacos（如 docker-compose up -d）

param(
    [string]$NacosServer = "http://127.0.0.1:8848",
    [string]$NamespaceId = ""
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$configDir = $scriptDir
$group = "DEFAULT_GROUP"
$api = "$NacosServer/nacos/v2/cs/config"

# 上传顺序：先 common、datasource，再各服务
$order = @(
    "application-common.yml",
    "datasource.yml",
    "artemis-system.yml",
    "artemis-gateway.yml",
    "artemis-auth.yml"
)

function Invoke-NacosPublish {
    param([string]$DataId, [string]$Content)
    $encoded = [System.Net.WebUtility]::UrlEncode($Content)
    $body = "dataId=$DataId&group=$group&content=$encoded"
    if ($NamespaceId -ne "") {
        $body += "&namespaceId=$NamespaceId"
    }
    try {
        $resp = Invoke-RestMethod -Uri $api -Method Post -Body $body -ContentType "application/x-www-form-urlencoded" -TimeoutSec 10
        if ($resp.code -eq 0) {
            Write-Host "  OK  $DataId" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  FAIL $DataId  code=$($resp.code) $($resp.message)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "  ERROR $DataId  $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

Write-Host "Nacos: $NacosServer  Group: $group  NamespaceId: '$NamespaceId'" -ForegroundColor Cyan
Write-Host ""

$found = 0
$ok = 0
foreach ($dataId in $order) {
    $path = Join-Path $configDir $dataId
    if (-not (Test-Path $path)) { continue }
    $found++
    $content = Get-Content -Path $path -Raw -Encoding UTF8
    if (Invoke-NacosPublish -DataId $dataId -Content $content) { $ok++ }
}

Write-Host ""
Write-Host "Done: $ok/$found config(s) published." -ForegroundColor $(if ($ok -eq $found) { "Green" } else { "Yellow" })
