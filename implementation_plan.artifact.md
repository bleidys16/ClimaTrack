# Mejora de Geolocalización para Técnicos

Este plan detalla las mejoras para asegurar que la ubicación del técnico se capture de forma precisa y sea visible para él, garantizando que el administrador pueda verlo en el mapa.

## User Review Required

> [!IMPORTANT]
> Se requerirá que el usuario acepte los permisos de ubicación al activar el estado "Activo" por primera vez.

## Proposed Changes

### [Dashboard del Técnico]

Se realizarán cambios en la interfaz y la lógica para hacer la ubicación más transparente y confiable.

#### [MODIFY] [activity_dashboard.xml](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/res/layout/activity_dashboard.xml)
- Añadir un contenedor de información de ubicación dentro del `cardStatus`.
- Incluir un `TextView` para mostrar las coordenadas actuales.
- Añadir un `ImageButton` para permitir la actualización manual de la ubicación.

#### [MODIFY] [DashboardActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/DashboardActivity.kt)
- Implementar la solicitud de permisos de ubicación (`ACCESS_FINE_LOCATION`).
- Reemplazar `lastLocation` por `getCurrentLocation` con alta precisión.
- Actualizar la interfaz de usuario con las coordenadas capturadas.
- Añadir lógica para el botón de actualización manual.

## Verification Plan

### Manual Verification
1. Iniciar sesión como técnico.
2. Activar el switch "Activo".
3. Verificar que aparezca un diálogo de permisos (si no se han concedido).
4. Confirmar que las coordenadas aparezcan en pantalla debajo del switch.
5. Presionar el botón de refrescar y verificar que las coordenadas se actualicen.
6. Entrar como administrador y verificar que el técnico aparezca en el mapa en la posición correcta.
