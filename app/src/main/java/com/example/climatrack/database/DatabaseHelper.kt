package com.example.climatrack.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "climatrack.db"
        private const val DATABASE_VERSION = 3

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
        const val COL_REP_PRECIO = "precio"

        // Tabla Detalle Repuestos
        const val TABLE_DETALLE_REPUESTOS = "detalle_repuestos"
        const val COL_DET_ID = "id"
        const val COL_DET_MANT_ID = "mantenimiento_id"
        const val COL_DET_REP_ID = "repuesto_id"
        const val COL_DET_CANT = "cantidad"
        const val COL_DET_OBS = "observacion"
        const val COL_DET_PRECIO = "precio_historico"

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
                "$COL_REP_UNIDAD TEXT, " +
                "$COL_REP_PRECIO REAL DEFAULT 0)"

        val createDetalleRepuestos = "CREATE TABLE $TABLE_DETALLE_REPUESTOS (" +
                "$COL_DET_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_DET_MANT_ID INTEGER, " +
                "$COL_DET_REP_ID INTEGER, " +
                "$COL_DET_CANT INTEGER, " +
                "$COL_DET_OBS TEXT, " +
                "$COL_DET_PRECIO REAL DEFAULT 0, " +
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
        // Usuarios
        val tecnico1 = ContentValues().apply {
            put(COL_USUARIO_USER, "tecnico01")
            put(COL_USUARIO_PASS, "123456")
            put(COL_USUARIO_NOMBRE, "Técnico 01")
            put(COL_USUARIO_ROL, "Técnico")
        }
        val idUser1 = db?.insert(TABLE_USUARIOS, null, tecnico1) ?: 1

        val tecnico2 = ContentValues().apply {
            put(COL_USUARIO_USER, "tecnico02")
            put(COL_USUARIO_PASS, "123456")
            put(COL_USUARIO_NOMBRE, "Técnico 02")
            put(COL_USUARIO_ROL, "Técnico")
        }
        db?.insert(TABLE_USUARIOS, null, tecnico2)

        // Clientes
        val clientes = listOf(
            Triple("Hotel del Mar", "555-0101", "Av. Playa 123"),
            Triple("ACME S.A.S", "555-0202", "Calle 10 #45-20"),
            Triple("Frio Total Ltda.", "555-0303", "Carrera 50 #12-30"),
            Triple("Hotel Caribe", "555-0404", "Via del Mar Km 5"),
            Triple("Oficinas Plaza", "555-0505", "Av. Central #100"),
            Triple("Clinica del Norte", "555-0606", "Calle 80 #20-10")
        )
        val clienteIds = mutableMapOf<String, Long>()
        clientes.forEach { (nombre, tel, dir) ->
            val cv = ContentValues().apply {
                put(COL_CLIENTE_NOMBRE, nombre)
                put(COL_CLIENTE_TEL, tel)
                put(COL_CLIENTE_DIR, dir)
            }
            clienteIds[nombre] = db?.insert(TABLE_CLIENTES, null, cv) ?: 0
        }

        // Equipos
        val eqs = listOf(
            listOf("EQ-001", "Aire Acondicionado Central", "York", "YXC-48", "SN-987654", "48.000 BTU", "Azotea Bloque A", "Hotel del Mar", "OPERATIVO"),
            listOf("EQ-00015", "Split Pared", "LG", "Dual Inverter 24K", "LG24TI2022015", "24.000 BTU", "Oficina Gerencia", "ACME S.A.S", "OPERATIVO"),
            listOf("EQ-00016", "Cassette", "Samsung", "360 Cassette 36K", "SAM36C2021120", "36.000 BTU", "Sala de Juntas", "Frio Total Ltda.", "EN MANTENIMIENTO"),
            listOf("EQ-00017", "Mini Split", "Midea", "MS-18K", "MIDEA18K3344", "18.000 BTU", "Habitación 101", "Hotel Caribe", "FUERA DE SERVICIO"),
            listOf("EQ-00018", "Chiller", "York", "YK-50TR", "YORK50TR7788", "50 TR", "Planta Baja", "Clinica del Norte", "OPERATIVO")
        )
        val equipoIds = mutableMapOf<String, Long>()
        eqs.forEach { data ->
            val cv = ContentValues().apply {
                put(COL_EQUIPO_COD, data[0])
                put(COL_EQUIPO_TIPO, data[1])
                put(COL_EQUIPO_MARCA, data[2])
                put(COL_EQUIPO_MODELO, data[3])
                put(COL_EQUIPO_SERIAL, data[4])
                put(COL_EQUIPO_CAPACIDAD, data[5])
                put(COL_EQUIPO_UBICACION, data[6])
                put(COL_EQUIPO_CLIENTE_ID, clienteIds[data[7]])
                put(COL_EQUIPO_ESTADO, data[8])
            }
            equipoIds[data[0]] = db?.insert(TABLE_EQUIPOS, null, cv) ?: 0
        }

        // Órdenes Pendientes
        val ordenes = listOf(
            listOf("OT-00001", "2026-08-19", "Hotel del Mar", "EQ-001", "PREVENTIVO", "PENDIENTE"),
            listOf("OT-00025", "2026-08-18", "ACME S.A.S", "EQ-00015", "PREVENTIVO", "PENDIENTE"),
            listOf("OT-00026", "2026-08-18", "Frio Total Ltda.", "EQ-00016", "PREVENTIVO", "PENDIENTE"),
            listOf("OT-00027", "2026-08-19", "Hotel Caribe", "EQ-00017", "PREVENTIVO", "PENDIENTE"),
            listOf("OT-00029", "2026-08-20", "Clinica del Norte", "EQ-00018", "PREVENTIVO", "PENDIENTE")
        )
        val orderIds = mutableMapOf<String, Long>()
        ordenes.forEach { data ->
            val cv = ContentValues().apply {
                put(COL_ORDEN_NUM, data[0])
                put(COL_ORDEN_FECHA, data[1])
                put(COL_ORDEN_CLIENTE_ID, clienteIds[data[2]])
                put(COL_ORDEN_EQUIPO_ID, equipoIds[data[3]])
                put(COL_ORDEN_TECNICO_ID, idUser1)
                put(COL_ORDEN_TIPO, data[4])
                put(COL_ORDEN_ESTADO, data[5])
            }
            orderIds[data[0]] = db?.insert(TABLE_ORDENES, null, cv) ?: 0
        }

        // Historial (Mantenimientos Finalizados)
        val historial = listOf(
            listOf("OT-00007", "2026-05-05", "EQ-00018", "CORRECTIVO", "Fuga de refrigerante, sellado y carga.", "Técnico 03"),
            listOf("OT-00012", "2026-06-10", "EQ-00015", "PREVENTIVO", "Mantenimiento preventivo general.", "Técnico 01"),
            listOf("OT-00018", "2026-07-12", "EQ-00016", "CORRECTIVO", "Cambio de capacitor y limpieza de serpentín.", "Técnico 02")
        )
        historial.forEach { data ->
            // Primero creamos la orden como FINALIZADA
            val cvOrder = ContentValues().apply {
                put(COL_ORDEN_NUM, data[0])
                put(COL_ORDEN_FECHA, data[1])
                put(COL_ORDEN_CLIENTE_ID, 1) // Dummy
                put(COL_ORDEN_EQUIPO_ID, equipoIds[data[2]])
                put(COL_ORDEN_TECNICO_ID, idUser1)
                put(COL_ORDEN_TIPO, data[3])
                put(COL_ORDEN_ESTADO, "FINALIZADA")
            }
            val oId = db?.insert(TABLE_ORDENES, null, cvOrder) ?: 0
            
            // Luego el mantenimiento
            val cvMant = ContentValues().apply {
                put(COL_MANT_ORDEN_ID, oId)
                put(COL_MANT_FECHA, data[1])
                put(COL_MANT_DIAG, "Mantenimiento realizado según reporte.")
                put(COL_MANT_TRABAJO, data[4])
                put(COL_MANT_ESTADO_EQ, "OPERATIVO")
                put(COL_MANT_TIEMPO, "1h 30m")
            }
            db?.insert(TABLE_MANTENIMIENTOS, null, cvMant)
        }

        // Repuestos
        val parts = listOf(
            listOf("Filtro de aire lavable", "RPT-0007", "Unidad", "25000"),
            listOf("Capacitor 35 + 5 uF", "RPT-0012", "Unidad", "18000"),
            listOf("Contactor 24V 40A", "RPT-0021", "Unidad", "45000"),
            listOf("Gas Refrigerante R410A", "RPT-0030", "Gramos", "120") // 120 por gramo aprox
        )
        parts.forEach { data ->
            val cv = ContentValues().apply {
                put(COL_REP_NOMBRE, data[0])
                put(COL_REP_COD, data[1])
                put(COL_REP_UNIDAD, data[2])
                put(COL_REP_PRECIO, data[3].toDouble())
            }
            db?.insert(TABLE_REPUESTOS, null, cv)
        }
    }
}
