# Cafeteria — Guía de ejecución (Backend + Frontend)

Este proyecto tiene un backend Spring Boot (Java 21) y un frontend (carpeta `cafeteria-frontend`).
Puedes ejecutarlo de varias formas. La más simple para evaluación es usar Docker para backend + base de datos;
para el frontend puedes usar Node.js localmente.

## Requisitos rápidos
- Opción recomendada (solo para backend): Docker Desktop instalado.
- Para el frontend en modo dev: Node.js LTS (18/20) y npm.
- Si prefieres ejecutar el backend desde el IDE: Java 21.

---

## Opción A — Solo Docker para backend (recomendada)
Levanta Postgres y el backend con un solo comando. No necesitas Java ni Maven.

1) En la raíz del proyecto:
   - `docker compose up -d --build`
2) Backend disponible en `http://localhost:8080`
3) Frontend (desde carpeta `cafeteria-frontend`):
   - `npm install`
   - `npm start` (normalmente abre `http://localhost:4200`)

Archivos implicados:
- `docker-compose.yml:1` — define servicios `db` (Postgres) y `app` (Spring Boot)
- `Dockerfile:1` — build multi-stage del backend (Maven → JRE 21)

Notas:
- El servicio `db` se llama `mi-postgres` y expone `5432:5432`.
- El backend expone `8080:8080`.
- La app arranca con `SPRING_JPA_HIBERNATE_DDL_AUTO=update` para auto-crear tablas.
- Si ya tienes un contenedor llamado `mi-postgres`, puede haber conflicto de nombre. Soluciones:
  - Eliminar el contenedor previo: `docker rm -f mi-postgres` y volver a `docker compose up -d --build`.
  - O edita `docker-compose.yml` para cambiar `container_name`/puertos.

Comandos útiles:
- Ver logs: `docker compose logs -f app`
- Apagar: `docker compose down`
- Resetear DB (borra datos): `docker compose down -v`

---

## Opción B — Scripts de conveniencia (Windows/macOS/Linux)
Ya vienen listos scripts que automatizan el arranque.

- Windows (doble click): `scripts\run_compose_up.bat`
  - Hace `docker compose up -d --build` y abre `http://localhost:8080`.
- PowerShell: `scripts/run_compose_up.ps1`
- Bash (macOS/Linux): `scripts/run_with_docker_db.sh` (levanta Postgres y ejecuta JAR; requiere Java 21 si no usas Compose).

Archivos:
- `scripts/run_compose_up.bat:1`
- `scripts/run_compose_up.ps1:1`
- `scripts/run_with_docker_db.ps1:1`
- `scripts/run_with_docker_db.bat:1`
- `scripts/run_with_docker_db.sh:1`

Parámetros útiles (scripts DB + JAR):
- PowerShell: `./scripts/run_with_docker_db.ps1 -ContainerName mi-postgres -DbName cafeteria_db -DbPassword mysecretpassword -HostPort 5432`
- Bash: `CONTAINER_NAME=mi-postgres DB_NAME=cafeteria_db DB_PASSWORD=mysecretpassword HOST_PORT=5432 ./scripts/run_with_docker_db.sh`

---

## Opción C — Ejecutar desde el IDE (tu flujo actual)
Si prefieres correr como en clase (IDE + npm):

1) Asegura Postgres en Docker (si no usas Compose):
   - Crear/usar contenedor local (ejemplo del ingeniero):
     - `docker run --name mi-postgres -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_DB=cafeteria_db -p 5432:5432 -d postgres`
2) Backend: Ejecuta `src/main/java/com/app/cafeteria/CafeteriaApplication.java:1` desde el IDE.
   - Config por defecto del backend: `src/main/resources/application.properties:1`
   - Conecta a `jdbc:postgresql://localhost:5432/cafeteria_db` (usuario `postgres`, pass `mysecretpassword`).
3) Frontend:
   - `cd cafeteria-frontend`
   - `npm install`
   - `npm start` → `http://localhost:4200`

Si tu contenedor crea solo la DB `postgres`, ajusta la URL o crea `cafeteria_db`:
- Crear DB: `docker exec -it mi-postgres psql -U postgres -c "CREATE DATABASE cafeteria_db;"`
- O usar DB `postgres` cambiando la URL a `jdbc:postgresql://localhost:5432/postgres`.

---

## Requisitos del Frontend
- Node.js LTS (https://nodejs.org/) y npm.
- En `cafeteria-frontend`:
  - `npm install`
  - `npm start`
- El dev server suele correr en `http://localhost:4200`.
- Si el frontend llama al backend, confirma las URLs y CORS (backend `http://localhost:8080`).

---

## Problemas comunes
- Puerto 5432 ocupado (Postgres): cambia el mapeo en `docker-compose.yml` (por ejemplo `"55432:5432"`) y ajusta `SPRING_DATASOURCE_URL` si ejecutas fuera de Compose.
- Puerto 8080 ocupado (backend): cambia `"9090:8080"` en `docker-compose.yml`.
- Contenedor `mi-postgres` ya existe: `docker rm -f mi-postgres` o cambia `container_name` en `docker-compose.yml`.
- Sin Java instalado: usa la Opción A (Compose) que no requiere Java/Maven.
- Limpieza total (incluida data): `docker compose down -v`.

---

## Resumen rápido (para el profe/compañero)
- Instalar Docker Desktop.
- En la raíz del repo: `docker compose up -d --build`.
- Backend: `http://localhost:8080`.
- Frontend:
  - Instalar Node.js LTS.
  - `cd cafeteria-frontend && npm install && npm start` → `http://localhost:4200`.

Si prefieren un “doble click” en Windows:
- Abrir `scripts\run_compose_up.bat`.
