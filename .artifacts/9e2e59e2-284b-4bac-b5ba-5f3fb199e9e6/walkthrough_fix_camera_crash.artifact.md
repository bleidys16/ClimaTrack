# Walkthrough: Solución de Crash en Captura de Evidencias

Se ha corregido el error crítico que causaba que **ClimaTrack** se cerrara inesperadamente al intentar tomar una fotografía. La funcionalidad de evidencias ahora es robusta y maneja correctamente los permisos y el almacenamiento.

## Correcciones Implementadas

### Gestión de Permisos en Tiempo de Ejecución
- Se implementó un launcher para solicitar el permiso de `CAMERA` antes de abrir el Intent de captura.
- Se añadió una verificación preventiva con `ContextCompat.checkSelfPermission` para asegurar que la app no intente acceder al hardware sin autorización, lo cual evitó el crash reportado.

### Robustez en el Manejo de Archivos (Safe I/O)
- Se añadió un bloque `try-catch` alrededor de la creación del archivo temporal y la generación de la URI para capturar excepciones de entrada/salida.
- Se agregaron validaciones de disponibilidad de almacenamiento externo (`getExternalFilesDir`) para prevenir fallos en dispositivos con poco espacio o almacenamiento montado como solo lectura.
- Se configuró el `FileProvider` en `file_paths.xml` con una ruta más flexible (`path="."`) para asegurar que cualquier URI generada bajo la raíz de archivos externos sea válida y accesible por la aplicación de la cámara.

### Mejoras de UX
- **Mensajería**: La app ahora informa al usuario si el permiso fue denegado o si ocurrió un error técnico al crear la imagen, en lugar de cerrarse abruptamente.
- **Limpieza**: Se añadió lógica para eliminar archivos temporales si la captura es cancelada por el usuario, evitando el llenado innecesario del almacenamiento.

## Verificación Final

> [!IMPORTANT]
> El proyecto compila satisfactoriamente y se ha verificado que la transición a la cámara es segura.

### Resultados de las Pruebas
1. **Denegación de Permisos**: Al denegar el permiso de cámara, se muestra un Toast informativo y la app permanece estable.
2. **Captura Exitosa**: Al otorgar permisos, la cámara abre correctamente, permite tomar la foto y esta se visualiza inmediatamente en el grid de la orden.
3. **Cancelación**: Si el usuario abre la cámara y regresa con el botón atrás, no ocurre ningún crash.
