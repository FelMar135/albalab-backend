# Albalab Backend - Microservicios Spring Boot

Este repositorio contiene la version modular del backend de Albalab. El proyecto
monolitico original fue separado en microservicios Spring Boot independientes,
cada uno con su propio `pom.xml`, su base de datos y su wrapper de Maven.

## Arquitectura

| Servicio | Puerto | Base de datos | Responsabilidad |
| --- | ---: | --- | --- |
| `api-gateway` | 9090 | - | Punto de entrada unico, validacion JWT y enrutamiento |
| `usuario-service` | 9091 | `albalab_usuarios` | Registro, login y gestion de usuarios |
| `admin-service` | 9092 | `albalab_admin` | Administradores con correo y contrasena BCrypt |
| `colaborador-service` | 9093 | `albalab_colaboradores` | CRUD de colaboradores |
| `webpay-service` | 9094 | `albalab_webpay` | Webpay Plus (crear, confirmar, estado y reembolso) |

Las bases de datos se crean automaticamente con
`createDatabaseIfNotExist=true` en el MySQL local configurado con las variables
`DB_*` (por defecto `localhost:3306`).

## Estructura

```text
albalab-backend/
├── api-gateway/
├── usuario-service/
├── admin-service/
├── colaborador-service/
├── webpay-service/
├── configuracion-maven/
│   ├── settings.xml
│   └── maven-wrapper.properties
├── actualizar-maven.bat
├── build-all.bat
├── clean-all.bat
└── README.md
```

Cada microservicio es un proyecto Spring Boot independiente. No hay un `pom.xml`
padre que los una: se pueden abrir, compilar y desplegar por separado.

## Requisitos

- JDK 21 o superior.
- MySQL 8 en `localhost:3306` (o define las variables `DB_*`).
- No es necesario instalar Maven: cada servicio incluye su propio `mvnw`.

## Maven especial del proyecto de referencia

Se copio la configuracion de Maven del proyecto de referencia:

1. `configuracion-maven/settings.xml` usa el mirror de Google para Maven Central:
   `https://maven-central.storage-download.googleapis.com/maven2`.
2. `configuracion-maven/maven-wrapper.properties` descarga Maven `3.9.14` desde
   ese mismo mirror y usa `distributionType=only-script`.
3. Cada servicio tiene su propio `mvnw`, `mvnw.cmd` y
   `.mvn/wrapper/maven-wrapper.properties` con esa configuracion.
4. `actualizar-maven.bat` copia la configuracion del wrapper a todos los
   proyectos que tengan un `pom.xml`.
5. `build-all.bat` y `clean-all.bat` invocan el wrapper con
   `-s ..\configuracion-maven\settings.xml`.

Compilacion manual de un solo servicio:

```bat
cd usuario-service
.\mvnw.cmd clean package -DskipTests -s ..\configuracion-maven\settings.xml
```

Compilacion de todos:

```bat
build-all.bat
```

## Ejecucion local

1. Inicia un MySQL local en `localhost:3306` con usuario `root` y contrasena
   vacia (o define las variables `DB_*`).
2. Ejecuta cada servicio en una terminal distinta:

```bat
cd usuario-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd admin-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd colaborador-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd webpay-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd api-gateway
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

Verifica el gateway:

```text
http://localhost:9090/actuator/health
```

## Variables de entorno

| Variable | Valor por defecto | Uso |
| --- | --- | --- |
| `SERVER_PORT` | Puerto de cada servicio | Puerto HTTP |
| `DB_HOST` | `localhost` | Host de MySQL |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_NAME` | Nombre propio del servicio | Base de datos |
| `DB_USERNAME` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | vacio | Contrasena de MySQL |
| `JWT_SECRET` | `esta_es_mi_compleja_clave_secreta` | Secreto compartido para firmar y validar JWT |
| `JWT_EXPIRATION_HOURS` | `8` | Duracion del token |
| `WEBPAY_COMMERCE_CODE` | vacio (TEST) | Codigo de comercio Webpay |
| `WEBPAY_API_KEY` | vacio (TEST) | API Key Webpay |
| `WEBPAY_ENVIRONMENT` | `TEST` | `TEST` o `LIVE` |
| `USUARIO_SERVICE_URI` | `http://localhost:9091` | Ruta del gateway al servicio |
| `ADMIN_SERVICE_URI` | `http://localhost:9092` | Ruta del gateway al servicio |
| `COLABORADOR_SERVICE_URI` | `http://localhost:9093` | Ruta del gateway al servicio |
| `WEBPAY_SERVICE_URI` | `http://localhost:9094` | Ruta del gateway al servicio |

Los servicios se comunican entre si por `localhost` usando los puertos por
defecto. El gateway enruta a cada servicio con las variables `*_SERVICE_URI`.

## Autenticacion JWT

Los servicios `usuario-service` y `admin-service` emiten tokens JWT firmados con
HMAC-SHA256. El gateway y los demas servicios validan el mismo token.

