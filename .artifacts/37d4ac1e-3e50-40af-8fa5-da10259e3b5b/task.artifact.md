# Tareas: Reparación de Asignación y Estabilidad de Sincronización

- `[x]` Modificar `OrdenRepository.kt`:
    - `[x]` Proteger `fetchOrdersFromCloud` contra sobreescritura de cambios locales.
    - `[x]` Proteger `fetchMaintenanceFromCloud` contra sobreescritura.
    - `[x]` Proteger `fetchPartsFromCloud` contra sobreescritura.
    - `[x]` Asegurar `is_synced = 0` en `assignTechnician`.
- `[x]` Modificar `AdminDashboardActivity.kt`:
    - `[x]` Validar lista de técnicos antes de mostrar diálogo.
- `[x]` Verificar compilación.
