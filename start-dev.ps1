$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$backendScript = Join-Path $projectRoot "start-backend.ps1"
$frontendScript = Join-Path $projectRoot "start-frontend.ps1"

function Start-ScriptInWindow {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ScriptPath
    )

    $command = @"
try {
    & '$ScriptPath'
}
catch {
    Write-Host ''
    Write-Host 'Start fehlgeschlagen:' -ForegroundColor Red
    Write-Host \$_.Exception.Message -ForegroundColor Red
}
finally {
    Write-Host ''
    Read-Host 'Enter zum Schliessen'
}
"@

    Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $command -WorkingDirectory $projectRoot
}

if (-not (Test-Path $backendScript)) {
    throw "Backend-Skript nicht gefunden: $backendScript"
}

if (-not (Test-Path $frontendScript)) {
    throw "Frontend-Skript nicht gefunden: $frontendScript"
}

Start-ScriptInWindow -ScriptPath $backendScript
Start-Sleep -Seconds 4
Start-ScriptInWindow -ScriptPath $frontendScript

Write-Host "Backend und Frontend wurden in separaten PowerShell-Fenstern gestartet."
