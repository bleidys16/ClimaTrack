package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityLoginBinding
import com.example.climatrack.repositories.UsuarioRepository
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

        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getUserRol() ?: "")
            return
        }

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
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
        
        usuarioRepository.login(identifier, pass) { user ->
            runOnUiThread {
                binding.btnLogin.isEnabled = true
                if (user != null) {
                    sessionManager.saveSession(user.id, user.nombre, user.rol)
                    navigateToDashboard(user.rol)
                } else {
                    Toast.makeText(this, "Credenciales incorrectas o usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToDashboard(rol: String) {
        val intent = when (rol.uppercase()) {
            "ADMINISTRADOR" -> Intent(this, AdminDashboardActivity::class.java)
            "CLIENTE" -> Intent(this, ClientDashboardActivity::class.java)
            else -> Intent(this, DashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
