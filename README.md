# TíoTec Preventivo

DSY1105 Desarrollo de Aplicaciones Móviles. Experiencia de Aprendizaje 3.

Equipo 3: Bárbara Tolorza Escobar y Christopher Jared Villa Vera.

## Proyecto

TíoTec permite registrar un perfil y un dispositivo ficticios para revisar cuatro hábitos: respaldo, actualizaciones, autenticación y recuperación de la cuenta. Las respuestas generan un plan de acciones que se puede consultar y actualizar.

La aplicación tiene cuatro secciones:

- Evaluar: formulario con validación, preguntas y foto del dispositivo.
- Historial: evaluaciones guardadas con Room, edición, eliminación y avance del plan.
- Servidor: crear, consultar, editar y eliminar evaluaciones mediante un microservicio Spring Boot.
- Mi sistema: consultar el soporte de versiones de Android y Windows mediante la API externa endoflife.date.

La interfaz utiliza Jetpack Compose y Material 3. Los ViewModel mantienen el estado con StateFlow y los repositorios acceden a Room o a los servicios con Retrofit.

## Organización del código

El código Android está en `app/src/main/java/com/example/tiotec`.

| Carpeta | Contenido |
|---|---|
| `data/model` | Datos de los formularios y respuestas de las APIs |
| `data/remote` | `ApiService.kt` con las interfaces HTTP y `RetrofitInstance.kt` con las conexiones |
| `repository` | Acceso a las evaluaciones y al catálogo de soporte |
| `viewmodel` | Estado de las pantallas y operaciones de la app |
| `ui/screens` | Pantallas de Compose |
| `data` | Base de datos Room y almacenamiento de fotos |
| `validation` | Validación del formulario local |

El backend está en `backend/src/main/java/cl/tiotec`. `RevisionesApplication.java` inicia Spring Boot. En `revision` están el controlador HTTP, el servicio, el repositorio JPA, la entidad y los datos de entrada y salida.

La sección Servidor utiliza `ServidorScreen.kt`, `ServidorViewModel.kt` y `ServidorRepository.kt`. El CRUD local de Historial conserva su propio repositorio.

## Endpoints

| Servicio | Método | Ruta | Uso |
|---|---|---|---|
| Spring Boot | GET | `/api/salud` | Comprobar que el servidor responde |
| Spring Boot | GET | `/api/revisiones` | Listar evaluaciones |
| Spring Boot | GET | `/api/revisiones/{id}` | Consultar una evaluación |
| Spring Boot | POST | `/api/revisiones` | Crear una evaluación |
| Spring Boot | PUT | `/api/revisiones/{id}` | Editar una evaluación |
| Spring Boot | DELETE | `/api/revisiones/{id}` | Eliminar una evaluación |
| endoflife.date | GET | `https://endoflife.date/api/v1/products/android/` | Obtener versiones de Android |
| endoflife.date | GET | `https://endoflife.date/api/v1/products/windows/` | Obtener versiones de Windows |

Las solicitudes de creación y edición incluyen nombre, dispositivo, sistema, experiencia, plazo, cuatro respuestas y avance de acciones. El servidor valida los campos y devuelve el registro con su identificador y fecha.

La API externa entrega un catálogo. Después de descargarlo, la persona elige una versión y ve su estado de soporte y las fechas disponibles. Los valores que la fuente no informa se muestran como desconocidos.

## Cómo ejecutar

Se utiliza Java 21 para Gradle, Android SDK 35 y un emulador o teléfono con Android 13 / API 33 o superior. Para compilar el backend se necesita Maven. La primera sincronización requiere internet.

1. Extraer el ZIP y abrir en Android Studio la carpeta extraída que contiene `settings.gradle.kts` y `app`.
2. Sincronizar Gradle y comprobar que Android Studio tiene configurado el SDK del computador. `local.properties` se genera en cada equipo.
3. Abrir `backend` como proyecto Maven en el IDE y ejecutar `RevisionesApplication`. También puede iniciarse desde una terminal en `backend`:

