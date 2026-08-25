# Tareas de Corrección de Crash en Evidencias

- [x] **Configuración de Rutas (FileProvider)**
    - [x] Actualizar `file_paths.xml` para cubrir la raíz de archivos externos
- [x] **Gestión de Permisos y Robustez (EvidenceActivity)**
    - [x] Implementar `requestCameraPermissionLauncher`
    - [x] Añadir chequeo de permisos en `prepareAndTakePhoto`
    - [x] Envolver creación de archivo y URI en `try-catch`
    - [x] Validar disponibilidad de almacenamiento externo
- [x] **Verificación**
    - [x] Probar denegación de permisos
    - [x] Probar flujo completo de captura
