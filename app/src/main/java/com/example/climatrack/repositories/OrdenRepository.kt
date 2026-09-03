package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Orden
import com.example.climatrack.models.OrdenInfo
import com.example.climatrack.utils.FirebaseHelper
import com.google.firebase.firestore.SetOptions

class OrdenRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val firestore = FirebaseHelper.db

    fun syncOrderToCloud(id: Int) {
        // We'll let SyncManager handle background sync
        com.example.climatrack.utils.SyncManager.startImmediateSync(context)
    }

    fun getAllInfoByTecnico(tecnicoId: Int): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Cliente Externo') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                (if (tecnicoId != -1) "WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = ?" else "")
        
        val cursor = if (tecnicoId != -1) db.rawQuery(query, arrayOf(tecnicoId.toString())) else db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(OrdenInfo(
                    id = cursor.getInt(0),
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
                    firmaBase64 = cursor.getString(15)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAllByTecnico(tecnicoId: Int): List<Orden> {
        val list = mutableListOf<Orden>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDENES,
            null,
            "${DatabaseHelper.COL_ORDEN_TECNICO_ID}=?",
            arrayOf(tecnicoId.toString()),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrden(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getById(id: Int): Orden? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDENES,
            null,
            "${DatabaseHelper.COL_ORDEN_ID}=?",
            arrayOf(id.toString()),
            null, null, null
        )
        var orden: Orden? = null
        if (cursor.moveToFirst()) {
            orden = cursorToOrden(cursor)
        }
        cursor.close()
        return orden
    }

    fun updateEstado(id: Int, nuevoEstado: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_ESTADO, nuevoEstado)
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id.toString()))
        if (result > 0) syncOrderToCloud(id)
        return result
    }

    fun updatePrecio(id: Int, precio: Double): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_PRECIO, precio)
            put(DatabaseHelper.COL_ORDEN_ESTADO, "PENDIENTE APROBACIÓN")
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id.toString()))
        if (result > 0) syncOrderToCloud(id)
        return result
    }

    fun saveFirma(id: Int, firmaBase64: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_FIRMA, firmaBase64)
            put(DatabaseHelper.COL_ORDEN_ESTADO, "FINALIZADA")
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id.toString()))
        if (result > 0) syncOrderToCloud(id)
        return result
    }

    fun updateFeedback(id: Int, calificacion: Int, comentario: String?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_CALIFICACION, calificacion)
            put(DatabaseHelper.COL_ORDEN_COMENTARIO, comentario)
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id.toString()))
        if (result > 0) syncOrderToCloud(id)
        return result
    }

    fun updateTechnicianGps(id: Int, lat: Double, lon: Double): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_TECH_LAT, lat)
            put(DatabaseHelper.COL_ORDEN_TECH_LON, lon)
        }
        val result = db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id.toString()))
        if (result > 0) {
            // Update only specific fields in Firestore for performance
            val orden = getById(id)
            if (orden != null) {
                firestore.collection("ordenes").document(orden.numero)
                    .update("tecnicoLat", lat, "tecnicoLon", lon)
            }
        }
        return result
    }

    fun create(orden: Orden): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
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
        }
        val result = db.insert(DatabaseHelper.TABLE_ORDENES, null, values)
        if (result > 0) syncOrderToCloud(result.toInt())
        return result
    }

    fun getOrdenesByCliente(clienteId: Int): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Cliente Externo') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = ?"
        
        val cursor = db.rawQuery(query, arrayOf(clienteId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(OrdenInfo(
                    id = cursor.getInt(0),
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
                    firmaBase64 = cursor.getString(15)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUnassignedOrders(): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Cliente Externo') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} IS NULL"
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(OrdenInfo(
                    id = cursor.getInt(0),
                    numero = cursor.getString(1),
                    fecha = cursor.getString(2),
                    clienteNombre = cursor.getString(3),
                    equipoNombre = cursor.getString(4),
                    tipoServicio = cursor.getString(5),
                    estado = cursor.getString(6),
                    descripcion = cursor.getString(7),
                    direccion = cursor.getString(8),
                    calificacion = cursor.getInt(9),
                    comentario = cursor.getString(10),
                    firmaBase64 = cursor.getString(11)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getDashboardStats(tecnicoId: Int): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        val db = dbHelper.readableDatabase
        val query = "SELECT ${DatabaseHelper.COL_ORDEN_ESTADO}, COUNT(*) as count " +
                    "FROM ${DatabaseHelper.TABLE_ORDENES} " +
                    "WHERE ${DatabaseHelper.COL_ORDEN_TECNICO_ID} = ? " +
                    "GROUP BY ${DatabaseHelper.COL_ORDEN_ESTADO}"
        
        val cursor = db.rawQuery(query, arrayOf(tecnicoId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ESTADO))
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
                stats[estado] = count
            } while (cursor.moveToNext())
        }
        cursor.close()
        return stats
    }

    fun assignTechnician(orderId: Int, tecnicoId: Int): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ORDEN_TECNICO_ID, tecnicoId)
            put(DatabaseHelper.COL_ORDEN_ESTADO, "PENDIENTE")
        }
        return db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(orderId.toString()))
    }

    fun getTechnicianWithLeastWork(): Int {
        val db = dbHelper.readableDatabase
        val query = "SELECT u.${DatabaseHelper.COL_USUARIO_ID}, COUNT(o.${DatabaseHelper.COL_ORDEN_ID}) as workload " +
                    "FROM ${DatabaseHelper.TABLE_USUARIOS} u " +
                    "LEFT JOIN ${DatabaseHelper.TABLE_ORDENES} o ON u.${DatabaseHelper.COL_USUARIO_ID} = o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} " +
                    "WHERE u.${DatabaseHelper.COL_USUARIO_ROL} = 'Técnico' " +
                    "GROUP BY u.${DatabaseHelper.COL_USUARIO_ID} " +
                    "ORDER BY workload ASC LIMIT 1"
        
        val cursor = db.rawQuery(query, null)
        var techId = -1
        if (cursor.moveToFirst()) {
            techId = cursor.getInt(0)
        }
        cursor.close()
        return techId
    }

    fun getOrdenesByEquipo(equipoId: Int): List<OrdenInfo> {
        val list = mutableListOf<OrdenInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT o.${DatabaseHelper.COL_ORDEN_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, o.${DatabaseHelper.COL_ORDEN_FECHA}, " +
                "COALESCE(c.${DatabaseHelper.COL_CLIENTE_NOMBRE}, u_cli.${DatabaseHelper.COL_USUARIO_NOMBRE}, 'Cliente Externo') as cliente_nombre, " +
                "e.${DatabaseHelper.COL_EQUIPO_MARCA} || ' ' || e.${DatabaseHelper.COL_EQUIPO_MODELO} as equipo, " +
                "o.${DatabaseHelper.COL_ORDEN_TIPO}, o.${DatabaseHelper.COL_ORDEN_ESTADO}, u_tech.${DatabaseHelper.COL_USUARIO_NOMBRE}, " +
                "o.${DatabaseHelper.COL_ORDEN_PRECIO}, e.${DatabaseHelper.COL_EQUIPO_MARCA}, e.${DatabaseHelper.COL_EQUIPO_MODELO}, " +
                "o.${DatabaseHelper.COL_ORDEN_DESC}, o.${DatabaseHelper.COL_ORDEN_DIR_EXACTA}, o.${DatabaseHelper.COL_ORDEN_CALIFICACION}, o.${DatabaseHelper.COL_ORDEN_COMENTARIO}, o.${DatabaseHelper.COL_ORDEN_FIRMA} " +
                "FROM ${DatabaseHelper.TABLE_ORDENES} o " +
                "LEFT JOIN ${DatabaseHelper.TABLE_CLIENTES} c ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = c.${DatabaseHelper.COL_CLIENTE_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_cli ON o.${DatabaseHelper.COL_ORDEN_CLIENTE_ID} = u_cli.${DatabaseHelper.COL_USUARIO_ID} " +
                "JOIN ${DatabaseHelper.TABLE_EQUIPOS} e ON o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = e.${DatabaseHelper.COL_EQUIPO_ID} " +
                "LEFT JOIN ${DatabaseHelper.TABLE_USUARIOS} u_tech ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u_tech.${DatabaseHelper.COL_USUARIO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_EQUIPO_ID} = ? " +
                "ORDER BY o.${DatabaseHelper.COL_ORDEN_FECHA} DESC"
        
        val cursor = db.rawQuery(query, arrayOf(equipoId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(OrdenInfo(
                    id = cursor.getInt(0),
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
                    firmaBase64 = cursor.getString(15)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTopBrandsStats(): List<com.example.climatrack.adapters.StatItem> {
        val list = mutableListOf<com.example.climatrack.adapters.StatItem>()
        val db = dbHelper.readableDatabase
        val query = "SELECT e.${DatabaseHelper.COL_EQUIPO_MARCA}, COUNT(*) as count " +
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
                    val orden = doc.toObject(Orden::class.java)
                    if (orden != null) {
                        val values = ContentValues().apply {
                            put(DatabaseHelper.COL_ORDEN_NUM, orden.numero)
                            put(DatabaseHelper.COL_ORDEN_FECHA, orden.fecha)
                            put(DatabaseHelper.COL_ORDEN_CLIENTE_ID, orden.clienteId)
                            put(DatabaseHelper.COL_ORDEN_EQUIPO_ID, orden.equipoId)
                            put(DatabaseHelper.COL_ORDEN_TECNICO_ID, orden.tecnicoId)
                            put(DatabaseHelper.COL_ORDEN_TIPO, orden.tipoServicio)
                            put(DatabaseHelper.COL_ORDEN_DESC, orden.descripcion)
                            put(DatabaseHelper.COL_ORDEN_ESTADO, orden.estado)
                            put(DatabaseHelper.COL_ORDEN_PRECIO, orden.precioServicio)
                            put(DatabaseHelper.COL_ORDEN_DIR_EXACTA, orden.direccionExacta)
                            put(DatabaseHelper.COL_ORDEN_FIRMA, orden.firmaBase64)
                            put(DatabaseHelper.COL_ORDEN_CALIFICACION, orden.calificacion)
                            put(DatabaseHelper.COL_ORDEN_COMENTARIO, orden.comentario)
                        }
                        
                        val count = db.update(DatabaseHelper.TABLE_ORDENES, values, 
                            "${DatabaseHelper.COL_ORDEN_NUM}=?", arrayOf(orden.numero))
                        
                        if (count == 0) {
                            db.insert(DatabaseHelper.TABLE_ORDENES, null, values)
                        }
                    }
                }
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }

    private fun cursorToOrden(cursor: Cursor): Orden {
        val techIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECNICO_ID)
        val tecnicoId = if (cursor.isNull(techIdx)) null else cursor.getInt(techIdx)

        return Orden(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ID)),
            numero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_NUM)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_FECHA)),
            clienteId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_CLIENTE_ID)),
            equipoId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_EQUIPO_ID)),
            tecnicoId = tecnicoId,
            tipoServicio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TIPO)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_DESC)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ESTADO)),
            precioServicio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_PRECIO)),
            latitudCliente = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LAT)),
            longitudCliente = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LON)),
            direccionExacta = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_DIR_EXACTA)),
            firmaBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_FIRMA))
        )
    }
}
