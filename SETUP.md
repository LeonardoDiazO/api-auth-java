# Setup Guide — api-auth-java

Guía completa para levantar el servicio localmente con Supabase y desplegarlo en producción.

---

## Índice

1. [Pre-requisitos](#1-pre-requisitos)
2. [Crear proyecto en Supabase](#2-crear-proyecto-en-supabase)
3. [Generar claves RSA para JWT](#3-generar-claves-rsa-para-jwt)
4. [Configurar el archivo .env](#4-configurar-el-archivo-env)
5. [Correr localmente con Maven](#5-correr-localmente-con-maven)
6. [Correr localmente con Docker](#6-correr-localmente-con-docker)
7. [Verificación local](#7-verificación-local)
8. [Configurar AWS SES para emails](#8-configurar-aws-ses-para-emails)
9. [Despliegue en producción](#9-despliegue-en-producción)
10. [Verificación en producción](#10-verificación-en-producción)

---

## 1. Pre-requisitos

| Herramienta | Versión mínima | Verificar |
|---|---|---|
| Java (JDK) | 17 | `java -version` |
| Maven | 3.9 | `mvn -version` |
| Docker | cualquiera | `docker -v` |
| OpenSSL | cualquiera | `openssl version` |
| Git | cualquiera | `git -v` |

**Cuentas necesarias:**
- [Supabase](https://supabase.com) — base de datos PostgreSQL (gratis)
- [Mailtrap](https://mailtrap.io) — captura de emails en local (gratis)
- [AWS](https://aws.amazon.com) — SES para emails en producción (opcional para local)

---

## 2. Crear proyecto en Supabase

### 2.1 Crear el proyecto

1. Ve a [supabase.com](https://supabase.com) → **New project**
2. Elige organización, nombre del proyecto, contraseña de BD y región
3. Espera ~2 minutos a que el proyecto esté listo

### 2.2 Obtener las URLs de conexión

Ve a **Settings → Database → Connection string**

Necesitas **dos** URLs según el contexto:

**Direct Connection** (puerto 5432) — para desarrollo local y migraciones Flyway:
```
jdbc:postgresql://db.<PROJECT_REF>.supabase.co:5432/postgres?sslmode=require
```

**Transaction Pooler** (puerto 6543) — para la app en producción (pgBouncer):
```
jdbc:postgresql://aws-0-<region>.pooler.supabase.co:6543/postgres?sslmode=require
```

> La contraseña es la que definiste al crear el proyecto. Puedes resetearla en **Settings → Database → Reset database password**.

### 2.3 Las migraciones (Flyway las ejecuta automáticamente)

El servicio incluye migraciones Flyway en `src/main/resources/db/migration/`. Al iniciar por primera vez, Flyway crea todas las tablas automáticamente. No necesitas ejecutar ningún SQL manualmente.

Si quieres ver el schema completo, las migraciones en orden son:

| Archivo | Qué hace |
|---|---|
| `V1__init.sql` | Tablas base: users, roles, permissions, applications, login_logs, passwordresets |
| `V2__add_indices_and_constraints.sql` | Índices de rendimiento y foreign keys |
| `V3__alter_users_add_app_lockout_email_verification.sql` | Asocia users a applications, agrega lockout y verificación de email |
| `V4__add_social_refresh_email_tables.sql` | social_accounts, refresh_tokens, email_verifications |
| `V5__drop_user_app_settings.sql` | Elimina tabla no utilizada |

---

## 3. Generar claves RSA para JWT

El servicio firma los JWT con RSA-2048. **Solo necesitas hacer esto una vez.**

> Para local puedes saltarte este paso — el servicio genera las claves automáticamente en `~/.auth-service/keys/`. Para producción es **obligatorio** porque el filesystem de los contenedores es efímero.

### En Linux / macOS:

```bash
# 1. Generar clave privada
openssl genrsa -out private.pem 2048

# 2. Exportar a formato DER PKCS8 (que entiende Java)
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in private.pem -out private.der

# 3. Exportar clave pública a DER
openssl rsa -in private.pem -pubout -outform DER -out public.der

# 4. Convertir a Base64 (estos son los valores para las env vars)
JWT_PRIVATE_KEY_BASE64=$(base64 -w 0 private.der)
JWT_PUBLIC_KEY_BASE64=$(base64 -w 0 public.der)

echo "JWT_PRIVATE_KEY_BASE64=$JWT_PRIVATE_KEY_BASE64"
echo "JWT_PUBLIC_KEY_BASE64=$JWT_PUBLIC_KEY_BASE64"

# 5. Eliminar archivos temporales
rm private.pem private.der public.der
```

### En Windows (PowerShell):

```powershell
# 1. Generar clave privada
openssl genrsa -out private.pem 2048

# 2. Exportar a formato DER PKCS8
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in private.pem -out private.der

# 3. Exportar clave pública a DER
openssl rsa -in private.pem -pubout -outform DER -out public.der

# 4. Convertir a Base64
$privateB64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("private.der"))
$publicB64  = [Convert]::ToBase64String([IO.File]::ReadAllBytes("public.der"))

Write-Host "JWT_PRIVATE_KEY_BASE64=$privateB64"
Write-Host "JWT_PUBLIC_KEY_BASE64=$publicB64"

# 5. Eliminar archivos temporales
Remove-Item private.pem, private.der, public.der
```

> **Importante:** Guarda los valores Base64 en un lugar seguro (gestor de secretos). Si los pierdes, todos los JWT en circulación se invalidan y los usuarios tendrán que hacer login de nuevo.

---

## 4. Configurar el archivo .env

```bash
cp .env.example .env
```

Edita `.env` y completa los valores según tu entorno.

### Para desarrollo local (mínimo indispensable):

```env
SPRING_PROFILES_ACTIVE=dev

# Supabase — Direct Connection (puerto 5432)
SPRING_DATASOURCE_URL=jdbc:postgresql://db.<TU_REF>.supabase.co:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<tu-password-supabase>

# RSA keys — dejar vacías localmente (se generan automáticamente en disco)
JWT_PRIVATE_KEY_BASE64=
JWT_PUBLIC_KEY_BASE64=

JWT_ISSUER=http://localhost:8081
CORS_ORIGINS=http://localhost:4200,http://localhost:3000

# Mailtrap (para capturar emails sin enviarlos)
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=<tu-usuario-mailtrap>
MAIL_PASSWORD=<tu-password-mailtrap>
MAIL_FROM=noreply@tu-dominio.com
MAIL_VERIFICATION_URL=http://localhost:8081/api/auth/verify-email
MAIL_RESET_URL=http://localhost:3000/reset-password

# Google OAuth — dejar vacío si no lo necesitas ahora
GOOGLE_CLIENT_ID=
```

### Dónde obtener las credenciales de Mailtrap:
1. Regístrate en [mailtrap.io](https://mailtrap.io)
2. Ve a **Email Testing → Inboxes → tu inbox → SMTP Settings**
3. Copia **Username** y **Password** de la sección "Integrations: SMTP"

---

## 5. Correr localmente con Maven

### Opción A — Cargar .env manualmente (shell):

```bash
# Linux / macOS
export $(grep -v '^#' .env | xargs)
mvn spring-boot:run

# Windows PowerShell
Get-Content .env | Where-Object { $_ -notmatch '^#' -and $_ -ne '' } |
  ForEach-Object { $k,$v = $_ -split '=',2; [System.Environment]::SetEnvironmentVariable($k, $v) }
mvn spring-boot:run
```

### Opción B — Desde IntelliJ IDEA:

1. Abre **Run/Debug Configurations** (icono arriba a la derecha)
2. Selecciona la configuración de Spring Boot
3. En **Environment variables** → haz clic en el icono de archivo 📄
4. Selecciona tu archivo `.env`
5. Run

### Opción C — Con el plugin dotenv de Maven:

Agrega al `pom.xml` dentro de `<build><plugins>`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
</plugin>
```

O simplemente usa Docker (Opción del Paso 6) que es más simple.

---

## 6. Correr localmente con Docker

```bash
# 1. Build de la imagen
docker build -t api-auth-java:local .

# 2. Correr con el archivo .env
docker run --rm \
  --name api-auth-java \
  -p 8081:8081 \
  --env-file .env \
  api-auth-java:local
```

Para correr en background (detached):

```bash
docker run -d \
  --name api-auth-java \
  --restart unless-stopped \
  -p 8081:8081 \
  --env-file .env \
  api-auth-java:local
```

Ver logs:
```bash
docker logs -f api-auth-java
```

Detener:
```bash
docker stop api-auth-java
```

---

## 7. Verificación local

Una vez que el servicio esté corriendo en `http://localhost:8081`:

### 7.1 Health check
```bash
curl http://localhost:8081/actuator/health
# Esperado: {"status":"UP"}
```

### 7.2 Crear una aplicación
```bash
curl -X POST http://localhost:8081/api/applications \
  -H "Content-Type: application/json" \
  -d '{"appName": "Mi App", "description": "App de prueba"}'
# Respuesta: {"appId": 1, "appName": "Mi App", ...}
```

### 7.3 Registrar un usuario
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@ejemplo.com",
    "password": "Test123!",
    "fullName": "Usuario Test",
    "appId": 1
  }'
```

### 7.4 Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!",
    "appId": 1
  }'
# Respuesta: {"access_token": "eyJ...", "refresh_token": "...", "token_type": "Bearer", "expires_in": 900}
```

### 7.5 Usar el access token
```bash
ACCESS_TOKEN="eyJ..."  # pega el token del paso anterior

curl http://localhost:8081/api/users \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 7.6 Verificar emails en Mailtrap
- Ve a [mailtrap.io](https://mailtrap.io) → **Email Testing → Inboxes**
- Deberías ver el email de verificación enviado al registrar el usuario

---

## 8. Configurar AWS SES para emails

> Solo necesario para producción. En local usa Mailtrap (Paso 4).

### 8.1 Verificar tu dominio o email remitente

1. Ve a **AWS Console → SES → Verified identities → Create identity**
2. Elige **Domain** (recomendado) e ingresa `tu-dominio.com`
3. Agrega los registros DNS que SES te indica (TXT para DKIM y SPF)
4. Espera la verificación (puede tomar unos minutos)

### 8.2 Salir del Sandbox (para enviar a cualquier email)

Por defecto SES solo puede enviar a emails verificados (sandbox). Para producción real:

1. Ve a **SES → Account dashboard → Request production access**
2. Completa el formulario explicando tu caso de uso
3. AWS responde en 24-48 horas

### 8.3 Crear credenciales SMTP

1. Ve a **SES → SMTP settings → Create SMTP credentials**
2. Crea un usuario IAM (o usa uno existente)
3. Guarda **SMTP Username** y **SMTP Password** — solo se muestran una vez
4. El host es `email-smtp.<tu-region>.amazonaws.com`, puerto `587`

---

## 9. Despliegue en producción

### Pre-flight: valores del .env para producción

Antes de desplegar, ten listos estos valores (todos `<COMPLETAR>`):

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-<region>.pooler.supabase.co:6543/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<tu-password-supabase>
JWT_PRIVATE_KEY_BASE64=<generado en Paso 3>
JWT_PUBLIC_KEY_BASE64=<generado en Paso 3>
JWT_ISSUER=https://auth.tu-dominio.com
CORS_ORIGINS=https://app.tu-dominio.com
GOOGLE_CLIENT_ID=<tu-client-id>.apps.googleusercontent.com
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=<credenciales SES>
MAIL_PASSWORD=<credenciales SES>
MAIL_FROM=noreply@tu-dominio.com
MAIL_VERIFICATION_URL=https://auth.tu-dominio.com/api/auth/verify-email
MAIL_RESET_URL=https://app.tu-dominio.com/reset-password
```

> Para producción usa Transaction Pooler (puerto 6543) en `SPRING_DATASOURCE_URL`.

---

### Opción A — Railway (la más simple)

1. Ve a [railway.app](https://railway.app) → **New Project → Deploy from GitHub repo**
2. Selecciona el repositorio `api-auth-java`
3. Railway detecta el `Dockerfile` automáticamente
4. En **Variables**, agrega todas las variables de producción una por una
5. En **Settings → Networking → Generate Domain**, obtén tu URL pública
6. Actualiza `JWT_ISSUER` y `MAIL_VERIFICATION_URL` con la URL asignada

### Opción B — Render

1. Ve a [render.com](https://render.com) → **New → Web Service**
2. Conecta el repositorio GitHub → elige tipo **Docker**
3. Selecciona región más cercana
4. En **Environment**, agrega todas las variables
5. Render asigna HTTPS automáticamente (`https://<nombre>.onrender.com`)

### Opción C — VPS (DigitalOcean, Hetzner, Linode, etc.)

```bash
# En el servidor
# 1. Instalar Docker
curl -fsSL https://get.docker.com | sh

# 2. Crear archivo de env vars con permisos restringidos
sudo touch /etc/api-auth-java.env
sudo chmod 600 /etc/api-auth-java.env
sudo nano /etc/api-auth-java.env  # pega las variables de producción

# 3. Pull y run
docker pull ghcr.io/leonardodiazo/api-auth-java:latest

docker run -d \
  --name api-auth-java \
  --restart unless-stopped \
  -p 8081:8081 \
  --env-file /etc/api-auth-java.env \
  ghcr.io/leonardodiazo/api-auth-java:latest

# 4. (Opcional) Nginx como reverse proxy con SSL
# sudo apt install nginx certbot python3-certbot-nginx
# Configurar /etc/nginx/sites-available/auth para proxy_pass a localhost:8081
# certbot --nginx -d auth.tu-dominio.com
```

### Build y push de la imagen Docker

```bash
# Build local
docker build -t ghcr.io/leonardodiazo/api-auth-java:latest .

# Login al registry de GitHub
echo $GITHUB_TOKEN | docker login ghcr.io -u leonardodiazo --password-stdin

# Push
docker push ghcr.io/leonardodiazo/api-auth-java:latest
```

---

## 10. Verificación en producción

```bash
BASE_URL=https://auth.tu-dominio.com

# 1. Health check
curl $BASE_URL/actuator/health
# Esperado: {"status":"UP"}

# 2. Crear aplicación
curl -X POST $BASE_URL/api/applications \
  -H "Content-Type: application/json" \
  -d '{"appName": "Mi App Prod", "description": "Producción"}'

# 3. Registro
curl -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testprod",
    "email": "tu@email.com",
    "password": "Test123!",
    "appId": 1
  }'

# 4. Login
curl -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testprod", "password": "Test123!", "appId": 1}'
# Debe retornar access_token y refresh_token

# 5. Verificar que el email de verificación llegó a tu bandeja real
```

---

## Resumen rápido

```
SUPABASE
  → Crear proyecto
  → Copiar Direct Connection URL (puerto 5432)

RSA KEYS
  → Ejecutar comandos openssl del Paso 3
  → Guardar JWT_PRIVATE_KEY_BASE64 y JWT_PUBLIC_KEY_BASE64

.ENV
  → cp .env.example .env
  → Completar SPRING_DATASOURCE_URL, SPRING_DATASOURCE_PASSWORD
  → Completar MAIL_USERNAME, MAIL_PASSWORD (Mailtrap para local)

LOCAL
  → docker build -t api-auth-java:local .
  → docker run --env-file .env -p 8081:8081 api-auth-java:local
  → curl localhost:8081/actuator/health → {"status":"UP"}

PRODUCCIÓN
  → Cambiar SPRING_PROFILES_ACTIVE=prod
  → Cambiar URL a Transaction Pooler (puerto 6543)
  → Completar JWT_PRIVATE_KEY_BASE64, JWT_PUBLIC_KEY_BASE64
  → Cambiar JWT_ISSUER y CORS_ORIGINS a dominios reales con HTTPS
  → Cambiar MAIL a credenciales AWS SES
  → Deploy en Railway / Render / VPS
```
