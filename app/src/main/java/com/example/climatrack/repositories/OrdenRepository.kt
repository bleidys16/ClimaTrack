package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Orden

class OrdenRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

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
        return db.update(DatabaseHelper.TABLE_ORDENES, values, "${DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(id.toString()))
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

    private fun cursorToOrden(cursor: Cursor): Orden {
        return Orden(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ID)),
            numero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_NUM)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_FECHA)),
            clienteId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_CLIENTE_ID)),
            equipoId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_EQUIPO_ID)),
            tecnicoId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECNICO_ID)),
            tipoServicio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TIPO)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_DESC)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ESTADO))
        )
    }
}
