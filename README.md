# ClimaTrack 🌬️

ClimaTrack es una plataforma móvil integral diseñada para optimizar la gestión de servicios técnicos en sistemas de climatización. La solución ofrece una arquitectura multi-rol que conecta a Administradores, Técnicos de campo y Clientes en un ecosistema digital eficiente.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)
![Android API](https://img.shields.io/badge/API-24%2B-green.svg)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20Storage-orange.svg)
![Build Status](https://img.shields.io/badge/Build-Success-brightgreen.svg)

---

## 👥 Arquitectura Multi-Rol

### 👑 Panel Administrativo
- **Gestión de Personal**: Visualización de técnicos activos y su ubicación GPS en tiempo real.
- **Asignación Inteligente**: Sistema de asignación automática de órdenes basado en la carga de trabajo.
- **Inteligencia de Fallas**: Análisis predictivo y reportes de incidencias comunes.
- **Control Total**: Registro de nuevos técnicos y supervisión de todas las órdenes del sistema.

### 🛠️ Herramientas para el Técnico
- **Flujo de Trabajo Digital**: Recepción de órdenes, inicio de servicio y registro de mantenimientos (Preventivo/Correctivo).
- **Geolocalización Activa**: Sistema de "Activo/Inactivo" con seguimiento GPS para reportar disponibilidad.
- **Multimedia y Evidencias**: Captura de fotos y anexos vinculados directamente a la orden de trabajo.
- **Escaneo QR**: Identificación rápida de equipos mediante códigos QR para acceder a su ficha técnica.
- **Chat Integrado**: Comunicación directa con el cliente o administración para coordinar detalles.

### 📱 Portal del Cliente
- **Solicitud de Servicios**: Formulario simplificado para requerir asistencia técnica.
- **Seguimiento en Vivo**: Visualización en mapa del técnico asignado cuando este inicia el servicio.
- **Aprobación Digital**: Firma y validación de cotizaciones y reportes finales desde la app.
- **Historial de Equipos**: Acceso a la hoja de vida de todos los equipos registrados bajo su nombre.

---

## 🚀 Funcionalidades Destacadas

- **Offline-First**: Capacidad de trabajar sin conexión a internet. Los datos se guardan en SQLite y se sincronizan automáticamente con Firebase al recuperar la señal.
- **Generación de Reportes PDF**: Creación instantánea de actas técnicas profesionales listas para compartir.
- **Notificaciones Push**: Alertas en tiempo real sobre nuevas asignaciones o cambios de estado.
- **Sincronización Inteligente**: Uso de `WorkManager` para tareas de sincronización pesadas (imágenes y backups) en segundo plano.

---

## 🛠️ Stack Tecnológico

- **Core**: Kotlin + Jetpack View Binding.
- **Base de Datos**: SQLite (Local) + Firebase Firestore (Cloud Sync).
- **Autenticación**: Firebase Auth (Email & Password).
- **Multimedia**: Firebase Storage.
- **Mapas y Ubicación**: Google Maps SDK + Play Services Location.
- **AI & Visión**: ML Kit Barcode Scanning (QR).
- **UI/UX**: Material Design 3 + Fuentes Geist.

---

## ⚙️ Configuración del Proyecto

Para ejecutar este proyecto, es necesario configurar los servicios externos:

### 1. Firebase 🔥
1. Crea un proyecto en la [Consola de Firebase](https://console.firebase.google.com/).
2. Habilita los servicios: **Authentication** (Email/Password), **Cloud Firestore**, y **Storage**.
3. Registra la app con el package name `com.example.climatrack`.
4. Descarga el archivo `google-services.json` y colócalo en la carpeta `/app/`.

### 2. Google Maps API 🗺️
1. Obtén una clave de API en [Google Cloud Console](https://console.cloud.google.com/).
2. Habilita **Maps SDK for Android**.
3. Agrega tu clave en el archivo `app/src/main/AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="TU_API_KEY_AQUI" />
   ```

### 3. Instalación
1. Clona el repositorio:
   ```bash
   git clone https://github.com/bleidys16/ClimaTrack.git
   ```
2. Abre el proyecto en **Android Studio Koala (o superior)**.
3. Sincroniza Gradle y ejecuta la variante `debug`.

---

## 🔧 Estructura de Paquetes

```text
com.example.climatrack
  ├── activities/      # Actividades principales segmentadas por rol
  ├── adapters/        # Adaptadores para Listas, Chats e Historial
  ├── database/        # Helpers de SQLite
  ├── models/          # Entidades (Usuario, Orden, Equipo, Mantenimiento)
  ├── repositories/    # Lógica de sincronización Local/Cloud
  ├── utils/           # Ayudantes de Firebase, Sesión y Generación de PDF
  └── services/        # Firebase Messaging y Workers de sincronización
```

---
Desarrollado para la transformación digital del mantenimiento técnico. 🌬️⚒️
