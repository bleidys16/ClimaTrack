# Walkthrough - Corrección de Navegación por Rol

Se ha corregido el error donde los administradores eran redirigidos al panel del técnico al intentar volver al inicio desde las pantallas de Órdenes, Equipos o Historial.

## Cambios realizados

### [BaseActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/BaseActivity.kt)
- Se implementó el método `navigateToHome()` que utiliza el `SessionManager` para detectar el rol del usuario y dirigirlo a la actividad correspondiente:
    - `ADMINISTRADOR` -> `AdminDashboardActivity`
    - `CLIENTE` -> `ClientDashboardActivity`
    - Otros (Técnico) -> `DashboardActivity`
- Se configuraron los flags `FLAG_ACTIVITY_NEW_TASK` y `FLAG_ACTIVITY_CLEAR_TASK` para asegurar que el stack de actividades se limpie al volver al inicio.

### Pantallas Actualizadas
Se actualizó la lógica de la barra de navegación inferior en las siguientes actividades para usar el nuevo método `navigateToHome()`:
- [OrdersActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/OrdersActivity.kt)
- [EquipmentActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/EquipmentActivity.kt)
- [HistoryActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/HistoryActivity.kt)

## Verificación

> [!TIP]
> Para verificar el cambio, inicia sesión como Administrador, entra en "Ver todas las órdenes" y presiona el icono de Inicio. Ahora deberías regresar al panel de administrador correctamente.

render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/BaseActivity.kt)
render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/OrdersActivity.kt)
