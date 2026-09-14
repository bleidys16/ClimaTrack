package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.*
import com.example.climatrack.utils.SyncManager

class ServicioRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addRepuesto(detalle: DetalleRepuesto): String {
        val db = dbHelper.writableDatabase
        val id = if (detalle.id.isEmpty()) java.util.UUID.randomUUID().toString() else detalle.id
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_DET_ID, id)
            put(DatabaseHelper.COL_DET_MANT_ID, detalle.mantenimientoId)
            put(DatabaseHelper.COL_DET_REP_ID, detalle.repuestoId)
            put(DatabaseHelper.COL_DET_CANT, detalle.cantidad)
            put(DatabaseHelper.COL_DET_OBS, detalle.observacion)
            put(DatabaseHelper.COL_DET_PRECIO, detalle.precioHistorico)
            put(DatabaseHelper.COL_DET_PRECIO_UNIT, detalle.precioUnitario)
            put(DatabaseHelper.COL_SYNCED, 0)
        }
        val result = db.insert(DatabaseHelper.TABLE_DETALLE_REPUESTOS, null, values)
        if (result > 0) SyncManager.startImmediateSync(context)
        return id
    }

    fun getRepuestosByMantenimiento(mantenimientoId: String): List<DetalleRepuestoInfo> {
        val list = mutableListOf<DetalleRepuestoInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT d.${DatabaseHelper.COL_DET_ID}, r.${DatabaseHelper.COL_REP_NOMBRE}, " +
                "r.${DatabaseHelper.COL_REP_COD}, r.${DatabaseHelper.COL_REP_UNIDAD}, " +
                "d.${DatabaseHelper.COL_DET_CANT}, d.${DatabaseHelper.COL_DET_PRECIO_UNIT}, d.${DatabaseHelper.COL_DET_OBS} " +
                "FROM ${DatabaseHelper.TABLE_DETALLE_REPUESTOS} d " +
                "JOIN ${DatabaseHelper.TABLE_REPUESTOS} r ON d.${DatabaseHelper.COL_DET_REP_ID} = r.${DatabaseHelper.COL_REP_ID} " +
                "WHERE d.${DatabaseHelper.COL_DET_MANT_ID} = ?"
        
        val cursor = db.rawQuery(query, arrayOf(mantenimientoId))
        if (cursor.moveToFirst()) {
            do {
                list.add(DetalleRepuestoInfo(
                    id = cursor.getString(0),
                    repuestoNombre = cursor.getString(1),
                    repuestoCodigo = cursor.getString(2),
                    repuestoUnidad = cursor.getString(3),
                    cantidad = cursor.getInt(4),
                    precio = cursor.getDouble(5),
                    observacion = cursor.getString(6)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteRepuesto(id: String): Int {
        return dbHelper.writableDatabase.delete(DatabaseHelper.TABLE_DETALLE_REPUESTOS, 
            "${DatabaseHelper.COL_DET_ID}=?", arrayOf(id))
    }

    fun addEvidencia(evidencia: Evidencia): String {
        val db = dbHelper.writableDatabase
        val id = if (evidencia.id.isEmpty()) java.util.UUID.randomUUID().toString() else evidencia.id
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_EVI_ID, id)
            put(DatabaseHelper.COL_EVI_ORDEN_ID, evidencia.ordenId)
            put(DatabaseHelper.COL_EVI_RUTA, evidencia.rutaFoto)
            put(DatabaseHelper.COL_EVI_FECHA, evidencia.fecha)
            put(DatabaseHelper.COL_SYNCED, 0)
        }
        val result = db.insert(DatabaseHelper.TABLE_EVIDENCIAS, null, values)
        if (result > 0) SyncManager.startImmediateSync(context)
        return id
    }

    fun addUbicacion(ubicacion: Ubicacion): String {
        val db = dbHelper.writableDatabase
        val id = if (ubicacion.id.isEmpty()) java.util.UUID.randomUUID().toString() else ubicacion.id
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_UBI_ID, id)
            put(DatabaseHelper.COL_UBI_ORDEN_ID, ubicacion.ordenId)
            put(DatabaseHelper.COL_UBI_LAT, ubicacion.latitud)
            put(DatabaseHelper.COL_UBI_LON, ubicacion.longitud)
            put(DatabaseHelper.COL_UBI_DIR, ubicacion.direccion)
            put(DatabaseHelper.COL_UBI_FECHA, ubicacion.fecha)
        }
        db.insert(DatabaseHelper.TABLE_UBICACIONES, null, values)
        return id
    }

    fun getUbicacionByOrden(orderId: String): Ubicacion? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_UBICACIONES, null, 
            "${DatabaseHelper.COL_UBI_ORDEN_ID}=?", arrayOf(orderId), null, null, null)
        
        var ubi: Ubicacion? = null
        if (cursor.moveToFirst()) {
            ubi = Ubicacion(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_ID)),
                ordenId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_ORDEN_ID)),
                latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_LAT)),
                longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_LON)),
                direccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_DIR)),
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_FECHA))
            )
        }
        cursor.close()
        return ubi
    }

    fun addAprobacion(aprobacion: Aprobacion): String {
        val db = dbHelper.writableDatabase
        val id = if (aprobacion.id.isEmpty()) java.util.UUID.randomUUID().toString() else aprobacion.id
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_APROB_ID, id)
            put(DatabaseHelper.COL_APROB_ORDEN_ID, aprobacion.ordenId)
            put(DatabaseHelper.COL_APROB_CLIENTE, aprobacion.cliente)
            put(DatabaseHelper.COL_APROB_ACEPTADO, aprobacion.aceptado)
            put(DatabaseHelper.COL_APROB_FECHA, aprobacion.fecha)
        }
        db.insert(DatabaseHelper.TABLE_APROBACIONES, null, values)
        return id
    }

    fun getEvidenciasByOrden(orderId: String): List<Evidencia> {
        val list = mutableListOf<Evidencia>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_EVIDENCIAS, null, 
            "${DatabaseHelper.COL_EVI_ORDEN_ID}=?", arrayOf(orderId), null, null, null)
        
        if (cursor.moveToFirst()) {
            do {
                list.add(Evidencia(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_ID)),
                    ordenId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_ORDEN_ID)),
                    rutaFoto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_RUTA)),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_FECHA))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteEvidencia(id: String): Int {
        return dbHelper.writableDatabase.delete(DatabaseHelper.TABLE_EVIDENCIAS, 
            "${DatabaseHelper.COL_EVI_ID}=?", arrayOf(id))
    }

    fun getTopPartsStats(): List<com.example.climatrack.adapters.StatItem> {
        val list = mutableListOf<com.example.climatrack.adapters.StatItem>()
        val db = dbHelper.readableDatabase
        val query = "SELECT r.${DatabaseHelper.COL_REP_NOMBRE}, SUM(d.${DatabaseHelper.COL_DET_CANT}) as total " +
                "FROM ${DatabaseHelper.TABLE_DETALLE_REPUESTOS} d " +
                "JOIN ${DatabaseHelper.TABLE_REPUESTOS} r ON d.${DatabaseHelper.COL_DET_REP_ID} = r.${DatabaseHelper.COL_REP_ID} " +
                "GROUP BY r.${DatabaseHelper.COL_REP_ID} ORDER BY total DESC LIMIT 5"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.climatrack.adapters.StatItem(cursor.getString(0), cursor.getInt(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
