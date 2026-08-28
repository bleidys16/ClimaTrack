# Plan de Corrección: Crash en Equipos y Visibilidad de Órdenes

Este plan aborda el cierre inesperado al entrar en la sección de Equipos y asegura que las órdenes de prueba sean visibles para el técnico en todos los dispositivos.

## User Review Required

> [!IMPORTANT]
> El crash en la sección de Equipos probablemente se debe a una inconsistencia en la base de datos local o a una falla al recuperar la columna de imagen. Se implementará un manejo seguro de columnas.

> [!NOTE]
> Para asegurar que las órdenes de prueba se vean siempre, se ajustará la lógica de inserción inicial para asociar las órdenes al ID real del usuario `tecnico01`, independientemente de cuál sea su ID incremental en SQLite.

## Proposed Changes

### [Robustez de Datos]

#### [MODIFY] [DatabaseHelper.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/database/DatabaseHelper.kt)
- Refactorizar `insertInitialData` para que busque el ID real del técnico tras insertarlo (o si ya existe) antes de crear las órdenes.
- Asegurar que las órdenes y equipos se asocien correctamente mediante consultas de verificación.

#### [MODIFY] [EquipoRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/repositories/EquipoRepository.kt)
- Reemplazar `getColumnIndexOrThrow` por una verificación segura con `getColumnIndex`. Si la columna no existe (por una migración fallida), se asignará `null` en lugar de provocar un crash.

---

### [Estabilidad de Interfaz]

#### [MODIFY] [EquipmentAdapter.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/adapters/EquipmentAdapter.kt)
- Añadir bloques `try-catch` en la carga de imágenes para evitar cierres por archivos corruptos o URIs inválidas.
- Validar la existencia de `ivEquipmentPhoto` antes de manipularla.

---

### [Sincronización de Cambios]

- Realizar un commit y push de las correcciones para que todos los dispositivos que descarguen la versión de GitHub vean las órdenes de prueba y no experimenten el crash.

## Verification Plan

### Manual Verification
1.  **Borrar datos de la app** (para forzar `onCreate` y la nueva carga de datos).
2.  **Iniciar sesión** con `tecnico01` / `123456`.
3.  **Entrar a Equipos**: Verificar que no se cierre y se listen los equipos iniciales.
4.  **Entrar a Órdenes**: Verificar que aparezcan las 5 órdenes de prueba (OT-00001, OT-00025, etc.).
