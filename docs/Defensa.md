# Preparación de la defensa individual

Esta guía es para estudiar y practicar antes de la evaluación. La pauta exige defender sin IA ni recursos de apoyo durante la defensa.

## Qué problema resuelve

TíoTec ayuda a organizar una revisión preventiva de hábitos digitales en un caso ficticio. Cada evaluación reúne un perfil, un equipo y cuatro respuestas: respaldo, actualizaciones, autenticación y recuperación. A partir de las respuestas se priorizan acciones.

## Recorrido de 15 minutos

| Minutos | Qué mostrar y explicar |
|---|---|
| 0-2 | Abrir el APK y comprobar que el backend está iniciado. Diferenciar Historial (Room, en este dispositivo) y Nube (servidor). |
| 2-4 | Explicar MVVM: pantalla muestra estado, ViewModel decide, Repository accede a datos. Mostrar un formulario inválido y luego uno válido. |
| 4-7 | En Nube crear una evaluación ficticia, actualizar el listado, editar el nombre o plazo y eliminarla. Explicar los verbos POST, GET, PUT y DELETE. |
| 7-8 | Abrir Avisos. Explicar que Retrofit consulta una API pública de GitHub, independiente del microservicio propio. |
| 8-10 | Ejecutar una prueba unitaria. Explicar su entrada, resultado esperado y el repositorio falso que reemplaza la red. Mostrar el informe de cobertura. |
| 10-11 | Mostrar el APK firmado y explicar KeyStore, alias, contraseña privada y configuración release. |
| 11-13 | Practicar una modificación de una regla con su prueba: por ejemplo, cambiar el plazo máximo. Revisar tanto validación Kotlin como validación Java. |
| 13-15 | Explicar los aportes reales que puedan demostrar con el historial de GitHub y el tablero Trello. Responder preguntas. |

## Variables con un caso de ejemplo

Use “Luffy”, “Notebook de prueba” y “Windows 11” como datos ficticios.

- `nombre`: identifica el perfil ficticio.
- `dispositivo` y `sistema`: describen el equipo del caso.
- `respuestas`: lista ordenada de cuatro elementos. Los índices 0, 1, 2 y 3 representan respaldo, actualizaciones, autenticación y recuperación.
- `diasRevision`: plazo de 7 a 90 días para volver a revisar.
- `accionesHechas`: máscara de cuatro bits. Cada bit indica si se completó una acción. Para el índice 2, `1 shl 2` equivale a 4.
- `ocupado`: indica una operación en curso y evita enviar dos veces desde la interfaz.
- `editando`: ID remoto al editar; `null` representa una creación.
- `StateFlow`: estado observable. Compose vuelve a dibujar la pantalla cuando cambia.

## Preguntas que debe poder contestar

1. ¿Qué sucede cuando la red falla? Se muestra un error, se libera el estado ocupado y se conserva el formulario para reintentar. El historial local sigue funcionando.
2. ¿Dónde se guarda cada cosa? Las evaluaciones locales y sus rutas de fotos quedan en Room; las fotos quedan en archivos privados. Las evaluaciones remotas quedan en H2 del microservicio.
3. ¿Por qué dos repositorios? Uno encapsula Room; el otro encapsula Retrofit. El ViewModel no construye SQL ni llamadas HTTP.
4. ¿Qué diferencia hay entre API externa y microservicio propio? GitHub proporciona avisos públicos; el equipo controla el servicio de evaluaciones con Spring Boot, entidades, reglas y base de datos.
5. ¿Qué valida el servidor? Longitud de campos, nivel de experiencia, cuatro respuestas válidas, plazo y máscara de acciones. La validación del móvil no sustituye la del servidor.
6. ¿Qué demuestra una prueba con un repositorio falso? Comprueba reglas y cambios de estado de forma repetible, sin depender de la disponibilidad de internet.
7. ¿Qué no demuestra? Una prueba unitaria no reemplaza comprobar una instalación real del APK, la cámara o la conectividad del dispositivo.
8. ¿Cómo se firma? Keytool genera la llave JKS; la configuración de Gradle usa el alias y contraseñas; assembleRelease produce el APK y apksigner verifica el certificado.

## Modificación para practicar

Cambiar el plazo máximo de 90 a 60 días:

1. Buscar `diasRevision` en Kotlin y Java.
2. Cambiar la regla en `Validacion.kt`, `Nube.kt` y `RevisionEntrada.java`.
3. Ajustar el rango de ambos Slider de Compose.
4. Actualizar los casos de borde de las pruebas.
5. Ejecutar pruebas, recompilar e instalar. Comprobar que el servidor rechaza un plazo fuera de rango.

Los aportes y capturas de colaboración deben corresponder a trabajo real. Este documento no acredita participación individual ni reemplaza el historial de cambios.
