# Walkthrough - Expansión de Roles y Funcionalidades

He implementado los cambios solicitados para transformar ClimaTrack en una plataforma multi-rol con gestión avanzada de servicios.

## Cambios Realizados

### 1. Sistema Multi-Rol
- **Administrador**: Nuevo dashboard para gestionar órdenes sin asignar y realizar asignaciones inteligentes (al técnico con menos carga).
- **Cliente**: Nuevo dashboard para ver el historial de servicios, solicitar nuevos mantenimientos con ubicación GPS y firmar digitalmente.
- **Técnico**: Se añadió la capacidad de establecer precios de servicios y visualizar la ubicación del cliente en Google Maps.

### 2. Módulo de Registro y Autenticación
- Implementada la `RegisterActivity` para que los clientes puedan crear sus propias cuentas.
- Actualizada la `LoginActivity` para redirigir automáticamente al dashboard correspondiente según el rol del usuario.
- El `SessionManager` ahora almacena y gestiona el rol del usuario activo.

### 3. Funcionalidades Geográficas (Google Maps)
- Integración real de Google Maps en `LocationActivity`.
- Los clientes capturan sus coordenadas al solicitar un servicio.
- Los técnicos ven un marcador en el mapa con la posición exacta del cliente.
- Configurada la dependencia `play-services-maps` y el meta-data en el manifiesto.

### 4. Firma Virtual y Cotizaciones
- Implementada la `SignatureView` personalizada para captura de trazos.
- Los clientes ahora deben realizar una firma virtual para finalizar la aceptación del servicio.
- La firma se codifica en Base64 y se almacena directamente en la base de datos SQLite.
- Los técnicos pueden ingresar el precio del servicio, el cual cambia el estado de la orden a "PENDIENTE APROBACIÓN".

### 5. Base de Datos
- Actualizada la versión de la DB a **6**.
- Nuevas columnas en `usuarios`: `email`, `telefono`.
- Nuevas columnas en `ordenes`: `precio`, `latitud`, `longitud`, `direccion_exacta`, `firma`.

## Cómo Probarlo
1. **Registro**: Inicia la app, ve a "Regístrate aquí" y crea una cuenta de cliente.
2. **Solicitud**: Desde el Dashboard de cliente, toca "SOLICITAR SERVICIO", permite el acceso al GPS y envía la solicitud.
3. **Asignación**: Entra como `admin` (`admin123`) y toca "ASIGNACIÓN INTELIGENTE".
4. **Técnico**: Entra como `tecnico01` (`123456`), abre la orden asignada, toca el icono de ubicación para ver al cliente en el mapa. Luego en "Mantenimiento", ingresa un precio y guarda.
5. **Firma**: Vuelve a entrar como cliente, verás la orden pendiente de aprobación. Ve a la pantalla de aprobación, realiza la firma y guarda.

render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main%20(1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/database/DatabaseHelper.kt)
render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main%20(1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/models/Models.kt)
render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main%20(1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/repositories/OrdenRepository.kt)
