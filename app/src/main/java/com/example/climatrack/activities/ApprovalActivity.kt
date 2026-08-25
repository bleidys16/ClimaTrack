package com.example.climatrack.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import com.example.climatrack.databinding.ActivityApprovalBinding
import com.example.climatrack.models.Aprobacion
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.ServicioRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ApprovalActivity : BaseActivity() {

    private lateinit var binding: ActivityApprovalBinding
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var sessionManager: com.example.climatrack.utils.SessionManager
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        servicioRepository = ServicioRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        ordenRepository = OrdenRepository(this)
        sessionManager = com.example.climatrack.utils.SessionManager(this)
        
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        loadOrderInfo()
        loadSummary()
        
        binding.btnClearSignature.setOnClickListener { binding.signatureView.clear() }
        binding.btnSaveApproval.setOnClickListener { saveApproval() }
        binding.ivToolbarSave.setOnClickListener { saveApproval() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderInfo() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            binding.tvClientDisplay.text = "Cliente: ${it.clienteNombre}"
            binding.tvEquipDisplay.text = "Equipo: ${it.equipoNombre}"
            binding.tvStatusDisplay.text = it.estado
        }
    }

    private fun loadSummary() {
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        
        if (mant != null && info != null) {
            val summary = "• Tipo de servicio: ${info.tipoServicio}\n" +
                    "• Fecha del mantenimiento: ${mant.fecha}\n" +
                    "• Técnico: ${sessionManager.getUserName()}\n" +
                    "• Trabajo realizado: ${mant.trabajoRealizado}"
            binding.tvSummary.text = summary
        }
    }

    private fun saveApproval() {
        val clientName = binding.etClientName.text.toString().trim()
        val accepted = if (binding.swAccept.isChecked) 1 else 0

        if (clientName.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del cliente", Toast.LENGTH_SHORT).show()
            return
        }

        // Save signature as file
        val signatureBitmap = binding.signatureView.getSignatureBitmap()
        val signaturePath = saveSignatureToFile(signatureBitmap)

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val aprobacion = Aprobacion(
            ordenId = orderId,
            cliente = clientName,
            aceptado = accepted,
            fecha = date
            // Note: We could add signaturePath to Aprobacion model if needed
        )

        val result = servicioRepository.addAprobacion(aprobacion)
        if (result > 0) {
            Toast.makeText(this, "Aprobación registrada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al registrar aprobación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSignatureToFile(bitmap: Bitmap): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SIG_${orderId}_${timeStamp}.png"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, fileName)
        
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file.absolutePath
    }
}
