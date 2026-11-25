# Frontend EMS Store

Aplicación web Angular. Para desarrollo local, ejecuta `npm install` y `npm start` desde esta carpeta; Angular estará disponible en `http://localhost:4200` y usará la API local por defecto.

## Desplegar en Vercel

1. Importa el repositorio y establece `ems-store-frontend` como **Root Directory**.
2. Usa `npm run build` como comando de compilación y `dist/ems-store-frontend/browser` como directorio de salida.
3. Añade la variable de entorno `NG_APP_API_URL` con la URL pública del backend terminada en `/api`.
4. Despliega. `vercel.json` configura la salida y las rutas de Angular.
5. Configura `APP_FRONTEND_URL` en Render con el origen asignado por Vercel y redepliega el backend para habilitar CORS.

`NG_APP_API_URL` es una URL pública, no un secreto. Los cambios en variables de entorno requieren un nuevo despliegue.
