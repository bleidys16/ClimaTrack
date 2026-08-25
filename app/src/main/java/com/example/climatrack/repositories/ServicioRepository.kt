package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Aprobacion
import com.example.climatrack.models.DetalleRepuesto
import com.example.climatrack.models.Evidencia
import com.example.climatrack.models.Ubicacion

class ServicioRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addRepuesto(detalle: DetalleRepuesto): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_DET_MANT_ID, detalle.mantenimientoId)
            put(DatabaseHelper.COL_DET_REP_ID, detalle.repuestoId)
            put(DatabaseHelper.COL_DET_CANT, detalle.cantidad)
            put(DatabaseHelper.COL_DET_OBS, detalle.observacion)
            put(DatabaseHelper.COL_DET_PRECIO, detalle.precioHistorico)
        }
        return db.insert(DatabaseHelper.TABLE_DETALLE_REPUESTOS, null, values)
    }

    fun getRepuestosByMantenimiento(mantenimientoId: Int): List<com.example.climatrack.models.DetalleRepuestoInfo> {
        val list = mutableListOf<com.example.climatrack.models.DetalleRepuestoInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT d.${DatabaseHelper.COL_DET_ID}, r.${DatabaseHelper.COL_REP_NOMBRE}, " +
                "r.${DatabaseHelper.COL_REP_COD}, r.${DatabaseHelper.COL_REP_UNIDAD}, " +
                "d.${DatabaseHelper.COL_DET_CANT}, d.${DatabaseHelper.COL_DET_PRECIO}, d.${DatabaseHelper.COL_DET_OBS} " +
                "FROM ${DatabaseHelper.TABLE_DETALLE_REPUESTOS} d " +
                "JOIN ${DatabaseHelper.TABLE_REPUESTOS} r ON d.${DatabaseHelper.COL_DET_REP_ID} = r.${DatabaseHelper.COL_REP_ID} " +
                "WHERE d.${DatabaseHelper.COL_DET_MANT_ID} = ?"
        
        val cursor = db.rawQuery(query, arrayOf(mantenimientoId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.climatrack.models.DetalleRepuestoInfo(
                    id = cursor.getInt(0),
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

    fun deleteRepuesto(detalleId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_DETALLE_REPUESTOS, "${DatabaseHelper.COL_DET_ID}=?", arrayOf(detalleId.toString()))
    }

    fun addEvidencia(evidencia: Evidencia): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_EVI_ORDEN_ID, evidencia.ordenId)
            put(DatabaseHelper.COL_EVI_RUTA, evidencia.rutaFoto)
            put(DatabaseHelper.COL_EVI_FECHA, evidencia.fecha)
        }
        return db.insert(DatabaseHelper.TABLE_EVIDENCIAS, null, values)
    }

    fun addUbicacion(ubicacion: Ubicacion): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_UBI_ORDEN_ID, ubicacion.ordenId)
            put(DatabaseHelper.COL_UBI_LAT, ubicacion.latitud)
            put(DatabaseHelper.COL_UBI_LON, ubicacion.longitud)
            put(DatabaseHelper.COL_UBI_DIR, ubicacion.direccion)
            put(DatabaseHelper.COL_UBI_FECHA, ubicacion.fecha)
        }
        return db.insert(DatabaseHelper.TABLE_UBICACIONES, null, values)
    }

    fun getUbicacionByOrden(ordenId: Int): Ubicacion? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_UBICACIONES,
            null,
            "${DatabaseHelper.COL_UBI_ORDEN_ID}=?",
            arrayOf(ordenId.toString()),
            null, null, "${DatabaseHelper.COL_UBI_FECHA} DESC", "1"
        )

        var ubicacion: Ubicacion? = null
        if (cursor.moveToFirst()) {
            ubicacion = Ubicacion(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_ID)),
                ordenId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_ORDEN_ID)),
                latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_LAT)),
                longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_LON)),
                direccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_DIR)),
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBI_FECHA))
            )
        }
        cursor.close()
        return ubicacion
    }

    fun addAprobacion(aprobacion: Aprobacion): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_APROB_ORDEN_ID, aprobacion.ordenId)
            put(DatabaseHelper.COL_APROB_CLIENTE, aprobacion.cliente)
            put(DatabaseHelper.COL_APROB_ACEPTADO, aprobacion.aceptado)
            put(DatabaseHelper.COL_APROB_FECHA, aprobacion.fecha)
        }
        return db.insert(DatabaseHelper.TABLE_APROBACIONES, null, values)
    }

    fun getEvidenciasByOrden(ordenId: Int): List<Evidencia> {
        val evidencias = mutableListOf<Evidencia>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_EVIDENCIAS,
            null,
            "${DatabaseHelper.COL_EVI_ORDEN_ID}=?",
            arrayOf(ordenId.toString()),
            null, null, "${DatabaseHelper.COL_EVI_FECHA} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                evidencias.add(
                    Evidencia(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_ID)),
                        ordenId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_ORDEN_ID)),
                        rutaFoto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_RUTA)),
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_FECHA))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return evidencias
    }

    fun deleteEvidencia(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_EVIDENCIAS,
            "${DatabaseHelper.COL_EVI_ID}=?",
            arrayOf(id.toString())
        )
    }
}
