# TíoTec Preventivo

Proyecto DSY1105, equipo 3. Integrantes: Bárbara Tolorza Escobar y Christopher Jared Villa Vera.

La app permite evaluar cuatro hábitos digitales de un perfil y equipo ficticios: respaldo, actualizaciones, autenticación y recuperación. Genera acciones priorizadas y registra su avance.

## Funcionalidades

- **Evaluar e Historial:** formulario validado, cámara/galería, plan, edición y eliminación. Room guarda evaluaciones y rutas; las fotos quedan en archivos privados.
- **Nube:** CRUD mediante Retrofit y un microservicio Spring Boot con H2 persistente.
- **Avisos:** API pública GitHub Advisory para bibliotecas Java/Android del ecosistema Maven, con gravedad, fecha y enlace.
- Material 3, Navigation Compose y ViewModel con StateFlow, estados de carga y errores recuperables.
- Pruebas de lógica, pruebas de integración del backend y cobertura JaCoCo.
- Configuración release, llave académica privada y APK firmado.

## Cómo ejecutar

1. Abrir este directorio en Android Studio, con Java 21, Android SDK 35 y un emulador API 33 o superior.
2. Abrir **Iniciar_servidor.cmd** y mantener la ventana abierta. Si falta el JAR, lo compila con Maven instalado.
3. Instalar **entrega/TioTec-firmado.apk**. **Conectar_dispositivo.cmd** permite instalarlo y abrirlo.
4. En Nube, pulsar Actualizar. Crear, editar y eliminar un caso ficticio. Avisos necesita internet.

Paquete de esta entrega: `com.example.tiotec.entrega`.

El emulador Android estándar accede al PC mediante `http://10.0.2.2:8080/`. En teléfono físico por USB se utiliza `http://127.0.0.1:8080/` con `adb reverse tcp:8080 tcp:8080`. El servidor escucha solo en el PC local. No hay una nube pública publicada.

La base de datos queda en `backend/datos` y persiste al reiniciar. Los tests usan otra base en memoria.

Comandos desde backend (Maven 3.6.3 o superior):

```powershell
mvn test
mvn package
java -jar target/revisiones-1.1.0.jar
```

Comandos desde la raíz del proyecto Android:

```powershell
.\gradlew.bat testDebugUnitTest coberturaLogica
.\gradlew.bat assembleRelease
```

## Endpoints

| Origen | Método y ruta |
|---|---|
| Microservicio | GET /api/salud |
| Microservicio | GET y POST /api/revisiones |
| Microservicio | GET, PUT y DELETE /api/revisiones/{id} |
| API externa | GET https://api.github.com/advisories?per_page=6&ecosystem=maven |

[Documentación técnica, arquitectura, reglas y límites](docs/Arquitectura.md).

## Pruebas

Fuentes en `app/src/test` y `backend/src/test`. JaCoCo mide model, validation, repository y viewmodel. Reporte: `app/build/reports/jacoco/coberturaLogica`. La cámara y la interfaz requieren comprobación del APK.

Los resultados efectivamente verificados se detallan en `docs/Resultado_verificacion.md`, con evidencia en `entrega/evidencias`.

## Firma

Se utiliza un KeyStore JKS RSA de 3072 bits, alias `tiotec`. Gradle lee `firma.properties`. La llave y contraseñas están en la entrega privada y se excluyen de GitHub.

En Android Studio: Build > Generate Signed App Bundle / APK > APK > seleccionar el KeyStore, alias y contraseñas > release. Mantener la misma llave para actualizar instalaciones firmadas con ella.

El APK y la verificación del certificado quedan en `entrega`. Un clon público puede compilar debug; para release necesita una llave propia o la llave académica privada.

## Colaboración y defensa

Repositorio: https://github.com/Bartolo18/TioTec-Preventivo-Equipo

Tablero existente: https://trello.com/b/e0OPKumv/tiotec-parciales-2-y-3

[Tareas técnicas para organizar en Trello](docs/Planificacion.csv). El CSV no sustituye los commits ni las tareas reales de cada integrante.

[Guía para estudiar y practicar la defensa individual](docs/Defensa.md).
