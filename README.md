# ClimaTrack 🌬️

ClimaTrack es una solución integral para la gestión de mantenimientos de sistemas de climatización, diseñada específicamente para técnicos de campo. Permite llevar un control detallado de las órdenes de trabajo, equipos y el historial de servicios de manera eficiente y profesional.

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

- `activities/`: Contiene los controladores de la interfaz de usuario.
- `adapters/`: Adaptadores para el manejo de listas dinámicas (RecyclerView).
- `models/`: Definición de las entidades de datos (Equipo, Orden, Mantenimiento, etc.).
- `repositories/`: Capa de acceso a datos que encapsula la lógica de SQLite.
- `database/`: Helper para la creación y gestión de la versión de la base de datos.
- `utils/`: Clases de utilidad como el gestor de sesiones.

## 🔧 Configuración e Instalación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/bleidys16/ClimaTrack.git
   ```
2. Abrir el proyecto en **Android Studio (versión Koala o superior)**.
3. Sincronizar el proyecto con los archivos de Gradle.
4. Ejecutar en un dispositivo o emulador con **Android 7.0 (API 24)** o superior.

---
Desarrollado con ❤️ para la optimización de procesos técnicos.
