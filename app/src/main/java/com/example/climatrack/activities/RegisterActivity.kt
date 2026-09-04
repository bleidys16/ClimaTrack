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

        // Debug: Log the email to ensure it's correct
        android.util.Log.d("REGISTER_DEBUG", "Attempting register with email: $email")

        // 1. Register in Firebase Auth
        FirebaseHelper.auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                binding.btnRegister.visibility = android.view.View.VISIBLE
                binding.progressBar.visibility = android.view.View.GONE
                
                if (task.isSuccessful) {
                    val authResult = task.result
                    android.util.Log.d("REGISTER_DEBUG", "Auth successful for: ${authResult?.user?.uid}")
                    
                    val newUser = Usuario(
                        usuario = user,
                        password = pass,
                        nombre = fullName,
                        rol = "Cliente",
                        email = email,
                        telefono = phone
                    )

                    try {
                        // 2. Save in Local DB
                        val localId = usuarioRepository.register(newUser)
                        
                        // 3. Save in Firestore
                        usuarioRepository.syncUserToCloud(newUser.copy(id = localId.toInt()))

                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error local: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val e = task.exception
                    android.util.Log.e("REGISTER_ERROR", "Firebase error code: ${(e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode}", e)
                    
                    val errorMsg = when {
                        e is com.google.firebase.FirebaseNetworkException -> "Error de conexión persistente. ¿El Hotspot tiene datos activos?"
                        e is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "El correo ya está registrado."
                        e is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "La contraseña es muy corta."
                        else -> "Error: ${e?.message}"
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
    }
}
