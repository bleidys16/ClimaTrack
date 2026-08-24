# Plan de Mejora Visual y UX - ClimaTrack

Este plan detalla el rediseño estético de la aplicación móvil **ClimaTrack**, enfocándose en la unificación visual, modernización de componentes mediante Material Design 3 y mejora de la experiencia de usuario para técnicos de campo.

## User Review Required

> [!IMPORTANT]
> Se mantendrá intacta la lógica de negocio, base de datos y funcionalidad actual. Los cambios son puramente estéticos y de organización visual.

> [!TIP]
> Se utilizará Material Design 3 para garantizar una apariencia moderna y responsiva.

## Proposed Changes

### Identidad Visual y Recursos

#### [MODIFY] [colors.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/values/colors.xml)
*   Actualizar paleta de azules: Primary (#1565C0), Dark (#0D47A1), Light (#E3F2FD).
*   Estandarizar colores de estado: Pendiente (Naranja), En Proceso (Azul), Finalizada (Verde), Error (Rojo).
*   Agregar colores de fondo: Gris claro (#F5F7FA) y Blanco puro.

#### [MODIFY] [themes.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/values/themes.xml)
*   Refactorizar a `Theme.Material3.DayNight.NoActionBar`.
*   Definir estilos globales para `MaterialCardView` (bordes redondeados de 16dp), `Button`, y `TextInputLayout`.

#### [MODIFY] [strings.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/values/strings.xml)
*   Agregar textos para subtítulos, etiquetas de sección y mensajes de estados vacíos.

#### [NEW] Drawables y Formas
*   Crear `bg_chip_status.xml` para badges de colores.
*   Crear formas redondeadas para botones y campos de entrada.

---

### Pantallas Principales

#### [MODIFY] [activity_splash.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_splash.xml)
*   Rediseño limpio: Fondo claro, logo centrado, tipografía moderna y subtítulo empresarial.

#### [MODIFY] [activity_login.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_login.xml)
*   Agregar ilustración técnica.
*   Campos con iconos, bordes redondeados y opción de visualización de contraseña.
*   Botón "INGRESAR" prominente y estilizado.

#### [MODIFY] [activity_dashboard.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_dashboard.xml)
*   Cabecera con avatar y bienvenida personalizada.
*   Tarjetas de resumen con iconos y números grandes.
*   Sección de accesos rápidos con botones tipo "grid" o lista visualmente atractiva.

#### [NEW] Bottom Navigation
*   Implementar `menu_bottom_nav.xml`.
*   Integrar `BottomNavigationView` en las actividades principales (Dashboard, Orders, Equipment, History).

---

### Gestión y Formularios

#### [MODIFY] [activity_orders.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_orders.xml) y [item_order.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/item_order.xml)
*   Uso de `TabLayout` para filtrar estados (Pendiente, Proceso, Finalizada).
*   Tarjetas de orden con chips de colores para el estado y jerarquía clara de información.

#### [MODIFY] [activity_equipment.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_equipment.xml) e [item_equipment.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/item_equipment.xml)
*   Barra de búsqueda moderna.
*   Tarjetas con iconos descriptivos del tipo de equipo y badges de operatividad.

#### [MODIFY] [activity_maintenance.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_maintenance.xml)
*   Organización por secciones: Info Servicio, Diagnóstico, Trabajo, Observaciones.
*   Resumen de orden en la parte superior.
*   Campos con `TextInputLayout` consistentes.

#### [MODIFY] Pantallas de Evidencia, Repuestos, Ubicación y Aprobación
*   Galería de fotos en grid de 2 columnas para Evidencias.
*   Tarjetas detalladas para Repuestos.
*   Interfaz clara para GPS y formulario de firma/aprobación con resumen final.

---

### Estados de Interfaz y Consistencia

#### [NEW] Empty States y Loading
*   Diseñar layouts reutilizables para "Sin datos" y "Cargando" con ilustraciones discretas.

## Verification Plan

### Manual Verification
*   Navegación completa por todas las pantallas para verificar la consistencia visual.
*   Prueba de los estados de color en órdenes y equipos.
*   Verificación de que el formulario de mantenimiento sigue validando campos obligatorios.
*   Confirmación de que el Login funciona con `tecnico01` / `123456`.
*   Comprobación de responsividad en diferentes tamaños de pantalla (Vertical/Horizontal).
