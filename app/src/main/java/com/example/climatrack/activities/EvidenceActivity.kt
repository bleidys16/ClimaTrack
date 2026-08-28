package com.example.climatrack.activities

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.climatrack.adapters.EvidenceAdapter
import com.example.climatrack.databinding.ActivityEvidenceBinding
import com.example.climatrack.models.Evidencia
import com.example.climatrack.repositories.ServicioRepository
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvidenceActivity : BaseActivity() {

    private lateinit var binding: ActivityEvidenceBinding
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var adapter: EvidenceAdapter
    private var orderId: Int = -1
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            saveEvidenceToDb()
        } else {
            Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show()
            // Limpiar archivo temporal si existe
            photoFile?.let { if (it.exists()) it.delete() }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            prepareAndTakePhoto()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado. No se puede tomar la foto.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("ORDER_ID", orderId)
        photoFile?.let { outState.putString("PHOTO_PATH", it.absolutePath) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvidenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        servicioRepository = ServicioRepository(this)
        
        orderId = savedInstanceState?.getInt("ORDER_ID", -1) ?: intent.getIntExtra("ORDER_ID", -1)
        val savedPath = savedInstanceState?.getString("PHOTO_PATH")
        if (savedPath != null) photoFile = File(savedPath)

        if (orderId == -1) {
            Toast.makeText(this, "Error: Orden no identificada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadOrderInfo()
        loadEvidences()

        binding.btnTakePhoto.setOnClickListener {
            checkPermissionsAndTake()
        }
        binding.ivAddEvidence.setOnClickListener {
            checkPermissionsAndTake()
        }
    }

    private fun checkPermissionsAndTake() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            prepareAndTakePhoto()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun prepareAndTakePhoto() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            
            if (storageDir == null) {
                Toast.makeText(this, "Error: No se pudo acceder al almacenamiento", Toast.LENGTH_SHORT).show()
                return
            }

            if (!storageDir.exists() && !storageDir.mkdirs()) {
                Toast.makeText(this, "Error: No se pudo crear el directorio de fotos", Toast.LENGTH_SHORT).show()
                return
            }

            photoFile = File.createTempFile("CT_${timeStamp}_", ".jpg", storageDir)
            
            photoUri = FileProvider.getUriForFile(
                this,
                "com.example.climatrack.fileprovider",
                photoFile!!
            )
            
            photoUri?.let {
                takePictureLauncher.launch(it)
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error al crear archivo de imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadOrderInfo() {
        val ordenRepository = com.example.climatrack.repositories.OrdenRepository(this)
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            binding.tvClientDisplay.text = "Cliente: ${it.clienteNombre}"
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = EvidenceAdapter(emptyList()) { evidence ->
            confirmDelete(evidence)
        }
        binding.rvEvidence.layoutManager = GridLayoutManager(this, 2)
        binding.rvEvidence.adapter = adapter
    }

    private fun loadEvidences() {
        val list = servicioRepository.getEvidenciasByOrden(orderId)
        adapter.updateList(list)
    }

    private fun confirmDelete(evidence: Evidencia) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Evidencia")
            .setMessage("¿Desea eliminar esta fotografía?")
            .setPositiveButton("Eliminar") { _, _ ->
                val result = servicioRepository.deleteEvidencia(evidence.id)
                if (result > 0) {
                    // Opcional: eliminar archivo físico
                    val file = File(evidence.rutaFoto)
                    if (file.exists()) file.delete()
                    
                    loadEvidences()
                    Toast.makeText(this, "Evidencia eliminada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
            loadEvidences()
        }
    }
}
