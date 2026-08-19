package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Mantenimiento

class MantenimientoRepository(context: Context) {
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
        }
        return db.insert(DatabaseHelper.TABLE_MANTENIMIENTOS, null, values)
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
            tiempoEmpleado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_TIEMPO))
        )
    }
}
