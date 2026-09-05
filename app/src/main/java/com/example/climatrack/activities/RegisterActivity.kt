package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.databinding.ActivityRegisterBinding
import com.example.climatrack.models.Usuario
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.FirebaseHelper
import com.example.climatrack.utils.SessionManager

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicialización forzada de Firebase
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
                android.util.Log.d("FIREBASE_INIT", "Firebase inicializado manualmente")
            }
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_INIT", "Error al inicializar Firebase", e)
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        usuarioRepository = UsuarioRepository(this)
        sessionManager = SessionManager(this)

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val user = binding.etUser.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (fullName.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.visibility = android.view.View.INVISIBLE
        binding.progressBar.visibility = android.view.View.VISIBLE

        // 1. Save locally FIRST (Ensures the app works even if SENA internet blocks Firebase)
        val newUser = Usuario(
            usuario = user,
            password = pass,
            nombre = fullName,
            rol = "Cliente",
            email = email,
            telefono = phone
        )

        try {
            val localId = usuarioRepository.register(newUser)
            android.util.Log.d("REGISTER_DEBUG", "Local registration success for ID: $localId")
            
            // 2. Try Firebase Auth (Background-ish)
            FirebaseHelper.auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    binding.btnRegister.visibility = android.view.View.VISIBLE
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (task.isSuccessful) {
                        // Sync to Firestore if Firebase succeeded
                        usuarioRepository.syncUserToCloud(newUser.copy(id = localId.toInt()))
                        Toast.makeText(this, "Registro exitoso y sincronizado", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val e = task.exception
                        android.util.Log.e("REGISTER_ERROR", "Firebase blocked or failed", e)
                        
                        // IF it's a network error (common in SENA), we allow the user to continue locally
                        if (e is com.google.firebase.FirebaseNetworkException) {
                            Toast.makeText(this, "Internet SENA detectado. Registro local exitoso (Modo Offline)", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            // If it's another error (like email exists), maybe we should warn the user
                            val errorMsg = when (e) {
                                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "El correo ya está registrado."
                                else -> "Registro local guardado. Error en nube: ${e?.message}"
                            }
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }
        } catch (e: Exception) {
            binding.btnRegister.visibility = android.view.View.VISIBLE
            binding.progressBar.visibility = android.view.View.GONE
            Toast.makeText(this, "Error al guardar localmente: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
