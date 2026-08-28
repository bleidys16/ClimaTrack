# Plan de Mejora: Módulo de Cliente y Geolocalización Precisa

Este plan detalla las mejoras solicitadas para el módulo de cliente, incluyendo el autocompletado de direcciones mediante GPS y una vista de órdenes más detallada con información del técnico asignado.

## User Review Required

> [!IMPORTANT]
> - **Autocompletado de Dirección**: Se utilizará el servicio de `Geocoder` de Android. Esto requiere que el dispositivo tenga conexión a Internet para traducir las coordenadas GPS en una dirección legible.
> - **Datos del Técnico**: La información del técnico (nombre y hora asignada) solo aparecerá en las órdenes que ya hayan sido procesadas por el administrador.

## Proposed Changes

---

### 1. Mejoras en Registro de Órdenes (Cliente)

#### [MODIFY] [OrderRequestActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/OrderRequestActivity.kt)
- Implementar la función `getAddressFromLocation(lat, lon)` tras capturar el GPS.
- Autocompletar el campo `etExactAddress` con el resultado del Geocoding.

---

### 2. Mejoras en Visualización de Órdenes

#### [MODIFY] [Models.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/models/Models.kt)
- Agregar campos a `OrdenInfo`: `tecnicoNombre`, `precioServicio`, `tipoServicio`, `equipoMarca`, `equipoModelo`.

#### [MODIFY] [OrdenRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/repositories/OrdenRepository.kt)
- Actualizar el query en `getOrdenesByCliente` para incluir un JOIN con la tabla de `Usuarios` (Técnico) y traer los detalles adicionales del equipo.

#### [MODIFY] [OrdersAdapter.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/adapters/OrdersAdapter.kt)
- Actualizar el diseño del `ItemOrder` para mostrar:
    - Marca y Modelo del aire acondicionado.
    - Nombre del Técnico (si está asignado).
    - Tipo de servicio y fecha/hora.

---

### 3. Dashboard del Cliente
...
---

### 4. Monitoreo de Técnicos (Admin/Técnico)

#### [MODIFY] [Usuario](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/models/Models.kt)
- Nuevos campos: `isActive`, `workEndTime`, `lastLat`, `lastLon`.

#### [MODIFY] [DashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/DashboardActivity.kt)
- Interfaz para que el técnico se marque como "Activo" y defina su hora de salida.

#### [NEW] [TechnicianMapActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/TechnicianMapActivity.kt)
- Mapa interactivo para que el administrador vea a todo su personal en tiempo real.

#### [MODIFY] [ClientDashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/ClientDashboardActivity.kt)
- Configurar el `RecyclerView` con el adaptador actualizado.
- Cargar las órdenes del cliente logueado al iniciar la actividad.

## Verification Plan

### Manual Verification
1. **GPS y Dirección**: Abrir "Solicitar Servicio", capturar GPS y verificar que la dirección se escriba sola.
2. **Visualización**: Entrar como Cliente y verificar que en sus órdenes aparezca la marca/modelo del aire y el nombre del técnico si ya fue asignado.
3. **Roles**: Verificar que el técnico vea sus servicios realizados correctamente en su historial.
