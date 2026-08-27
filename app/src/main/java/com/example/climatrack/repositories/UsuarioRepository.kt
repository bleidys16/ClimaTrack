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
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_TEL))
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
        }
        return db.insert(DatabaseHelper.TABLE_USUARIOS, null, values)
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
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_TEL))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
