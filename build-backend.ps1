param(
    [switch]$SkipTests = $true
)

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot

function Resolve-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        return $env:JAVA_HOME
    }

    $candidates = @(
        "C:\Users\Martin.Mueller\.jdks\openjdk-25.0.2",
        "C:\Program Files\Eclipse Adoptium\jdk-21",
        "C:\Program Files\Java\jdk-21"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path (Join-Path $candidate "bin\java.exe")) {
            return $candidate
        }
    }

    throw "Kein JDK gefunden. Bitte JAVA_HOME setzen."
}

function Resolve-MavenCmd {
    $wrapperCmd = Join-Path $projectRoot "mvnw.cmd"
    if (Test-Path $wrapperCmd) {
        return $wrapperCmd
    }

    $command = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $candidates = @(
        "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3\plugins\maven\lib\maven3\bin\mvn.cmd"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "Kein Maven Wrapper oder Maven gefunden. Bitte mvnw.cmd oder mvn.cmd bereitstellen."
}

$env:JAVA_HOME = Resolve-JavaHome
$mavenCmd = Resolve-MavenCmd
$mavenArgs = @("clean", "package")

if ($SkipTests) {
    $mavenArgs = @("clean", "package", "-DskipTests")
}

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "Maven=$mavenCmd"

& $mavenCmd @mavenArgs