### Usuario

Registro:

```bash
curl -X POST http://localhost:9090/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@albalab.cl","password":"Usuario1234"}'
```

Login:

```bash
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@albalab.cl","password":"Usuario1234"}'
```

Respuesta:

```json
{
  "status": "ok",
  "token": "eyJ...",
  "role": "USER",
  "email": "usuario@albalab.cl",
  "expiresInSeconds": 28800
}
```

### Administrador

El administrador inicial se inserta mediante Liquibase al arrancar
`admin-service`, desde el changeset `albalab:2` en
`src/main/resources/db/changelog/db.changelog-master.sql`:

```text
correo: admin@albalab.cl
contrasena: Admin1234
```

En la base de datos la contrasena queda guardada como hash BCrypt. Para
cambiarla, inicia sesion y usa `PUT /api/admin/{id}` con un token ADMIN. El
changeset solo inserta el administrador si el correo no existe, por lo que no
sobreescribe cambios posteriores.

Login:

```bash
curl -X POST http://localhost:9090/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@albalab.cl","password":"Admin1234"}'
```

Las rutas `/api/admin/**` requieren un token con rol `ADMIN`. El gateway exige
`SCOPE_ADMIN` y el propio `admin-service` exige `ROLE_ADMIN` (defensa en
profundidad).

## Endpoints principales

Todas las rutas se consumen a traves del gateway en `http://localhost:9090`.

### Usuarios

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/auth/registro` | Publico |
| POST | `/api/auth/login` | Publico |
| GET | `/api/usuarios` | Autenticado |
| GET | `/api/usuarios/me` | Autenticado |
| GET | `/api/usuarios/{id}` | Autenticado |
| PUT | `/api/usuarios/{id}` | Autenticado |
| DELETE | `/api/usuarios/{id}` | Autenticado |

### Administradores

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/admin/auth/login` | Publico |
| POST | `/api/admin/auth/registro` | ADMIN |
| GET | `/api/admin` | ADMIN |
| GET | `/api/admin/me` | ADMIN |
| GET | `/api/admin/{id}` | ADMIN |
| POST | `/api/admin` | ADMIN |
| PUT | `/api/admin/{id}` | ADMIN |
| DELETE | `/api/admin/{id}` | ADMIN |

### Colaboradores

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/colaboradores` | Autenticado |
| GET | `/api/colaboradores/{id}` | Autenticado |
| POST | `/api/colaboradores` | Autenticado |
| PUT | `/api/colaboradores/{id}` | Autenticado |
| DELETE | `/api/colaboradores/{id}` | Autenticado |

Ejemplo:

```bash
curl -X POST http://localhost:9090/api/colaboradores \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Fundacion Albalab","fotoUri":"https://...","montoDonacion":10000}'
```

### Webpay

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/webpay/crear` | Autenticado |
| POST | `/api/webpay/confirmar` | Autenticado |
| GET | `/api/webpay/estado/{token}` | Autenticado |
| POST | `/api/webpay/reembolsar` | Autenticado |
| POST/GET | `/api/webpay/retorno` | Publico (Transbank) |
| GET | `/api/webpay` | Autenticado |

## Flujo Webpay Plus

1. Obtener un token con el login de usuario.
2. Crear la transaccion:

```bash
curl -X POST http://localhost:9090/api/webpay/crear \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "ordenCompra":"ORD-1001",
    "idSesion":"SES-1001",
    "monto":10000,
    "urlRetorno":"http://localhost:9090/api/webpay/retorno"
  }'
```

3. La respuesta incluye `token` y `url`. Abre `url` en el navegador para pagar.
4. Transbank redirige a `urlRetorno` con `token_ws`; `webpay-service` confirma la
   transaccion automaticamente y responde con el estado final.
5. Tambien se puede confirmar manualmente con
   `POST /api/webpay/confirmar` enviando `{"token":"..."}`.

Con las variables `WEBPAY_COMMERCE_CODE` y `WEBPAY_API_KEY` vacias se usan las
credenciales publicas de integracion de Transbank (`TEST`). Para produccion hay
que definir el codigo de comercio, la API key y `WEBPAY_ENVIRONMENT=LIVE`.

> Para una app movil, el `urlRetorno` debe ser una URL publica accesible por
> Transbank. Si se necesita volver a un deep link de la app despues de confirmar,
> se puede agregar una redireccion en el controlador `/retorno` manteniendo la
> confirmacion en el backend.

## Seguridad

- Las contrasenas se almacenan con `BCryptPasswordEncoder`.
- Los JWT se firman con HMAC-SHA256. La clave se deriva con SHA-256 del valor de
  `JWT_SECRET`, por lo que todos los servicios deben usar exactamente el mismo
  secreto.
