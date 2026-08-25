package com.example.climatrack.activities

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.climatrack.adapters.EvidenceAdapter
import com.example.climatrack.databinding.ActivityEvidenceBinding
import com.example.climatrack.models.Evidencia
import com.example.climatrack.repositories.ServicioRepository
import java.io.File
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
            Toast.makeText(this, "Error al capturar fotografía", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvidenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        servicioRepository = ServicioRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadOrderInfo()
        loadEvidences()

        binding.btnTakePhoto.setOnClickListener {
            prepareAndTakePhoto()
        }
        binding.ivAddEvidence.setOnClickListener {
            prepareAndTakePhoto()
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
            loadEvidences()
        }
    }
}
