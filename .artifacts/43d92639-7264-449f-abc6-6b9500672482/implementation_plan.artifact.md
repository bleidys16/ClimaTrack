# Plan de Mejora de Responsividad y Edge-to-Edge

Este plan aborda los problemas visuales reportados, específicamente la superposición de elementos con la barra de estado y la falta de adaptación fluida a diferentes alturas de dispositivos (como el Samsung A35).

## Cambios Propuestos

### Componentes de Interfaz (Layouts)

Se estandarizarán los encabezados para usar `AppBarLayout` como el receptor de los insets del sistema, asegurando que el contenido del `Toolbar` no se vea comprimido.

#### [MODIFY] [activity_client_dashboard.xml](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/res/layout/activity_client_dashboard.xml)
- Añadir ID `appBarLayout` al contenedor principal del encabezado.
- Cambiar `layout_height` del `Toolbar` a `wrap_content` con `minHeight="64dp"`.

#### [MODIFY] [activity_admin_dashboard.xml](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/res/layout/activity_admin_dashboard.xml)
- Asegurar ID `appBarLayout`.
- Cambiar `layout_height` del `Toolbar` a `wrap_content`.

#### [MODIFY] [activity_dashboard.xml](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/res/layout/activity_dashboard.xml)
- Asegurar ID `appBarLayout`.
- Cambiar `layout_height` del `Toolbar` a `wrap_content`.

#### [MODIFY] [activity_maintenance.xml](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/res/layout/activity_maintenance.xml)
- Añadir ID `appBarLayout`.

### Lógica de Actividades (Kotlin)

Se actualizará la llamada a `setupEdgeToEdge` para pasar el `AppBarLayout` en lugar del `Toolbar` directamente, permitiendo un espaciado correcto bajo la barra de estado.

#### [MODIFY] [ClientDashboardActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/ClientDashboardActivity.kt)
- Usar `binding.appBarLayout`.

#### [MODIFY] [AdminDashboardActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/AdminDashboardActivity.kt)
- Usar `binding.appBarLayout`.

#### [MODIFY] [DashboardActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/DashboardActivity.kt)
- Usar `binding.appBarLayout`.

#### [MODIFY] [MaintenanceActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/MaintenanceActivity.kt)
- Implementar llamada a `setupEdgeToEdge(binding.root, binding.appBarLayout)`.

#### [MODIFY] [LoginActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/LoginActivity.kt)
- Implementar llamada a `setupEdgeToEdge(binding.root)`.

#### [MODIFY] [RegisterActivity.kt](file:///C:/Users/Aprendiz/ClimaTrack/app/src/main/java/com/example/climatrack/activities/RegisterActivity.kt)
- Implementar llamada a `setupEdgeToEdge(binding.root)`.

## Verificación Plan

### Manual Verification
- Desplegar la app en el dispositivo Samsung A35.
- Verificar que el título "Mi ClimaTrack" y los iconos de logout/perfil no estén tapados por la barra de estado.
- Verificar que al abrir el teclado en Login/Registro, el contenido se desplace correctamente (si aplica).
