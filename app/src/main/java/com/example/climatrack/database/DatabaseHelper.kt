package com.example.climatrack.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "climatrack.db"
        private const val DATABASE_VERSION = 33

        const val COL_SYNCED = "is_synced"

        // Tabla Usuarios
        const val TABLE_USUARIOS = "usuarios"
        const val COL_USUARIO_ID = "id" // TEXT UUID
        const val COL_USUARIO_USER = "usuario"
        const val COL_USUARIO_PASS = "password"
        const val COL_USUARIO_NOMBRE = "nombre"
        const val COL_USUARIO_ROL = "rol"
        const val COL_USUARIO_EMAIL = "email"
        const val COL_USUARIO_TEL = "telefono"
        const val COL_USUARIO_ACTIVE = "is_active"
        const val COL_USUARIO_WORK_START = "work_start_time"
        const val COL_USUARIO_WORK_END = "work_end_time"
        const val COL_USUARIO_LAT = "last_lat"
        const val COL_USUARIO_LON = "last_lon"
        const val COL_USUARIO_IMAGEN = "imagen_perfil"
        const val COL_USUARIO_FCM = "fcm_token"

        // Tabla Actividad Técnico
        const val TABLE_ACTIVIDAD = "actividad_tecnico"
        const val COL_ACT_ID = "id"
        const val COL_ACT_TECH_ID = "tecnico_id"
        const val COL_ACT_FECHA = "fecha"
        const val COL_ACT_INICIO = "hora_inicio"
        const val COL_ACT_FIN = "hora_fin"
        const val COL_ACT_LAT = "latitud"
        const val COL_ACT_LON = "longitud"

        // Tabla Clientes
        const val TABLE_CLIENTES = "clientes"
        const val COL_CLIENTE_ID = "id"
        const val COL_CLIENTE_NOMBRE = "nombre"
        const val COL_CLIENTE_TEL = "telefono"
        const val COL_CLIENTE_DIR = "direccion"
        const val COL_CLIENTE_EMAIL = "email"

        // Tabla Equipos
        const val TABLE_EQUIPOS = "equipos"
        const val COL_EQUIPO_ID = "id"
        const val COL_EQUIPO_COD = "codigo"
        const val COL_EQUIPO_NOMBRE = "nombre_personalizado"
        const val COL_EQUIPO_TIPO = "tipo"
        const val COL_EQUIPO_MARCA = "marca"
        const val COL_EQUIPO_MODELO = "modelo"
        const val COL_EQUIPO_SERIAL = "serial"
        const val COL_EQUIPO_CAPACIDAD = "capacidad"
        const val COL_EQUIPO_UBICACION = "ubicacion"
        const val COL_EQUIPO_CLIENTE_ID = "cliente_id"
        const val COL_EQUIPO_ESTADO = "estado"
        const val COL_EQUIPO_IMAGEN = "imagen_path"

        // Tabla Ordenes
        const val TABLE_ORDENES = "ordenes"
        const val COL_ORDEN_ID = "id"
        const val COL_ORDEN_NUM = "numero"
        const val COL_ORDEN_FECHA = "fecha"
        const val COL_ORDEN_CLIENTE_ID = "cliente_id"
        const val COL_ORDEN_EQUIPO_ID = "equipo_id"
        const val COL_ORDEN_TECNICO_ID = "tecnico_id"
        const val COL_ORDEN_TIPO = "tipo_servicio"
        const val COL_ORDEN_DESC = "descripcion"
        const val COL_ORDEN_ESTADO = "estado"
        const val COL_ORDEN_PRECIO = "precio"
        const val COL_ORDEN_LAT = "latitud"
        const val COL_ORDEN_LON = "longitud"
        const val COL_ORDEN_DIR_EXACTA = "direccion_exacta"
        const val COL_ORDEN_FIRMA = "firma"
        const val COL_ORDEN_CALIFICACION = "calificacion"
        const val COL_ORDEN_COMENTARIO = "comentario"
        const val COL_ORDEN_TECH_LAT = "tecnico_lat"
        const val COL_ORDEN_TECH_LON = "tecnico_lon"
        const val COL_ORDEN_PRECIO_MANT = "precio_mantenimiento"
        const val COL_ORDEN_OBS_CLI = "observacion_cliente"

        // Tabla Mantenimientos
        const val TABLE_MANTENIMIENTOS = "mantenimientos"
        const val COL_MANT_ID = "id"
        const val COL_MANT_ORDEN_ID = "orden_id"
        const val COL_MANT_FECHA = "fecha"
        const val COL_MANT_DIAG = "diagnostico"
        const val COL_MANT_TRABAJO = "trabajo_realizado"
        const val COL_MANT_OBS = "observaciones"
        const val COL_MANT_RECOM = "recomendaciones"
        const val COL_MANT_ESTADO_EQ = "estado_equipo"
        const val COL_MANT_TIEMPO = "tiempo_empleado"

        // Tabla Repuestos
        const val TABLE_REPUESTOS = "repuestos"
        const val COL_REP_ID = "id"
        const val COL_REP_NOMBRE = "nombre"
        const val COL_REP_COD = "codigo"
        const val COL_REP_UNIDAD = "unidad"
        const val COL_REP_PRECIO = "precio"

        // Tabla Detalle Repuestos
        const val TABLE_DETALLE_REPUESTOS = "detalle_repuestos"
        const val COL_DET_ID = "id"
        const val COL_DET_MANT_ID = "mantenimiento_id"
        const val COL_DET_REP_ID = "repuesto_id"
        const val COL_DET_CANT = "cantidad"
        const val COL_DET_OBS = "observacion"
        const val COL_DET_PRECIO = "precio_historico"
        const val COL_DET_PRECIO_UNIT = "precio_unitario"

        // Tabla Evidencias
        const val TABLE_EVIDENCIAS = "evidencias"
        const val COL_EVI_ID = "id"
        const val COL_EVI_ORDEN_ID = "orden_id"
        const val COL_EVI_RUTA = "ruta_foto"
        const val COL_EVI_FECHA = "fecha"

        // Tabla Aprobaciones
        const val TABLE_APROBACIONES = "aprobaciones"
        const val COL_APROB_ID = "id"
        const val COL_APROB_ORDEN_ID = "orden_id"
        const val COL_APROB_CLIENTE = "cliente"
        const val COL_APROB_ACEPTADO = "aceptado"
        const val COL_APROB_FECHA = "fecha"

        // Tabla Ubicaciones
        const val TABLE_UBICACIONES = "ubicaciones"
        const val COL_UBI_ID = "id"
        const val COL_UBI_ORDEN_ID = "orden_id"
        const val COL_UBI_LAT = "latitud"
        const val COL_UBI_LON = "longitud"
        const val COL_UBI_DIR = "direccion"
        const val COL_UBI_FECHA = "fecha"

        // Tabla Chat
        const val TABLE_MENSAJES = "mensajes"
        const val COL_MSG_ID = "id"
        const val COL_MSG_ORDEN_ID = "orden_id"
        const val COL_MSG_REMITENTE_ID = "remitente_id"
        const val COL_MSG_REMITENTE_NOMBRE = "nombre_remitente"
        const val COL_MSG_TEXTO = "texto"
        const val COL_MSG_FECHA = "fecha"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USUARIOS ($COL_USUARIO_ID TEXT PRIMARY KEY, $COL_USUARIO_USER TEXT UNIQUE, $COL_USUARIO_PASS TEXT, $COL_USUARIO_NOMBRE TEXT, $COL_USUARIO_ROL TEXT, $COL_USUARIO_EMAIL TEXT, $COL_USUARIO_TEL TEXT, $COL_USUARIO_ACTIVE INTEGER, $COL_USUARIO_WORK_START TEXT, $COL_USUARIO_WORK_END TEXT, $COL_USUARIO_LAT REAL, $COL_USUARIO_LON REAL, $COL_USUARIO_IMAGEN TEXT, $COL_USUARIO_FCM TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_CLIENTES ($COL_CLIENTE_ID TEXT PRIMARY KEY, $COL_CLIENTE_NOMBRE TEXT, $COL_CLIENTE_TEL TEXT, $COL_CLIENTE_DIR TEXT, $COL_CLIENTE_EMAIL TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_EQUIPOS ($COL_EQUIPO_ID TEXT PRIMARY KEY, $COL_EQUIPO_COD TEXT UNIQUE, $COL_EQUIPO_NOMBRE TEXT, $COL_EQUIPO_TIPO TEXT, $COL_EQUIPO_MARCA TEXT, $COL_EQUIPO_MODELO TEXT, $COL_EQUIPO_SERIAL TEXT, $COL_EQUIPO_CAPACIDAD TEXT, $COL_EQUIPO_UBICACION TEXT, $COL_EQUIPO_CLIENTE_ID TEXT, $COL_EQUIPO_ESTADO TEXT, $COL_EQUIPO_IMAGEN TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_ORDENES ($COL_ORDEN_ID TEXT PRIMARY KEY, $COL_ORDEN_NUM TEXT UNIQUE, $COL_ORDEN_FECHA TEXT, $COL_ORDEN_CLIENTE_ID TEXT, $COL_ORDEN_EQUIPO_ID TEXT, $COL_ORDEN_TECNICO_ID TEXT, $COL_ORDEN_TIPO TEXT, $COL_ORDEN_DESC TEXT, $COL_ORDEN_ESTADO TEXT, $COL_ORDEN_PRECIO REAL, $COL_ORDEN_LAT REAL, $COL_ORDEN_LON REAL, $COL_ORDEN_DIR_EXACTA TEXT, $COL_ORDEN_FIRMA TEXT, $COL_ORDEN_CALIFICACION INTEGER, $COL_ORDEN_COMENTARIO TEXT, $COL_ORDEN_TECH_LAT REAL, $COL_ORDEN_TECH_LON REAL, $COL_ORDEN_PRECIO_MANT REAL, $COL_ORDEN_OBS_CLI TEXT, $COL_SYNCED INTEGER)")
        db?.execSQL("CREATE TABLE $TABLE_MANTENIMIENTOS ($COL_MANT_ID TEXT PRIMARY KEY, $COL_MANT_ORDEN_ID TEXT, $COL_MANT_FECHA TEXT, $COL_MANT_DIAG TEXT, $COL_MANT_TRABAJO TEXT, $COL_MANT_OBS TEXT, $COL_MANT_RECOM TEXT, $COL_MANT_ESTADO_EQ TEXT, $COL_MANT_TIEMPO TEXT, $COL_SYNCED INTEGER)")
        db?.execSQL("CREATE TABLE $TABLE_REPUESTOS ($COL_REP_ID TEXT PRIMARY KEY, $COL_REP_NOMBRE TEXT, $COL_REP_COD TEXT UNIQUE, $COL_REP_UNIDAD TEXT, $COL_REP_PRECIO REAL)")
        db?.execSQL("CREATE TABLE $TABLE_DETALLE_REPUESTOS ($COL_DET_ID TEXT PRIMARY KEY, $COL_DET_MANT_ID TEXT, $COL_DET_REP_ID TEXT, $COL_DET_CANT INTEGER, $COL_DET_OBS TEXT, $COL_DET_PRECIO REAL, $COL_DET_PRECIO_UNIT REAL, $COL_SYNCED INTEGER)")
        db?.execSQL("CREATE TABLE $TABLE_EVIDENCIAS ($COL_EVI_ID TEXT PRIMARY KEY, $COL_EVI_ORDEN_ID TEXT, $COL_EVI_RUTA TEXT, $COL_EVI_FECHA TEXT, $COL_SYNCED INTEGER)")
        db?.execSQL("CREATE TABLE $TABLE_APROBACIONES ($COL_APROB_ID TEXT PRIMARY KEY, $COL_APROB_ORDEN_ID TEXT, $COL_APROB_CLIENTE TEXT, $COL_APROB_ACEPTADO INTEGER, $COL_APROB_FECHA TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_UBICACIONES ($COL_UBI_ID TEXT PRIMARY KEY, $COL_UBI_ORDEN_ID TEXT, $COL_UBI_LAT REAL, $COL_UBI_LON REAL, $COL_UBI_DIR TEXT, $COL_UBI_FECHA TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_ACTIVIDAD ($COL_ACT_ID TEXT PRIMARY KEY, $COL_ACT_TECH_ID TEXT, $COL_ACT_FECHA TEXT, $COL_ACT_INICIO TEXT, $COL_ACT_FIN TEXT, $COL_ACT_LAT REAL, $COL_ACT_LON REAL)")
        db?.execSQL("CREATE TABLE $TABLE_MENSAJES ($COL_MSG_ID TEXT PRIMARY KEY, $COL_MSG_ORDEN_ID TEXT, $COL_MSG_REMITENTE_ID TEXT, $COL_MSG_REMITENTE_NOMBRE TEXT, $COL_MSG_TEXTO TEXT, $COL_MSG_FECHA TEXT)")

        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MENSAJES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIVIDAD")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_UBICACIONES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_APROBACIONES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EVIDENCIAS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DETALLE_REPUESTOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REPUESTOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MANTENIMIENTOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ORDENES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EQUIPOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CLIENTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    private fun insertInitialData(db: SQLiteDatabase?) {
        val admin = ContentValues().apply {
            put(COL_USUARIO_ID, "user_admin_001")
            put(COL_USUARIO_USER, "admin")
            put(COL_USUARIO_PASS, "admin123")
            put(COL_USUARIO_NOMBRE, "Administrador Sistema")
            put(COL_USUARIO_ROL, "Administrador")
            put(COL_USUARIO_EMAIL, "admin@climatrack.com")
        }
        db?.insert(TABLE_USUARIOS, null, admin)

        val tecnico1 = ContentValues().apply {
            put(COL_USUARIO_ID, "user_tech_001")
            put(COL_USUARIO_USER, "tecnico01")
            put(COL_USUARIO_PASS, "123456")
            put(COL_USUARIO_NOMBRE, "Técnico 01")
            put(COL_USUARIO_ROL, "Técnico")
            put(COL_USUARIO_EMAIL, "tecnico01@climatrack.com")
        }
        db?.insert(TABLE_USUARIOS, null, tecnico1)

        val clienteUser = ContentValues().apply {
            put(COL_USUARIO_ID, "user_cli_001")
            put(COL_USUARIO_USER, "cliente01")
            put(COL_USUARIO_PASS, "123456")
            put(COL_USUARIO_NOMBRE, "Cliente de Prueba")
            put(COL_USUARIO_ROL, "Cliente")
            put(COL_USUARIO_EMAIL, "cliente01@gmail.com")
        }
        db?.insert(TABLE_USUARIOS, null, clienteUser)
        
        val parts = listOf(
            listOf("Filtro de aire lavable", "RPT-0007", "Unidad", "25000"),
            listOf("Capacitor 35 + 5 uF", "RPT-0012", "Unidad", "18000"),
            listOf("Contactor 24V 40A", "RPT-0021", "Unidad", "45000"),
            listOf("Gas Refrigerante R410A", "RPT-0030", "Gramos", "120")
        )
        parts.forEach { data ->
            val cv = ContentValues().apply {
                put(COL_REP_ID, UUID.randomUUID().toString())
                put(COL_REP_NOMBRE, data[0])
                put(COL_REP_COD, data[1])
                put(COL_REP_UNIDAD, data[2])
                put(COL_REP_PRECIO, data[3].toDouble())
            }
            db?.insert(TABLE_REPUESTOS, null, cv)
        }
    }
}
