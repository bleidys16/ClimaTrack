package com.example.climatrack.repositories

import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Repuesto

class RepuestoRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun getAll(): List<Repuesto> {
        val list = mutableListOf<Repuesto>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_REPUESTOS}", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToRepuesto(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    private fun cursorToRepuesto(cursor: Cursor): Repuesto {
        return Repuesto(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REP_ID)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REP_NOMBRE)),
            codigo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REP_COD)),
            unidad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REP_UNIDAD)),
            precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REP_PRECIO))
        )
    }
}
