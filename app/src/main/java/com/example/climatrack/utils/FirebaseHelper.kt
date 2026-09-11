package com.example.climatrack.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.storage.FirebaseStorage

object FirebaseHelper {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    
    val db: FirebaseFirestore by lazy {
        val instance = FirebaseFirestore.getInstance()
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            instance.firestoreSettings = settings
        } catch (_: Exception) {
            // Settings already set
        }
        instance
    }
    
    val storage: FirebaseStorage get() = FirebaseStorage.getInstance()

    fun getUserId(): String? = auth.currentUser?.uid
}
