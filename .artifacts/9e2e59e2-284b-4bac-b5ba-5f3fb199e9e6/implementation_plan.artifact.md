# Plan de Corrección: Crash en Captura de Evidencias

Este plan aborda el error "ClimaTrack continua fallando" que ocurre al intentar tomar una fotografía. El problema parece estar relacionado con la gestión de permisos de cámara y la robustez en la creación de archivos para `FileProvider`.

## User Review Required

> [!IMPORTANT]
> Aunque el contrato `TakePicture` no siempre requiere el permiso de `CAMERA` de forma estricta, el hecho de tenerlo declarado en el `AndroidManifest.xml` obliga a solicitarlo explícitamente en tiempo de ejecución en muchas versiones de Android para evitar bloqueos de seguridad.

## Proposed Changes

### [Robustez en Captura]

#### [MODIFY] [EvidenceActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/EvidenceActivity.kt)
- **Gestión de Permisos**: Implementar la solicitud del permiso `CAMERA` antes de intentar abrir la cámara.
- **Seguridad en I/O**: Añadir bloques `try-catch` alrededor de la creación del archivo temporal y la generación de la URI para capturar excepciones de almacenamiento o de `FileProvider`.
- **Validación de Directorio**: Asegurar que el directorio de imágenes exista antes de intentar crear el archivo.
- **Feedback al Usuario**: Mostrar mensajes de error específicos en lugar de permitir que la aplicación se cierre.

#### [MODIFY] [file_paths.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/xml/file_paths.xml)
- **Ampliación de Rutas**: Cambiar el mapeo de `Pictures` a `.` (raíz de archivos externos) para mayor flexibilidad y evitar errores de mapeo en `FileProvider`.

## Verification Plan

### Manual Verification
1. **Flujo de Permisos**: Denegar el permiso de cámara y verificar que la app informe al usuario sin cerrarse.
2. **Captura Exitosa**: Conceder el permiso y verificar que la cámara abra correctamente.
3. **Persistencia**: Tomar una foto, verificar que se guarde en SQLite y se muestre en el grid.
4. **Cancelación**: Abrir la cámara y regresar sin tomar la foto; verificar que no ocurra ningún crash.
