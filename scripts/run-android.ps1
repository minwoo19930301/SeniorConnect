[CmdletBinding()]
param(
    [string]$AvdName = "senior",
    [int]$BootTimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' was not found on PATH. Follow docs/DEV_SETUP.md to configure the Android command-line tools."
    }
}

function Invoke-NativeCommand {
    param(
        [string]$Description,
        [scriptblock]$Command
    )

    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

function Test-EmulatorRunning {
    $devices = & adb devices
    return $devices | Where-Object { $_ -match '^emulator-\d+\s+device$' }
}

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Require-Command "adb"
Require-Command "emulator"

if (-not (Test-EmulatorRunning)) {
    Write-Host "Starting emulator '$AvdName'..."
    Start-Process -FilePath "emulator" -ArgumentList "-avd", $AvdName, "-no-snapshot", "-no-audio" -WindowStyle Hidden
} else {
    Write-Host "Using the running emulator."
}

Write-Host "Waiting for Android to finish booting..."
Invoke-NativeCommand "Waiting for the device" { adb wait-for-device }
$deadline = (Get-Date).AddSeconds($BootTimeoutSeconds)
do {
    $bootCompleted = (& adb shell getprop sys.boot_completed).Trim()
    if ($bootCompleted -eq "1") {
        break
    }
    Start-Sleep -Seconds 2
} while ((Get-Date) -lt $deadline)

if ($bootCompleted -ne "1") {
    throw "The emulator did not finish booting within $BootTimeoutSeconds seconds."
}

Write-Host "Setting simulated location to Islamabad, Pakistan..."
# adb emu geo fix expects longitude first, then latitude.
Invoke-NativeCommand "Setting the simulated location" { adb emu geo fix 73.05756228372401 33.7192128264106 }

Write-Host "Building the debug APK..."
Invoke-NativeCommand "Building the debug APK" { .\gradlew.bat :app:assembleDebug }

Write-Host "Installing the debug APK..."
Invoke-NativeCommand "Installing the debug APK" { .\gradlew.bat :app:installDebug }

Write-Host "Launching SeniorConnect..."
Invoke-NativeCommand "Launching SeniorConnect" { adb shell am start -n org.seniorconnect.app/.MainActivity }
