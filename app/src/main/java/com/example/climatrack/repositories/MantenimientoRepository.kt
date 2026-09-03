package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Mantenimiento
import com.example.climatrack.utils.SyncManager

class MantenimientoRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun create(mantenimiento: Mantenimiento): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_MANT_ORDEN_ID, mantenimiento.ordenId)
            put(DatabaseHelper.COL_MANT_FECHA, mantenimiento.fecha)
            put(DatabaseHelper.COL_MANT_DIAG, mantenimiento.diagnostico)
            put(DatabaseHelper.COL_MANT_TRABAJO, mantenimiento.trabajoRealizado)
            put(DatabaseHelper.COL_MANT_OBS, mantenimiento.observaciones)
            put(DatabaseHelper.COL_MANT_RECOM, mantenimiento.recomendaciones)
            put(DatabaseHelper.COL_MANT_ESTADO_EQ, mantenimiento.estadoEquipo)
            put(DatabaseHelper.COL_MANT_TIEMPO, mantenimiento.tiempoEmpleado)
            put(DatabaseHelper.COL_SYNCED, 0)
        }
        val result = db.insert(DatabaseHelper.TABLE_MANTENIMIENTOS, null, values)
        if (result > 0) SyncManager.startImmediateSync(context)
        return result
    }

    fun update(mantenimiento: Mantenimiento): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_MANT_ORDEN_ID, mantenimiento.ordenId)
            put(DatabaseHelper.COL_MANT_FECHA, mantenimiento.fecha)
            put(DatabaseHelper.COL_MANT_DIAG, mantenimiento.diagnostico)
            put(DatabaseHelper.COL_MANT_TRABAJO, mantenimiento.trabajoRealizado)
            put(DatabaseHelper.COL_MANT_OBS, mantenimiento.observaciones)
            put(DatabaseHelper.COL_MANT_RECOM, mantenimiento.recomendaciones)
            put(DatabaseHelper.COL_MANT_ESTADO_EQ, mantenimiento.estadoEquipo)
            put(DatabaseHelper.COL_MANT_TIEMPO, mantenimiento.tiempoEmpleado)
            put(DatabaseHelper.COL_SYNCED, 0)
        }
        val result = db.update(DatabaseHelper.TABLE_MANTENIMIENTOS, values, "${DatabaseHelper.COL_MANT_ORDEN_ID}=?", arrayOf(mantenimiento.ordenId.toString()))
        if (result > 0) SyncManager.startImmediateSync(context)
        return result
    }

    fun getByOrdenId(ordenId: Int): Mantenimiento? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_MANTENIMIENTOS,
            null,
            "${DatabaseHelper.COL_MANT_ORDEN_ID}=?",
            arrayOf(ordenId.toString()),
            null, null, null
        )
        var mant: Mantenimiento? = null
        if (cursor.moveToFirst()) {
            mant = cursorToMantenimiento(cursor)
        }
        cursor.close()
        return mant
    }

    fun getHistorialInfo(tecnicoId: Int): List<com.example.climatrack.models.MantenimientoInfo> {
        val list = mutableListOf<com.example.climatrack.models.MantenimientoInfo>()
        val db = dbHelper.readableDatabase
        val query = "SELECT m.${DatabaseHelper.COL_MANT_ID}, o.${DatabaseHelper.COL_ORDEN_NUM}, " +
                "m.${DatabaseHelper.COL_MANT_FECHA}, m.${DatabaseHelper.COL_MANT_DIAG}, " +
                "m.${DatabaseHelper.COL_MANT_TRABAJO}, o.${DatabaseHelper.COL_ORDEN_TIPO}, u.${DatabaseHelper.COL_USUARIO_NOMBRE} " +
                "FROM ${DatabaseHelper.TABLE_MANTENIMIENTOS} m " +
                "JOIN ${DatabaseHelper.TABLE_ORDENES} o ON m.${DatabaseHelper.COL_MANT_ORDEN_ID} = o.${DatabaseHelper.COL_ORDEN_ID} " +
                "JOIN ${DatabaseHelper.TABLE_USUARIOS} u ON o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u.${DatabaseHelper.COL_USUARIO_ID} " +
                "WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = ?"
        
        val cursor = db.rawQuery(query, arrayOf(tecnicoId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.climatrack.models.MantenimientoInfo(
                    id = cursor.getInt(0),
                    ordenNumero = cursor.getString(1),
                    fecha = cursor.getString(2),
                    diagnostico = cursor.getString(3),
                    trabajoRealizado = cursor.getString(4),
                    tipoServicio = cursor.getString(5),
                    tecnicoNombre = cursor.getString(6)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getHistorial(tecnicoId: Int): List<Mantenimiento> {
        val list = mutableListOf<Mantenimiento>()
        val db = dbHelper.readableDatabase
        val query = "SELECT m.* FROM ${DatabaseHelper.TABLE_MANTENIMIENTOS} m " +
                    "JOIN ${DatabaseHelper.TABLE_ORDENES} o ON m.${DatabaseHelper.COL_MANT_ORDEN_ID} = o.${DatabaseHelper.COL_ORDEN_ID} " +
                    "WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = ?"
        
        val cursor = db.rawQuery(query, arrayOf(tecnicoId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToMantenimiento(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    private fun cursorToMantenimiento(cursor: Cursor): Mantenimiento {
        return Mantenimiento(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_ID)),
            ordenId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_ORDEN_ID)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_FECHA)),
            diagnostico = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_DIAG)),
            trabajoRealizado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_TRABAJO)),
            observaciones = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_OBS)),
            recomendaciones = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_RECOM)),
            estadoEquipo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_ESTADO_EQ)),
            tiempoEmpleado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_TIEMPO)),
            isSynced = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SYNCED))
        )
    }
}
