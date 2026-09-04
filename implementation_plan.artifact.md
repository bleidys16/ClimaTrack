# Plan de Implementación - Botón de Retroceso en Solicitud de Servicio

Este plan detalla la adición de un botón de retroceso (flecha) en la pantalla de solicitud de servicio del cliente (`OrderRequestActivity`) para mejorar la navegación y permitir al usuario volver al inicio de forma rápida.

## Proposed Changes

### [Interfaz de Usuario]

#### [MODIFY] [activity_order_request.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_order_request.xml)
- Envolver el contenido actual en un `CoordinatorLayout`.
- Añadir un `AppBarLayout` y un `MaterialToolbar` en la parte superior.
- Configurar el `MaterialToolbar` con un icono de navegación (flecha de retroceso).
- Ajustar el padding y las restricciones del `NestedScrollView` para que se desplace debajo de la barra de herramientas.

### [Lógica de Actividad]

#### [MODIFY] [OrderRequestActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/OrderRequestActivity.kt)
- Configurar el `Toolbar` en el método `onCreate`.
- Implementar el listener para el clic en el icono de navegación que llame a `onBackPressedDispatcher.onBackPressed()` o simplemente `finish()`.
- Asegurar que `setupEdgeToEdge` maneje correctamente el nuevo `Toolbar`.

## Verification Plan

### Manual Verification
1. Iniciar sesión como **Cliente**.
2. Presionar el botón **"SOLICITAR SERVICIO"** en el dashboard.
3. Verificar que aparezca una flecha de retroceso en la parte superior izquierda de la nueva pantalla.
4. Presionar la flecha y confirmar que se regrese correctamente al **ClientDashboardActivity**.
5. Verificar que el diseño se vea bien y no haya solapamientos con la barra de estado.
