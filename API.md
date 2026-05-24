# API Reference — api-auth-java

**Base URL local:** `http://localhost:8081`

---

## Autenticación

Los endpoints protegidos requieren el header:
```
Authorization: Bearer <access_token>
```

El `access_token` se obtiene en `/api/auth/login` o `/api/auth/register` → login posterior.

---

## Datos semilla (perfil dev)

Al arrancar por primera vez, el DataInitializer crea:

| Dato | Valor |
|---|---|
| App | `POS System` → `appId: 1` |
| Usuario | `admin` / `Admin123!` |
| Rol | `ADMIN` (con permisos READ, WRITE, DELETE, REPORTS) |

Usa estos datos para probar sin crear nada primero.

---

## 1. Auth — `/api/auth`

> Todos los endpoints de `/api/auth` son **públicos** (no requieren token).

### POST `/api/auth/login`

Login estándar. Devuelve access token + refresh token.

**Body:**
```json
{
  "username": "admin",
  "password": "Admin123!",
  "appId": 1
}
```

**Response 200:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "refresh_token": "d4f8a2b1-...",
  "token_type": "Bearer",
  "expires_in": 900
}
```

**Errores:**
- `401` — credenciales incorrectas
- `401` — email no verificado
- `423` — cuenta bloqueada (demasiados intentos fallidos)

---

### POST `/api/auth/register`

Auto-registro. Crea el usuario y envía email de verificación.
El usuario **no puede hacer login** hasta verificar el email.

**Body:**
```json
{
  "appId": 1,
  "username": "juanperez",
  "email": "juan@ejemplo.com",
  "password": "Pass123!",
  "fullName": "Juan Pérez",
  "phone": "+573001234567"
}
```

> `phone` es opcional. El password requiere: mayúscula, minúscula, número y carácter especial (`@$!%*?&`).

**Response 201:**
```json
{
  "message": "Registration successful. Please check your email to verify your account."
}
```

**Errores:**
- `409` — username o email ya existe para esa app

---

### GET `/api/auth/verify-email?token=...`

Verifica el email del usuario. El token llega por email.

**Query param:** `token` (UUID del link enviado por email)

**Response 200:**
```json
{
  "message": "Email verified successfully. You can now log in."
}
```

---

### POST `/api/auth/refresh`

Renueva el access token usando el refresh token (ventana deslizante de 60 min).

**Body:**
```json
{
  "refreshToken": "d4f8a2b1-...",
  "appId": 1
}
```

**Response 200:** mismo formato que login (`access_token`, `refresh_token`, etc.)

**Errores:**
- `401` — refresh token inválido, expirado o revocado

---

### POST `/api/auth/logout`

Revoca el refresh token. El access token expira naturalmente (15 min).

**Body:**
```json
{
  "refreshToken": "d4f8a2b1-...",
  "appId": 1
}
```

**Response 200:** vacío

---

### POST `/api/auth/forgot-password`

Envía link de reset por email. Siempre devuelve 200 (evita enumeración de emails).

**Body:**
```json
{
  "email": "juan@ejemplo.com",
  "appId": 1
}
```

**Response 200:**
```json
{
  "message": "If that email exists, a reset link has been sent."
}
```

---

### POST `/api/auth/reset-password`

Cambia la contraseña usando el token recibido por email. Token válido por 1 hora.

**Body:**
```json
{
  "token": "uuid-del-email",
  "newPassword": "NuevoPass123!"
}
```

**Response 200:**
```json
{
  "message": "Password reset successfully. You can now log in."
}
```

---

### POST `/api/auth/google`

Login con Google (Option B). El cliente obtiene el `idToken` con el SDK de Google y lo intercambia aquí por tokens de la app.

**Body:**
```json
{
  "idToken": "eyJhbGci...",
  "appId": 1
}
```

**Response 200:** mismo formato que login

---

## 2. Users — `/api/users`

> Todos los endpoints de usuarios requieren **`Authorization: Bearer <token>`**.

### POST `/api/users`

Crea un usuario (admin crea usuarios directamente, sin verificación de email).

**Body:**
```json
{
  "appId": 1,
  "username": "cajero01",
  "email": "cajero01@pos.com",
  "password": "Pass123!",
  "fullName": "Cajero Uno",
  "phone": "+573009876543"
}
```

**Response 201:**
```json
{
  "userId": 2,
  "username": "cajero01",
  "email": "cajero01@pos.com",
  "phone": "+573009876543",
  "fullName": "Cajero Uno",
  "isActive": true,
  "isEmailVerified": false,
  "createdAt": "2026-05-24T10:00:00",
  "lastPasswordChange": "2026-05-24T10:00:00",
  "appId": 1,
  "appName": "POS System",
  "roles": []
}
```

---

### GET `/api/users/{id}`

Obtiene un usuario por ID.

**Response 200:** mismo formato que el objeto de arriba

**Errores:**
- `404` — usuario no existe

---

### GET `/api/users`

Lista usuarios paginada. Parámetros opcionales:

| Param | Tipo | Default | Descripción |
|---|---|---|---|
| `appId` | Long | — | filtra por aplicación |
| `page` | int | 0 | número de página |
| `size` | int | 20 | usuarios por página |
| `sort` | string | `userId` | campo de ordenamiento |

**Ejemplos:**
```
GET /api/users
GET /api/users?appId=1
GET /api/users?appId=1&page=0&size=10&sort=username,asc
```

**Response 200:**
```json
{
  "content": [ { ...usuario... }, { ...usuario... } ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

### PUT `/api/users/{id}`

Actualiza datos del usuario. Todos los campos son opcionales.

**Body:**
```json
{
  "fullName": "Cajero Uno Actualizado",
  "phone": "+573001111111",
  "email": "nuevo@email.com"
}
```

**Response 200:** usuario actualizado

---

### DELETE `/api/users/{id}`

Elimina un usuario permanentemente.

**Response 204:** sin cuerpo

---

### PUT `/api/users/{id}/activate`

Activa un usuario desactivado.

**Response 200:**
```json
{
  "userId": 2,
  "isActive": true,
  ...
}
```

---

### PUT `/api/users/{id}/deactivate`

Desactiva un usuario (no lo elimina).

**Response 200:**
```json
{
  "userId": 2,
  "isActive": false,
  ...
}
```

---

### POST `/api/users/{id}/roles`

Asigna un rol a un usuario.

**Body:**
```json
{
  "roleId": 2,
  "appId": 1
}
```

**Response 200:** usuario con la lista `roles` actualizada

**Errores:**
- `409` — el usuario ya tiene ese rol en esa app

---

### DELETE `/api/users/{userId}/roles/{roleId}/applications/{appId}`

Quita un rol de un usuario.

**Ejemplo:** `DELETE /api/users/2/roles/2/applications/1`

**Response 200:** usuario con roles actualizados

---

### POST `/api/users/{id}/change-password`

Cambia la contraseña verificando la actual.

**Body:**
```json
{
  "currentPassword": "Admin123!",
  "newPassword": "NuevoPass456!"
}
```

**Response 200:** vacío

**Errores:**
- `400` — contraseña actual incorrecta

---

## 3. Applications — `/api/applications`

> Requieren **`Authorization: Bearer <token>`**.

### POST `/api/applications`

Crea una nueva aplicación.

**Body:**
```json
{
  "appName": "E-Commerce App",
  "description": "Tienda online principal"
}
```

**Response 201:**
```json
{
  "appId": 2,
  "appName": "E-Commerce App",
  "description": "Tienda online principal"
}
```

---

### GET `/api/applications`

Lista todas las aplicaciones.

**Response 200:** array de aplicaciones

---

### GET `/api/applications/{id}`

Obtiene una aplicación por ID.

**Response 200:** objeto aplicación

**Errores:**
- `404` — no existe

---

### PUT `/api/applications/{id}`

Actualiza nombre y descripción.

**Body:**
```json
{
  "appName": "E-Commerce App v2",
  "description": "Nueva descripción"
}
```

**Response 200:** aplicación actualizada

---

### DELETE `/api/applications/{id}`

Elimina una aplicación.

**Response 204:** sin cuerpo

---

## 4. Roles — `/api/roles`

> Requieren **`Authorization: Bearer <token>`**.

### POST `/api/roles`

Crea un rol para una aplicación.

**Body:**
```json
{
  "roleName": "SUPERVISOR",
  "appId": 1
}
```

**Response 201:**
```json
{
  "roleId": 4,
  "roleName": "SUPERVISOR",
  "appId": 1,
  "permissions": []
}
```

---

### GET `/api/roles`

Lista todos los roles.

---

### GET `/api/roles/{id}`

Obtiene un rol por ID.

---

### GET `/api/roles/application/{appId}`

Lista roles de una aplicación.

**Ejemplo:** `GET /api/roles/application/1`

---

### PUT `/api/roles/{id}`

Actualiza un rol.

**Body:**
```json
{
  "roleName": "SUPERVISOR_SENIOR",
  "appId": 1
}
```

---

### DELETE `/api/roles/{id}`

Elimina un rol.

**Response 204:** sin cuerpo

---

### POST `/api/roles/{id}/permissions`

Asigna un permiso a un rol.

**Body:**
```json
{
  "permissionId": 1
}
```

**Response 200:** rol con la lista `permissions` actualizada

---

### DELETE `/api/roles/{roleId}/permissions/{permissionId}`

Quita un permiso de un rol.

**Ejemplo:** `DELETE /api/roles/1/permissions/2`

**Response 200:** rol actualizado

---

## 5. Permissions — `/api/permissions`

> Requieren **`Authorization: Bearer <token>`**.

### POST `/api/permissions`

Crea un permiso.

**Body:**
```json
{
  "permissionName": "EXPORT",
  "appId": 1
}
```

**Response 201:**
```json
{
  "permissionId": 5,
  "permissionName": "EXPORT",
  "appId": 1
}
```

---

### GET `/api/permissions`

Lista todos los permisos.

---

### GET `/api/permissions/{id}`

Obtiene un permiso por ID.

---

### GET `/api/permissions/application/{appId}`

Lista permisos de una aplicación.

**Ejemplo:** `GET /api/permissions/application/1`

---

### PUT `/api/permissions/{id}`

Actualiza un permiso.

**Body:**
```json
{
  "permissionName": "EXPORT_ADVANCED",
  "appId": 1
}
```

---

### DELETE `/api/permissions/{id}`

Elimina un permiso.

**Response 204:** sin cuerpo

---

## 6. Health — `/actuator`

> Público. No requiere token.

### GET `/actuator/health`

**Response 200:**
```json
{
  "status": "UP"
}
```

---

## Flujo de prueba recomendado en Postman

Sigue este orden para probar todo desde cero:

```
1.  GET  /actuator/health               → verificar que el servicio está vivo

2.  POST /api/auth/login                → login con admin / Admin123! / appId:1
        → copiar access_token y refresh_token

3.  GET  /api/applications              → ver la app "POS System" (appId=1)
4.  GET  /api/roles/application/1       → ver roles ADMIN, CASHIER, SUPERVISOR
5.  GET  /api/permissions/application/1 → ver permisos READ, WRITE, DELETE, REPORTS

6.  POST /api/users                     → crear un usuario nuevo
7.  GET  /api/users                     → listar usuarios paginados
8.  GET  /api/users/{id}               → ver el usuario creado
9.  POST /api/users/{id}/roles         → asignarle un rol
10. PUT  /api/users/{id}/deactivate    → desactivarlo
11. PUT  /api/users/{id}/activate      → reactivarlo

12. POST /api/auth/refresh              → renovar tokens con el refresh_token
13. POST /api/auth/logout               → revocar refresh token

14. POST /api/auth/register             → registrar usuario nuevo (envía email)
15. POST /api/auth/forgot-password      → solicitar reset de contraseña
```

---

## Configuración de Postman

### Variable de entorno

Crea un Environment en Postman con:

| Variable | Valor inicial |
|---|---|
| `base_url` | `http://localhost:8081` |
| `access_token` | *(vacío — se llena con el script de login)* |
| `refresh_token` | *(vacío)* |
| `app_id` | `1` |

### Script automático para capturar el token

En el endpoint de **login**, pestaña **Tests**:
```javascript
const res = pm.response.json();
pm.environment.set("access_token", res.access_token);
pm.environment.set("refresh_token", res.refresh_token);
```

Luego en todos los endpoints protegidos, pestaña **Authorization**:
- Type: `Bearer Token`
- Token: `{{access_token}}`
