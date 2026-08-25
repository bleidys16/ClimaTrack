# Plan de Mejora: Pantalla de Geolocalización (Estilo Empresarial)

Este plan detalla las modificaciones necesarias para que la pantalla de **Geolocalización** muestre información técnica detallada (dirección, fecha, hora) y un soporte visual de mapa, siguiendo el estilo profesional de la aplicación.

## User Review Required

> [!IMPORTANT]
> Para cumplir con el requerimiento de mostrar la "Dirección aproximada", se utilizará la API `Geocoder` de Android. Esta funcionalidad requiere conexión a Internet en el momento de la captura. Una vez guardada, la dirección será accesible offline.

> [!CAUTION]
> Se realizará una modificación ligera en la base de datos (SQLite) y en el modelo `Ubicacion` para persistir la dirección obtenida, asegurando que se visualice correctamente al consultar registros antiguos.

## Proposed Changes

### [Capa de Datos y Modelos]

#### [MODIFY] [Models.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/models/Models.kt)
- Añadir el campo `direccion: String?` a la data class `Ubicacion`.

#### [MODIFY] [DatabaseHelper.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/database/DatabaseHelper.kt)
- Añadir la constante `COL_UBI_DIR = "direccion"`.
- Actualizar la sentencia `CREATE TABLE` de ubicaciones para incluir la nueva columna.

#### [MODIFY] [ServicioRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/repositories/ServicioRepository.kt)
- Actualizar `addUbicacion` para guardar el campo `direccion`.
- Actualizar `getUbicacionByOrden` para recuperar la dirección de la base de datos.

---

### [Interfaz de Usuario (UI)]

#### [MODIFY] [activity_location.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_location.xml)
- **Mapa**: Mejorar el placeholder del mapa con un gradiente o una imagen base más profesional y un icono de marcador central.
- **Card de Detalles**: Reorganizar el contenido para mostrar en filas claras:
    - **Dirección** (con icono de casa/puntero).
    - **Coordenadas** (Latitud y Longitud en la misma sección).
    - **Fecha** (con icono de calendario).
    - **Hora** (con icono de reloj).

---

### [Lógica de Negocio]

#### [MODIFY] [LocationActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/LocationActivity.kt)
- **Geocodificación**: Implementar `getAddressFromLocation(lat, lon)` usando `Geocoder`.
- **Formateo**: Separar la cadena de fecha/hora actual en dos campos distintos para la interfaz.
- **Persistencia**: Pasar la dirección obtenida al objeto `Ubicacion` antes de llamar al repositorio.

## Verification Plan

### Manual Verification
1.  **Captura Nueva**:
    *   Presionar "Actualizar Ubicación".
    *   Verificar que aparezcan Latitud, Longitud, Dirección (si hay internet), Fecha y Hora.
    *   Presionar "Guardar Coordenadas".
2.  **Consulta Offline**:
    *   Cerrar la aplicación.
    *   Abrir la misma orden y entrar a Geolocalización.
    *   Verificar que todos los datos (incluyendo dirección) se carguen desde SQLite.
3.  **Visual**:
    *   Confirmar que el diseño sea coherente con la paleta de colores `chinese_black` y `american_blue`.
