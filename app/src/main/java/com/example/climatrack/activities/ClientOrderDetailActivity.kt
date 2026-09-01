package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityClientOrderDetailBinding
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.PdfGenerator
import java.util.*

class ClientOrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityClientOrderDetailBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        ordenRepository = OrdenRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        loadOrderDetails()
        binding.btnSubmitFeedback.setOnClickListener { submitFeedback() }
        binding.btnDownloadReceipt.setOnClickListener { generateAndOpenReceipt() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderDetails() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        val mant = mantenimientoRepository.getByOrdenId(orderId)

        info?.let {
            binding.tvOrderNum.text = "Orden: ${it.numero}"
            binding.tvStatus.text = it.estado
            binding.tvServiceType.text = "Servicio: ${it.tipoServicio}"
            binding.tvDate.text = "Fecha: ${it.fecha}"
            binding.tvTechnician.text = "Técnico: ${it.tecnicoNombre ?: "Por asignar"}"
            binding.tvEquipment.text = "Aire: ${it.equipoMarca ?: ""} ${it.equipoModelo ?: ""}"
            binding.tvTotalCost.text = "Costo Total: $${String.format(Locale.getDefault(), "%.2f", it.precioServicio)}"

            // Status color logic
            val (containerColor, textColor) = when (it.estado) {
                "PENDIENTE" -> R.color.status_pending_container to R.color.status_pending
                "PENDIENTE APROBACIÓN" -> R.color.status_in_progress_container to R.color.status_in_progress
                "APROBADA" -> R.color.status_finished_container to R.color.status_finished
                "EN PROCESO" -> R.color.status_in_progress_container to R.color.status_in_progress
                "FINALIZADA" -> R.color.status_finished_container to R.color.status_finished
                "CANCELADA" -> R.color.status_error_container to R.color.status_error
                else -> R.color.status_pending_container to R.color.status_pending
            }
            binding.tvStatus.backgroundTintList = ContextCompat.getColorStateList(this, containerColor)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, textColor))

            if (it.estado == "FINALIZADA") {
                binding.btnDownloadReceipt.visibility = View.VISIBLE
            }

            setupFeedbackUI(it)
        }

        mant?.let {
            binding.tvWorkTitle.visibility = View.VISIBLE
            binding.cardWorkDetails.visibility = View.VISIBLE
            binding.tvDiagnosis.text = "Diagnóstico: ${it.diagnostico}"
            binding.tvWorkDone.text = "Trabajo Realizado: ${it.trabajoRealizado}"
        }
    }

    private fun setupFeedbackUI(order: com.example.climatrack.models.OrdenInfo) {
        if (order.estado == "FINALIZADA") {
            binding.tvFeedbackTitle.visibility = View.VISIBLE
            binding.cardFeedback.visibility = View.VISIBLE
            
            if (order.calificacion > 0) {
                // Already rated
                binding.ratingBar.rating = order.calificacion.toFloat()
                binding.ratingBar.setIsIndicator(true)
                binding.tilComment.visibility = View.GONE
                binding.btnSubmitFeedback.visibility = View.GONE
                
                if (!order.comentario.isNullOrEmpty()) {
                    binding.tvSavedComment.visibility = View.VISIBLE
                    binding.tvSavedComment.text = "Tu comentario: ${order.comentario}"
                }
            } else {
                // Not rated yet
                binding.ratingBar.setIsIndicator(false)
                binding.tilComment.visibility = View.VISIBLE
                binding.btnSubmitFeedback.visibility = View.VISIBLE
                binding.tvSavedComment.visibility = View.GONE
            }
        } else {
            binding.tvFeedbackTitle.visibility = View.GONE
            binding.cardFeedback.visibility = View.GONE
        }
    }

    private fun submitFeedback() {
        val rating = binding.ratingBar.rating.toInt()
        val comment = binding.etComment.text.toString().trim()

        if (rating == 0) {
            Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
            return
        }

        val result = ordenRepository.updateFeedback(orderId, rating, if (comment.isEmpty()) null else comment)
        if (result > 0) {
            Toast.makeText(this, "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show()
            loadOrderDetails()
        } else {
            Toast.makeText(this, "Error al guardar calificación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateAndOpenReceipt() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        
        info?.let {
            val pdfFile = PdfGenerator(this).generateClientReport(it, mant)
            if (pdfFile != null) {
                openPdf(pdfFile)
            } else {
                Toast.makeText(this, "Error al generar certificado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openPdf(file: java.io.File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No hay lector de PDF instalado", Toast.LENGTH_SHORT).show()
        }
    }
}
