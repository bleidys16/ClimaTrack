# Plan de Corrección: Estabilidad de Inicio (App Crash/Hang)

Este plan aborda el problema reportado de que la aplicación no puede iniciar, centrándose en la robustez de la pantalla de bienvenida (`SplashActivity`) y la clase base de actividades (`BaseActivity`).

## User Review Required

> [!IMPORTANT]
> Se ha detectado una posible recursión infinita en las animaciones de la Splash Screen que podría estar bloqueando el hilo principal o causando un desbordamiento de pila en algunos dispositivos.

## Proposed Changes

### [Robustez en Inicio]

#### [MODIFY] [SplashActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/SplashActivity.kt)
- **Animaciones Seguras**: Reemplazar la recursión manual en `onAnimationEnd` por el uso de `repeatCount = INFINITE` en los animadores individuales.
- **Control de Ciclo de Vida**: Asegurar que las animaciones se detengan si la actividad es destruida.
- **Reducción de Tiempo**: Ajustar el tiempo de espera de 4s a 2.5s para una mejor percepción de rendimiento.

#### [MODIFY] [BaseActivity.kt](file:///C:/Users/Aprendiz/Downloads/ClimaTrack/app/src/main/java/com/example/climatrack/activities/BaseActivity.kt)
- **Null Safety**: Agregar verificaciones para evitar crashes en `findViewById` o `getChildAt`.
- **Validación de ViewGroup**: Asegurar que `TransitionManager` solo actúe sobre contenedores válidos.

## Verification Plan

### Manual Verification
- Iniciar la aplicación y verificar que la Splash Screen se muestre y transicione correctamente al Login o Dashboard.
- Probar el inicio en modo "Dark Theme" y "Light Theme".
- Verificar que no haya bloqueos en el Dashboard tras la navegación.
