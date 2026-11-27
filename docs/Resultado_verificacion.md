# Verificación de la entrega

Verificado el 19 de noviembre de 2026, hora de Santiago.

| Comprobación | Resultado |
|---|---|
| Compilación Kotlin/Java Android debug y release | Aprobada |
| Generación de APK release | Aprobada |
| Firma APK con apksigner | Verificada; esquema v2, RSA 3072 bits |
| Pruebas unitarias Android | 19 aprobadas, 0 fallidas |
| Pruebas backend con MockMvc/JPA/H2 | 6 aprobadas, 0 fallidas |
| Cobertura de líneas de lógica | 325 de 366, 88,80% |
| Empaquetado ejecutable Spring Boot | Aprobado |
| Android Studio | Proyecto actualizado abierto |
| Emulador | Iniciado; pendiente instalar y comprobar el nuevo APK |
| CRUD móvil contra servidor en ejecución | Pendiente de comprobación en dispositivo |
| API externa desde el APK | Pendiente de comprobación en dispositivo |

## Alcance de la cobertura

Se incluyeron las clases compiladas de los paquetes model, validation, repository y viewmodel, con los filtros estándar de JaCoCo para código generado por Kotlin.

| Paquete | Líneas cubiertas / totales |
|---|---|
| model | 75 / 75 |
| validation | 11 / 11 |
| viewmodel | 227 / 259 |
| repository | 12 / 21 |

No se incluyen Compose, MainActivity, clientes HTTP ni adaptadores Android de archivos/cámara en esta métrica de lógica.

## Evidencia

- `entrega/evidencias/pruebas-app.txt`: salida real de JUnitCore.
- `entrega/evidencias/pruebas-backend.xml`: informe de Maven Surefire.
- `entrega/evidencias/cobertura/index.html`: reporte real JaCoCo.
- `entrega/evidencias/cobertura/cobertura.xml`: métricas.
- `entrega/evidencias/firma-apk.txt`: certificado y resultado de apksigner.

Las pruebas Android se compilaron con Gradle y se ejecutaron en una JVM independiente con JUnitCore y el agente JaCoCo, utilizando el mismo classpath y Android mockable configurado para unit tests. Esto evita que el entorno restringido necesite una conexión entre procesos de Gradle. El backend se probó dentro del proceso con MockMvc y una base H2 independiente.

## Restricciones de esta sesión

El entorno de ejecución bloquea conexiones locales de red. Tomcat no pudo iniciar su selector NIO por `Permission denied: connect`, y el cliente ADB de esta sesión tampoco pudo comunicarse con su servidor. Estos bloqueos no se presentan como pruebas aprobadas del APK.

El análisis Lint de release no se ejecutó porque `lint-gradle:31.9.2` no estaba disponible en caché y la sesión no podía descargarlo. La compilación y firma sí se completaron. No se deshabilita Lint permanentemente en el proyecto.

La defensa individual y las evidencias de participación de cada integrante deben practicarse y acreditarse por los estudiantes.
