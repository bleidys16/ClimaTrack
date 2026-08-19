package com.example.climatrack.activities

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.climatrack.databinding.ActivityEvidenceBinding
import com.example.climatrack.models.Evidencia
import com.example.climatrack.repositories.ServicioRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvidenceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEvidenceBinding
    private lateinit var servicioRepository: ServicioRepository
    private var orderId: Int = -1
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.imgPreview.setImageURI(photoUri)
            saveEvidenceToDb()
        } else {
            Toast.makeText(this, "Error al capturar fotografía", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvidenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        servicioRepository = ServicioRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        binding.btnTakePhoto.setOnClickListener {
            prepareAndTakePhoto()
        }
    }

    private fun prepareAndTakePhoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        photoFile = File.createTempFile("CT_${timeStamp}_", ".jpg", storageDir)
        
        photoUri = FileProvider.getUriForFile(
            this,
            "com.example.climatrack.fileprovider",
            photoFile!!
        )
        
        photoUri?.let {
            takePictureLauncher.launch(it)
        }
    }

    private fun saveEvidenceToDb() {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val evidencia = Evidencia(
            ordenId = orderId,
            rutaFoto = photoFile?.absolutePath ?: "",
            fecha = date
        )
        
        val result = servicioRepository.addEvidencia(evidencia)
        if (result > 0) {
            Toast.makeText(this, "Evidencia guardada correctamente", Toast.LENGTH_SHORT).show()
        }
    }
}
