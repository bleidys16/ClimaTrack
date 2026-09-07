package com.example.climatrack.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.load
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityOrderDetailBinding
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.PdfGenerator
import com.google.android.gms.location.LocationServices
import com.example.climatrack.services.LocationTrackingService
import com.example.climatrack.utils.SessionManager

class OrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var servicioRepository: com.example.climatrack.repositories.ServicioRepository
    private lateinit var sessionManager: SessionManager
    private var orderId: Int = -1
    private val handler = Handler(Looper.getMainLooper())
    private var isTracking = false

    private val trackingRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                updateTechnicianLocation()
                handler.postDelayed(this, 10000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        ordenRepository = OrdenRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        servicioRepository = com.example.climatrack.repositories.ServicioRepository(this)
        sessionManager = SessionManager(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        loadOrderData()
        setupToolbar()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderData() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderNum.text = "Orden: ${it.numero}"
            binding.tvStatus.text = it.estado
            binding.tvClientInfo.text = "Cliente: ${it.clienteNombre}"
            binding.tvEquipInfo.text = "Equipo: ${it.equipoNombre}"
            binding.tvDate.text = "Fecha: ${it.fecha}"
            binding.tvProblemInfo.text = "Problema: ${it.descripcion}"
            binding.tvServiceType.text = "Servicio: ${it.tipoServicio}"

            val (containerColor, textColor) = when (it.estado) {
                "PENDIENTE" -> R.color.status_pending_container to R.color.status_pending
                "EN DIAGNÓSTICO" -> R.color.status_in_progress_container to R.color.status_in_progress
                "PENDIENTE APROBACIÓN" -> R.color.status_in_progress_container to R.color.status_in_progress
                "APROBADA" -> R.color.status_finished_container to R.color.status_finished
                "EN PROCESO" -> R.color.status_in_progress_container to R.color.status_in_progress
                "FINALIZADA" -> R.color.status_finished_container to R.color.status_finished
                "CANCELADA" -> R.color.status_error_container to R.color.status_error
                else -> R.color.status_pending_container to R.color.status_pending
            }
            binding.tvStatus.backgroundTintList = ContextCompat.getColorStateList(this, containerColor)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, textColor))
            
            updateUIVisibility(it.estado)
            loadEvidences()
        }
    }

    private fun updateUIVisibility(estado: String) {
        binding.btnSendQuote.visibility = View.GONE
        binding.btnStartService.visibility = View.GONE
        binding.btnDownloadPdf.visibility = View.GONE
        binding.btnRegisterMaint.isEnabled = false
        binding.btnSpareParts.isEnabled = false
        binding.btnEvidence.isEnabled = false
        binding.btnUbicacion.isEnabled = false
        binding.btnFinishOrder.isEnabled = false
        binding.llFinishContainer.alpha = 0.5f

        when (estado) {
            "PENDIENTE" -> {
                binding.btnSendQuote.visibility = View.VISIBLE
                binding.btnSendQuote.text = getString(R.string.start_diagnostic)
            }
            "EN DIAGNÓSTICO" -> {
                binding.btnSendQuote.visibility = View.VISIBLE
                binding.btnSendQuote.text = getString(R.string.send_quote)
                binding.btnRegisterMaint.isEnabled = true
                binding.btnSpareParts.isEnabled = true
                binding.btnEvidence.isEnabled = true
                binding.btnUbicacion.isEnabled = true
            }
            "APROBADA" -> {
                binding.btnStartService.visibility = View.VISIBLE
            }
            "EN PROCESO" -> {
                binding.btnRegisterMaint.isEnabled = true
                binding.btnSpareParts.isEnabled = true
                binding.btnEvidence.isEnabled = true
                binding.btnUbicacion.isEnabled = true
                binding.btnFinishOrder.isEnabled = true
                binding.llFinishContainer.alpha = 1.0f
                binding.tvFinishLabel.text = "Finalizar"
                startTracking()
                startLocationService()
            }
            "FINALIZADA" -> {
                binding.tvFinishLabel.text = "ORDEN FINALIZADA"
                binding.btnDownloadPdf.visibility = View.VISIBLE
                stopTracking()
                stopLocationService()
            }
        }
    }

    private fun startLocationService() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        val serviceIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_UPDATE_ORDER
            putExtra(LocationTrackingService.EXTRA_ORDER_NUM, info?.numero)
        }
        startService(serviceIntent)
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_ORDER
        }
        startService(serviceIntent)
    }

    private fun loadEvidences() {
        val evidences = servicioRepository.getEvidenciasByOrden(orderId)
        if (evidences.isNotEmpty()) {
            binding.tvEvidencesTitle.visibility = View.VISIBLE
            binding.cardEvidences.visibility = View.VISIBLE
            binding.llEvidencesContainer.removeAllViews()
            
            evidences.forEach { evidence ->
                val imageView = ImageView(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        300, 300
                    ).apply {
                        setMargins(8, 8, 8, 8)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    load(evidence.rutaFoto) {
                        crossfade(true)
                        placeholder(R.drawable.ic_nav_equipment)
                    }
                }
                binding.llEvidencesContainer.addView(imageView)
            }
        } else {
            binding.tvEvidencesTitle.visibility = View.GONE
            binding.cardEvidences.visibility = View.GONE
        }
    }

    private fun startTracking() {
        if (!isTracking) {
            isTracking = true
            handler.post(trackingRunnable)
        }
    }

    private fun stopTracking() {
        isTracking = false
        handler.removeCallbacks(trackingRunnable)
    }

    private fun updateTechnicianLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        fusedClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                ordenRepository.updateTechnicianGps(orderId, it.latitude, it.longitude)
            }
        }
    }

    private fun setupButtons() {
        binding.btnSendQuote.setOnClickListener {
            val currentStatus = binding.tvStatus.text.toString()
            if (currentStatus == "PENDIENTE") {
                startDiagnostic()
            } else {
                validateAndShowQuoteDialog()
            }
        }
        binding.btnStartService.setOnClickListener { startService() }
        binding.btnDownloadPdf.setOnClickListener { generateAndOpenPdf() }

        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
            intent.putExtra("ORDER_NUM", info?.numero)
            startActivity(intent)
        }

        binding.btnRegisterMaint.setOnClickListener {
            Intent(this, MaintenanceActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnSpareParts.setOnClickListener {
            Intent(this, SparePartsActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnEvidence.setOnClickListener {
            Intent(this, EvidenceActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnUbicacion.setOnClickListener {
            Intent(this, TrackingMapActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnApproval.setOnClickListener {
            Intent(this, ApprovalActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }

        binding.btnFinishOrder.setOnClickListener { confirmFinish() }
    }

    private fun startDiagnostic() {
        ordenRepository.updateEstado(orderId, "EN DIAGNÓSTICO")
        Toast.makeText(this, "Diagnóstico iniciado en sitio", Toast.LENGTH_SHORT).show()
        loadOrderData()
    }

    private fun validateAndShowQuoteDialog() {
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        val evidences = servicioRepository.getEvidenciasByOrden(orderId)

        if (mant == null || evidences.isEmpty()) {
            Toast.makeText(this, getString(R.string.diagnostic_required), Toast.LENGTH_LONG).show()
            return
        }

        showQuoteDialog()
    }

    private fun showQuoteDialog() {
        val input = EditText(this)
        input.hint = "Ingresar costo del servicio"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(60, 20, 60, 0)
        input.layoutParams = lp
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Enviar Cotización")
            .setMessage("Ingrese el valor total para que el cliente lo apruebe.")
            .setView(container)
            .setPositiveButton("Enviar") { _, _ ->
                val price = input.text.toString().toDoubleOrNull() ?: 0.0
                if (price > 0) {
                    ordenRepository.updatePrecio(orderId, price)
                    Toast.makeText(this, "Cotización enviada al cliente", Toast.LENGTH_SHORT).show()
                    loadOrderData()
                } else {
                    Toast.makeText(this, "Ingrese un precio válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun startService() {
        ordenRepository.updateEstado(orderId, "EN PROCESO")
        Toast.makeText(this, "Servicio iniciado", Toast.LENGTH_SHORT).show()
        loadOrderData()
    }

    private fun confirmFinish() {
        AlertDialog.Builder(this)
            .setTitle("Finalizar Orden")
            .setMessage("¿Confirma que desea finalizar esta orden de trabajo?")
            .setPositiveButton("Finalizar") { _, _ ->
                ordenRepository.updateEstado(orderId, "FINALIZADA")
                Toast.makeText(this, "Orden finalizada correctamente", Toast.LENGTH_SHORT).show()
                loadOrderData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun generateAndOpenPdf() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        
        info?.let {
            val pdfFile = PdfGenerator(this).generateTechnicalReport(it, mant)
            if (pdfFile != null) {
                openPdf(pdfFile)
            } else {
                Toast.makeText(this, "Error al generar PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openPdf(file: java.io.File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "No hay lector de PDF instalado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }
}
