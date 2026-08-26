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
            put(DatabaseHelper.COL_EQUIPO_IMAGEN, equipo.imagenPath)
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
            put(DatabaseHelper.COL_EQUIPO_IMAGEN, equipo.imagenPath)
        }
        return db.update(DatabaseHelper.TABLE_EQUIPOS, values, "${DatabaseHelper.COL_EQUIPO_ID}=?", arrayOf(equipo.id.toString()))
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_EQUIPOS, "${DatabaseHelper.COL_EQUIPO_ID}=?", arrayOf(id.toString()))
    }

    private fun cursorToEquipo(cursor: Cursor): Equipo {
        val idIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_ID)
        val codIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_COD)
        val tipoIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_TIPO)
        val marcaIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_MARCA)
        val modelIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_MODELO)
        val serialIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_SERIAL)
        val capIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_CAPACIDAD)
        val ubiIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_UBICACION)
        val clientIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_CLIENTE_ID)
        val statusIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_ESTADO)
        val imgIdx = cursor.getColumnIndex(DatabaseHelper.COL_EQUIPO_IMAGEN)

        return Equipo(
            id = if (idIdx != -1) cursor.getInt(idIdx) else 0,
            codigo = if (codIdx != -1) cursor.getString(codIdx) else "",
            tipo = if (tipoIdx != -1) cursor.getString(tipoIdx) else "",
            marca = if (marcaIdx != -1) cursor.getString(marcaIdx) else "",
            modelo = if (modelIdx != -1) cursor.getString(modelIdx) else "",
            serial = if (serialIdx != -1) cursor.getString(serialIdx) else null,
            capacidad = if (capIdx != -1) cursor.getString(capIdx) else null,
            ubicacion = if (ubiIdx != -1) cursor.getString(ubiIdx) else null,
            clienteId = if (clientIdx != -1) cursor.getInt(clientIdx) else 0,
            estado = if (statusIdx != -1) cursor.getString(statusIdx) ?: "PENDIENTE" else "PENDIENTE",
            imagenPath = if (imgIdx != -1) cursor.getString(imgIdx) else null
        )
    }
}
