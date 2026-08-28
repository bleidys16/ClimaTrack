package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.databinding.ActivityRegisterBinding
import com.example.climatrack.models.Usuario
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.SessionManager

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val newUser = Usuario(
            usuario = user,
            password = pass,
            nombre = fullName,
            rol = "Cliente",
            email = email,
            telefono = phone
        )

        val result = usuarioRepository.register(newUser)

        if (result > 0) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            // Podríamos loguear automáticamente o ir al login
            finish()
        } else {
            Toast.makeText(this, "Error al registrar: El usuario ya existe", Toast.LENGTH_SHORT).show()
        }
    }
}
