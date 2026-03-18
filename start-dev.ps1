$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$backendScript = Join-Path $projectRoot "start-backend.ps1"
$frontendScript = Join-Path $projectRoot "start-frontend.ps1"

if (-not (Test-Path $backendScript)) {
    throw "Backend-Skript nicht gefunden: $backendScript"
}

if (-not (Test-Path $frontendScript)) {
    throw "Frontend-Skript nicht gefunden: $frontendScript"
}

Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", $backendScript -WorkingDirectory $projectRoot
Start-Sleep -Seconds 4
Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", $frontendScript -WorkingDirectory $projectRoot

Write-Host "Backend und Frontend wurden in separaten PowerShell-Fenstern gestartet."
