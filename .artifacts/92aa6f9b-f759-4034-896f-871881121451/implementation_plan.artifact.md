# Plan de Implementación: Expansión de Roles y Funcionalidades ClimaTrack

Este plan detalla los cambios necesarios para incorporar los roles de Cliente y Administrador, automatizar la asignación de técnicos, integrar Google Maps y permitir la firma virtual de servicios.

## User Review Required

> [!IMPORTANT]
> - **Google Maps API Key**: Se requerirá una API Key válida en el archivo `local.properties` o `AndroidManifest.xml` para que el mapa funcione correctamente.
> - **Firma Virtual**: La firma se almacenará localmente como una imagen o como un string Base64 en la base de datos para simplificar la implementación inicial.
> - **Lógica de Asignación**: El administrador podrá elegir entre asignación manual o automática (técnico con menos carga).

## Proposed Changes

---

### 1. Base de Datos y Modelos

#### [MODIFY] [Models.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/models/Models.kt)
- Agregar `email` y `telefono` a `Usuario`.
- Agregar `precioServicio`, `latitudCliente`, `longitudCliente`, `direccionExacta` y `firmaBase64` a `Orden`.

#### [MODIFY] [DatabaseHelper.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/database/DatabaseHelper.kt)
- Actualizar `TABLE_ORDENES` con las nuevas columnas: `precio`, `latitud`, `longitud`, `direccion_exacta`, `firma`.
- Incrementar `DATABASE_VERSION`.

---

### 2. Autenticación y Registro

#### [NEW] [RegisterActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/RegisterActivity.kt)
- Formulario para que nuevos clientes creen su cuenta.

#### [MODIFY] [LoginActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/LoginActivity.kt)
- Lógica de redirección:
    - `Técnico` -> `DashboardActivity`
    - `Cliente` -> `ClientDashboardActivity`
    - `Administrador` -> `AdminDashboardActivity`

---

### 3. Funcionalidades de Cliente

#### [NEW] [ClientDashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/ClientDashboardActivity.kt)
- Botón para solicitar mantenimiento (genera orden).
- Lista de mis servicios.
- Notificación/Alerta para aceptar precio y firmar.

#### [NEW] [SupportActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/SupportActivity.kt)
- Sección de ayuda y contacto técnico.

---

### 4. Funcionalidades de Administrador

#### [NEW] [AdminDashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/AdminDashboardActivity.kt)
- Panel de control de órdenes sin asignar.
- Botón de "Asignación Inteligente" (basado en carga de trabajo).

---

### 5. Funcionalidades de Técnico

#### [MODIFY] [MaintenanceActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/MaintenanceActivity.kt)
- Campo para ingresar el costo del servicio.
- Botón para enviar cotización al cliente.

#### [MODIFY] [LocationActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/LocationActivity.kt)
- Integración real con Google Maps para ver la ubicación del cliente mediante marcador.

---

### 6. Repositorios (Lógica de Negocio)

#### [MODIFY] [OrdenRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main (1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/repositories/OrdenRepository.kt)
- Método `assignTechnician(orderId, techId)`.
- Método `getTechnicianWorkload()` para encontrar al técnico con menos órdenes.
- Método `updatePriceAndNotify(orderId, price)`.
- Método `saveClientSignature(orderId, signatureBase64)`.

## Verification Plan

### Automated Tests
- Pruebas unitarias en `OrdenRepository` para verificar que la asignación automática elija al técnico correcto.
- Pruebas de integración de base de datos para asegurar que los nuevos campos persistan.

### Manual Verification
1. Registrar un nuevo Cliente.
2. Solicitar un mantenimiento desde la cuenta del Cliente.
3. Entrar como Administrador y asignar la orden.
4. Entrar como Técnico, poner precio y ver ubicación en el mapa.
5. Volver al Cliente, aceptar el precio y realizar la firma virtual.
