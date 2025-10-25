#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME=${1:-cafeteria-db}
POSTGRES_IMAGE=${POSTGRES_IMAGE:-postgres:16}
DB_NAME=${DB_NAME:-cafeteria_db}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-mysecretpassword}
HOST_PORT=${HOST_PORT:-5432}

echo "[INFO] Checking Docker availability..."
if ! docker version >/dev/null 2>&1; then
  echo "[ERROR] Docker no está instalado o no está corriendo." >&2
  exit 1
fi

echo "[INFO] Ensuring Postgres container '$CONTAINER_NAME' is running..."
if docker ps -a --format '{{.Names}}' | grep -wq "$CONTAINER_NAME"; then
  if ! docker ps --format '{{.Names}}' | grep -wq "$CONTAINER_NAME"; then
    docker start "$CONTAINER_NAME" >/dev/null
  fi
else
  docker run -d --name "$CONTAINER_NAME" \
    -e POSTGRES_DB="$DB_NAME" \
    -e POSTGRES_USER="$DB_USER" \
    -e POSTGRES_PASSWORD="$DB_PASSWORD" \
    -p "$HOST_PORT:5432" \
    "$POSTGRES_IMAGE" >/dev/null
fi

echo "[INFO] Waiting for database to be ready..."
for i in $(seq 1 60); do
  if docker logs "$CONTAINER_NAME" --since 5s 2>/dev/null | grep -q "database system is ready to accept connections"; then
    break
  fi
  sleep 2
done

JAR="$(cd "$(dirname "$0")/.." && pwd)/target/cafeteria-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR" ]; then
  echo "[INFO] Building application (Maven)..."
  pushd "$(dirname "$0")/.." >/dev/null
  if [ -f mvnw ]; then ./mvnw -q -DskipTests package; else mvn -q -DskipTests package; fi
  popd >/dev/null
fi

export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${HOST_PORT}/${DB_NAME}"
export SPRING_DATASOURCE_USERNAME="$DB_USER"
export SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD"

echo "[INFO] Launching application..."
exec java -jar "$JAR"

