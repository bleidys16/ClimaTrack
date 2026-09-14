# Walkthrough - Monitoreo de Técnicos y Disponibilidad

He implementado un sistema de monitoreo en tiempo real para el administrador y controles de disponibilidad para el personal técnico.

## Cambios Realizados

### 1. Para el Técnico: Control de Disponibilidad
- Se añadió una tarjeta de **"Estado de Disponibilidad"** en el Dashboard del Técnico.
- **Switch de Activo**: El técnico puede marcarse como activo. Al hacerlo, la app captura su ubicación actual y la envía al sistema.
- **Hora de Finalización**: Permite al técnico definir a qué hora termina su jornada laboral mediante un selector de tiempo.

### 2. Para el Administrador: Monitoreo en Tiempo Real
- **Contador de Activos**: En el Dashboard de Admin ahora aparece un recuadro indicando cuántos técnicos están conectados en el momento.
- **Mapa de Técnicos**: Nuevo botón **"VER MAPA DE TÉCNICOS"** que abre una vista de Google Maps con marcadores azules para cada técnico activo.
- **Información Detallada**: Al tocar un marcador en el mapa, el administrador puede ver el nombre del técnico y su hora estimada de finalización.

### 3. Infraestructura de Datos
- Se actualizó la tabla de `usuarios` para persistir el estado, la hora de salida y las coordenadas GPS.
- Sincronización automática de ubicación al activar la disponibilidad.

## Cómo Probarlo
1. **Técnico**: Entra como `tecnico01`, activa el switch de disponibilidad y selecciona una hora de finalización (ej: 18:00).
2. **Administrador**: Entra como `admin`, observa que el contador de técnicos activos se ha incrementado. Toca en el mapa y verás a `tecnico01` posicionado en su ubicación actual.

render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main%20(1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/DashboardActivity.kt)
render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main%20(1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/AdminDashboardActivity.kt)
render_diffs(file:///C:/Users/Aprendiz/Downloads/ClimaTrack-main%20(1)/ClimaTrack-main/app/src/main/java/com/example/climatrack/repositories/UsuarioRepository.kt)
