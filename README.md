# Auth Service — Authorization & Authentication Server

Microservicio centralizado de autenticación y autorización multi-aplicación.
Diseñado para ser consumido desde **Angular**, **Flutter** y otros **microservicios Spring Boot**.

---

## Tabla de Contenidos

- [Primeros pasos al clonar](#primeros-pasos-al-clonar)
- [Características](#características)
- [Stack Tecnológico](#stack-tecnológico)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Modelo de Datos](#modelo-de-datos)
- [Configuración](#configuración)
- [Ejecución Local](#ejecución-local)
- [Prueba Rápida Local (paso a paso)](#prueba-rápida-local-paso-a-paso)
- [API Endpoints](#api-endpoints)
- [Flujo de Tokens](#flujo-de-tokens)
- [Google OAuth2 — Integración](#google-oauth2--integración)
- [Integración con Microservicios](#integración-con-microservicios)
- [Integración Angular](#integración-angular)
- [Integración Flutter](#integración-flutter)
- [Despliegue en AWS](#despliegue-en-aws)
- [Seguridad y JWT](#seguridad-y-jwt)
- [Manejo de Excepciones](#manejo-de-excepciones)
- [Tests](#tests)

---

## Primeros pasos al clonar

### Levantar en local (solo necesitas Docker)

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/auth-service.git
cd auth-service

# 2. Crear tu archivo de variables de entorno
cp .env.example .env
# Editar .env solo si quieres cambiar puertos o contraseñas locales.
# Para prueba inmediata, los valores por defecto funcionan sin cambios.

# 3. Levantar MySQL + auth-service
docker-compose up -d

# 4. Esperar ~20 segundos y verificar
curl http://localhost:8081/actuator/health
# {"status":"UP"}
```

Listo. El servicio está corriendo en `http://localhost:8081`.
Flyway ejecuta las 4 migraciones automáticamente al arrancar.

> Para pruebas locales sin servidor de email, activa la verificación de email
> directamente en la base de datos después de registrar un usuario:
> ```sql
> -- Conectar a MySQL: puerto 3307, user: auth_user, pass: auth_password_local
> UPDATE auth_db.users SET is_email_verified = TRUE WHERE username = 'tu_usuario';
> ```

---

### Pasar a producción

1. Completar **todas** las variables marcadas con `<REEMPLAZAR>` en `.env` (o configurarlas en tu servidor / AWS ECS)
2. Cambiar `JWT_ISSUER` al dominio real con HTTPS
3. Cambiar `CORS_ORIGINS` a los dominios de producción
4. Montar el directorio de claves RSA en un volumen persistente (EFS en AWS)
5. Ver la sección [Despliegue en AWS](#despliegue-en-aws) para el checklist completo

---

## Características

- **Login username/password** con bloqueo temporal (3 intentos → 30 minutos)
- **Login con Google** (Option B: exchange de idToken en el servidor)
- **Tokens JWT RS256** — access token (15 min) + refresh token (60 min sliding window)
- **Multi-tenant** — usuarios, roles y permisos aislados por aplicación
- **Verificación de email** obligatoria antes del primer login
- **Reset de contraseña** vía email
- **Logout con revocación** de refresh token
- **JWKS público** (`/oauth2/jwks`) para que los microservicios validen tokens sin consultar la BD
- **CORS configurable** por variable de entorno
- **Dockerizado** — Docker Compose con MySQL 8.0

---

## Stack Tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.3 |
| Seguridad | Spring Security + OAuth2 Authorization Server 1.3.0 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos (prod) | MySQL 8.0 |
| Base de datos (test) | H2 in-memory |
| Migraciones | Flyway |
| JWT | Nimbus JOSE (RS256) |
| Email | Spring Mail (AWS SES compatible) |
| Contenerización | Docker + Docker Compose |
| Deploy objetivo | AWS (ECS + RDS + API Gateway + SES) |

---

## Estructura del Proyecto

```
auth-service/
├── src/main/java/com/universal/auth/
│   ├── config/
│   │   ├── AppProperties.java              # Configuración tipada (jwt/cors/lockout/google/mail)
│   │   ├── AuthorizationServerConfig.java  # JWK setup + JWKS endpoint
│   │   ├── WebSecurityConfig.java          # Cadena de seguridad API (público/protegido)
│   │   ├── CorsConfig.java                 # CORS configurable por env
│   │   ├── SecurityBeans.java              # BCryptPasswordEncoder bean
│   │   ├── Jwks.java                       # Wrapper generación RSA
│   │   └── JwksPersistence.java            # Carga/guarda claves RSA en disco
│   ├── controller/
│   │   ├── AuthController.java             # Login, Google, refresh, logout, register, etc.
│   │   ├── UserController.java             # CRUD usuarios + roles + password
│   │   ├── ApplicationController.java      # CRUD aplicaciones
│   │   ├── RoleController.java             # CRUD roles + permisos
│   │   └── PermissionController.java       # CRUD permisos
│   ├── domain/entities/
│   │   ├── User.java                       # users (con app_id, locked_until, is_email_verified)
│   │   ├── ApplicationEntity.java          # applications
│   │   ├── Role.java                       # roles
│   │   ├── Permission.java                 # permissions
│   │   ├── RolePermission.java             # rolepermissions (pivot)
│   │   ├── UserRole.java                   # user_roles (pivot)
│   │   ├── LoginLog.java                   # login_logs (auditoría)
│   │   ├── PasswordReset.java              # passwordresets
│   │   ├── SocialAccount.java              # social_accounts (Google OAuth)
│   │   ├── RefreshToken.java               # refresh_tokens
│   │   ├── EmailVerification.java          # email_verifications
│   │   └── UserAppSettings.java            # user_app_settings
│   ├── service/
│   │   ├── AuthService.java                # Login, Google, refresh, logout, verify, reset
│   │   ├── JwtTokenGenerator.java          # Generación JWT (RS256)
│   │   ├── RefreshTokenService.java        # Rotación tokens + SHA-256 hash
│   │   ├── EmailService.java               # Verificación + reset via JavaMailSender
│   │   ├── GoogleTokenVerifier.java        # Verifica idToken de Google
│   │   ├── UserService.java / impl/        # CRUD usuarios
│   │   ├── ApplicationService.java / impl/ # CRUD aplicaciones
│   │   ├── RoleService.java / impl/        # CRUD roles
│   │   └── PermissionService.java / impl/  # CRUD permisos
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── AccountLockedException.java
│       ├── EmailNotVerifiedException.java
│       ├── InvalidTokenException.java
│       └── ...otros
└── src/main/resources/db/migration/
    ├── V1__init.sql                         # Schema inicial
    ├── V2__add_indices_and_constraints.sql  # Índices y FK
    ├── V3__alter_users_add_app_lockout_email_verification.sql
    └── V4__add_social_refresh_email_tables.sql
```

---

## Modelo de Datos

```
applications ──┬──< roles ──< rolepermissions >── permissions
               │
               └──< users (app_id FK) >──< user_roles >── roles
                        │
                        ├──< login_logs
                        ├──< refresh_tokens
                        ├──< social_accounts
                        ├──< email_verifications
                        └──< passwordresets
```

### Tablas principales

| Tabla | Descripción |
|---|---|
| `applications` | Aplicaciones que usan el auth service (Mantenimiento, Arriendo, Estudio...) |
| `users` | Usuarios, **siempre asociados a una app** (`app_id NOT NULL`) |
| `roles` | Roles por aplicación |
| `permissions` | Permisos por aplicación |
| `rolepermissions` | Permisos asignados a roles |
| `user_roles` | Roles asignados a usuarios por app |
| `login_logs` | Auditoría de accesos (IP, user-agent, timestamp) |
| `social_accounts` | Cuentas Google vinculadas a usuarios |
| `refresh_tokens` | Tokens de refresco con sliding window de 60 min |
| `email_verifications` | Tokens de verificación de email |
| `passwordresets` | Tokens de reset de contraseña |

### Campos clave en `users`

| Campo | Descripción |
|---|---|
| `app_id` | Aplicación a la que pertenece el usuario (obligatorio) |
| `password_hash` | BCrypt — nullable para usuarios solo-Google |
| `is_email_verified` | Login bloqueado hasta verificar |
| `failed_login_attempts` | Contador de intentos fallidos |
| `locked_until` | Timestamp de fin de bloqueo (NULL = no bloqueado) |
| `is_active` | Desactivar sin borrar |

---

## Configuración

### Variables de Entorno

Copia `.env` y completa los valores reales:

```env
# Base de datos
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=auth_db
MYSQL_USER=auth_user
MYSQL_PASSWORD=auth_password
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/auth_db
SPRING_DATASOURCE_USERNAME=auth_user
SPRING_DATASOURCE_PASSWORD=auth_password

# JWT
JWT_ISSUER=https://tu-dominio.com          # en local: http://localhost:8081
JWT_ACCESS_TTL_MINUTES=15                  # duración del access token
JWT_REFRESH_TTL_MINUTES=60                 # inactividad máxima (sliding window)

# CORS (separado por comas)
CORS_ORIGINS=http://localhost:4200,http://localhost:3000

# Lockout
LOCKOUT_MAX_ATTEMPTS=3
LOCKOUT_DURATION_MINUTES=30

# Google OAuth2 (Client ID de Google Cloud Console)
GOOGLE_CLIENT_ID=123456789.apps.googleusercontent.com

# Email (AWS SES SMTP o cualquier SMTP)
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=TU_SES_USERNAME
MAIL_PASSWORD=TU_SES_PASSWORD
MAIL_FROM=noreply@tu-dominio.com
MAIL_VERIFICATION_URL=http://localhost:8081/api/auth/verify-email
MAIL_RESET_URL=http://localhost:3000/reset-password
```

### Claves RSA

Se generan automáticamente en el primer inicio y persisten en:

```
# Local
~/.auth-service/keys/public.key
~/.auth-service/keys/private.key

# Docker (volumen nombrado)
auth_keys:/home/spring/.auth-service/keys
```

> **Importante:** Si se pierden o regeneran las claves, todos los tokens activos quedan inválidos.
> En producción AWS, montar el directorio desde un volumen EFS o usar AWS Secrets Manager.

---

## Ejecución Local

### Opción A — Docker Compose (más fácil, sin instalar nada)

**Requisitos:** Docker Desktop instalado y corriendo.

```bash
# 1. Clonar / abrir el proyecto
cd auth-service

# 2. Levantar MySQL + auth-service (construye la imagen automáticamente)
docker-compose up -d

# 3. Ver logs en tiempo real
docker-compose logs -f auth-service

# 4. Verificar que está corriendo
curl http://localhost:8081/actuator/health
# Respuesta esperada: {"status":"UP"}

# 5. Detener
docker-compose down

# Para borrar también la base de datos (reset completo)
docker-compose down -v
```

### Opción B — Local sin Docker

**Requisitos:** Java 17+, Maven 3.6+, MySQL 8.0 corriendo en `localhost:3306`

```bash
# 1. Crear la base de datos
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS auth_db;"

# 2. Ajustar credenciales en application.properties o exportar variables:
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=tu_password

# 3. Compilar y ejecutar
mvn spring-boot:run

# El servicio arranca en http://localhost:8081
# Flyway ejecuta las migraciones automáticamente
```

---

## Prueba Rápida Local (paso a paso)

Esta guía te permite probar todos los flujos en 5 minutos usando `curl` o Postman.

### Paso 1 — Levantar el servicio

```bash
docker-compose up -d
# Esperar ~15-20 segundos a que MySQL arranque
curl http://localhost:8081/actuator/health
```

### Paso 2 — Crear una aplicación

```bash
curl -X POST http://localhost:8081/api/applications \
  -H "Content-Type: application/json" \
  -d '{"appName": "Mi App", "description": "App de prueba"}'

# Respuesta: { "appId": 1, "appName": "Mi App", ... }
```

### Paso 3 — Registrar un usuario

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "appId": 1,
    "username": "juanito",
    "email": "juanito@test.com",
    "password": "Segura123!",
    "fullName": "Juan Pérez"
  }'

# Respuesta 201: {"message": "Registration successful. Please check your email..."}
# El servicio intentará enviar un email de verificación.
# En local sin SMTP configurado, el registro igual crea el usuario.
```

> **Truco para pruebas locales sin SMTP:** Activa el email directamente en la BD:
> ```sql
> UPDATE users SET is_email_verified = TRUE WHERE username = 'juanito';
> ```

### Paso 4 — Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "juanito", "password": "Segura123!", "appId": 1}'

# Respuesta 200:
# {
#   "accessToken": "eyJhbGci...",
#   "refreshToken": "uuid-uuid",
#   "tokenType": "Bearer",
#   "expiresIn": 900
# }
```

Guarda el `accessToken` y el `refreshToken`.

### Paso 5 — Usar el access token

```bash
export TOKEN="eyJhbGci..."

# Listar usuarios de la app
curl http://localhost:8081/api/users?appId=1 \
  -H "Authorization: Bearer $TOKEN"

# Ver la app
curl http://localhost:8081/api/applications/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Paso 6 — Refrescar el token (sliding window)

Cuando el access token expire (15 min), o en cualquier momento:

```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "uuid-uuid", "appId": 1}'

# Respuesta: nuevo accessToken + nuevo refreshToken
# Cada refresh reinicia el contador de 60 minutos
```

### Paso 7 — Logout

```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "uuid-uuid", "appId": 1}'

# Respuesta: 200 OK
# El refresh token queda revocado en BD
```

### Paso 8 — Crear roles y permisos (opcional)

```bash
# Crear permiso
curl -X POST http://localhost:8081/api/permissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"permissionName": "LEER_REPORTES", "appId": 1}'

# Crear rol
curl -X POST http://localhost:8081/api/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"roleName": "ADMINISTRADOR", "appId": 1}'

# Asignar permiso al rol (roleId=1, permissionId=1)
curl -X POST http://localhost:8081/api/roles/1/permissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"permissionId": 1}'

# Asignar rol al usuario
curl -X POST http://localhost:8081/api/users/1/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"roleId": 1, "appId": 1}'
```

El próximo login incluirá los roles y permisos en el JWT.

### Paso 9 — Probar bloqueo de cuenta

```bash
# Intentar login 3 veces con contraseña incorrecta
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "juanito", "password": "Mal123!", "appId": 1}'
# 401 tres veces → al tercero la cuenta se bloquea 30 minutos

# El 4to intento (incluso con contraseña correcta) retorna:
# 423 Locked: "Account is temporarily locked until 2026-03-07T15:30:00"
```

### Paso 10 — Reset de contraseña

```bash
# Solicitar reset (siempre responde 200 para no revelar si el email existe)
curl -X POST http://localhost:8081/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "juanito@test.com", "appId": 1}'

# Obtener el token desde la BD (para pruebas locales sin email):
# SELECT reset_token FROM passwordresets WHERE is_used = FALSE ORDER BY reset_id DESC LIMIT 1;

curl -X POST http://localhost:8081/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token": "el-token-de-la-bd", "newPassword": "NuevaSegura456!"}'
```

### Verificar el JWKS (para resource servers)

```bash
curl http://localhost:8081/oauth2/jwks
# Retorna la clave pública RSA que los microservicios usan para validar tokens
```

---

## API Endpoints

### Autenticación — públicos (sin token)

| Método | Endpoint | Body | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | `{username, password, appId}` | Login con contraseña |
| `POST` | `/api/auth/google` | `{idToken, appId}` | Login/registro con Google |
| `POST` | `/api/auth/refresh` | `{refreshToken, appId}` | Renovar tokens |
| `POST` | `/api/auth/logout` | `{refreshToken, appId}` | Cerrar sesión |
| `POST` | `/api/auth/register` | `{appId, username, email, password, fullName}` | Auto-registro |
| `GET` | `/api/auth/verify-email?token=...` | — | Verificar email |
| `POST` | `/api/auth/forgot-password` | `{email, appId}` | Solicitar reset |
| `POST` | `/api/auth/reset-password` | `{token, newPassword}` | Resetear contraseña |
| `GET` | `/oauth2/jwks` | — | Clave pública para resource servers |
| `GET` | `/actuator/health` | — | Health check |

### Gestión de Usuarios — requieren `Authorization: Bearer <token>`

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/users?appId=1` | Listar usuarios de una app |
| `GET` | `/api/users/{id}` | Obtener usuario por ID |
| `POST` | `/api/users` | Crear usuario (admin) |
| `PUT` | `/api/users/{id}` | Actualizar usuario |
| `DELETE` | `/api/users/{id}` | Eliminar usuario |
| `POST` | `/api/users/{id}/roles` | Asignar rol `{roleId, appId}` |
| `DELETE` | `/api/users/{userId}/roles/{roleId}/applications/{appId}` | Remover rol |
| `POST` | `/api/users/{id}/change-password` | Cambiar contraseña |
| `PUT` | `/api/users/{id}/activate` | Activar usuario |
| `PUT` | `/api/users/{id}/deactivate` | Desactivar usuario |

### Aplicaciones, Roles y Permisos — requieren token

| Método | Endpoint | Descripción |
|---|---|---|
| `POST/GET/PUT/DELETE` | `/api/applications/{id}` | CRUD de aplicaciones |
| `POST/GET/PUT/DELETE` | `/api/roles/{id}` | CRUD de roles |
| `GET` | `/api/roles/application/{appId}` | Roles de una app |
| `POST` | `/api/roles/{id}/permissions` | Asignar permiso `{permissionId}` |
| `DELETE` | `/api/roles/{roleId}/permissions/{permissionId}` | Remover permiso |
| `POST/GET/PUT/DELETE` | `/api/permissions/{id}` | CRUD de permisos |
| `GET` | `/api/permissions/application/{appId}` | Permisos de una app |

---

## Flujo de Tokens

```
[Login / Google / Register]
          │
          ▼
   access_token (15 min)  +  refresh_token (60 min)
          │
          │  App hace requests con access_token
          │
          ▼  access_token expira
   POST /api/auth/refresh {refreshToken, appId}
          │
          ├── OK (token no expiró y no fue revocado)
          │       ▼
          │   nuevo access_token + nuevo refresh_token (ventana reinicia a 60 min)
          │
          └── FAIL (pasaron 60 min sin refresh)
                  ▼
              401 → Usuario debe hacer login nuevamente
```

**Puntos clave:**
- El refresh token se guarda en BD como SHA-256 hash (nunca en texto plano)
- Cada uso del refresh token lo revoca y emite uno nuevo (rotación automática)
- `POST /logout` revoca el refresh token inmediatamente
- El access token expira solo por tiempo (15 min) — no hay blacklist de access tokens

---

## Google OAuth2 — Integración

El servicio implementa **Option B (Token Exchange)**: el cliente obtiene el `idToken` de Google directamente y lo intercambia aquí por el JWT de la app.

### Flujo completo

```
[Flutter / Angular]  →  Google SDK  →  idToken
        │
        ▼
POST /api/auth/google
{
  "idToken": "eyJhbGci... (de Google)",
  "appId": 1
}
        │
        ▼ auth-service verifica con clave pública de Google (JWKS automático)
        │
        ├── ¿Ya existe social_account para este googleUserId?
        │       ▼ SÍ → cargar usuario
        │
        └── NO → ¿Existe usuario con ese email en esa app?
                    ├── SÍ → vincular cuenta Google al usuario existente
                    └── NO → crear nuevo usuario (email_verified=true, sin password)
        │
        ▼
   Retorna access_token + refresh_token (igual que login normal)
```

### Angular — google-signin

```typescript
import { SocialAuthService, GoogleLoginProvider } from '@abacritt/angularx-social-login';

constructor(private authService: SocialAuthService) {}

async loginWithGoogle(appId: number) {
  const user = await this.authService.signIn(GoogleLoginProvider.PROVIDER_ID);

  const response = await fetch('http://localhost:8081/api/auth/google', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken: user.idToken, appId })
  });

  const tokens = await response.json();
  localStorage.setItem('accessToken', tokens.accessToken);
  localStorage.setItem('refreshToken', tokens.refreshToken);
}
```

### Flutter — google_sign_in

```dart
import 'package:google_sign_in/google_sign_in.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

final _googleSignIn = GoogleSignIn(scopes: ['email', 'profile']);

Future<void> loginWithGoogle(int appId) async {
  final account = await _googleSignIn.signIn();
  if (account == null) return;

  final auth = await account.authentication;
  final idToken = auth.idToken;

  final response = await http.post(
    Uri.parse('http://localhost:8081/api/auth/google'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({'idToken': idToken, 'appId': appId}),
  );

  final tokens = jsonDecode(response.body);
  // Guardar tokens.accessToken y tokens.refreshToken en secure storage
}
```

---

## Integración con Microservicios

Los microservicios Spring Boot validan los tokens emitidos por este servicio sin consultar la BD, usando el JWKS público.

### Dependencia en el microservicio

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### Configuración en `application.properties` del microservicio

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://auth-service:8081/oauth2/jwks
```

### Security config en el microservicio

```java
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

### Leer claims del JWT en un endpoint

```java
@GetMapping("/mi-endpoint")
public ResponseEntity<?> miEndpoint(@AuthenticationPrincipal Jwt jwt) {
    Long userId = jwt.getClaim("userId");
    String username = jwt.getClaim("username");
    List<String> roles = jwt.getClaim("roles");
    List<String> permissions = jwt.getClaim("permissions");
    Long appId = jwt.getClaim("appId");
    return ResponseEntity.ok(Map.of("userId", userId, "roles", roles));
}
```

---

## Integración Angular

### HTTP Interceptor para token automático

```typescript
// auth.interceptor.ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('accessToken');

    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next.handle(req).pipe(
      catchError(error => {
        if (error.status === 401) {
          return this.handle401(req, next);
        }
        return throwError(() => error);
      })
    );
  }

  private handle401(req: HttpRequest<any>, next: HttpHandler) {
    const refreshToken = localStorage.getItem('refreshToken');
    const appId = localStorage.getItem('appId');

    return this.authService.refresh(refreshToken, +appId).pipe(
      switchMap(tokens => {
        localStorage.setItem('accessToken', tokens.accessToken);
        localStorage.setItem('refreshToken', tokens.refreshToken);
        return next.handle(req.clone({
          setHeaders: { Authorization: `Bearer ${tokens.accessToken}` }
        }));
      }),
      catchError(() => {
        this.authService.logout();
        return throwError(() => new Error('Session expired'));
      })
    );
  }
}
```

---

## Integración Flutter

### Almacenamiento seguro + refresh automático

```dart
// Usar flutter_secure_storage para guardar tokens
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

final storage = FlutterSecureStorage();

// Guardar al hacer login
await storage.write(key: 'accessToken', value: tokens['accessToken']);
await storage.write(key: 'refreshToken', value: tokens['refreshToken']);

// Interceptor con dio
class AuthInterceptor extends Interceptor {
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401) {
      final refreshToken = await storage.read(key: 'refreshToken');
      final appId = await storage.read(key: 'appId');

      final response = await Dio().post('http://auth-service/api/auth/refresh',
        data: {'refreshToken': refreshToken, 'appId': int.parse(appId!)});

      await storage.write(key: 'accessToken', value: response.data['accessToken']);
      await storage.write(key: 'refreshToken', value: response.data['refreshToken']);

      // Reintentar la request original
      handler.resolve(await Dio().fetch(err.requestOptions));
      return;
    }
    handler.next(err);
  }
}
```

---

## Despliegue en AWS

### Arquitectura recomendada

```
Internet
    │
    ▼
API Gateway (rate limiting, WAF, HTTPS terminación)
    │
    ▼
ALB (Application Load Balancer)
    │
    ▼
ECS Fargate — auth-service containers
    │              │
    ▼              ▼
RDS MySQL     EFS Volume (RSA keys compartidas entre instancias)
(Multi-AZ)

auth-service → AWS SES (emails)
auth-service → AWS Secrets Manager (credenciales)
```

### Variables de entorno en ECS Task Definition

```json
{
  "SPRING_DATASOURCE_URL": "jdbc:mysql://tu-rds-endpoint:3306/auth_db",
  "SPRING_DATASOURCE_USERNAME": "{{resolve:secretsmanager:auth-db-creds:SecretString:username}}",
  "SPRING_DATASOURCE_PASSWORD": "{{resolve:secretsmanager:auth-db-creds:SecretString:password}}",
  "JWT_ISSUER": "https://auth.tu-dominio.com",
  "CORS_ORIGINS": "https://app.tu-dominio.com,https://admin.tu-dominio.com",
  "GOOGLE_CLIENT_ID": "{{resolve:secretsmanager:google-oauth:SecretString:clientId}}",
  "MAIL_HOST": "email-smtp.us-east-1.amazonaws.com",
  "MAIL_USERNAME": "{{resolve:secretsmanager:ses-creds:SecretString:username}}",
  "MAIL_PASSWORD": "{{resolve:secretsmanager:ses-creds:SecretString:password}}",
  "MAIL_FROM": "noreply@tu-dominio.com",
  "MAIL_VERIFICATION_URL": "https://auth.tu-dominio.com/api/auth/verify-email",
  "MAIL_RESET_URL": "https://app.tu-dominio.com/reset-password"
}
```

### Checklist de producción

- [ ] `.env` en `.gitignore` (nunca commitear credenciales)
- [ ] `JWT_ISSUER` apunta al dominio real (con HTTPS)
- [ ] `CORS_ORIGINS` solo contiene dominios de producción
- [ ] RSA keys en volumen EFS compartido entre instancias
- [ ] RDS Multi-AZ habilitado
- [ ] API Gateway con throttling configurado (rate limiting)
- [ ] WAF con reglas para bloquear IPs sospechosas
- [ ] AWS SES en modo producción (salir de sandbox)
- [ ] HTTPS obligatorio (terminación en ALB)
- [ ] Logs a CloudWatch

---

## Seguridad y JWT

### Claims del token JWT

```json
{
  "sub": "usuario@email.com",
  "userId": 1,
  "username": "juanito",
  "email": "usuario@email.com",
  "appId": 1,
  "appName": "Mi App",
  "roles": ["ADMINISTRADOR"],
  "permissions": ["LEER_REPORTES", "EDITAR_DATOS"],
  "iss": "http://localhost:8081",
  "aud": ["Mi App"],
  "iat": 1709000000,
  "exp": 1709000900
}
```

### Ventajas del contrato JWT

- **Microservicios no consultan la BD** — toda la autorización está en el token
- **Validación rápida** — solo se verifica la firma RSA con la clave pública de `/oauth2/jwks`
- **Frontend dinámico** — muestra/oculta UI según los claims `roles` y `permissions`
- **Stateless** — sin sesiones en servidor

### Algoritmo

- **RS256** — RSA 2048-bit + SHA-256
- Claves generadas con `JwksPersistence` en el primer inicio
- Persistidas en disco para sobrevivir reinicios
- Key ID fijo: `auth-service-rsa-key`

---

## Manejo de Excepciones

Todas las excepciones retornan un `ErrorResponse` estándar:

```json
{
  "timestamp": "2026-03-07T13:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

### Mapa de Excepciones → HTTP Status

| Excepción | Status | Cuándo ocurre |
|---|---|---|
| `BadCredentialsException` | `401` | Usuario/contraseña incorrectos |
| `InvalidTokenException` | `401` | Refresh/reset/verify token inválido o expirado |
| `EmailNotVerifiedException` | `403` | Login antes de verificar email |
| `AccountLockedException` | `423` | Más de 3 intentos fallidos |
| `UserNotFoundException` | `404` | Usuario no existe |
| `RoleNotFoundException` | `404` | Rol no existe |
| `ApplicationNotFoundException` | `404` | App no existe |
| `DuplicateUsernameException` | `409` | Username ya existe en esa app |
| `DuplicateEmailException` | `409` | Email ya existe en esa app |
| `DuplicateRoleAssignmentException` | `409` | Rol ya asignado |
| `InvalidPasswordException` | `400` | Contraseña actual incorrecta |
| `MethodArgumentNotValidException` | `400` | Validación de campos fallida |

---

## Tests

```bash
# Suite completa
mvn test

# Solo unitarios
mvn test -Dtest="*ServiceTest,*ServiceImplTest"

# Solo integración
mvn test -Dtest="*IntegrationTest"
```

Los tests de integración usan H2 in-memory con `MODE=MySQL` y perfil `test`.

> **Nota:** Los tests de integración necesitan actualización tras los cambios de V3/V4
> (users ahora requieren `app_id`). Ver `src/test/resources/db/migration/test/`.
