# Plan de Mejoras de Experiencia de Usuario (UX) y Estabilidad

Este plan se enfoca en pulir la interfaz, mejorar el feedback visual al usuario y optimizar la carga de datos para que la aplicación se sienta más profesional y fluida.

## User Review Required

> [!NOTE]
> Se añadirán gestos táctiles (Swipe-to-Refresh) que permitirán a los usuarios actualizar los datos manualmente sin tener que cerrar y abrir la pantalla.

## Proposed Changes

### [UI/UX] Feedback Visual y Gestos

#### [MODIFY] [activity_orders.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_orders.xml) y [activity_admin_dashboard.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/activity_admin_dashboard.xml)
- Implementar `SwipeRefreshLayout` envolviendo las listas principales. Esto permite que el usuario "hale" hacia abajo para sincronizar con la nube.

#### [MODIFY] [OrdersActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/OrdersActivity.kt)
- Integrar la lógica del `SwipeRefreshLayout` para disparar `fetchOrdersFromCloud` y mostrar el indicador de carga correctamente.

#### [MODIFY] [AdminDashboardActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/AdminDashboardActivity.kt)
- Añadir soporte para actualización gestual en el panel de administrador.

---

### [Performance] Optimización de Carga

#### [MODIFY] [OrdenRepository.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/repositories/OrdenRepository.kt)
- Añadir logs detallados de sincronización para facilitar la depuración futura.
- Asegurar que las consultas pesadas no bloqueen el hilo principal (UI Thread).

---

### [Polished UI] Detalles de Diseño

#### [MODIFY] [item_order.xml](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/res/layout/item_order.xml)
- Mejorar el contraste de los textos secundarios.
- Ajustar márgenes para un look más "Material 3".

## Verification Plan

### Automated Tests
- `gradlew assembleDebug`: Confirmar que las nuevas dependencias (SwipeRefreshLayout) se resuelven bien.

### Manual Verification
1.  **Gesto de Actualización**: Ir a la lista de órdenes y deslizar hacia abajo. Verificar que el círculo de carga aparece y los datos se refrescan.
2.  **Dashboard Admin**: Verificar que el botón de auto-asignación y la actualización manual funcionan sincronizadamente.
3.  **Look & Feel**: Abrir la app en modo oscuro/claro para asegurar consistencia visual en las tarjetas.
