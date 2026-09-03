package com.example.climatrack.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Evidencia
import com.example.climatrack.models.Mantenimiento
import com.example.climatrack.models.Orden
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val dbHelper = DatabaseHelper(appContext)
    private val firestore = FirebaseHelper.db
    private val storage = FirebaseStorage.getInstance()

    override suspend fun doWork(): Result {
        return try {
            syncOrders()
            syncMaintenance()
            syncEvidences()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncOrders() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_ORDENES, null, 
            "${DatabaseHelper.COL_SYNCED} = 0", null, null, null, null)
        
        if (cursor.moveToFirst()) {
            do {
                val orderNum = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_NUM))
                val order = Orden(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ID)),
                    numero = orderNum,
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_FECHA)),
                    clienteId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_CLIENTE_ID)),
                    equipoId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_EQUIPO_ID)),
                    tecnicoId = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECNICO_ID))) null else cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECNICO_ID)),
                    tipoServicio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TIPO)),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_DESC)),
                    estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_ESTADO)),
                    precioServicio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_PRECIO)),
                    latitudCliente = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LAT)),
                    longitudCliente = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_LON)),
                    direccionExacta = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_DIR_EXACTA)),
                    firmaBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_FIRMA)),
                    isSynced = 1,
                    calificacion = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_CALIFICACION)),
                    comentario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_COMENTARIO)),
                    tecnicoLat = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECH_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECH_LAT)),
                    tecnicoLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECH_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDEN_TECH_LON))
                )

                firestore.collection("ordenes").document(orderNum).set(order, SetOptions.merge()).await()
                
                val values = ContentValues().apply { put(DatabaseHelper.COL_SYNCED, 1) }
                dbHelper.writableDatabase.update(DatabaseHelper.TABLE_ORDENES, values, 
                    "${DatabaseHelper.COL_ORDEN_NUM} = ?", arrayOf(orderNum))
                
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private suspend fun syncMaintenance() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_MANTENIMIENTOS, null, 
            "${DatabaseHelper.COL_SYNCED} = 0", null, null, null, null)
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_ID))
                val mant = Mantenimiento(
                    id = id,
                    ordenId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_ORDEN_ID)),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_FECHA)),
                    diagnostico = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_DIAG)),
                    trabajoRealizado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_TRABAJO)),
                    observaciones = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_OBS)),
                    recomendaciones = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_RECOM)),
                    estadoEquipo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_ESTADO_EQ)),
                    tiempoEmpleado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MANT_TIEMPO)),
                    isSynced = 1
                )

                firestore.collection("mantenimientos").document(id.toString()).set(mant, SetOptions.merge()).await()
                
                val values = ContentValues().apply { put(DatabaseHelper.COL_SYNCED, 1) }
                dbHelper.writableDatabase.update(DatabaseHelper.TABLE_MANTENIMIENTOS, values, 
                    "${DatabaseHelper.COL_MANT_ID} = ?", arrayOf(id.toString()))
                
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private suspend fun syncEvidences() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_EVIDENCIAS, null, 
            "${DatabaseHelper.COL_SYNCED} = 0", null, null, null, null)
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_ID))
                val localPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_RUTA))
                val file = File(localPath)
                
                if (file.exists()) {
                    val storageRef = storage.reference.child("evidencias/${file.name}")
                    storageRef.putFile(Uri.fromFile(file)).await()
                    val downloadUrl = storageRef.downloadUrl.await().toString()
                    
                    val evidence = Evidencia(
                        id = id,
                        ordenId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_ORDEN_ID)),
                        rutaFoto = downloadUrl,
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVI_FECHA)),
                        isSynced = 1
                    )

                    firestore.collection("evidencias").document(id.toString()).set(evidence, SetOptions.merge()).await()
                }

                val values = ContentValues().apply { put(DatabaseHelper.COL_SYNCED, 1) }
                dbHelper.writableDatabase.update(DatabaseHelper.TABLE_EVIDENCIAS, values, 
                    "${DatabaseHelper.COL_EVI_ID} = ?", arrayOf(id.toString()))
                
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}
