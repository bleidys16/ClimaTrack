# Walkthrough - Apartado de Comprobantes para Clientes

Se ha implementado una nueva funcionalidad que permite a los clientes visualizar y descargar los comprobantes de pago (PDF) de sus servicios finalizados directamente desde el Dashboard.

## Cambios Realizados

### [activity_client_dashboard.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_client_dashboard.xml)
- Se añadió un sistema de pestañas (`TabLayout`) para alternar entre la lista de **Servicios** (todas las órdenes) y la de **Comprobantes** (solo servicios finalizados).

### [item_receipt.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/item_receipt.xml)
- Se creó un diseño de tarjeta optimizado para recibos, resaltando el costo total y un botón de descarga con un icono intuitivo.

### [ReceiptsAdapter.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/adapters/ReceiptsAdapter.kt)
- Se desarrolló un nuevo adaptador para manejar la visualización de los comprobantes y la lógica de interacción con el botón de descarga.

### [ClientDashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/ClientDashboardActivity.kt)
- Se integró la lógica para cambiar dinámicamente entre el listado de servicios y el de comprobantes.
- Se implementó la generación y apertura automática del PDF usando `PdfGenerator` y `FileProvider`.

## Verificación

> [!TIP]
> 1. Inicia sesión como Cliente.
> 2. Verás dos pestañas: **SERVICIOS** y **COMPROBANTES**.
> 3. En **COMPROBANTES**, solo aparecerán los servicios que ya han sido **FINALIZADOS**.
> 4. Presiona el botón del icono de documento a la derecha para generar y abrir tu comprobante oficial en PDF.

render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/ClientDashboardActivity.kt)
render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_client_dashboard.xml)
