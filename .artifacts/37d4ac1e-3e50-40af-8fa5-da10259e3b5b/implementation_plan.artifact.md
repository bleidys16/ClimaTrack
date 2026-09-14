# Plan de Corrección: Asignación de Órdenes y Sincronización Robusta

Este plan corrige el problema donde la asignación de técnicos parece no funcionar (debido a colisiones de sincronización) y refuerza la estabilidad para evitar cierres inesperados.

## User Review Required

> [!IMPORTANT]
> **Conflicto de Sincronización Detectado**: El problema de "no hace nada" ocurre porque al asignar un técnico localmente, la descarga automática de la nube (que ocurre casi al mismo tiempo) sobreescribe el cambio local con los datos viejos antes de que el celular logre subir el cambio.

## Proposed Changes

### [Core] Repositorios (Sincronización Inteligente)

#### [MODIFY] [OrdenRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/repositories/OrdenRepository.kt)
- **Blindaje de Descarga**: Modificar `fetchOrdersFromCloud`, `fetchMaintenanceFromCloud` y `fetchPartsFromCloud` para que NO actualicen registros locales que tengan `is_synced = 0`. Esto garantiza que los cambios locales "ganen" hasta que se suban con éxito.
- **Estado de Sincronización**: Asegurar que `assignTechnician` y `updateFinalApproval` marquen la orden con `is_synced = 0`.

---

### [UI] Admin Dashboard (Flujo de Asignación)

#### [MODIFY] [AdminDashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/AdminDashboardActivity.kt)
- **Validación de Técnicos**: Verificar si la lista de técnicos está vacía antes de mostrar el diálogo de asignación para evitar confusión o cierres.
- **Feedback Inmediato**: Mostrar un mensaje claro de "Sincronizando..." al realizar cambios importantes.

---

### [Bugfix] Estabilidad

#### [MODIFY] [UsuarioRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/repositories/UsuarioRepository.kt)
- Seguir reforzando `cursorToUsuario` para manejar casos de inconsistencia de datos que puedan venir de instalaciones antiguas.

## Verification Plan

### Automated Tests
- `gradlew assembleDebug`: Confirmar compilación.

### Manual Verification
1.  **Asignación**: Abrir el Panel Admin, asignar una orden a un técnico y verificar que la orden se mueva de "Sin Asignar" a "En Proceso/Asignada" y que NO regrese a su estado anterior después de unos segundos.
2.  **Modo Offline**: Desactivar internet, asignar un técnico (el cambio debe quedar local), reactivar internet y verificar que se suba correctamente.