- El gateway valida el token y aplica la regla de rol para `/api/admin/**`.
- Cada microservicio tambien valida el token: no depende solo del gateway.
- `/api/webpay/retorno` es publico porque Transbank debe poder invocarlo sin JWT.
- En produccion cambia `JWT_SECRET`, cambia la contrasena del administrador
  inicial y restringe CORS a los origenes reales de la app.
- El administrador inicial solo se inserta si el correo no existe. No se
  sobreescribe una contrasena ya cambiada.

## Ejecucion en los computadores del Duoc

Esta seccion resume los pasos para levantar todo el backend en un equipo de
Duoc, incluyendo el Maven especial del proyecto.

### 1. Requisitos

- JDK 21 o superior.
- MySQL 8 / MariaDB en `localhost:3306`.
- Usuario `root` y contrasena vacia, o definir las variables `DB_*`.
- No es necesario instalar Maven global: cada servicio incluye `mvnw` / `mvnw.cmd`.

### 2. Copiar el proyecto

Copiar la carpeta completa `albalab-backend`. La configuracion especial de Maven
esta en:

```text
configuracion-maven/settings.xml
configuracion-maven/maven-wrapper.properties
```

### 3. Actualizar el wrapper de Maven

```bat
cd albalab-backend
actualizar-maven.bat
```

Este comando copia la configuracion del wrapper a todos los microservicios que
tengan un `pom.xml`.

### 4. Compilar todos los servicios

```bat
cd albalab-backend
build-all.bat
```

El script usa la configuracion especial:

```bat
-s ..\configuracion-maven\settings.xml
```

y descarga Maven `3.9.14` desde el mirror configurado. Si falla porque los
JARs estan en uso, detener los procesos Java y volver a ejecutar.

Compilacion manual de un solo servicio:

```bat
cd usuario-service
.\mvnw.cmd clean package -DskipTests -s ..\configuracion-maven\settings.xml
```

### 5. Iniciar MySQL

Iniciar MySQL/MariaDB en `localhost:3306`. Las bases se crean automaticamente:

- `albalab_usuarios`
- `albalab_admin`
- `albalab_colaboradores`
- `albalab_webpay`

### 6. Levantar cada microservicio

Ejecutar cada servicio en una terminal distinta:

```bat
cd api-gateway
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd usuario-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd admin-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd colaborador-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

```bat
cd webpay-service
.\mvnw.cmd spring-boot:run -s ..\configuracion-maven\settings.xml
```

### 7. Verificar el backend

Abrir en el navegador:

```text
http://localhost:9090/actuator/health
```

Debe responder:

```json
{"status":"UP"}
```

### 8. Credenciales iniciales

- Administrador: `admin@albalab.cl` / `Admin1234`
- Usuario de prueba: `prueba.app@albalab.cl` / `Usuario1234`

### 9. Conectar la app movil

Obtener la IPv4 del PC:

```bat
ipconfig
```

En `RetrofitClient.kt` de la app, usar esa IP:

```kotlin
private const val HOST = "IP_DEL_PC_DUOC"
```

Luego recompilar el APK. El telefono y el PC deben estar en la misma red.
Tambien se deben permitir en el firewall los puertos:

- `9090`
- `9091`
- `9092`
- `9093`
- `9094`

### 10. Maven especial del proyecto

El proyecto usa una configuracion de Maven copiada del proyecto de referencia:

1. `configuracion-maven/settings.xml` usa el mirror de Google para Maven
   Central:
   `https://maven-central.storage-download.googleapis.com/maven2`.
2. `configuracion-maven/maven-wrapper.properties` descarga Maven `3.9.14`
   desde ese mismo mirror y usa `distributionType=only-script`.
3. Cada servicio tiene su propio `mvnw`, `mvnw.cmd` y
   `.mvn/wrapper/maven-wrapper.properties` con esa configuracion.
4. `actualizar-maven.bat` copia la configuracion del wrapper a todos los
   proyectos que tengan un `pom.xml`.
5. `build-all.bat` y `clean-all.bat` invocan el wrapper con
   `-s ..\configuracion-maven\settings.xml`.

Si en Duoc no hay acceso a Maven Central, usar la configuracion ya incluida
en `configuracion-maven/settings.xml`. Si tampoco funciona, copiar la carpeta
`.m2` con las dependencias ya descargadas.

### 11. Puertos

| Servicio | Puerto |
| --- | ---: |
| API Gateway | 9090 |
| usuario-service | 9091 |
| admin-service | 9092 |
| colaborador-service | 9093 |
| webpay-service | 9094 |

### 12. Notas importantes

- `JWT_SECRET` debe ser igual en todos los servicios.
- En produccion cambiar `JWT_SECRET`, la contrasena del administrador inicial
  y restringir CORS.
- Para Webpay real, definir `WEBPAY_COMMERCE_CODE`, `WEBPAY_API_KEY` y
  `WEBPAY_ENVIRONMENT=LIVE`.
- El administrador inicial solo se inserta si el correo no existe.
