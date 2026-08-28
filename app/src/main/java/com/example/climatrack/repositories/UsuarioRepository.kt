package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Usuario

class UsuarioRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun login(usuario: String, password: String): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COL_USUARIO_USER}=? AND ${DatabaseHelper.COL_USUARIO_PASS}=?",
            arrayOf(usuario, password),
            null, null, null
        )

        var user: Usuario? = null
        if (cursor.moveToFirst()) {
            user = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ID)),
                usuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_USER)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_PASS)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_NOMBRE)),
                rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ROL)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_EMAIL)),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_TEL)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ACTIVE)),
                workStartTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_START)),
                workEndTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_END)),
                lastLat = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT)),
                lastLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))
            )
        }
        cursor.close()
        return user
    }

    fun register(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_USER, usuario.usuario)
            put(DatabaseHelper.COL_USUARIO_PASS, usuario.password)
            put(DatabaseHelper.COL_USUARIO_NOMBRE, usuario.nombre)
            put(DatabaseHelper.COL_USUARIO_ROL, usuario.rol)
            put(DatabaseHelper.COL_USUARIO_EMAIL, usuario.email)
            put(DatabaseHelper.COL_USUARIO_TEL, usuario.telefono)
            put(DatabaseHelper.COL_USUARIO_ACTIVE, usuario.isActive)
            put(DatabaseHelper.COL_USUARIO_WORK_START, usuario.workStartTime)
            put(DatabaseHelper.COL_USUARIO_WORK_END, usuario.workEndTime)
        }
        return db.insert(DatabaseHelper.TABLE_USUARIOS, null, values)
    }

    fun getById(id: Int): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COL_USUARIO_ID}=?",
            arrayOf(id.toString()),
            null, null, null
        )
        var user: Usuario? = null
        if (cursor.moveToFirst()) {
            user = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ID)),
                usuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_USER)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_PASS)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_NOMBRE)),
                rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ROL)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_EMAIL)),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_TEL)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ACTIVE)),
                workStartTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_START)),
                workEndTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_END)),
                lastLat = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT)),
                lastLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))
            )
        }
        cursor.close()
        return user
    }

    fun getAllTecnicos(): List<Usuario> {
        val list = mutableListOf<Usuario>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COL_USUARIO_ROL}=?",
            arrayOf("Técnico"),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(Usuario(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ID)),
                    usuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_USER)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_PASS)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_NOMBRE)),
                    rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ROL)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_EMAIL)),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_TEL)),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ACTIVE)),
                    workStartTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_START)),
                    workEndTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_END)),
                    lastLat = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT)),
                    lastLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getActiveTechnicians(): List<Usuario> {
        val list = mutableListOf<Usuario>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COL_USUARIO_ROL}=? AND ${DatabaseHelper.COL_USUARIO_ACTIVE}=1",
            arrayOf("Técnico"),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(Usuario(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ID)),
                    usuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_USER)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_PASS)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_NOMBRE)),
                    rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ROL)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_EMAIL)),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_TEL)),
                    isActive = 1,
                    workStartTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_START)),
                    workEndTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_END)),
                    lastLat = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT)),
                    lastLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateStatus(userId: Int, isActive: Int, workStart: String?, workEnd: String?, lat: Double?, lon: Double?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_ACTIVE, isActive)
            put(DatabaseHelper.COL_USUARIO_WORK_START, workStart)
            put(DatabaseHelper.COL_USUARIO_WORK_END, workEnd)
            put(DatabaseHelper.COL_USUARIO_LAT, lat)
            put(DatabaseHelper.COL_USUARIO_LON, lon)
        }
        return db.update(DatabaseHelper.TABLE_USUARIOS, values, "${DatabaseHelper.COL_USUARIO_ID}=?", arrayOf(userId.toString()))
    }
}
