# Walkthrough de Correcciones: Geolocalización y Evidencias

Se han corregido y robustecido las funcionalidades de captura de ubicación GPS y evidencias fotográficas, asegurando que los datos se guarden correctamente en SQLite y se asocien a la orden de trabajo activa.

## Cambios Realizados

### Repositorio y Datos
- **[ServicioRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/repositories/ServicioRepository.kt)**: Se implementó `getUbicacionByOrden` para recuperar coordenadas guardadas previamente.

### Geolocalización (LocationActivity)
- **Carga de Persistencia**: Al abrir la pantalla, la aplicación ahora verifica si ya existe una ubicación para la orden y la muestra, desactivando el botón de guardado si ya está registrada.
- **Robustez de GPS**: Se reemplazó `lastLocation` por `getCurrentLocation` para forzar una lectura fresca del sensor.
- **Validación de Sensor**: Se añadió una alerta que guía al usuario a los ajustes del sistema si el GPS está desactivado.
- **Mensajería**: Se mejoraron los mensajes de estado ("Obteniendo ubicación...", "Capturado el...", etc.).

### Evidencias Fotográficas (EvidenceActivity)
- **Persistencia de Estado**: Se implementó `onSaveInstanceState` para evitar que la aplicación pierda el ID de la orden o la ruta de la foto si el sistema destruye la actividad mientras la cámara está abierta.
- **Refresco de UI**: Se aseguró que tras confirmar la captura de una foto, esta aparezca inmediatamente en la cuadrícula de evidencias.
- **Manejo de Archivos**: Se verificó la integración con `FileProvider` y se añadió lógica para evitar registros huérfanos si la captura se cancela.

## Guía de Prueba

### Geolocalización
1. Seleccione una **Orden** desde el Dashboard.
2. Presione el botón **Ubicación**.
3. Si los permisos no han sido otorgados, acéptelos.
4. Si el GPS está apagado, siga las instrucciones del diálogo para activarlo.
5. Presione **ACTUALIZAR UBICACIÓN**.
6. Una vez obtenidas las coordenadas, presione **GUARDAR COORDENADAS**.
7. Salga de la pantalla y vuelva a entrar; verá que la ubicación aparece como "YA REGISTRADA".

### Evidencias
1. En el detalle de la orden, presione **Evidencias**.
2. Presione **TOMAR NUEVA FOTO**.
3. Capture la fotografía y acéptela.
4. Verifique que la imagen aparezca en la lista con su fecha de captura.
5. (Opcional) Mantenga presionada o use el botón de eliminar (si el adaptador lo permite) para borrar una evidencia.

> [!TIP]
> Para pruebas en emulador, asegúrese de configurar una ubicación manual en los "Extended Controls" del emulador para que el sensor GPS retorne datos válidos.
