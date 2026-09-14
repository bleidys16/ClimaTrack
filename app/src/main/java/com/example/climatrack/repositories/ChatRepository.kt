package com.example.climatrack.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.climatrack.database.DatabaseHelper
import com.example.climatrack.models.Mensaje
import com.example.climatrack.utils.FirebaseHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class ChatRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val firestore: FirebaseFirestore = FirebaseHelper.db

    fun getMessagesLocal(ordenId: String): List<Mensaje> {
        val list = mutableListOf<Mensaje>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_MENSAJES, null,
            "${DatabaseHelper.COL_MSG_ORDEN_ID}=?", arrayOf(ordenId),
            null, null, "${DatabaseHelper.COL_MSG_FECHA} ASC")
        
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToMensaje(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun sendMessage(mensaje: Mensaje) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_MSG_ORDEN_ID, mensaje.ordenId)
            put(DatabaseHelper.COL_MSG_REMITENTE_ID, mensaje.remitenteId)
            put(DatabaseHelper.COL_MSG_REMITENTE_NOMBRE, mensaje.nombreRemitente)
            put(DatabaseHelper.COL_MSG_TEXTO, mensaje.texto)
            put(DatabaseHelper.COL_MSG_FECHA, mensaje.fecha)
        }
        db.insert(DatabaseHelper.TABLE_MENSAJES, null, values)

        firestore.collection("ordenes").document(mensaje.ordenId)
            .collection("mensajes").add(mensaje)
    }

    fun listenToMessages(ordenId: String, onNewMessages: (List<Mensaje>) -> Unit) {
        firestore.collection("ordenes").document(ordenId)
            .collection("mensajes")
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val messages = snapshot.toObjects(Mensaje::class.java)
                syncLocalMessages(ordenId, messages)
                onNewMessages(messages)
            }
    }

    private fun syncLocalMessages(ordenId: String, cloudMessages: List<Mensaje>) {
        val db = dbHelper.writableDatabase
        cloudMessages.forEach { msg ->
            val cursor = db.query(DatabaseHelper.TABLE_MENSAJES, null, 
                "${DatabaseHelper.COL_MSG_FECHA}=? AND ${DatabaseHelper.COL_MSG_TEXTO}=?", 
                arrayOf(msg.fecha, msg.texto), null, null, null)
            
            if (cursor.count == 0) {
                val values = ContentValues().apply {
                    put(DatabaseHelper.COL_MSG_ORDEN_ID, ordenId)
                    put(DatabaseHelper.COL_MSG_REMITENTE_ID, msg.remitenteId)
                    put(DatabaseHelper.COL_MSG_REMITENTE_NOMBRE, msg.nombreRemitente)
                    put(DatabaseHelper.COL_MSG_TEXTO, msg.texto)
                    put(DatabaseHelper.COL_MSG_FECHA, msg.fecha)
                }
                db.insert(DatabaseHelper.TABLE_MENSAJES, null, values)
            }
            cursor.close()
        }
    }

    private fun cursorToMensaje(cursor: Cursor): Mensaje {
        return Mensaje(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_ID)),
            ordenId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_ORDEN_ID)),
            remitenteId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_REMITENTE_ID)),
            nombreRemitente = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_REMITENTE_NOMBRE)),
            texto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_TEXTO)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_FECHA))
        )
    }
}
