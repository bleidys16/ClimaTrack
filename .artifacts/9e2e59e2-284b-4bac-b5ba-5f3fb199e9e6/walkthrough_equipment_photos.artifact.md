# Walkthrough: Integración de Fotografías en Equipos

Se ha completado la mejora de la sección de **Gestión de Equipos**, permitiendo ahora asociar una fotografía real a cada unidad para una mejor identificación visual.

## Mejoras Realizadas

### Persistencia de Imágenes
- **Esquema SQLite**: Se actualizó la base de datos a la versión 3, añadiendo la columna `imagen_path` a la tabla de equipos.
- **Repositorio**: Se actualizaron las operaciones CRUD para guardar y recuperar la ruta de la fotografía desde la base de datos.
- **Modelos**: Se añadió el campo `imagenPath` a la data class `Equipo`.

### Nueva Interfaz de Equipos
- **Miniaturas en Lista**: El elemento de la lista (`item_equipment.xml`) ahora incluye una miniatura lateral con bordes redondeados.
- **Placeholders Inteligentes**: Si un equipo no tiene foto, se muestra automáticamente un icono representativo con un diseño coherente.
- **Captura Integrada**: Se añadió una sección de fotografía en el formulario de creación/edición, permitiendo capturar la imagen directamente desde la cámara del dispositivo.

### Detalles Técnicos
- **Manejo de Archivos**: Se utiliza `FileProvider` para una integración segura con la cámara del sistema.
- **Carga Eficiente**: Las imágenes se cargan de forma optimizada desde el almacenamiento interno de la aplicación.

## Guía de Verificación

1.  **Captura de Foto**:
    - Entre a **Equipos** y pulse el botón flotante **(+)**.
    - Pulse **TOMAR FOTO** y capture la imagen del equipo.
    - Complete los campos obligatorios y pulse **GUARDAR EQUIPO**.
2.  **Visualización**:
    - Verifique que en la lista principal aparezca el nuevo equipo con su fotografía.
3.  **Estado sin Foto**:
    - Cree un equipo sin tomar fotografía y verifique que se muestre el icono azul predeterminado.
4.  **Persistencia**:
    - Cierre la app y vuelva a entrar; las fotos deben permanecer visibles.
