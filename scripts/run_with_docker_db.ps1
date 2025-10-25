Param(
  [string]$ContainerName = "cafeteria-db",
  [string]$PostgresImage = "postgres:16",
  [string]$DbName = "cafeteria_db",
  [string]$DbUser = "postgres",
  [string]$DbPassword = "mysecretpassword",
  [int]$HostPort = 5432
)

function Fail($msg) {
  Write-Host "[ERROR] $msg" -ForegroundColor Red
  exit 1
}

Write-Host "[INFO] Checking Docker availability..."
docker version *> $null 2>&1
if ($LASTEXITCODE -ne 0) { Fail "Docker no está instalado o no está corriendo." }

# Start or create Postgres container
Write-Host "[INFO] Ensuring Postgres container '$ContainerName' is running..."
$existing = docker ps -a --format '{{.Names}}' | Where-Object { $_ -eq $ContainerName }
if ($existing) {
  $running = docker ps --format '{{.Names}}' | Where-Object { $_ -eq $ContainerName }
  if (-not $running) {
    docker start $ContainerName | Out-Null
  }
} else {
  docker run -d --name $ContainerName `
    -e POSTGRES_DB=$DbName `
    -e POSTGRES_USER=$DbUser `
    -e POSTGRES_PASSWORD=$DbPassword `
    -p ${HostPort}:5432 `
    $PostgresImage | Out-Null
}

if ($LASTEXITCODE -ne 0) { Fail "No se pudo iniciar el contenedor de Postgres." }

# Wait for DB readiness
Write-Host "[INFO] Waiting for database to be ready..."
$ready = $false
for ($i=0; $i -lt 60; $i++) {
  $logs = docker logs $ContainerName --since 5s 2>$null
  if ($logs -match "database system is ready to accept connections") { $ready = $true; break }
  Start-Sleep -Seconds 2
}
if (-not $ready) { Write-Host "[WARN] Continuando aunque la DB no confirmó readiness aún..." -ForegroundColor Yellow }

# Build JAR if missing
$jar = Join-Path $PSScriptRoot "..\target\cafeteria-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jar)) {
  Write-Host "[INFO] Building application (Maven)..."
  Push-Location (Join-Path $PSScriptRoot "..")
  if (Test-Path "mvnw.cmd") { .\mvnw.cmd -q -DskipTests package } else { mvn -q -DskipTests package }
  if ($LASTEXITCODE -ne 0) { Pop-Location; Fail "Falló el build de Maven." }
  Pop-Location
}

# Run app with env overrides
Write-Host "[INFO] Launching application..."
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:${HostPort}/${DbName}"
$env:SPRING_DATASOURCE_USERNAME = $DbUser
$env:SPRING_DATASOURCE_PASSWORD = $DbPassword

Push-Location (Join-Path $PSScriptRoot "..")
java -jar "target\cafeteria-0.0.1-SNAPSHOT.jar"
Pop-Location

