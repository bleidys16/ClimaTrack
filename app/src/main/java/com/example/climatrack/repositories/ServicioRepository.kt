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
        }
        return db.insert(DatabaseHelper.TABLE_DETALLE_REPUESTOS, null, values)
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
            put(DatabaseHelper.COL_UBI_FECHA, ubicacion.fecha)
        }
        return db.insert(DatabaseHelper.TABLE_UBICACIONES, null, values)
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
