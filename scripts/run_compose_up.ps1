function Fail($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red; exit 1 }

Write-Host "[INFO] Verificando Docker..."
docker version *> $null 2>&1
if ($LASTEXITCODE -ne 0) { Fail "Docker no está instalado o no está corriendo." }

Write-Host "[INFO] Levantando servicios (docker compose up -d --build)..."
Push-Location (Join-Path $PSScriptRoot "..")
docker compose up -d --build
if ($LASTEXITCODE -ne 0) { Pop-Location; Fail "Falló docker compose up." }

Write-Host "[INFO] Abriendo http://localhost:8080"
Start-Process "http://localhost:8080"
Pop-Location

