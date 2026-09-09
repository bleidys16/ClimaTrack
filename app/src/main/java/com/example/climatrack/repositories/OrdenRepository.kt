package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Orden
import com.example.climatrack.models.OrdenInfo
import com.example.climatrack.utils.FirebaseHelper
import java.util.UUID

class OrdenRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val firestore = FirebaseHelper.db

    fun syncOrderToCloud() {
        com.example.climatrack.utils.SyncManager.startImmediateSync(context)
    }

    fun getAllInfo(): List<OrdenInfo> {
        return getAllInfoByTecnico("-1")
    }

    fun getDashboardStats(tecnicoId: String): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        val db = dbHelper.readableDatabase
        val query = "SELECT ${DatabaseHelper.COL_ORDEN_ESTADO}, COUNT(*) FROM ${DatabaseHelper.TABLE_ORDENES} " +
                "WHERE ${DatabaseHelper.COL_ORDEN_TECNICO_ID} = ? GROUP BY ${DatabaseHelper.COL_ORDEN_ESTADO}"
        val cursor = db.rawQuery(query, arrayOf(tecnicoId))
        if (cursor.moveToFirst()) {
            do {
                stats[cursor.getString(0)] = cursor.getInt(1)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return stats
    }

    fun getAllInfoByTecnico(tecnicoId: String): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, 'Usuario Sincronizado') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, " +
                "COALESCE(u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Por asignar') as tecnico_nombre, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA}, " +
                "o.${DatabaseHelper.COL_ORDEN_TECH_LAT}, o.${DatabaseHelper.COL_ORDEN_TECH_LON}, o.${DatabaseHelper.COL_ORDEN_LAT}, o.${DatabaseHelper.COL_ORDEN_LON}, " +
                "u_cli.${DatabaseHelper.COL_USUARIO_EMAIL}, o.${DatabaseHelper.COL_ORDEN_PRECIO_MANT}, o.${DatabaseHelper.COL_ORDEN_OBS_CLI} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                (if (tecnicoId != "-1") "WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = ?" else "")
        
        val cursor = if (tecnicoId != "-1") db.rawQuery(query, arrayOf(tecnicoId)) else db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrdenInfo(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getById(id: String): Orden? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_ORDENES, null, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id), null, null, null)
        var orden: Orden? = null
        if (cursor.moveToFirst()) {
            orden = cursorToOrden(cursor)
        }
        cursor.close()
        return orden
    }

    fun updateEstado(id: String, nuevoEstado: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_ESTADO, nuevoEstado)
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id))
        if (result > 0) syncOrderToCloud()
        return result
    }

    fun updateTechnicianGps(orderId: String, lat: Double, lon: Double): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_TECH_LAT, lat)
            put(DatabaseHelper.COL_ORDEN_TECH_LON, lon)
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(orderId))
        if (result > 0) {
            getById(orderId)?.let { orden ->
                firestore.collection("ordenes").document(orden.numero).update(
                    "tecnicoLat", lat,
                    "tecnicoLon", lon
                )
            }
        }
        return result
    }

    fun updatePrecio(orderId: String, precio: Double): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_PRECIO, precio)
            put(DatabaseHelper.COL_ORDEN_ESTADO, "PENDIENTE APROBACIÓN")
            put(DatabaseHelper.COL_SYNCED, 0)
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(orderId))
        if (result > 0) syncOrderToCloud()
        return result
    }

    fun updateFeedback(orderId: String, calificacion: Int, comentario: String?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_CALIFICACION, calificacion)
            put(DatabaseHelper.COL_ORDEN_COMENTARIO, comentario)
            put(DatabaseHelper.COL_SYNCED, 0)
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(orderId))
        if (result > 0) syncOrderToCloud()
        return result
    }

    fun create(orden: Orden): String {
        val db = dbHelper.writableDatabase
        val id = if (orden.id.isEmpty()) UUID.randomUUID().toString() else orden.id
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_ID, id)
            put(DatabaseHelper.COL_ORDEN_NUM, orden.numero)
            put(DatabaseHelper.COL_ORDEN_FECHA, orden.fecha)
            put(DatabaseHelper.COL_ORDEN_CLIENTE_ID, orden.clienteId)
            put(DatabaseHelper.COL_ORDEN_EQUIPO_ID, orden.equipoId)
            put(DatabaseHelper.COL_ORDEN_TECNICO_ID, orden.tecnicoId)
            put(DatabaseHelper.COL_ORDEN_TIPO, orden.tipoServicio)
            put(DatabaseHelper.COL_ORDEN_DESC, orden.descripcion)
            put(DatabaseHelper.COL_ORDEN_ESTADO, orden.estado)
            put(DatabaseHelper.COL_ORDEN_LAT, orden.latitudCliente)
            put(DatabaseHelper.COL_ORDEN_LON, orden.longitudCliente)
            put(DatabaseHelper.COL_ORDEN_DIR_EXACTA, orden.direccionExacta)
            put(DatabaseHelper.COL_SYNCED, 0)
        }
        db.insert(DatabaseHelper.TABLE_ORDENES, null, values)
        syncOrderToCloud()
        return id
    }

    fun getOrdenesByCliente(clienteId: String): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, 'Usuario Sincronizado') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, " +
                "COALESCE(u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Por asignar') as tecnico_nombre, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA}, " +
                "o.${DatabaseHelper.COL_ORDEN_TECH_LAT}, o.${DatabaseHelper.COL_ORDEN_TECH_LON}, o.${DatabaseHelper.COL_ORDEN_LAT}, o.${DatabaseHelper.COL_ORDEN_LON}, " +
                "u_cli.${DatabaseHelper.COL_USUARIO_EMAIL}, o.${DatabaseHelper.COL_ORDEN_PRECIO_MANT}, o.${DatabaseHelper.COL_ORDEN_OBS_CLI} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = ?"
        
        val cursor = db.rawQuery(query, arrayOf(clienteId))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrdenInfo(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getOrdenesByEquipo(equipoId: String): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, 'Usuario Sincronizado') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, " +
                "COALESCE(u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Por asignar') as tecnico_nombre, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA}, " +
                "o.${DatabaseHelper.COL_ORDEN_TECH_LAT}, o.${DatabaseHelper.COL_ORDEN_TECH_LON}, o.${DatabaseHelper.COL_ORDEN_LAT}, o.${DatabaseHelper.COL_ORDEN_LON}, " +
                "u_cli.${DatabaseHelper.COL_USUARIO_EMAIL}, o.${DatabaseHelper.COL_ORDEN_PRECIO_MANT}, o.${DatabaseHelper.COL_ORDEN_OBS_CLI} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = ?"
        
        val cursor = db.rawQuery(query, arrayOf(equipoId))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrdenInfo(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUnassignedOrders(): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, 'Usuario Sincronizado') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, " +
                "COALESCE(u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Por asignar') as tecnico_nombre, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA}, " +
                "o.${DatabaseHelper.COL_ORDEN_TECH_LAT}, o.${DatabaseHelper.COL_ORDEN_TECH_LON}, o.${DatabaseHelper.COL_ORDEN_LAT}, o.${DatabaseHelper.COL_ORDEN_LON}, " +
                "u_cli.${DatabaseHelper.COL_USUARIO_EMAIL}, o.${DatabaseHelper.COL_ORDEN_PRECIO_MANT}, o.${DatabaseHelper.COL_ORDEN_OBS_CLI} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} IS NULL OR o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = ''"
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrdenInfo(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAssignedActiveOrders(): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, 'Usuario Sincronizado') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, " +
                "COALESCE(u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Por asignar') as tecnico_nombre, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA}, " +
                "o.${DatabaseHelper.COL_ORDEN_TECH_LAT}, o.${DatabaseHelper.COL_ORDEN_TECH_LON}, o.${DatabaseHelper.COL_ORDEN_LAT}, o.${DatabaseHelper.COL_ORDEN_LON}, " +
                "u_cli.${DatabaseHelper.COL_USUARIO_EMAIL}, o.${DatabaseHelper.COL_ORDEN_PRECIO_MANT}, o.${DatabaseHelper.COL_ORDEN_OBS_CLI} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} IS NOT NULL AND o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} != '' AND o.${DatabaseHelper.COL_ORDEN_ESTADO} != 'FINALIZADA'"
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrdenInfo(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun assignTechnician(orderId: String, tecnicoId: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_TECNICO_ID, tecnicoId)
            put(DatabaseHelper.COL_ORDEN_ESTADO, "PENDIENTE")
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(orderId))
        if (result > 0) syncOrderToCloud()
        return result
    }

    fun getTechnicianWithLeastWork(): String {
        val db = dbHelper.readableDatabase
        val query = "SELECT u.${DatabaseHelper.COL_USUARIO_ID}, COUNT(o.${DatabaseHelper.COL_ORDEN_ID}) as work_load " +
                "FROM ${DatabaseHelper.TABLE_USUARIOS} u " +
                "LEFT JOIN ${DatabaseHelper.TABLE_ORDENES} o ON u.${DatabaseHelper.COL_USUARIO_ID} = o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} " +
                "WHERE u.${DatabaseHelper.COL_USUARIO_ROL} LIKE 'T%cnico%' " +
                "GROUP BY u.${DatabaseHelper.COL_USUARIO_ID} " +
                "ORDER BY work_load ASC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        var techId = ""
        if (cursor.moveToFirst()) {
            techId = cursor.getString(0)
        }
        cursor.close()
        return techId
    }

    fun getTopBrandsStats(): List<com.example.climatrack.adapters.StatItem> {
        val list = mutableListOf<com.example.climatrack.adapters.StatItem>()
        val db = dbHelper.readableDatabase
        val query = "SELECT e.${DatabaseHelper.COL_EQUIPO_MARCA}, COUNT(o.${DatabaseHelper.COL_ORDEN_ID}) as count " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "GROUP BY e.${DatabaseHelper.COL_EQUIPO_MARCA} " +
                "ORDER BY count DESC LIMIT 5"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.climatrack.adapters.StatItem(cursor.getString(0), cursor.getInt(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun fetchOrdersFromCloud(onComplete: () -> Unit) {
        firestore.collection("ordenes")
            .get()
            .addOnSuccessListener { documents ->
                val db = dbHelper.writableDatabase
                for (doc in documents) {
                    val id = doc.id
                    val values = ContentValues().apply {
                        put(DatabaseHelper.COL_ORDEN_ID, id)
                        put(DatabaseHelper.COL_ORDEN_NUM, doc.getString("numero"))
                        put(DatabaseHelper.COL_ORDEN_FECHA, doc.getString("fecha"))
                        put(DatabaseHelper.COL_ORDEN_CLIENTE_ID, doc.getString("clienteId"))
                        put(DatabaseHelper.COL_ORDEN_EQUIPO_ID, doc.getString("equipoId"))
                        put(DatabaseHelper.COL_ORDEN_TECNICO_ID, doc.getString("tecnicoId"))
                        put(DatabaseHelper.COL_ORDEN_TIPO, doc.getString("tipoServicio"))
                        put(DatabaseHelper.COL_ORDEN_DESC, doc.getString("descripcion"))
                        put(DatabaseHelper.COL_ORDEN_ESTADO, doc.getString("estado"))
                        put(DatabaseHelper.COL_ORDEN_PRECIO, doc.getDouble("precioServicio"))
                        put(DatabaseHelper.COL_SYNCED, 1)
                    }
                    val count = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id))
                    if (count == 0) db.insert(DatabaseHelper.TABLE_ORDENES, null, values)
                }
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }

    private fun cursorToOrdenInfo(cursor: Cursor): OrdenInfo {
        return OrdenInfo(
            id = cursor.getString(0),
            numero = cursor.getString(1),
            fecha = cursor.getString(2),
            clienteNombre = cursor.getString(3),
            equipoNombre = cursor.getString(4),
            tipoServicio = cursor.getString(5),
            estado = cursor.getString(6),
            tecnicoNombre = cursor.getString(7),
            precioServicio = cursor.getDouble(8),
            equipoMarca = cursor.getString(9),
            equipoModelo = cursor.getString(10),
            descripcion = cursor.getString(11),
            direccion = cursor.getString(12),
            calificacion = cursor.getInt(13),
            comentario = cursor.getString(14),
            firmaBase64 = cursor.getString(15),
            tecnicoLat = if (cursor.isNull(16)) null else cursor.getDouble(16),
            tecnicoLon = if (cursor.isNull(17)) null else cursor.getDouble(17),
            latitudCliente = if (cursor.isNull(18)) null else cursor.getDouble(18),
            longitudCliente = if (cursor.isNull(19)) null else cursor.getDouble(19),
            clienteEmail = cursor.getString(20),
            precioMantenimiento = cursor.getDouble(21),
            observacionCliente = cursor.getString(22)
        )
    }

    private fun cursorToOrden(cursor: Cursor): Orden {
        return Orden(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ID)),
            numero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_NUM)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_FECHA)),
            clienteId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_CLIENTE_ID)),
            equipoId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_EQUIPO_ID)),
            tecnicoId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECNICO_ID)),
            tipoServicio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TIPO)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_DESC)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ESTADO)),
            precioServicio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_PRECIO)),
            latitudCliente = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LAT)),
            longitudCliente = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LON)),
            direccionExacta = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_DIR_EXACTA)),
            firmaBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_FIRMA)),
            precioMantenimiento = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_PRECIO_MANT)),
            observacionCliente = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_OBS_CLI))
        )
    }
}
