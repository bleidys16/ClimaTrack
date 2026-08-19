package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Equipo

class EquipoRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun create(equipo: Equipo): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_EQUIPO_COD, equipo.codigo)
            put(DatabaseHelper.COL_EQUIPO_TIPO, equipo.tipo)
            put(DatabaseHelper.COL_EQUIPO_MARCA, equipo.marca)
            put(DatabaseHelper.COL_EQUIPO_MODELO, equipo.modelo)
            put(DatabaseHelper.COL_EQUIPO_SERIAL, equipo.serial)
            put(DatabaseHelper.COL_EQUIPO_CAPACIDAD, equipo.capacidad)
            put(DatabaseHelper.COL_EQUIPO_UBICACION, equipo.ubicacion)
            put(DatabaseHelper.COL_EQUIPO_CLIENTE_ID, equipo.clienteId)
            put(DatabaseHelper.COL_EQUIPO_ESTADO, equipo.estado)
        }
        return db.insert(DatabaseHelper.TABLE_EQUIPOS, null, values)
    }

    fun getAll(): List<Equipo> {
        val list = mutableListOf<Equipo>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_EQUIPOS}", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToEquipo(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getById(id: Int): Equipo? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_EQUIPOS, null, "${DatabaseHelper.COL_EQUIPO_ID}=?", arrayOf(id.toString()), null, null, null)
        var equipo: Equipo? = null
        if (cursor.moveToFirst()) {
            equipo = cursorToEquipo(cursor)
        }
        cursor.close()
        return equipo
    }

    fun update(equipo: Equipo): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_EQUIPO_COD, equipo.codigo)
            put(DatabaseHelper.COL_EQUIPO_TIPO, equipo.tipo)
            put(DatabaseHelper.COL_EQUIPO_MARCA, equipo.marca)
            put(DatabaseHelper.COL_EQUIPO_MODELO, equipo.modelo)
            put(DatabaseHelper.COL_EQUIPO_SERIAL, equipo.serial)
            put(DatabaseHelper.COL_EQUIPO_CAPACIDAD, equipo.capacidad)
            put(DatabaseHelper.COL_EQUIPO_UBICACION, equipo.ubicacion)
            put(DatabaseHelper.COL_EQUIPO_CLIENTE_ID, equipo.clienteId)
            put(DatabaseHelper.COL_EQUIPO_ESTADO, equipo.estado)
        }
        return db.update(DatabaseHelper.TABLE_EQUIPOS, values, "${DatabaseHelper.COL_EQUIPO_ID}=?", arrayOf(equipo.id.toString()))
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_EQUIPOS, "${DatabaseHelper.COL_EQUIPO_ID}=?", arrayOf(id.toString()))
    }

    private fun cursorToEquipo(cursor: Cursor): Equipo {
        return Equipo(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_ID)),
            codigo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_COD)),
            tipo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_TIPO)),
            marca = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_MARCA)),
            modelo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_MODELO)),
            serial = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_SERIAL)),
            capacidad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_CAPACIDAD)),
            ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_UBICACION)),
            clienteId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_CLIENTE_ID)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPO_ESTADO))
        )
    }
}
