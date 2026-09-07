package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityLoginBinding
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.FirebaseHelper
import com.example.climatrack.utils.SessionManager

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        usuarioRepository = UsuarioRepository(this)
        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val identifier = binding.etUser.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (identifier.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false
        
        // 1. Intentar Login Local PRIMERO para velocidad y soporte offline
        val usuarioLocal = usuarioRepository.login(identifier, pass)
        if (usuarioLocal != null) {
            binding.btnLogin.isEnabled = true
            sessionManager.saveSession(usuarioLocal.id, usuarioLocal.nombre, usuarioLocal.rol)
            navigateToDashboard(usuarioLocal.rol)
            return
        }

        // 2. Si no está local, intentar Firebase
        Toast.makeText(this, "Buscando en la nube...", Toast.LENGTH_SHORT).show()
        if (identifier.contains("@")) {
            loginWithFirebase(identifier, pass)
        } else {
            FirebaseHelper.db.collection("usuarios")
                .whereEqualTo("usuario", identifier)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val email = documents.documents[0].getString("email")
                        if (email != null) loginWithFirebase(email, pass)
                        else {
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(this, "Usuario sin correo registrado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loginWithFirebase(email: String, pass: String) {
        FirebaseHelper.auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchCloudUserData(email, pass)
                } else {
                    val e = task.exception
                    android.util.Log.w("LOGIN_WARN", "Firebase Auth failed: ${e?.message}")
                    
                    if (e is com.google.firebase.FirebaseNetworkException) {
                        Toast.makeText(this, "Red restringida. Accediendo modo local.", Toast.LENGTH_SHORT).show()
                    }
                    
                    // Fallback a local usando el identificador original
                    fallbackToLocalLogin(binding.etUser.text.toString().trim(), pass)
                }
            }
    }

    private fun fetchCloudUserData(email: String, pass: String) {
        FirebaseHelper.db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                binding.btnLogin.isEnabled = true
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val id = doc.getLong("id")?.toInt() ?: -1
                    val nombre = doc.getString("nombre") ?: ""
                    val rol = doc.getString("rol") ?: ""
                    
                    val dbUser = com.example.climatrack.models.Usuario(
                        id = id,
                        usuario = doc.getString("usuario") ?: "",
                        password = pass,
                        nombre = nombre,
                        rol = rol,
                        email = doc.getString("email"),
                        telefono = doc.getString("telefono"),
                        isActive = doc.getLong("isActive")?.toInt() ?: 0,
                        workStartTime = doc.getString("workStartTime"),
                        workEndTime = doc.getString("workEndTime"),
                        lastLat = doc.getDouble("lastLat"),
                        lastLon = doc.getDouble("lastLon"),
                        imagenPerfil = doc.getString("imagenPerfil"),
                        fcmToken = doc.getString("fcmToken"),
                    )
                    usuarioRepository.register(dbUser)
                    sessionManager.saveSession(id, nombre, rol)
                    navigateToDashboard(rol)
                } else {
                    fallbackToLocalLogin(email, pass)
                }
            }
            .addOnFailureListener {
                fallbackToLocalLogin(email, pass)
            }
    }

    private fun fallbackToLocalLogin(email: String, pass: String) {
        binding.btnLogin.isEnabled = true
        val usuario = usuarioRepository.login(email, pass)
        if (usuario != null) {
            sessionManager.saveSession(usuario.id, usuario.nombre, usuario.rol)
            navigateToDashboard(usuario.rol)
        } else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDashboard(rol: String) {
        val intent = when (rol.uppercase()) {
            "ADMINISTRADOR" -> Intent(this, AdminDashboardActivity::class.java)
            "CLIENTE" -> Intent(this, ClientDashboardActivity::class.java)
            else -> Intent(this, DashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
