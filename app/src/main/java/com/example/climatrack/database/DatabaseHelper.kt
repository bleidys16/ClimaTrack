package com.example.climatrack.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "climatrack.db"
        private const val DATABASE_VERSION = 1

        // Tabla Usuarios
        const val TABLE_USUARIOS = "usuarios"
        const val COL_USUARIO_ID = "id"
        const val COL_USUARIO_USER = "usuario"
        const val COL_USUARIO_PASS = "password"
        const val COL_USUARIO_NOMBRE = "nombre"
        const val COL_USUARIO_ROL = "rol"

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
        const val COL_EQUIPO_TIPO = "tipo"
        const val COL_EQUIPO_MARCA = "marca"
        const val COL_EQUIPO_MODELO = "modelo"
        const val COL_EQUIPO_SERIAL = "serial"
        const val COL_EQUIPO_CAPACIDAD = "capacidad"
        const val COL_EQUIPO_UBICACION = "ubicacion"
        const val COL_EQUIPO_CLIENTE_ID = "cliente_id"
        const val COL_EQUIPO_ESTADO = "estado"

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

        // Tabla Detalle Repuestos
        const val TABLE_DETALLE_REPUESTOS = "detalle_repuestos"
        const val COL_DET_ID = "id"
        const val COL_DET_MANT_ID = "mantenimiento_id"
        const val COL_DET_REP_ID = "repuesto_id"
        const val COL_DET_CANT = "cantidad"
        const val COL_DET_OBS = "observacion"

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
        const val COL_UBI_FECHA = "fecha"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsuarios = "CREATE TABLE $TABLE_USUARIOS (" +
                "$COL_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_USUARIO_USER TEXT NOT NULL UNIQUE, " +
                "$COL_USUARIO_PASS TEXT NOT NULL, " +
                "$COL_USUARIO_NOMBRE TEXT NOT NULL, " +
                "$COL_USUARIO_ROL TEXT NOT NULL)"

        val createClientes = "CREATE TABLE $TABLE_CLIENTES (" +
                "$COL_CLIENTE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_CLIENTE_NOMBRE TEXT NOT NULL, " +
                "$COL_CLIENTE_TEL TEXT, " +
                "$COL_CLIENTE_DIR TEXT, " +
                "$COL_CLIENTE_EMAIL TEXT)"

        val createEquipos = "CREATE TABLE $TABLE_EQUIPOS (" +
                "$COL_EQUIPO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_EQUIPO_COD TEXT NOT NULL UNIQUE, " +
                "$COL_EQUIPO_TIPO TEXT NOT NULL, " +
                "$COL_EQUIPO_MARCA TEXT NOT NULL, " +
                "$COL_EQUIPO_MODELO TEXT NOT NULL, " +
                "$COL_EQUIPO_SERIAL TEXT, " +
                "$COL_EQUIPO_CAPACIDAD TEXT, " +
                "$COL_EQUIPO_UBICACION TEXT, " +
                "$COL_EQUIPO_CLIENTE_ID INTEGER, " +
                "$COL_EQUIPO_ESTADO TEXT, " +
                "FOREIGN KEY($COL_EQUIPO_CLIENTE_ID) REFERENCES $TABLE_CLIENTES($COL_CLIENTE_ID))"

        val createOrdenes = "CREATE TABLE $TABLE_ORDENES (" +
                "$COL_ORDEN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_ORDEN_NUM TEXT NOT NULL UNIQUE, " +
                "$COL_ORDEN_FECHA TEXT NOT NULL, " +
                "$COL_ORDEN_CLIENTE_ID INTEGER, " +
                "$COL_ORDEN_EQUIPO_ID INTEGER, " +
                "$COL_ORDEN_TECNICO_ID INTEGER, " +
                "$COL_ORDEN_TIPO TEXT, " +
                "$COL_ORDEN_DESC TEXT, " +
                "$COL_ORDEN_ESTADO TEXT, " +
                "FOREIGN KEY($COL_ORDEN_CLIENTE_ID) REFERENCES $TABLE_CLIENTES($COL_CLIENTE_ID), " +
                "FOREIGN KEY($COL_ORDEN_EQUIPO_ID) REFERENCES $TABLE_EQUIPOS($COL_EQUIPO_ID), " +
                "FOREIGN KEY($COL_ORDEN_TECNICO_ID) REFERENCES $TABLE_USUARIOS($COL_USUARIO_ID))"

        val createMantenimientos = "CREATE TABLE $TABLE_MANTENIMIENTOS (" +
                "$COL_MANT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_MANT_ORDEN_ID INTEGER, " +
                "$COL_MANT_FECHA TEXT NOT NULL, " +
                "$COL_MANT_DIAG TEXT NOT NULL, " +
                "$COL_MANT_TRABAJO TEXT NOT NULL, " +
                "$COL_MANT_OBS TEXT, " +
                "$COL_MANT_RECOM TEXT, " +
                "$COL_MANT_ESTADO_EQ TEXT, " +
                "$COL_MANT_TIEMPO TEXT, " +
                "FOREIGN KEY($COL_MANT_ORDEN_ID) REFERENCES $TABLE_ORDENES($COL_ORDEN_ID))"

        val createRepuestos = "CREATE TABLE $TABLE_REPUESTOS (" +
                "$COL_REP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_REP_NOMBRE TEXT NOT NULL, " +
                "$COL_REP_COD TEXT NOT NULL UNIQUE, " +
                "$COL_REP_UNIDAD TEXT)"

        val createDetalleRepuestos = "CREATE TABLE $TABLE_DETALLE_REPUESTOS (" +
                "$COL_DET_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_DET_MANT_ID INTEGER, " +
                "$COL_DET_REP_ID INTEGER, " +
                "$COL_DET_CANT INTEGER, " +
                "$COL_DET_OBS TEXT, " +
                "FOREIGN KEY($COL_DET_MANT_ID) REFERENCES $TABLE_MANTENIMIENTOS($COL_MANT_ID), " +
                "FOREIGN KEY($COL_DET_REP_ID) REFERENCES $TABLE_REPUESTOS($COL_REP_ID))"

        val createEvidencias = "CREATE TABLE $TABLE_EVIDENCIAS (" +
                "$COL_EVI_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_EVI_ORDEN_ID INTEGER, " +
                "$COL_EVI_RUTA TEXT, " +
                "$COL_EVI_FECHA TEXT, " +
                "FOREIGN KEY($COL_EVI_ORDEN_ID) REFERENCES $TABLE_ORDENES($COL_ORDEN_ID))"

        val createAprobaciones = "CREATE TABLE $TABLE_APROBACIONES (" +
                "$COL_APROB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_APROB_ORDEN_ID INTEGER, " +
                "$COL_APROB_CLIENTE TEXT, " +
                "$COL_APROB_ACEPTADO INTEGER, " +
                "$COL_APROB_FECHA TEXT, " +
                "FOREIGN KEY($COL_APROB_ORDEN_ID) REFERENCES $TABLE_ORDENES($COL_ORDEN_ID))"

        val createUbicaciones = "CREATE TABLE $TABLE_UBICACIONES (" +
                "$COL_UBI_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_UBI_ORDEN_ID INTEGER, " +
                "$COL_UBI_LAT REAL, " +
                "$COL_UBI_LON REAL, " +
                "$COL_UBI_FECHA TEXT, " +
                "FOREIGN KEY($COL_UBI_ORDEN_ID) REFERENCES $TABLE_ORDENES($COL_ORDEN_ID))"

        db?.execSQL(createUsuarios)
        db?.execSQL(createClientes)
        db?.execSQL(createEquipos)
        db?.execSQL(createOrdenes)
        db?.execSQL(createMantenimientos)
        db?.execSQL(createRepuestos)
        db?.execSQL(createDetalleRepuestos)
        db?.execSQL(createEvidencias)
        db?.execSQL(createAprobaciones)
        db?.execSQL(createUbicaciones)

        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
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
        // Usuario Técnico de Prueba
        val valuesUser = ContentValues().apply {
            put(COL_USUARIO_USER, "tecnico01")
            put(COL_USUARIO_PASS, "123456")
            put(COL_USUARIO_NOMBRE, "Técnico de Prueba")
            put(COL_USUARIO_ROL, "Técnico")
        }
        db?.insert(TABLE_USUARIOS, null, valuesUser)

        // Clientes iniciales
        val client1 = ContentValues().apply {
            put(COL_CLIENTE_NOMBRE, "Hotel del Mar")
            put(COL_CLIENTE_TEL, "555-0101")
            put(COL_CLIENTE_DIR, "Av. Playa 123")
            put(COL_CLIENTE_EMAIL, "info@hotelmar.com")
        }
        val idClient1 = db?.insert(TABLE_CLIENTES, null, client1)

        // Equipos iniciales
        val equipo1 = ContentValues().apply {
            put(COL_EQUIPO_COD, "EQ-001")
            put(COL_EQUIPO_TIPO, "Aire Acondicionado Central")
            put(COL_EQUIPO_MARCA, "York")
            put(COL_EQUIPO_MODELO, "YXC-48")
            put(COL_EQUIPO_SERIAL, "SN-987654")
            put(COL_EQUIPO_CAPACIDAD, "48.000 BTU")
            put(COL_EQUIPO_UBICACION, "Azotea Bloque A")
            put(COL_EQUIPO_CLIENTE_ID, idClient1)
            put(COL_EQUIPO_ESTADO, "OPERATIVO")
        }
        val idEquipo1 = db?.insert(TABLE_EQUIPOS, null, equipo1)

        // Ordenes iniciales
        val orden1 = ContentValues().apply {
            put(COL_ORDEN_NUM, "OT-00001")
            put(COL_ORDEN_FECHA, "2026-08-19")
            put(COL_ORDEN_CLIENTE_ID, idClient1)
            put(COL_ORDEN_EQUIPO_ID, idEquipo1)
            put(COL_ORDEN_TECNICO_ID, 1)
            put(COL_ORDEN_TIPO, "PREVENTIVO")
            put(COL_ORDEN_DESC, "Mantenimiento semestral de unidad central")
            put(COL_ORDEN_ESTADO, "PENDIENTE")
        }
        db?.insert(TABLE_ORDENES, null, orden1)

        // Repuestos iniciales
        val rep1 = ContentValues().apply {
            put(COL_REP_NOMBRE, "Filtro de Aire G4")
            put(COL_REP_COD, "RP-001")
            put(COL_REP_UNIDAD, "Unidad")
        }
        db?.insert(TABLE_REPUESTOS, null, rep1)
    }
}
