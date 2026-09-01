package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.ActividadTecnico
import com.example.climatrack.models.TecnicoStats
import com.example.climatrack.models.Usuario
import java.text.SimpleDateFormat
import java.util.*

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
                lastLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON)),
                imagenPerfil = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_IMAGEN))
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
            put(DatabaseHelper.COL_USUARIO_IMAGEN, usuario.imagenPerfil)
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
                lastLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON)),
                imagenPerfil = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_IMAGEN))
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

    fun getAllClientes(): List<Usuario> {
        val list = mutableListOf<Usuario>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COL_USUARIO_ROL}=?",
            arrayOf("Cliente"),
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
                    workEndTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_END))
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
        
        val result = db.update(DatabaseHelper.TABLE_USUARIOS, values, "${DatabaseHelper.COL_USUARIO_ID}=?", arrayOf(userId.toString()))
        
        if (result > 0) {
            logActivity(userId, isActive, workStart, workEnd, lat, lon)
        }
        
        return result
    }

    private fun logActivity(userId: Int, isActive: Int, start: String?, end: String?, lat: Double?, lon: Double?) {
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
        val fecha = sdf.format(Date())
        
        // Buscar si ya hay registro para hoy
        val cursor = db.query(DatabaseHelper.TABLE_ACTIVIDAD, arrayOf(DatabaseHelper.COL_ACT_ID),
            "${DatabaseHelper.COL_ACT_TECH_ID}=? AND ${DatabaseHelper.COL_ACT_FECHA}=?",
            arrayOf(userId.toString(), fecha), null, null, null)
        
        val values = ContentValues().apply {
            if (isActive == 1) {
                put(DatabaseHelper.COL_ACT_INICIO, start)
                put(DatabaseHelper.COL_ACT_LAT, lat)
                put(DatabaseHelper.COL_ACT_LON, lon)
            } else {
                put(DatabaseHelper.COL_ACT_FIN, end)
            }
        }

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            db.update(DatabaseHelper.TABLE_ACTIVIDAD, values, "${DatabaseHelper.COL_ACT_ID}=?", arrayOf(id.toString()))
        } else {
            values.put(DatabaseHelper.COL_ACT_TECH_ID, userId)
            values.put(DatabaseHelper.COL_ACT_FECHA, fecha)
            db.insert(DatabaseHelper.TABLE_ACTIVIDAD, null, values)
        }
        cursor.close()
    }

    fun getTechnicianStats(): List<TecnicoStats> {
        val list = mutableListOf<TecnicoStats>()
        val db = dbHelper.readableDatabase
        val query = "SELECT u.${DatabaseHelper.COL_USUARIO_ID}, u.${DatabaseHelper.COL_USUARIO_NOMBRE}, " +
                "u.${DatabaseHelper.COL_USUARIO_ACTIVE}, u.${DatabaseHelper.COL_USUARIO_EMAIL}, u.${DatabaseHelper.COL_USUARIO_TEL}, " +
                "(SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_ORDENES} o WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u.${DatabaseHelper.COL_USUARIO_ID} AND o.${DatabaseHelper.COL_ORDEN_ESTADO} = 'FINALIZADA') as count, " +
                "(SELECT AVG(${DatabaseHelper.COL_ORDEN_CALIFICACION}) FROM ${DatabaseHelper.TABLE_ORDENES} o WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u.${DatabaseHelper.COL_USUARIO_ID} AND o.${DatabaseHelper.COL_ORDEN_CALIFICACION} > 0) as avg_rating " +
                "FROM ${DatabaseHelper.TABLE_USUARIOS} u " +
                "WHERE u.${DatabaseHelper.COL_USUARIO_ROL} = 'Técnico'"
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(TecnicoStats(
                    id = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    isActive = cursor.getInt(2),
                    email = cursor.getString(3),
                    telefono = cursor.getString(4),
                    trabajosRealizados = cursor.getInt(5),
                    promedioCalificacion = cursor.getDouble(6)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTechnicianHistory(techId: Int): List<ActividadTecnico> {
        val list = mutableListOf<ActividadTecnico>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_ACTIVIDAD, null,
            "${DatabaseHelper.COL_ACT_TECH_ID}=?", arrayOf(techId.toString()),
            null, null, "${DatabaseHelper.COL_ACT_FECHA} DESC")
        
        if (cursor.moveToFirst()) {
            do {
                list.add(ActividadTecnico(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_ID)),
                    tecnicoId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_TECH_ID)),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_FECHA)),
                    horaInicio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_INICIO)),
                    horaFin = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_FIN)),
                    lat = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_LAT)),
                    lon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_LON))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateProfile(userId: Int, nombre: String, email: String?, telefono: String?, imagePath: String?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_NOMBRE, nombre)
            put(DatabaseHelper.COL_USUARIO_EMAIL, email)
            put(DatabaseHelper.COL_USUARIO_TEL, telefono)
            put(DatabaseHelper.COL_USUARIO_IMAGEN, imagePath)
        }
        return db.update(DatabaseHelper.TABLE_USUARIOS, values, "${DatabaseHelper.COL_USUARIO_ID}=?", arrayOf(userId.toString()))
    }
}
