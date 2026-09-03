package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val email = binding.etUser.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Try Firebase Auth
        FirebaseHelper.auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // 2. Fetch User Data from Firestore
                FirebaseHelper.db.collection("usuarios")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val doc = documents.documents[0]
                            val id = doc.getLong("id")?.toInt() ?: -1
                            val nombre = doc.getString("nombre") ?: ""
                            val rol = doc.getString("rol") ?: ""
                            
                            // Save to local DB to ensure consistency
                            val dbUser = com.example.climatrack.models.Usuario(
                                id = id,
                                usuario = doc.getString("usuario") ?: "",
                                password = pass, // Not ideal but for local consistency
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
                                fcmToken = doc.getString("fcmToken")
                            )
                            usuarioRepository.register(dbUser) // This handles upsert if we modify repository or if it's new

                            // 3. Save Session
                            sessionManager.saveSession(id, nombre, rol)
                            
                            navigateToDashboard(rol)
                        } else {
                            Toast.makeText(this, "Usuario no encontrado en la nube", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .addOnFailureListener {
                // 4. Fallback to Local Login (for existing local-only users)
                val usuario = usuarioRepository.login(email, pass)
                if (usuario != null) {
                    sessionManager.saveSession(usuario.id, usuario.nombre, usuario.rol)
                    navigateToDashboard(usuario.rol)
                } else {
                    Toast.makeText(this, "Error de acceso", Toast.LENGTH_SHORT).show()
                }
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