```powershell
mvn spring-boot:run
```

Para generar y ejecutar el JAR desde `backend`:

```powershell
mvn package
java -jar target/revisiones-1.1.0.jar
```

4. Ejecutar el módulo `app` desde Android Studio, o instalar `entrega/TioTec-firmado.apk`.
5. Probar una evaluación local, crear y editar una evaluación en Servidor y consultar una versión en Mi sistema.

En un emulador Android estándar, el backend se consulta en `http://10.0.2.2:8080/`. Para un teléfono conectado por USB se utiliza `http://127.0.0.1:8080/` y debe configurarse la conexión con:

```powershell
adb reverse tcp:8080 tcp:8080
```

Mi sistema necesita acceso a internet. El backend debe permanecer iniciado para trabajar en Servidor.

## Almacenamiento

Room guarda el historial local y las rutas de las fotos. Las imágenes se copian al almacenamiento privado de la app.

El backend utiliza H2 en archivo. Al iniciarlo desde `backend`, la base queda en `backend/datos/revisiones.mv.db` y conserva los datos al reiniciar. Las evaluaciones del servidor y el historial local son registros separados.

## Pruebas

Las pruebas de lógica están en `app/src/test/java` y las de pantalla en `app/src/androidTest/java`, como indica la guía 15.

- Kotest y MockK prueban el ViewModel del servidor y los repositorios, simulando sus dependencias.
- `kotlinx-coroutines-test` permite controlar las operaciones asíncronas.
- Las pruebas JUnit 4 existentes se ejecutan en JUnit Platform mediante Vintage.
- Las pruebas Compose utilizan `createAndroidComposeRule`, como el ejemplo de pantalla de la guía.
- El backend tiene pruebas de integración con MockMvc y una base H2 en memoria.

En la copia local de la entrega, las cinco pruebas de pantalla se ejecutaron correctamente en el emulador API 37 el 25 de noviembre de 2026. Se utiliza Espresso 3.7.0 para corregir la compatibilidad de la herramienta de pruebas con Android reciente. La salida real está en `entrega/evidencias/pruebas-pantalla.txt`.

Desde la raíz del proyecto Android:

```powershell
.\gradlew.bat testDebugUnitTest coberturaLogica
.\gradlew.bat connectedDebugAndroidTest
```

La primera tarea genera el informe JaCoCo en `app/build/reports/jacoco/coberturaLogica` y verifica un mínimo de 80% de cobertura de líneas en ViewModel y Repository. Las pruebas de pantalla necesitan un dispositivo conectado.

Desde `backend`:

```powershell
mvn test
```

Los resultados comprobados y las verificaciones pendientes se registran en `entrega/evidencias/VERIFICACION.txt`. Una prueba con una API simulada no comprueba la conectividad real del teléfono.

## APK firmado

La configuración `Grupo3` está en `app/build.gradle.kts`. Lee los datos de `firma.properties` y utiliza la llave `firma/Grupo3.jks`, con alias `tiotec`.

Para generar el APK desde Android Studio:

1. Abrir Build > Generate Signed App Bundle / APK.
2. Elegir APK.
3. Seleccionar `firma/Grupo3.jks`, ingresar su contraseña y el alias `tiotec`.
4. Elegir la variante release y generar el archivo.

También puede compilarse con:

```powershell
.\gradlew.bat assembleRelease
```

El APK de entrega está en `entrega/TioTec-firmado.apk`. `entrega/evidencias/firma-apk.txt` contiene la verificación de su certificado. La llave y su configuración se incluyen en la entrega privada.

La pauta pide una captura del APK y del JKS. Falta adjuntar la captura real antes de entregar.

## Trabajo en equipo

Repositorio: https://github.com/Bartolo18/TioTec-Preventivo-Equipo

Trello: https://trello.com/b/e0OPKumv/tiotec-parciales-2-y-3

La entrega debe incluir las evidencias reales de tareas y aportes de cada integrante. Falta comprobar esas evidencias y adjuntar las capturas correspondientes.
