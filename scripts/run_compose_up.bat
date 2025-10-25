@echo off
setlocal
set SCRIPT_DIR=%~dp0
echo [INFO] Verificando Docker...
docker version >nul 2>&1 || (
  echo [ERROR] Docker no esta instalado o no esta corriendo.
  exit /b 1
)
echo [INFO] Levantando servicios con docker compose...
pushd "%SCRIPT_DIR%.."
docker compose up -d --build || (
  echo [ERROR] Fallo docker compose up.
  popd
  exit /b 1
)
echo [INFO] Abriendo http://localhost:8080
start "" http://localhost:8080
popd
endlocal

