package com.example.climatrack.activities

import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.databinding.ActivityRegisterTechnicianBinding
import com.example.climatrack.models.Usuario
import com.example.climatrack.repositories.UsuarioRepository

class RegisterTechnicianActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterTechnicianBinding
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterTechnicianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        usuarioRepository = UsuarioRepository(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnRegisterTech.setOnClickListener {
            performRegister()
        }
    }

    private fun performRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val user = binding.etUser.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (fullName.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val newTech = Usuario(
            usuario = user,
            password = pass,
            nombre = fullName,
            rol = "Técnico",
            email = email
        )

        val result = usuarioRepository.register(newTech)

        if (result > 0) {
            Toast.makeText(this, "Técnico registrado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error: El usuario ya existe", Toast.LENGTH_SHORT).show()
        }
    }
}
