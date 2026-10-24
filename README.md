# TíoTec Preventivo

Aplicación Android para registrar evaluaciones preventivas de dispositivos ficticios. El formulario reúne datos del dispositivo, una foto y cuatro hábitos de seguridad. La app genera un plan de acciones que se puede consultar en Historial.

## Equipo

- Bárbara Tolorza
- Christopher Villa

El trabajo se organizó por etapas en [Trello](https://trello.com/b/e0OPKumv/tiotec-parciales-2-y-3) y se integró en este repositorio.

## Funciones

- Formulario con validación por campo, mensajes e íconos de error.
- Navegación entre Evaluar e Historial, menú lateral y ayuda.
- Foto tomada con la cámara o elegida del dispositivo.
- Evaluaciones almacenadas con Room; las fotos se guardan en el almacenamiento privado de la app y Room conserva su ruta.
- Historial con foto y detalle, edición, eliminación, acciones realizadas y filtro de acciones pendientes.
- Interfaz adaptable al ancho de pantalla y avisos en pantalla.

## Cómo ejecutar

1. Clonar este repositorio y abrir su carpeta raíz en Android Studio.
2. Usar JDK 21 para Gradle y sincronizar el proyecto.
3. Tener instalado el SDK de Android 35.
4. Seleccionar un teléfono o emulador con Android 13 (API 33) o posterior y ejecutar `app`.

Para compilar desde la raíz en Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Organización del código

- `MainActivity`: inicia la interfaz, aplica el tema y entrega el ancho de pantalla y el ViewModel a la app.
- `model`: estructura del formulario y preguntas.
- `validation`: reglas y mensajes de validación.
- `data`: entidades y operaciones de Room, además del almacenamiento de fotos.
- `repository`: comunica el ViewModel con las evaluaciones guardadas.
- `viewmodel`: mantiene el estado y coordina validación, fotos y guardado.
- `ui`: pantallas, navegación y controles visibles.

## Comprobaciones realizadas

El 24 de octubre de 2026 se compiló el proyecto y se ejecutó en el emulador Medium Phone de Android Studio, API 37. Se comprobó lo siguiente:

- La app abre en Evaluar y permite navegar a Historial, abrir el menú y consultar la ayuda.
- Guardar un formulario vacío muestra mensajes e íconos de error. Al completar los datos, los errores se despejan y se puede guardar.
- El selector permite elegir una imagen del dispositivo y cancelar la selección. La cámara permite tomar una foto y cancelar una captura.
- Una evaluación de prueba y su foto elegida continuaron en Historial después de cerrar y reabrir la app.
- Al editar la evaluación, el cambio de respuesta y una nueva foto tomada con la cámara aparecieron en el detalle de Historial.
- Se pudo marcar una acción, filtrar las pendientes y eliminar la evaluación de prueba. Las evaluaciones anteriores del emulador no se modificaron.

Queda por documentar una comprobación específica de permiso de cámara denegado, giro del dispositivo y transición de los avisos.
