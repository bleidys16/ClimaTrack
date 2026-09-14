package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.load
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityClientOrderDetailBinding
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.FirebaseHelper
import com.example.climatrack.utils.PdfGenerator
import com.google.firebase.firestore.ListenerRegistration
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.*

class ClientOrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityClientOrderDetailBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private var orderId: String = ""
    private var techMarker: Marker? = null
    private var firestoreListener: ListenerRegistration? = null
    private lateinit var servicioRepository: com.example.climatrack.repositories.ServicioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "ClimaTrackApp/1.0 (climatrack_client_contact@example.com) Android"
        
        val basePath = java.io.File(cacheDir.absolutePath, "osmdroid")
        Configuration.getInstance().osmdroidBasePath = basePath
        Configuration.getInstance().osmdroidTileCache = java.io.File(basePath, "tiles")

        binding = ActivityClientOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        ordenRepository = OrdenRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        servicioRepository = com.example.climatrack.repositories.ServicioRepository(this)
        orderId = intent.getStringExtra("ORDER_ID") ?: ""

        if (orderId.isEmpty()) {
            finish()
            return
        }

        setupMap()
        setupToolbar()
        loadOrderDetails()
        
        binding.btnSubmitFeedback.setOnClickListener { submitFeedback() }
        binding.btnDownloadReceipt.setOnClickListener { generateAndOpenReceipt() }

        binding.btnApproveNow.setOnClickListener {
            val intent = Intent(this, ApprovalActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
        }
        
        binding.btnChat.setOnClickListener {
            val info = ordenRepository.getAllInfoByTecnico("-1").find { it.id == orderId }
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            intent.putExtra("ORDER_NUM", info?.numero)
            startActivity(intent)
        }
    }

    private fun setupMap() {
        val esriTileSource = XYTileSource(
            "EsriWorldStreet",
            0, 18, 256, ".png",
            arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"),
            "© Esri, USGS, NOAA"
        )
        binding.mapViewTracking.setTileSource(esriTileSource)
        binding.mapViewTracking.setMultiTouchControls(true)
    }

    private fun startLiveTracking(orderNum: String) {
        if (orderNum.isEmpty()) return
        
        binding.tvTrackingTitle.visibility = View.VISIBLE
        binding.cardTrackingMap.visibility = View.VISIBLE

        try {
            firestoreListener = FirebaseHelper.db.collection("ordenes")
                .document(orderNum)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        android.util.Log.w("TRACKING_WARN", "Live tracking blocked or failed: ${e.message}")
                        return@addSnapshotListener
                    }
                    
                    if ((snapshot == null) || !snapshot.exists()) return@addSnapshotListener

                    val tecnicoLat = snapshot.getDouble("tecnicoLat")
                    val tecnicoLon = snapshot.getDouble("tecnicoLon")
                    
                    if (tecnicoLat != null && tecnicoLon != null && tecnicoLat != 0.0) {
                        updateMarker(tecnicoLat, tecnicoLon)
                    }
                }
        } catch (ignored: Exception) {
            android.util.Log.e("TRACKING_ERROR", "Error setting up firestore listener", ignored)
        }
    }

    private fun updateMarker(lat: Double, lon: Double) {
        val pos = GeoPoint(lat, lon)
        if (techMarker == null) {
            techMarker = Marker(binding.mapViewTracking)
            techMarker?.title = "Tu Técnico"
            binding.mapViewTracking.overlays.add(techMarker)
            binding.mapViewTracking.controller.setZoom(15.0)
            binding.mapViewTracking.controller.setCenter(pos)
        } else {
            techMarker?.position = pos
        }
        binding.mapViewTracking.invalidate()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderDetails() {
        val info = ordenRepository.getAllInfoByTecnico("-1").find { it.id == orderId }
        val mant = mantenimientoRepository.getByOrdenId(orderId)

        info?.let {
            binding.tvOrderNum.text = getString(R.string.order_num_label, it.numero)
            binding.tvStatus.text = it.estado
            binding.tvServiceType.text = "Servicio: ${it.tipoServicio}"
            binding.tvDate.text = "Fecha: ${it.fecha}"
            binding.tvTechnician.text = "Técnico: ${it.tecnicoNombre ?: "Por asignar"}"
            binding.tvEquipment.text = "Aire: ${it.equipoMarca ?: ""} ${it.equipoModelo ?: ""}"
            binding.tvTotalCost.text = "Costo Total: $${String.format(Locale.getDefault(), "%.2f", it.precioServicio)}"

            // Status color logic
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

            if (it.estado == "FINALIZADA") {
                binding.btnDownloadReceipt.visibility = View.VISIBLE
            }

            if (it.estado == "PENDIENTE APROBACIÓN") {
                binding.btnApproveNow.visibility = View.VISIBLE
            }

            if (it.estado == "EN PROCESO") {
                startLiveTracking(it.id)
            }

            setupFeedbackUI(it)
            loadEvidences()
        }

        mant?.let { m ->
            binding.tvWorkTitle.visibility = View.VISIBLE
            binding.cardWorkDetails.visibility = View.VISIBLE
            binding.tvDiagnosis.text = "Diagnóstico: ${m.diagnostico}"
            binding.tvWorkDone.text = "Trabajo Realizado: ${m.trabajoRealizado}"

            // Load Spare Parts
            val parts = servicioRepository.getRepuestosByMantenimiento(m.id)
            if (parts.isNotEmpty()) {
                binding.tvPartsTitle.visibility = View.VISIBLE
                binding.cardSpareParts.visibility = View.VISIBLE
                binding.llPartsContainer.removeAllViews()
                
                parts.forEach { part ->
                    val partBinding = com.example.climatrack.databinding.ItemSparePartClientBinding.inflate(layoutInflater, binding.llPartsContainer, false)
                    partBinding.tvPartName.text = part.repuestoNombre
                    partBinding.tvPartQty.text = "x${part.cantidad}"
                    partBinding.tvPartPrice.text = "$${String.format(Locale.getDefault(), "%.2f", part.precio * part.cantidad)}"
                    binding.llPartsContainer.addView(partBinding.root)
                }
            }
        }
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

    private fun setupFeedbackUI(order: com.example.climatrack.models.OrdenInfo) {
        if (order.estado == "FINALIZADA") {
            binding.tvFeedbackTitle.visibility = View.VISIBLE
            binding.cardFeedback.visibility = View.VISIBLE
            
            if (order.calificacion > 0) {
                binding.ratingBar.rating = order.calificacion.toFloat()
                binding.ratingBar.setIsIndicator(true)
                binding.tilComment.visibility = View.GONE
                binding.btnSubmitFeedback.visibility = View.GONE
                
                if (!order.comentario.isNullOrEmpty()) {
                    binding.tvSavedComment.visibility = View.VISIBLE
                    binding.tvSavedComment.text = "Tu comentario: ${order.comentario}"
                }
            } else {
                binding.ratingBar.setIsIndicator(false)
                binding.tilComment.visibility = View.VISIBLE
                binding.btnSubmitFeedback.visibility = View.VISIBLE
                binding.tvSavedComment.visibility = View.GONE
                binding.btnSubmitFeedback.text = "CALIFICAR SERVICIO"
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

        val result = ordenRepository.updateFeedback(orderId, rating, comment.ifEmpty { null })
        if (result > 0) {
            Toast.makeText(this, "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show()
            loadOrderDetails()
        } else {
            Toast.makeText(this, "Error al guardar calificación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateAndOpenReceipt() {
        val info = ordenRepository.getAllInfoByTecnico("-1").find { it.id == orderId }
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

    override fun onResume() {
        super.onResume()
        binding.mapViewTracking.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapViewTracking.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
    }
}
