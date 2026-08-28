# ClimaTrack 🌬️

ClimaTrack es una solución integral para la gestión de mantenimientos de sistemas de climatización, diseñada específicamente para técnicos de campo. Permite llevar un control detallado de las órdenes de trabajo, equipos y el historial de servicios de manera eficiente y profesional.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)
![Android API](https://img.shields.io/badge/API-24%2B-green.svg)
![Build Status](https://img.shields.io/badge/Build-Success-brightgreen.svg)

## 🚀 Características Principales

- **Dashboard Informativo**: Resumen visual de órdenes pendientes, en proceso y finalizadas.
- **Gestión de Órdenes**: Listado detallado de tareas asignadas con estados actualizables.
- **Inventario de Equipos**: Registro y consulta de equipos con filtrado avanzado por estado y búsqueda por código/serie.
- **Registro de Mantenimiento**: Formulario completo para capturar diagnósticos, trabajos realizados, tiempos y observaciones.
- **Historial de Servicios**: Consulta de mantenimientos previos clasificados por tipo (Preventivos, Correctivos e Inspecciones).
- **Funcionalidad Offline-First**: Registro de datos local mediante SQLite que permite trabajar sin conexión a internet y sincronizar cambios posteriormente.
- **Gestión de Multimedia**: Captura de evidencias fotográficas vinculadas a las órdenes de trabajo.
- **Ubicación Geográfica**: Registro de la ubicación del servicio mediante GPS.
- **Aprobación Digital**: Módulo para la firma y aceptación del cliente al finalizar el servicio.

## 🛠️ Stack Tecnológico

- **Lenguaje**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: Material Design 3 / Jetpack View Binding
- **Base de Datos**: SQLite (Local persistence)
- **Arquitectura**: Clean Architecture simplificada con Patrón Repository
- **Herramientas de Diseño**: Fuentes Geist (Bold, Medium, Regular) y paleta de colores personalizada.

## 📁 Estructura del Proyecto

```text
com.example.climatrack

  ├── activities/      # Controladores de la interfaz de usuario
  ├── fragments/       # Componentes de UI reutilizables (opcional)
  ├── adapters/        # Manejo de listas dinámicas (RecyclerView)
  ├── models/          # Entidades de datos (Equipo, Orden, etc.)
  ├── database/        # Gestión de la base de datos SQLite
  ├── repositories/    # Lógica de acceso a datos
  ├── utils/           # Clases de utilidad y constantes
  └── services/        # Lógica de segundo plano (opcional)
```

## 🔧 Configuración e Instalación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/bleidys16/ClimaTrack.git
   ```
2. Abrir el proyecto en **Android Studio (versión Koala o superior)**.
3. Sincronizar el proyecto con los archivos de Gradle.
4. Ejecutar en un dispositivo o emulador con **Android 7.0 (API 24)** o superior.

## 📦 Instrucciones de Compilación (APK Firmado)

Para generar el APK de distribución para producción, siga estos pasos en Android Studio:

1. Vaya al menú superior: **Build > Generate Signed Bundle / APK...**
2. Seleccione **APK** y haga clic en **Next**.
3. Seleccione o cree un **Key store path** (archivo .jks).
4. Ingrese las contraseñas de la clave y del almacén.
5. Seleccione la variante de compilación **release**.
6. Elija la carpeta de destino y haga clic en **Finish**.
7. El APK generado se encontrará en la carpeta `app/release/`.

---
Desarrollado con ❤️ para la optimización de procesos técnicos.
