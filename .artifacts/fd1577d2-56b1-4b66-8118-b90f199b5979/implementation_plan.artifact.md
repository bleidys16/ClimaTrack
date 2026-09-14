# Plan de Implementación: Órdenes de Prueba y Corrección de Error en Equipos

Este plan aborda dos requerimientos:
1.  Asegurar que las órdenes de prueba sean visibles en todos los dispositivos (agregándolas a la precarga de la base de datos).
2.  Corregir el cierre inesperado (crash) al ingresar al apartado de "Equipos".

## Cambios Propuestos

### Base de Datos
#### [MODIFY] [DatabaseHelper.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/database/DatabaseHelper.kt)
- Agregar órdenes de prueba iniciales en el método `insertInitialData` para que estén disponibles en todas las instalaciones nuevas o tras limpiar datos.
- Asegurar que el campo `estado` en la tabla de equipos sea `NOT NULL` para evitar errores de nulidad en el modelo de Kotlin.

### Apartado de Equipos
#### [MODIFY] [EquipmentActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/EquipmentActivity.kt)
- Refactorizar la carga de datos para que se realice de manera más segura y consistente con otras actividades del proyecto (moviendo `loadEquipment` de `onResume` a `onCreate`).
- Asegurar la inicialización correcta de todos los componentes antes de su uso.

#### [MODIFY] [EquipmentAdapter.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/adapters/EquipmentAdapter.kt)
- Agregar verificaciones de seguridad en la carga de imágenes y estados para evitar crashes por datos inesperados o nulos.

## Plan de Verificación

### Pruebas Automatizadas
- No se requieren pruebas automatizadas específicas para este cambio de datos iniciales, pero se verificará que la aplicación compile correctamente.

### Verificación Manual
1.  **Órdenes de Prueba**:
    - Iniciar la aplicación.
    - Ir al apartado de "Órdenes".
    - Verificar que aparezca la orden con número `TEST-ORD-001`.
2.  **Cierre en Equipos**:
    - Navegar al apartado de "Equipos" desde el Dashboard.
    - Verificar que la actividad se abra correctamente sin cerrarse.
    - Realizar una búsqueda en la lista de equipos para asegurar que el filtro funciona.
