package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.ActividadTecnico
import com.example.climatrack.models.TecnicoStats
import com.example.climatrack.models.Usuario
import com.example.climatrack.utils.FirebaseHelper
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class UsuarioRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val firestore = FirebaseHelper.db

    fun syncUserToCloud(usuario: Usuario) {
        val userMap = hashMapOf(
            "id" to usuario.id,
            "usuario" to usuario.usuario,
            "password" to usuario.password,
            "nombre" to usuario.nombre,
            "rol" to usuario.rol,
            "email" to usuario.email,
            "telefono" to usuario.telefono,
            "isActive" to usuario.isActive,
            "imagenPerfil" to usuario.imagenPerfil,
            "fcmToken" to usuario.fcmToken,
            "lastLat" to usuario.lastLat,
            "lastLon" to usuario.lastLon,
            "workStartTime" to usuario.workStartTime,
            "workEndTime" to usuario.workEndTime
        )
        firestore.collection("usuarios").document(usuario.id)
            .set(userMap, SetOptions.merge())
    }

    fun fetchUsersFromCloud(onComplete: () -> Unit) {
        firestore.collection("usuarios")
            .get()
            .addOnSuccessListener { documents ->
                val db = dbHelper.writableDatabase
                for (doc in documents) {
                    try {
                        val id = doc.id
                        val values = ContentValues().apply {
                            put(DatabaseHelper.COL_USUARIO_ID, id)
                            put(DatabaseHelper.COL_USUARIO_USER, doc.getString("usuario"))
                            put(DatabaseHelper.COL_USUARIO_PASS, doc.getString("password"))
                            put(DatabaseHelper.COL_USUARIO_NOMBRE, doc.getString("nombre"))
                            put(DatabaseHelper.COL_USUARIO_ROL, doc.getString("rol"))
                            put(DatabaseHelper.COL_USUARIO_EMAIL, doc.getString("email"))
                            put(DatabaseHelper.COL_USUARIO_TEL, doc.getString("telefono"))
                            put(DatabaseHelper.COL_USUARIO_ACTIVE, doc.getLong("isActive")?.toInt() ?: 0)
                            put(DatabaseHelper.COL_USUARIO_IMAGEN, doc.getString("imagenPerfil"))
                            put(DatabaseHelper.COL_USUARIO_FCM, doc.getString("fcmToken"))
                            put(DatabaseHelper.COL_USUARIO_LAT, doc.getDouble("lastLat"))
                            put(DatabaseHelper.COL_USUARIO_LON, doc.getDouble("lastLon"))
                            put(DatabaseHelper.COL_USUARIO_WORK_START, doc.getString("workStartTime"))
                            put(DatabaseHelper.COL_USUARIO_WORK_END, doc.getString("workEndTime"))
                        }
                        
                        val count = db.update(DatabaseHelper.TABLE_USUARIOS, values, 
                            "${DatabaseHelper.COL_USUARIO_ID}=?", arrayOf(id))
                        
                        if (count == 0) {
                            db.insert(DatabaseHelper.TABLE_USUARIOS, null, values)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC_ERROR", "Error fetching user doc: ${doc.id}", e)
                    }
                }
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }

    fun updateFCMToken(userId: String, token: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_FCM, token)
        }
        val result = db.update(DatabaseHelper.TABLE_USUARIOS, values, "${DatabaseHelper.COL_USUARIO_ID}=?", arrayOf(userId))
        
        if (result > 0) {
            firestore.collection("usuarios").document(userId)
                .update("fcmToken", token)
        }
    }

    fun login(identifier: String, password: String, onResult: (Usuario?) -> Unit) {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "(${DatabaseHelper.COL_USUARIO_USER}=? OR ${DatabaseHelper.COL_USUARIO_EMAIL}=?) AND ${DatabaseHelper.COL_USUARIO_PASS}=?",
            arrayOf(identifier, identifier, password),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val user = cursorToUsuario(cursor)
            cursor.close()
            onResult(user)
        } else {
            cursor.close()
            // Cloud fallback (Hybrid Login)
            firestore.collection("usuarios")
                .whereEqualTo("usuario", identifier)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val doc = docs.documents[0]
                        val user = doc.toObject(Usuario::class.java)
                        if (user != null) {
                            registerLocal(user)
                            onResult(user)
                        } else onResult(null)
                    } else {
                        // Try email
                        firestore.collection("usuarios")
                            .whereEqualTo("email", identifier)
                            .whereEqualTo("password", password)
                            .get()
                            .addOnSuccessListener { docsEmail ->
                                if (!docsEmail.isEmpty) {
                                    val doc = docsEmail.documents[0]
                                    val user = doc.toObject(Usuario::class.java)
                                    if (user != null) {
                                        registerLocal(user)
                                        onResult(user)
                                    } else onResult(null)
                                } else onResult(null)
                            }
                    }
                }
                .addOnFailureListener { onResult(null) }
        }
    }

    fun fetchTechniciansFromCloud(onComplete: () -> Unit) {
        fetchUsersFromCloud(onComplete)
    }

    private fun registerLocal(usuario: Usuario) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_ID, usuario.id)
            put(DatabaseHelper.COL_USUARIO_USER, usuario.usuario)
            put(DatabaseHelper.COL_USUARIO_PASS, usuario.password)
            put(DatabaseHelper.COL_USUARIO_NOMBRE, usuario.nombre)
            put(DatabaseHelper.COL_USUARIO_ROL, usuario.rol)
            put(DatabaseHelper.COL_USUARIO_EMAIL, usuario.email)
            put(DatabaseHelper.COL_USUARIO_TEL, usuario.telefono)
        }
        db.insertWithOnConflict(DatabaseHelper.TABLE_USUARIOS, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun register(usuario: Usuario): String {
        val db = dbHelper.writableDatabase
        val id = if (usuario.id.isEmpty()) UUID.randomUUID().toString() else usuario.id
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_ID, id)
            put(DatabaseHelper.COL_USUARIO_USER, usuario.usuario)
            put(DatabaseHelper.COL_USUARIO_PASS, usuario.password)
            put(DatabaseHelper.COL_USUARIO_NOMBRE, usuario.nombre)
            put(DatabaseHelper.COL_USUARIO_ROL, usuario.rol)
            put(DatabaseHelper.COL_USUARIO_EMAIL, usuario.email)
            put(DatabaseHelper.COL_USUARIO_TEL, usuario.telefono)
        }
        
        val rows = db.update(DatabaseHelper.TABLE_USUARIOS, values,
            "${DatabaseHelper.COL_USUARIO_USER}=?", arrayOf(usuario.usuario))
        
        if (rows == 0) {
            db.insert(DatabaseHelper.TABLE_USUARIOS, null, values)
        }
        
        val finalUser = usuario.copy(id = id)
        syncUserToCloud(finalUser)
        return id
    }

    fun getById(id: String): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COL_USUARIO_ID}=?",
            arrayOf(id),
            null, null, null
        )
        var user: Usuario? = null
        if (cursor.moveToFirst()) {
            user = cursorToUsuario(cursor)
        }
        cursor.close()
        return user
    }

    fun getAllTecnicos(): List<Usuario> {
        val list = mutableListOf<Usuario>()
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COL_USUARIO_ROL} LIKE 'T%cnico%'"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToUsuario(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAllClientes(): List<Usuario> {
        val list = mutableListOf<Usuario>()
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COL_USUARIO_ROL} = 'Cliente'"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToUsuario(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateStatus(userId: String, isActive: Int, workStart: String?, workEnd: String?, lat: Double?, lon: Double?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_ACTIVE, isActive)
            put(DatabaseHelper.COL_USUARIO_WORK_START, workStart)
            put(DatabaseHelper.COL_USUARIO_WORK_END, workEnd)
            put(DatabaseHelper.COL_USUARIO_LAT, lat)
            put(DatabaseHelper.COL_USUARIO_LON, lon)
        }
        
        val result = db.update(DatabaseHelper.TABLE_USUARIOS, values, "${DatabaseHelper.COL_USUARIO_ID}=?", arrayOf(userId))
        
        if (result > 0) {
            logActivity(userId, isActive, workStart, workEnd, lat, lon)
            getById(userId)?.let { syncUserToCloud(it) }
        }
        return result
    }

    private fun logActivity(userId: String, isActive: Int, start: String?, end: String?, lat: Double?, lon: Double?) {
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = sdf.format(Date())
        
        val values = ContentValues().apply {
            if (isActive == 1) {
                put(DatabaseHelper.COL_ACT_INICIO, start)
                put(DatabaseHelper.COL_ACT_LAT, lat)
                put(DatabaseHelper.COL_ACT_LON, lon)
            } else {
                put(DatabaseHelper.COL_ACT_FIN, end)
            }
        }

        val rows = db.update(DatabaseHelper.TABLE_ACTIVIDAD, values, 
            "${DatabaseHelper.COL_ACT_TECH_ID}=? AND ${DatabaseHelper.COL_ACT_FECHA}=?", arrayOf(userId, fecha))
        
        if (rows == 0 && isActive == 1) {
            val cvNew = ContentValues().apply {
                put(DatabaseHelper.COL_ACT_ID, UUID.randomUUID().toString())
                put(DatabaseHelper.COL_ACT_TECH_ID, userId)
                put(DatabaseHelper.COL_ACT_FECHA, fecha)
                put(DatabaseHelper.COL_ACT_INICIO, start)
                put(DatabaseHelper.COL_ACT_LAT, lat)
                put(DatabaseHelper.COL_ACT_LON, lon)
            }
            db.insert(DatabaseHelper.TABLE_ACTIVIDAD, null, cvNew)
        }
    }

    fun getTechnicianStats(): List<TecnicoStats> {
        val list = mutableListOf<TecnicoStats>()
        val db = dbHelper.readableDatabase
        val query = "SELECT u.${DatabaseHelper.COL_USUARIO_ID}, u.${DatabaseHelper.COL_USUARIO_NOMBRE}, " +
                "u.${DatabaseHelper.COL_USUARIO_ACTIVE}, u.${DatabaseHelper.COL_USUARIO_EMAIL}, u.${DatabaseHelper.COL_USUARIO_TEL}, " +
                "(SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_ORDENES} o WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u.${DatabaseHelper.COL_USUARIO_ID} AND o.${DatabaseHelper.COL_ORDEN_ESTADO} = 'FINALIZADA') as count, " +
                "(SELECT AVG(${DatabaseHelper.COL_ORDEN_CALIFICACION}) FROM ${DatabaseHelper.TABLE_ORDENES} o WHERE o.${DatabaseHelper.COL_ORDEN_TECNICO_ID} = u.${DatabaseHelper.COL_USUARIO_ID} AND o.${DatabaseHelper.COL_ORDEN_CALIFICACION} > 0) as avg_rating " +
                "FROM ${DatabaseHelper.TABLE_USUARIOS} u " +
                "WHERE u.${DatabaseHelper.COL_USUARIO_ROL} LIKE 'T%cnico%'"
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(TecnicoStats(
                    id = cursor.getString(0),
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

    fun getTechnicianHistory(techId: String): List<ActividadTecnico> {
        val list = mutableListOf<ActividadTecnico>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_ACTIVIDAD, null,
            "${DatabaseHelper.COL_ACT_TECH_ID}=?", arrayOf(techId),
            null, null, "${DatabaseHelper.COL_ACT_FECHA} DESC")
        
        if (cursor.moveToFirst()) {
            do {
                list.add(ActividadTecnico(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_ID)),
                    tecnicoId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACT_TECH_ID)),
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

    fun updateProfile(userId: String, nombre: String, email: String?, telefono: String?, imagePath: String?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USUARIO_NOMBRE, nombre)
            put(DatabaseHelper.COL_USUARIO_EMAIL, email)
            put(DatabaseHelper.COL_USUARIO_TEL, telefono)
            put(DatabaseHelper.COL_USUARIO_IMAGEN, imagePath)
        }
        val result = db.update(DatabaseHelper.TABLE_USUARIOS, values, "${DatabaseHelper.COL_USUARIO_ID}=?", arrayOf(userId))
        if (result > 0) {
            getById(userId)?.let { syncUserToCloud(it) }
        }
        return result
    }

    private fun cursorToUsuario(cursor: Cursor): Usuario {
        return try {
            Usuario(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ID)) ?: "",
                usuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_USER)) ?: "",
                password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_PASS)) ?: "",
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_NOMBRE)) ?: "Usuario desconocido",
                rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ROL)) ?: "Cliente",
                email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_EMAIL)),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_TEL)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ACTIVE)),
                workStartTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_START)),
                workEndTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_WORK_END)),
                lastLat = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LAT)),
                lastLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_LON)),
                imagenPerfil = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_IMAGEN)),
                fcmToken = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_FCM))
            )
        } catch (e: Exception) {
            android.util.Log.e("DB_ERROR", "Error parsing user from cursor", e)
            Usuario(id = "error", nombre = "Error de datos")
        }
    }
}
