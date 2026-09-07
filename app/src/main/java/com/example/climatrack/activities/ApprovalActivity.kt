package com.example.climatrack.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.climatrack.databinding.ActivityApprovalBinding
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.ServicioRepository
import com.example.climatrack.utils.PdfGenerator
import com.example.climatrack.utils.SessionManager
import java.io.ByteArrayOutputStream
import java.util.*

class ApprovalActivity : BaseActivity() {

    private lateinit var binding: ActivityApprovalBinding
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var sessionManager: SessionManager
    private var orderId: Int = -1
    private var isAccepted: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        servicioRepository = ServicioRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        ordenRepository = OrdenRepository(this)
        sessionManager = SessionManager(this)
        
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        loadOrderInfo()
        loadSummary()

        binding.btnAccept.setOnClickListener {
            isAccepted = true
            binding.tvCancelMessage.visibility = View.GONE
            Toast.makeText(this, "Servicio Aceptado", Toast.LENGTH_SHORT).show()
        }

        binding.btnReject.setOnClickListener {
            isAccepted = false
            binding.tvCancelMessage.visibility = View.VISIBLE
            Toast.makeText(this, "Servicio Rechazado", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearSignature.setOnClickListener {
            binding.signatureView.clear()
        }

        binding.btnSaveApproval.setOnClickListener { saveApproval() }
        binding.ivToolbarSave.setOnClickListener { saveApproval() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
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
        val info = ordenRepository.getById(orderId)
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        val parts = mant?.let { servicioRepository.getRepuestosByMantenimiento(it.id) } ?: emptyList()

        val sb = StringBuilder()
        sb.append("TIPO SERVICIO: ${info?.tipoServicio}\n")
        sb.append("FECHA/HORA: ${mant?.fecha ?: "--"}\n")
        sb.append("DIAGNÓSTICO: ${mant?.diagnostico ?: "--"}\n")
        sb.append("TRABAJO: ${mant?.trabajoRealizado ?: "--"}\n\n")
        
        val priceMant = info?.precioMantenimiento ?: 0.0
        sb.append("MANTENIMIENTO: $${String.format(Locale.getDefault(), "%.2f", priceMant)}\n")
        
        if (parts.isNotEmpty()) {
            sb.append("\nREPUESTOS:\n")
            parts.forEach { 
                sb.append("- ${it.repuestoNombre} (x${it.cantidad}) : $${String.format(Locale.getDefault(), "%.2f", it.precio * it.cantidad)}\n")
            }
        }

        val totalParts = parts.sumOf { it.precio * it.cantidad }
        val grandTotal = priceMant + totalParts

        binding.tvSummary.text = sb.toString()
        binding.tvPriceDisplay.text = "TOTAL COTIZACIÓN: $${String.format(Locale.getDefault(), "%.2f", grandTotal)}"
    }

    private fun saveApproval() {
        if (isAccepted == null) {
            Toast.makeText(this, "Por favor seleccione si el cliente acepta o rechaza", Toast.LENGTH_SHORT).show()
            return
        }

        if (isAccepted == true) {
            val clientName = binding.etClientName.text.toString().trim()
            if (clientName.isEmpty()) {
                Toast.makeText(this, "Ingrese el nombre del cliente", Toast.LENGTH_SHORT).show()
                return
            }

            if (binding.signatureView.isEmpty()) {
                Toast.makeText(this, "Se requiere la firma del cliente", Toast.LENGTH_SHORT).show()
                return
            }

            val signatureBase64 = encodeBitmapToBase64(binding.signatureView.getSignatureBitmap())
            val obs = binding.etObservations.text.toString().trim()

            // Update order: Status to "EN PROCESO", save signature, observation
            val db = com.example.climatrack.database.DatabaseHelper(this).writableDatabase
            val values = android.content.ContentValues().apply {
                put(com.example.climatrack.database.DatabaseHelper.COL_ORDEN_ESTADO, "EN PROCESO")
                put(com.example.climatrack.database.DatabaseHelper.COL_ORDEN_FIRMA, signatureBase64)
                put(com.example.climatrack.database.DatabaseHelper.COL_ORDEN_OBS_CLI, obs)
            }
            db.update(com.example.climatrack.database.DatabaseHelper.TABLE_ORDENES, values, 
                "${com.example.climatrack.database.DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(orderId.toString()))

            // Generar Comprobante
            generateFinalReport()

            Toast.makeText(this, "Cotización aprobada. Trabajo en proceso.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            // RECHAZADO: Cancelar orden
            ordenRepository.updateEstado(orderId, "CANCELADA")
            Toast.makeText(this, "Orden cancelada por el cliente", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun generateFinalReport() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        info?.let {
            val pdfFile = PdfGenerator(this).generateTechnicalReport(it, mant)
            if (pdfFile != null) {
                // Simular envío de correo
                android.util.Log.d("APPROVAL", "Reporte generado en: ${pdfFile.absolutePath}")
            }
        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
    }
}
