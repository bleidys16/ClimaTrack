# Plan de Implementación - Apartado de Comprobantes para Clientes

Este plan detalla la creación de una nueva sección en el dashboard del cliente para gestionar y descargar comprobantes de pago (PDF) de los servicios finalizados.

## User Review Required

> [!IMPORTANT]
> Los comprobantes solo estarán disponibles para órdenes con estado **FINALIZADA**.

## Proposed Changes

### [Interfaz de Usuario]

#### [MODIFY] [activity_client_dashboard.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_client_dashboard.xml)
- Añadir un `com.google.android.material.tabs.TabLayout` debajo de la tarjeta de solicitud.
- Tabs: "Servicios" y "Comprobantes".
- El `TextView` "Mis Servicios Recientes" cambiará dinámicamente según la pestaña.

#### [NEW] [item_receipt.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/item_receipt.xml)
- Crear un diseño de tarjeta moderno para los comprobantes.
- Incluir: Número de orden, fecha, icono de PDF y un botón prominente "DESCARGAR COMPROBANTE".

### [Lógica de Adaptador]

#### [NEW] [ReceiptsAdapter.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/adapters/ReceiptsAdapter.kt)
- Nuevo adaptador exclusivo para la pestaña de comprobantes.
- Implementar la lógica para invocar al `PdfGenerator` y abrir el archivo PDF al presionar el botón de descarga.

### [Dashboard del Cliente]

#### [MODIFY] [ClientDashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/ClientDashboardActivity.kt)
- Configurar el listener del `TabLayout`.
- Alternar entre `OrdersAdapter` (para todos los servicios) y `ReceiptsAdapter` (solo para finalizados).
- Filtrar la lista de órdenes locales y de la nube según la pestaña activa.

## Verification Plan

### Manual Verification
1. Iniciar sesión como **Cliente**.
2. Observar la nueva barra de pestañas.
3. En la pestaña **Comprobantes**, verificar que solo aparezcan órdenes terminadas.
4. Presionar "Descargar" en una orden y confirmar que se genere el PDF y se abra el lector del sistema.
5. Verificar que el flujo de "Servicios" siga funcionando normalmente.
