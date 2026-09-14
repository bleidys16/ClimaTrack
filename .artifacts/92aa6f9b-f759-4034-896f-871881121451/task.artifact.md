# Tareas de Implementación - Mejoras Cliente y Localización

- `[x]` Actualizar Modelos y Repositorios
    - `[x]` Modificar `Models.kt` (Agregar campos a `OrdenInfo`)
    - `[x]` Actualizar `OrdenRepository.kt` (Query con JOINS para técnico y equipo)
- `[x]` Implementar Autocompletado de Dirección
    - `[x]` Modificar `OrderRequestActivity.kt` (Integrar Geocoder tras obtener GPS)
- `[x]` Actualizar Interfaz de Usuario (UI)
    - `[x]` Modificar `ItemOrderBinding` y `OrdersAdapter.kt`
    - `[x]` Refinar `ClientDashboardActivity.kt`
- `[x]` Monitoreo de Técnicos (Admin y Técnico)
    - `[x]` Actualizar Base de Datos (columnas `is_active`, `work_end_time`, `last_lat`, `last_lon`)
    - `[x]` Implementar disponibilidad en Dashboard de Técnico
    - `[x]` Implementar contador de técnicos activos en Dashboard de Admin
    - `[x]` Crear `TechnicianMapActivity` para visualización en tiempo real
