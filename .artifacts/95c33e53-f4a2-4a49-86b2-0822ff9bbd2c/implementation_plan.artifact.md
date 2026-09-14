# Plan de Mitigación para Redes Restringidas (Internet SENA)

Este plan tiene como objetivo hacer que ClimaTrack sea funcional en entornos con internet limitado o bloqueado (como el internet estudiantil del SENA), priorizando el funcionamiento local y la sincronización asíncrona.

## User Review Required

> [!IMPORTANT]
> Se han detectado bloqueos en el registro de usuarios debido a la dependencia directa de Firebase Auth. Si el internet del SENA bloquea los servidores de Google, el registro falla incluso si el dispositivo tiene señal.

## Proposed Changes

### [Infraestructura de Red]

#### [MODIFY] [network_security_config.xml](file:///C:/Users/camil/Downloads/ClimaTrack-main/ClimaTrack-main/app/src/main/res/xml/network_security_config.xml)
- Añadir confianza en certificados de usuario (`<certificates src="user" />`) para permitir que la app funcione si se instala un certificado de proxy del SENA.

### [Autenticación y Registro]

#### [MODIFY] [RegisterActivity.kt](file:///C:/Users/camil/Downloads/ClimaTrack-main/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/RegisterActivity.kt)
- **Registro Local Primero:** Guardar el usuario en la base de datos SQLite antes de intentar subirlo a Firebase.
- **Manejo de Excepciones de Red:** Si Firebase lanza una `FirebaseNetworkException`, el registro se considerará exitoso localmente y se notificará al usuario que está en "Modo Offline".

#### [MODIFY] [LoginActivity.kt](file:///C:/Users/camil/Downloads/ClimaTrack-main/ClimaTrack-main/app/src/main/java/com/example/climatrack/activities/LoginActivity.kt)
- **Login Híbrido:** Mejorar el fallback local para que el inicio de sesión sea instantáneo si no hay red, evitando esperas largas de timeout de Firebase.

### [Repositorios y Sincronización]

#### [MODIFY] [UsuarioRepository.kt](file:///C:/Users/camil/Downloads/ClimaTrack-main/ClimaTrack-main/app/src/main/java/com/example/climatrack/repositories/UsuarioRepository.kt)
- Modificar el método `login` para que busque tanto por `usuario` como por `email`.

#### [MODIFY] [SyncManager.kt](file:///C:/Users/camil/Downloads/ClimaTrack-main/ClimaTrack-main/app/src/main/java/com/example/climatrack/utils/SyncManager.kt)
- Ajustar las restricciones de `WorkManager` para que los reintentos sean exponenciales y no bloqueen el hilo principal.

## Verification Plan

### Manual Verification
1.  **Modo Avión:** Intentar registrar un usuario sin internet. Debería decir "Registro local exitoso".
2.  **Login Offline:** Iniciar sesión con el usuario creado en el paso 1 (aún sin internet). Debería entrar al Dashboard.
3.  **Simulación SENA:** Usar una red con proxy o firewall y verificar que la app no se congele al intentar sincronizar.
