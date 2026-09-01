package com.example.climatrack.activities

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.climatrack.databinding.ActivityProfileBinding
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.SessionManager
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var sessionManager: SessionManager
    private var currentUserId: Int = -1
    private var selectedImageUri: Uri? = null
    private var profilePhotoPath: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfilePicture.setImageURI(it)
            saveImageLocally(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        usuarioRepository = UsuarioRepository(this)
        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserId()

        binding.toolbar.setNavigationOnClickListener { finish() }

        loadProfileData()

        binding.btnChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnSaveProfile.setOnClickListener { saveProfile() }
    }

    private fun loadProfileData() {
        val user = usuarioRepository.getById(currentUserId)
        user?.let {
            binding.tvProfileName.text = it.nombre
            binding.tvProfileRole.text = it.rol
            binding.etFullName.setText(it.nombre)
            binding.etEmail.setText(it.email ?: "")
            binding.etPhone.setText(it.telefono ?: "")
            
            it.imagenPerfil?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    profilePhotoPath = path
                    binding.ivProfilePicture.setImageURI(Uri.fromFile(file))
                }
            }
        }
    }

    private fun saveImageLocally(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(getExternalFilesDir(null), "profile_$currentUserId.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            profilePhotoPath = file.absolutePath
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveProfile() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val result = usuarioRepository.updateProfile(currentUserId, name, email, phone, profilePhotoPath)
        if (result > 0) {
            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
        }
    }
}
