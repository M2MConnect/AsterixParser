$ErrorActionPreference = "Stop"

$frontendDir = Join-Path $PSScriptRoot "frontend"

if (-not (Test-Path $frontendDir)) {
    throw "Frontend-Verzeichnis nicht gefunden: $frontendDir"
}

Push-Location $frontendDir
try {
    npm run dev
}
finally {
    Pop-Location
}
