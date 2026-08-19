package com.example.climatrack.repositories

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
                rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ROL))
            )
        }
        cursor.close()
        return user
    }
}
