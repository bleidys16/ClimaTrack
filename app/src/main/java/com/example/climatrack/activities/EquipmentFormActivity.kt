package com.example.climatrack.activities

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityEquipmentFormBinding
import com.example.climatrack.models.Equipo
import com.example.climatrack.repositories.EquipoRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EquipmentFormActivity : BaseActivity() {

    private lateinit var binding: ActivityEquipmentFormBinding
    private lateinit var equipoRepository: EquipoRepository
    private lateinit var usuarioRepository: com.example.climatrack.repositories.UsuarioRepository
    private var equipmentId: String = ""
    private var clientId: String = ""
    private var clientsList: List<com.example.climatrack.models.Usuario> = emptyList()
    private var photoUri: Uri? = null
    private var photoFile: File? = null
    private var currentPhotoPath: String? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoPath = photoFile?.absolutePath
            binding.ivEquipmentPreview.setImageURI(photoUri)
            binding.ivEquipmentPreview.clearColorFilter()
            binding.ivEquipmentPreview.alpha = 1.0f
        } else {
            Toast.makeText(this, "Captura de foto cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        equipoRepository = EquipoRepository(this)
        usuarioRepository = com.example.climatrack.repositories.UsuarioRepository(this)
        equipmentId = intent.getStringExtra("EQUIPMENT_ID") ?: ""
        clientId = intent.getStringExtra("CLIENT_ID") ?: ""

        if (clientId.isEmpty()) {
            val session = com.example.climatrack.utils.SessionManager(this)
            if (session.getUserRol() == "Cliente") {
                clientId = session.getUserId()
            }
        }

        setupToolbar()
        setupSpinners()
        
        val session = com.example.climatrack.utils.SessionManager(this)
        val userRol = session.getUserRol()?.uppercase() ?: ""
        
        if (userRol == "ADMINISTRADOR" || userRol == "TÉCNICO" || userRol == "TECNICO") {
            binding.llOwnerSelection.visibility = View.VISIBLE
            loadClients()
        }
        
        if (equipmentId.isNotEmpty()) {
            loadEquipmentData()
            binding.toolbar.title = "Editar Equipo"
            binding.btnDelete.visibility = View.VISIBLE
        }

        binding.btnSave.setOnClickListener { saveEquipment() }
        binding.btnDelete.setOnClickListener { confirmDelete() }
        binding.btnCapturePhoto.setOnClickListener { prepareAndTakePhoto() }
    }

    private fun prepareAndTakePhoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        photoFile = File.createTempFile("EQUIP_${timeStamp}_", ".jpg", storageDir)
        
        photoUri = FileProvider.getUriForFile(
            this,
            "com.example.climatrack.fileprovider",
            photoFile!!
        )
        
        photoUri?.let {
            takePictureLauncher.launch(it)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSpinners() {
        val adapterStatus = ArrayAdapter.createFromResource(
            this, R.array.equipment_status_array, android.R.layout.simple_spinner_item
        )
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnStatus.adapter = adapterStatus
    }

    private fun loadClients() {
        clientsList = usuarioRepository.getAllClientes()
        val clientNames = clientsList.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnClient.adapter = adapter

        if (clientId.isNotEmpty()) {
            val pos = clientsList.indexOfFirst { it.id == clientId }
            if (pos >= 0) binding.spnClient.setSelection(pos)
        }
    }

    private fun loadEquipmentData() {
        val equip = equipoRepository.getById(equipmentId)
        equip?.let {
            binding.etCode.setText(it.codigo)
            binding.etEquipName.setText(it.nombre)
            binding.etType.setText(it.tipo)
            binding.etBrand.setText(it.marca)
            binding.etModel.setText(it.modelo)
            binding.etSerial.setText(it.serial)
            binding.etCapacity.setText(it.capacidad)
            binding.etLocation.setText(it.ubicacion)
            
            it.imagenPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    currentPhotoPath = path
                    binding.ivEquipmentPreview.setImageURI(Uri.fromFile(file))
                    binding.ivEquipmentPreview.clearColorFilter()
                    binding.ivEquipmentPreview.alpha = 1.0f
                }
            }

            val statusArray = resources.getStringArray(R.array.equipment_status_array)
            val pos = statusArray.indexOf(it.estado)
            if (pos >= 0) binding.spnStatus.setSelection(pos)

            if (clientsList.isNotEmpty()) {
                val clientPos = clientsList.indexOfFirst { c -> c.id == it.clienteId }
                if (clientPos >= 0) binding.spnClient.setSelection(clientPos)
            }
        }
    }

    private fun saveEquipment() {
        val code = binding.etCode.text.toString().trim()
        val customName = binding.etEquipName.text.toString().trim()
        val type = binding.etType.text.toString().trim()
        val brand = binding.etBrand.text.toString().trim()
        val model = binding.etModel.text.toString().trim()
        val serial = binding.etSerial.text.toString().trim()
        val capacity = binding.etCapacity.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val status = binding.spnStatus.selectedItem.toString()
        
        var finalClientId = clientId
        if (binding.llOwnerSelection.visibility == View.VISIBLE && clientsList.isNotEmpty()) {
            val selectedPos = binding.spnClient.selectedItemPosition
            if (selectedPos >= 0) {
                finalClientId = clientsList[selectedPos].id
            }
        }

        if (code.isEmpty() || type.isEmpty() || brand.isEmpty() || model.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val equipo = Equipo(
            id = equipmentId,
            codigo = code,
            nombre = if (customName.isEmpty()) null else customName,
            tipo = type,
            marca = brand,
            modelo = model,
            serial = serial,
            capacidad = capacity,
            ubicacion = location,
            clienteId = if (finalClientId.isNotEmpty()) finalClientId else "user_cli_001",
            estado = status,
            imagenPath = currentPhotoPath
        )

        val result = if (equipmentId.isEmpty()) {
            equipoRepository.create(equipo).let { if (it.isNotEmpty()) 1L else 0L }
        } else {
            equipoRepository.update(equipo).toLong()
        }

        if (result > 0) {
            Toast.makeText(this, "Equipo guardado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al guardar equipo (Verifique el código)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Equipo")
            .setMessage("¿Está seguro de eliminar este equipo?")
            .setPositiveButton("Eliminar") { _, _ ->
                equipoRepository.delete(equipmentId)
                Toast.makeText(this, "Equipo eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
