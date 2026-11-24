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
| `colaborador-service` | 9093 | `albalab_colaboradores` | Colaboraciones por usuario y CRUD administrador |
| `product-service` | 9095 | `albalab_productos` | CRUD de productos |
| `webpay-service` | 9094 | `albalab_webpay` | Webpay Plus (crear, confirmar, estado y reembolso) |
| `historial-service` | 9096 | `albalab_historial` | Historial de compras por usuario autenticado |

Las bases de datos se crean automaticamente con
`createDatabaseIfNotExist=true` en el MySQL local configurado con las variables
`DB_*` (por defecto `localhost:3306`).

`usuario-service`, `admin-service`, `colaborador-service` y `product-service`
aplican su esquema con Liquibase al arrancar. Esos servicios usan
`spring.jpa.hibernate.ddl-auto=none`; los cambios de tablas se agregan en sus
respectivos `src/main/resources/db/changelog/db.changelog-master.sql`.

## Estructura

```text
albalab-backend/
├── api-gateway/
├── usuario-service/
├── admin-service/
├── colaborador-service/
├── product-service/
├── webpay-service/
├── historial-service/
├── configuracion-maven/
│   ├── settings.xml
│   └── maven-wrapper.properties
├── actualizar-maven.bat
├── build-all.bat
├── clean-all.bat
├── test-all.bat
└── README.md
```

Cada microservicio es un proyecto Spring Boot independiente. No hay un `pom.xml`
padre que los una: se pueden abrir, compilar y desplegar por separado.

## Requisitos

- JDK 21 o superior.
- MySQL 8 o MariaDB 10.4+ en `localhost:3306` (o define las variables `DB_*`).
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

## Pruebas automatizadas

La suite incluye pruebas de controlador con `@WebMvcTest`, `MockMvc` y servicios
simulados con `@MockitoBean`, pruebas de validacion y mapeo de DTO con Jakarta
Validation y AssertJ, y pruebas unitarias de la capa de servicio con Mockito.
No necesitan MySQL ni tener los servicios levantados.

Ejecutar las pruebas de un solo servicio:

```bat
cd usuario-service
.\mvnw.cmd test -s ..\configuracion-maven\settings.xml
```

Cambia `usuario-service` por `admin-service`, `colaborador-service`,
`product-service`, `webpay-service`, `historial-service` o `api-gateway`.

Ejecutar todas las pruebas:

```bat
test-all.bat
```

Tambien se puede ejecutar una clase de prueba especifica:

```bat
cd usuario-service
.\mvnw.cmd test -Dtest=UsuarioServiceTest -s ..\configuracion-maven\settings.xml
```

Cobertura de codigo con JaCoCo:

Cada ejecucion de `test` genera `target/jacoco.exec` y el reporte HTML en
`target/site/jacoco/index.html`. Para verlo:

```bat
cd usuario-service
.\mvnw.cmd test -s ..\configuracion-maven\settings.xml
start target\site\jacoco\index.html
```

Con `test-all.bat` se genera un reporte independiente en cada microservicio.

`api-gateway` no tiene controladores, DTO ni capa de servicio; su prueba verifica
que el contexto de Spring cargue correctamente. En los demas servicios las
pruebas cubren los endpoints principales, la validacion y el mapeo de DTO, y
las reglas de negocio de la capa de servicio sin levantar la base de datos.

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
cd product-service
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
| `PRODUCT_SERVICE_URI` | `http://localhost:9095` | Ruta del gateway al servicio |
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

El token de usuario incluye el claim `usuarioId` ademas de `sub` (correo),
`roles` y `scope`. `colaborador-service` usa `usuarioId` y el correo para
asociar y filtrar las colaboraciones del usuario autenticado.

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

## Usuarios y datos de prueba

Las siguientes cuentas quedan disponibles para probar el backend:

| Correo | Contrasena | Rol | Colaboraciones iniciales |
| --- | --- | --- | --- |
| `admin@albalab.cl` | `Admin1234` | ADMIN | Sin colaboraciones |
| `felipe@gmai.com` | `hola1234` | USER | 5 |
| `matias@gmail.com` | `hola1234` | USER | 2 |

