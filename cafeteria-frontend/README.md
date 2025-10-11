# Cafeteria Frontend

Aplicación Angular para la interfaz de Yumil Kool.

## Requisitos
- Node.js LTS (18.x o 20.x recomendado)
- npm 9+
- (Opcional) Angular CLI instalado globalmente: `npm install -g @angular/cli`

## Puesta en marcha
1. `cd cafeteria-frontend`
2. `npm install`
3. `npm start`
4. Abrir `http://localhost:4200`

## Integración con el backend
- El endpoint base se define en `src/environments/environment*.ts` (`apiBaseUrl`).
- El formulario de login aún no llama al backend; conecta tu servicio de autenticación en `LoginComponent.onSubmit()`.
- Si sirves el backend Spring en `http://localhost:8080`, habilita CORS para aceptar peticiones desde `http://localhost:4200`.

## Próximos pasos sugeridos
- Crear servicio de autenticación y almacenar tokens de sesión.
- Añadir navegación y rutas protegidas para el resto del sistema.
- Sustituir la imagen de fondo remota por un recurso local en `src/assets` si lo prefieres.
