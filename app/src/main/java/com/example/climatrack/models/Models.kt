package com.example.climatrack.models

data class Usuario(
    val id: Int = 0,
    val usuario: String,
    val password: String,
    val nombre: String,
    val rol: String
)

data class Cliente(
    val id: Int = 0,
    val nombre: String,
    val telefono: String?,
    val direccion: String?,
    val email: String?
)

data class Equipo(
    val id: Int = 0,
    val codigo: String,
    val tipo: String,
    val marca: String,
    val modelo: String,
    val serial: String?,
    val capacidad: String?,
    val ubicacion: String?,
    val clienteId: Int,
    val estado: String,
    val imagenPath: String? = null
)

data class Orden(
    val id: Int = 0,
    val numero: String,
    val fecha: String,
    val clienteId: Int,
    val equipoId: Int,
    val tecnicoId: Int,
    val tipoServicio: String,
    val descripcion: String?,
    val estado: String
)

data class OrdenInfo(
    val id: Int,
    val numero: String,
    val fecha: String,
    val clienteNombre: String,
    val equipoNombre: String,
    val tipoServicio: String,
    val estado: String
)

data class Mantenimiento(
    val id: Int = 0,
    val ordenId: Int,
    val fecha: String,
    val diagnostico: String,
    val trabajoRealizado: String,
    val observaciones: String?,
    val recomendaciones: String?,
    val estadoEquipo: String,
    val tiempoEmpleado: String
)

data class MantenimientoInfo(
    val id: Int,
    val ordenNumero: String,
    val fecha: String,
    val diagnostico: String,
    val trabajoRealizado: String,
    val tipoServicio: String,
    val tecnicoNombre: String
)

data class Repuesto(
    val id: Int = 0,
    val nombre: String,
    val codigo: String,
    val unidad: String?,
    val precio: Double = 0.0
)

data class DetalleRepuesto(
    val id: Int = 0,
    val mantenimientoId: Int,
    val repuestoId: Int,
    val cantidad: Int,
    val observacion: String?,
    val precioHistorico: Double = 0.0
)

data class DetalleRepuestoInfo(
    val id: Int,
    val repuestoNombre: String,
    val repuestoCodigo: String,
    val repuestoUnidad: String?,
    val cantidad: Int,
    val precio: Double,
    val observacion: String?
)

data class Evidencia(
    val id: Int = 0,
    val ordenId: Int,
    val rutaFoto: String,
    val fecha: String
)

data class Aprobacion(
    val id: Int = 0,
    val ordenId: Int,
    val cliente: String,
    val aceptado: Int, // 0 or 1
    val fecha: String
)

data class Ubicacion(
    val id: Int = 0,
    val ordenId: Int,
    val latitud: Double,
    val longitud: Double,
    val direccion: String? = null,
    val fecha: String
)
