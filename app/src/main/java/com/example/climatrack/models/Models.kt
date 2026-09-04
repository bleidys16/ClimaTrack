package com.example.climatrack.models

data class Usuario(
    val id: Int = 0,
    val usuario: String = "",
    val password: String = "",
    val nombre: String = "",
    val rol: String = "",
    val email: String? = null,
    val telefono: String? = null,
    val isActive: Int = 0, // 0: Inactivo, 1: Activo
    val workStartTime: String? = null,
    val workEndTime: String? = null,
    val lastLat: Double? = null,
    val lastLon: Double? = null,
    val imagenPerfil: String? = null,
    val fcmToken: String? = null
)

data class Cliente(
    val id: Int = 0,
    val nombre: String = "",
    val telefono: String? = null,
    val direccion: String? = null,
    val email: String? = null
)

data class Equipo(
    val id: Int = 0,
    val codigo: String = "",
    val tipo: String = "",
    val marca: String = "",
    val modelo: String = "",
    val serial: String? = null,
    val capacidad: String? = null,
    val ubicacion: String? = null,
    val clienteId: Int = 0,
    val estado: String = "",
    val imagenPath: String? = null
)

data class Orden(
    val id: Int = 0,
    val numero: String = "",
    val fecha: String = "",
    val clienteId: Int = 0,
    val equipoId: Int = 0,
    val tecnicoId: Int? = null,
    val tipoServicio: String = "",
    val descripcion: String? = null,
    val estado: String = "",
    val precioServicio: Double = 0.0,
    val latitudCliente: Double? = null,
    val longitudCliente: Double? = null,
    val direccionExacta: String? = null,
    val firmaBase64: String? = null,
    val isSynced: Int = 0, // 0: Local, 1: Sincronizado
    val calificacion: Int = 0, // 1-5
    val comentario: String? = null,
    val tecnicoLat: Double? = null,
    val tecnicoLon: Double? = null
)

data class OrdenInfo(
    val id: Int = 0,
    val numero: String = "",
    val fecha: String = "",
    val clienteNombre: String = "",
    val equipoNombre: String = "",
    val tipoServicio: String = "",
    val estado: String = "",
    val tecnicoNombre: String? = null,
    val precioServicio: Double = 0.0,
    val equipoMarca: String? = null,
    val equipoModelo: String? = null,
    val descripcion: String? = null,
    val direccion: String? = null,
    val calificacion: Int = 0,
    val comentario: String? = null,
    val firmaBase64: String? = null,
    val tecnicoLat: Double? = null,
    val tecnicoLon: Double? = null
)

data class Mantenimiento(
    val id: Int = 0,
    val ordenId: Int = 0,
    val fecha: String = "",
    val diagnostico: String = "",
    val trabajoRealizado: String = "",
    val observaciones: String? = null,
    val recomendaciones: String? = null,
    val estadoEquipo: String = "",
    val tiempoEmpleado: String = "",
    val isSynced: Int = 0
)

data class MantenimientoInfo(
    val id: Int = 0,
    val ordenNumero: String = "",
    val fecha: String = "",
    val diagnostico: String = "",
    val trabajoRealizado: String = "",
    val tipoServicio: String = "",
    val tecnicoNombre: String = ""
)

data class Repuesto(
    val id: Int = 0,
    val nombre: String = "",
    val codigo: String = "",
    val unidad: String? = null,
    val precio: Double = 0.0
)

data class DetalleRepuesto(
    val id: Int = 0,
    val mantenimientoId: Int = 0,
    val repuestoId: Int = 0,
    val cantidad: Int = 0,
    val observacion: String? = null,
    val precioHistorico: Double = 0.0,
    val isSynced: Int = 0
)

data class DetalleRepuestoInfo(
    val id: Int = 0,
    val repuestoNombre: String = "",
    val repuestoCodigo: String = "",
    val repuestoUnidad: String? = null,
    val cantidad: Int = 0,
    val precio: Double = 0.0,
    val observacion: String? = null
)

data class Evidencia(
    val id: Int = 0,
    val ordenId: Int = 0,
    val rutaFoto: String = "",
    val fecha: String = "",
    val isSynced: Int = 0
)

data class Aprobacion(
    val id: Int = 0,
    val ordenId: Int = 0,
    val cliente: String = "",
    val aceptado: Int = 0, // 0 or 1
    val fecha: String = ""
)

data class Ubicacion(
    val id: Int = 0,
    val ordenId: Int = 0,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val direccion: String? = null,
    val fecha: String = ""
)

data class ActividadTecnico(
    val id: Int = 0,
    val tecnicoId: Int = 0,
    val fecha: String = "",
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
)

data class Mensaje(
    val id: Int = 0,
    val ordenId: Int = 0,
    val remitenteId: Int = 0,
    val nombreRemitente: String = "",
    val texto: String = "",
    val fecha: String = ""
)

data class TecnicoStats(
    val id: Int = 0,
    val nombre: String = "",
    val trabajosRealizados: Int = 0,
    val isActive: Int = 0,
    val email: String? = null,
    val telefono: String? = null,
    val promedioCalificacion: Double = 0.0
)
