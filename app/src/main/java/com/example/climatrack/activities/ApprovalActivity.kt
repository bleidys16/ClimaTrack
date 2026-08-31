package com.example.climatrack.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.databinding.ActivityApprovalBinding
import com.example.climatrack.models.Aprobacion
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.ServicioRepository
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
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            if (it.estado == "PENDIENTE APROBACIÓN") {
                binding.tvSummaryTitle.text = "Detalles de la Cotización"
                binding.tvSummary.text = "• Problema: ${it.descripcion}\n• Servicio solicitado: ${it.tipoServicio}"
                binding.tvPriceDisplay.text = "Costo del Servicio: $${String.format(Locale.getDefault(), "%.2f", it.precioServicio)}"
                binding.btnSaveApproval.text = "CONFIRMAR Y APROBAR COTIZACIÓN"
            } else {
                binding.tvSummaryTitle.text = "Resumen del Servicio Realizado"
                val mant = mantenimientoRepository.getByOrdenId(orderId)
                if (mant != null) {
                    binding.tvSummary.text = "• Trabajo: ${mant.trabajoRealizado}\n• Diagnóstico: ${mant.diagnostico}"
                    binding.tvPriceDisplay.text = "Costo Final: $${String.format(Locale.getDefault(), "%.2f", it.precioServicio)}"
                }
                binding.btnSaveApproval.text = "GUARDAR CONFORMIDAD FINAL"
            }
        }
    }

    private fun saveApproval() {
        val clientName = binding.etClientName.text.toString().trim()
        val accepted = if (binding.swAccept.isChecked) 1 else 0

        if (clientName.isEmpty() || accepted == 0) {
            Toast.makeText(this, "Debe ingresar el nombre y aceptar los términos", Toast.LENGTH_SHORT).show()
            return
        }

        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        val nextStatus = if (info?.estado == "PENDIENTE APROBACIÓN") "APROBADA" else "FINALIZADA"

        // Save signature as Base64
        val signatureBitmap = binding.signatureView.getSignatureBitmap()
        val signatureBase64 = encodeBitmapToBase64(signatureBitmap)

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val aprobacion = Aprobacion(
            ordenId = orderId,
            cliente = clientName,
            aceptado = accepted,
            fecha = date
        )

        val result = servicioRepository.addAprobacion(aprobacion)
        if (result > 0) {
            ordenRepository.saveFirma(orderId, signatureBase64)
            ordenRepository.updateEstado(orderId, nextStatus)
            Toast.makeText(this, "Confirmación enviada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al registrar aprobación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }
}
