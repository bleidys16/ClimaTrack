# Plan de Mejora: Sección de Equipos con Imágenes

Este plan detalla los cambios necesarios para agregar una imagen representativa a cada equipo en la sección de **Gestión de Equipos**, permitiendo capturar fotografías reales de las unidades y visualizarlas en la lista principal.

## User Review Required

> [!IMPORTANT]
> Se añadirá la capacidad de capturar fotos de los equipos. Si un equipo no tiene foto, se mostrará un icono predeterminado estilizado.

> [!NOTE]
> Se incrementará la versión de la base de datos para incluir la columna de imagen. Esto es necesario para la persistencia de las rutas de las fotos.

## Proposed Changes

### [Capa de Datos]

#### [MODIFY] [Models.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/models/Models.kt)
- Añadir el campo `imagenPath: String? = null` a la clase `Equipo`.

#### [MODIFY] [DatabaseHelper.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/database/DatabaseHelper.kt)
- Definir `COL_EQUIPO_IMAGEN = "imagen"`.
- Actualizar la sentencia `CREATE TABLE equipos` para incluir la columna de imagen.
- Incrementar `DATABASE_VERSION`.

#### [MODIFY] [EquipoRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/repositories/EquipoRepository.kt)
- Actualizar los métodos `create`, `update` y `cursorToEquipo` para manejar la persistencia del campo `imagen`.

---

### [Interfaz de Usuario (UI)]

#### [MODIFY] [item_equipment.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/item_equipment.xml)
- Rediseñar el elemento de la lista para incluir una vista de imagen (miniatura) más prominente a la izquierda o arriba de la información.
- Aplicar bordes redondeados a la imagen para mantener la estética moderna.

#### [MODIFY] [activity_equipment_form.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_equipment_form.xml)
- Añadir una sección de "Fotografía del Equipo" con un `ImageView` de previsualización y un botón para abrir la cámara.

---

### [Lógica de Actividades]

#### [MODIFY] [EquipmentAdapter.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/adapters/EquipmentAdapter.kt)
- Lógica para cargar la imagen desde el almacenamiento local si existe, o mostrar un placeholder si no hay ruta guardada.

#### [MODIFY] [EquipmentFormActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/EquipmentFormActivity.kt)
- Implementar la funcionalidad de captura de fotografía similar a la de `EvidenceActivity`, permitiendo asociar la imagen al equipo antes de guardar.

## Verification Plan

### Manual Verification
1. **Creación de Equipo**:
   - Abrir el formulario de equipos.
   - Tomar una fotografía del equipo.
   - Completar los datos y guardar.
   - Verificar que el equipo aparezca en la lista con su foto.
2. **Edición de Equipo**:
   - Abrir un equipo existente.
   - Cambiar la foto y guardar.
   - Verificar la actualización en la lista.
3. **Estado sin imagen**:
   - Crear un equipo sin tomar foto.
   - Verificar que se muestre el icono predeterminado.
