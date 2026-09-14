package com.example.climatrack.models

data class Usuario(
    val id: String = "",
    val usuario: String = "",
    val password: String = "",
    val nombre: String = "",
    val rol: String = "",
    val email: String? = null,
    val telefono: String? = null,
    val isActive: Int = 0,
    val workStartTime: String? = null,
    val workEndTime: String? = null,
    val lastLat: Double? = null,
    val lastLon: Double? = null,
    val imagenPerfil: String? = null,
    val fcmToken: String? = null
)

data class Cliente(
    val id: String = "",
    val nombre: String = "",
    val telefono: String? = null,
    val direccion: String? = null,
    val email: String? = null
)

data class Equipo(
    val id: String = "",
    val codigo: String = "",
    val nombre: String? = null,
    val tipo: String = "",
    val marca: String = "",
    val modelo: String = "",
    val serial: String? = null,
    val capacidad: String? = null,
    val ubicacion: String? = null,
    val clienteId: String = "",
    val estado: String = "",
    val imagenPath: String? = null
)

data class Orden(
    val id: String = "",
    val numero: String = "",
    val fecha: String = "",
    val clienteId: String = "",
    val equipoId: String = "",
    val tecnicoId: String? = null,
    val tipoServicio: String = "",
    val descripcion: String? = null,
    val estado: String = "",
    val precioServicio: Double = 0.0,
    val latitudCliente: Double? = null,
    val longitudCliente: Double? = null,
    val direccionExacta: String? = null,
    val firmaBase64: String? = null,
    val isSynced: Int = 0,
    val calificacion: Int = 0,
    val comentario: String? = null,
    val tecnicoLat: Double? = null,
    val tecnicoLon: Double? = null,
    val clienteEmail: String? = null,
    val precioMantenimiento: Double = 0.0,
    val observacionCliente: String? = null
)

data class OrdenInfo(
    val id: String = "",
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
    val tecnicoLon: Double? = null,
    val latitudCliente: Double? = null,
    val longitudCliente: Double? = null,
    val clienteEmail: String? = null,
    val precioMantenimiento: Double = 0.0,
    val observacionCliente: String? = null
)

data class Mantenimiento(
    val id: String = "",
    val ordenId: String = "",
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
    val id: String = "",
    val ordenNumero: String = "",
    val fecha: String = "",
    val diagnostico: String = "",
    val trabajoRealizado: String = "",
    val tipoServicio: String = "",
    val tecnicoNombre: String = ""
)

data class Repuesto(
    val id: String = "",
    val nombre: String = "",
    val codigo: String = "",
    val unidad: String? = null,
    val precio: Double = 0.0
)

data class DetalleRepuesto(
    val id: String = "",
    val mantenimientoId: String = "",
    val repuestoId: String = "",
    val cantidad: Int = 0,
    val observacion: String? = null,
    val precioHistorico: Double = 0.0,
    val precioUnitario: Double = 0.0,
    val isSynced: Int = 0
)

data class DetalleRepuestoInfo(
    val id: String = "",
    val repuestoNombre: String = "",
    val repuestoCodigo: String = "",
    val repuestoUnidad: String? = null,
    val cantidad: Int = 0,
    val precio: Double = 0.0,
    val observacion: String? = null
)

data class Evidencia(
    val id: String = "",
    val ordenId: String = "",
    val rutaFoto: String = "",
    val fecha: String = "",
    val isSynced: Int = 0
)

data class Aprobacion(
    val id: String = "",
    val ordenId: String = "",
    val cliente: String = "",
    val aceptado: Int = 0,
    val fecha: String = ""
)

data class Ubicacion(
    val id: String = "",
    val ordenId: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val direccion: String? = null,
    val fecha: String = ""
)

data class ActividadTecnico(
    val id: String = "",
    val tecnicoId: String = "",
    val fecha: String = "",
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
)

data class Mensaje(
    val id: String = "",
    val ordenId: String = "",
    val remitenteId: String = "",
    val nombreRemitente: String = "",
    val texto: String = "",
    val fecha: String = ""
)

data class TecnicoStats(
    val id: String = "",
    val nombre: String = "",
    val trabajosRealizados: Int = 0,
    val isActive: Int = 0,
    val email: String? = null,
    val telefono: String? = null,
    val promedioCalificacion: Double = 0.0
)
