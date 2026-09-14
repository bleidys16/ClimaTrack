# Casos de Prueba - ClimaTrack

Este documento detalla los casos de prueba para la aplicación ClimaTrack, cubriendo las funcionalidades principales del sistema de gestión de mantenimiento.

## 1. Módulo de Autenticación

| ID | Caso de Prueba | Precondición | Pasos | Resultado Esperado |
|:---|:---|:---|:---|:---|
| CP-01 | Inicio de sesión exitoso | App instalada, usuario registrado. | 1. Ingresar usuario válido.<br>2. Ingresar contraseña válida.<br>3. Tocar botón "Ingresar". | El sistema redirige al Dashboard y muestra mensaje de bienvenida. |
| CP-02 | Inicio de sesión fallido (Campos vacíos) | Pantalla de login abierta. | 1. Dejar campos vacíos.<br>2. Tocar botón "Ingresar". | Se muestra mensaje: "Por favor complete todos los campos". |
| CP-03 | Inicio de sesión fallido (Credenciales incorrectas) | Pantalla de login abierta. | 1. Ingresar usuario inexistente.<br>2. Ingresar contraseña incorrecta.<br>3. Tocar "Ingresar". | Se muestra mensaje: "Usuario o contraseña inválidos". |

## 2. Gestión de Órdenes y Dashboard

| ID | Caso de Prueba | Precondición | Pasos | Resultado Esperado |
|:---|:---|:---|:---|:---|
| CP-04 | Visualización de estadísticas | Usuario logueado. | 1. Observar el Dashboard. | Se muestran contadores correctos de órdenes Pendientes, En Proceso y Finalizadas. |
| CP-05 | Listado de órdenes asignadas | Usuario logueado. | 1. Tocar botón "Órdenes" en Dashboard o menú. | Se muestra lista de órdenes asignadas al técnico con número, cliente y fecha. |
| CP-06 | Filtro de órdenes por estado | Listado de órdenes abierto. | 1. Seleccionar un filtro (ej: "Pendiente"). | La lista se hace dinámica mostrando solo las órdenes que coinciden con el estado. |

## 3. Registro de Mantenimiento

| ID | Caso de Prueba | Precondición | Pasos | Resultado Esperado |
|:---|:---|:---|:---|:---|
| CP-07 | Registro de diagnóstico y trabajo | Orden seleccionada en estado "Pendiente". | 1. Entrar a la orden.<br>2. Completar Diagnóstico y Trabajo Realizado.<br>3. Seleccionar estado del equipo.<br>4. Guardar. | Los datos se guardan, la orden cambia a "En Proceso" y se muestra confirmación. |
| CP-08 | Validación de campos obligatorios | Formulario de mantenimiento abierto. | 1. Intentar guardar sin llenar el diagnóstico. | Se impide el guardado y se resaltan los campos obligatorios faltantes. |

## 4. Evidencias y Repuestos

| ID | Caso de Prueba | Precondición | Pasos | Resultado Esperado |
|:---|:---|:---|:---|:---|
| CP-09 | Captura de evidencia fotográfica | Mantenimiento en curso. | 1. Abrir sección "Evidencias".<br>2. Tomar foto con la cámara.<br>3. Confirmar guardado. | La foto se almacena asociada a la orden y se visualiza en la galería de la orden. |
| CP-10 | Adición de repuestos | Mantenimiento en curso. | 1. Abrir "Repuestos".<br>2. Seleccionar repuesto del catálogo.<br>3. Ingresar cantidad.<br>4. Guardar. | El repuesto se suma al detalle del mantenimiento. |

## 5. Cierre de Orden y Aprobación

| ID | Caso de Prueba | Precondición | Pasos | Resultado Esperado |
|:---|:---|:---|:---|:---|
| CP-11 | Registro de ubicación GPS | Orden en ejecución. | 1. Tocar icono de ubicación. | El sistema captura las coordenadas actuales y las asocia a la orden. |
| CP-12 | Aprobación del cliente | Mantenimiento finalizado. | 1. Abrir pantalla de Aprobación.<br>2. El cliente ingresa su nombre.<br>3. Guardar. | La orden cambia a estado "Finalizada" y se genera el registro de aceptación. |

## 6. Gestión de Equipos

| ID | Caso de Prueba | Precondición | Pasos | Resultado Esperado |
|:---|:---|:---|:---|:---|
| CP-13 | Registro de nuevo equipo | Pantalla de Equipos abierta. | 1. Tocar botón "Nuevo".<br>2. Completar datos (Marca, Modelo, Serial).<br>3. Guardar. | El equipo se crea exitosamente y aparece en la lista. |
| CP-14 | Búsqueda de equipos | Lista de equipos con datos cargados. | 1. Escribir en la barra de búsqueda (ej: "Samsung"). | Se filtran los resultados mostrando solo coincidencias con el texto ingresado. |