> `felipe@gmai.com` esta escrito sin la segunda `l` antes de `.com`, tal como
> fue solicitado.

Las cuentas `felipe@gmai.com` y `matias@gmail.com` se insertan mediante
Liquibase en `usuario-service` y sus colaboraciones en `colaborador-service`:

| Usuario | Colaboraciones | Total |
| --- | --- | ---: |
| Felipe | 3 de $5.000 + 2 de $2.500 | $20.000 |
| Matias | 1 de $10.000 + 1 de $4.000 | $14.000 |

Todas las colaboraciones de prueba tienen estado `APROBADA`, son visibles y no
son anonimas. Para comprobarlas, iniciar sesion con una de las cuentas y
consultar `GET /api/colaboradores/mios`.

En una base local que ya se haya usado puede existir ademas el usuario
`prueba.app@albalab.cl` / `Usuario1234`; ese usuario no se crea por Liquibase.

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
| GET | `/api/colaboradores` | ADMIN |
| GET | `/api/colaboradores/admin/todos` | ADMIN |
| GET | `/api/colaboradores/mios` | Autenticado |
| GET | `/api/colaboradores/publicos` | Publico |
| GET | `/api/colaboradores/{id}` | Autenticado (dueno o ADMIN) |
| POST | `/api/colaboradores` | Autenticado |
| PUT | `/api/colaboradores/{id}` | ADMIN |
| DELETE | `/api/colaboradores/{id}` | ADMIN |

El backend obtiene el dueno de la colaboracion desde el JWT. El cliente no debe
enviar `usuarioId` ni `usuarioEmail` en el cuerpo.

Ejemplo:

```bash
curl -X POST http://localhost:9090/api/colaboradores \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":"Fundacion Albalab",
    "fotoUri":"https://...",
    "montoDonacion":10000,
    "esAnonimo":false,
    "webpayOrden":"DON-1001"
  }'
```

`webpayOrden` es opcional, pero se recomienda enviarlo para evitar duplicados.
Si ya existe una colaboracion con esa orden y pertenece al mismo usuario, el
backend devuelve la existente. Si pertenece a otro usuario, responde `409`.

La lista `mios` devuelve solo las colaboraciones del usuario autenticado. La
lista `publicos` no requiere token y oculta el nombre, la foto y el monto de las
colaboraciones anonimas.

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
- El gateway valida el token y aplica la regla de rol para `/api/admin/**` y
  `/api/colaboradores/admin/**`.
- Cada microservicio tambien valida el token: no depende solo del gateway.
- `/api/colaboradores/mios` filtra por el usuario del JWT. Un usuario no puede
  leer ni modificar colaboraciones de otro.
- `/api/colaboradores/publicos` es publico y solo devuelve colaboraciones
  aprobadas con los datos anonimos protegidos.
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
- `albalab_productos`
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

```bat
cd product-service
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
- Felipe: `felipe@gmai.com` / `hola1234`
- Matias: `matias@gmail.com` / `hola1234`
- Usuario local opcional: `prueba.app@albalab.cl` / `Usuario1234` (solo si ya existe en la base)

Las colaboraciones semilla de Felipe y Matias estan detalladas en
[Usuarios y datos de prueba](#usuarios-y-datos-de-prueba).

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
- `9095`

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
| product-service | 9095 |

### 12. Notas importantes

- `JWT_SECRET` debe ser igual en todos los servicios.
- En produccion cambiar `JWT_SECRET`, la contrasena del administrador inicial
  y restringir CORS.
- Para Webpay real, definir `WEBPAY_COMMERCE_CODE`, `WEBPAY_API_KEY` y
  `WEBPAY_ENVIRONMENT=LIVE`.
- El administrador inicial solo se inserta si el correo no existe.


### 13. Emergencia de puertos ocupados

netstat -ano | findstr "9090" //buscamos el puerto esta ocupado
taskkill /PID 26208 /F  //matamos el puerto

### 14. tarjeta a ocupar

Número: 3700 0000 0002 032
CVV: 1234
Fecha: cualquier fecha futura (ej. 12/30)

11.111.111-1 / 123

### 15. Ejecutar test

.\test-all.bat

cd usuario-service
cd target\site\jacoco
python -m http.server 8000

http://localhost:8000

