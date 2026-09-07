package com.example.climatrack.activities

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.climatrack.databinding.ActivityTechnicianMapBinding
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.FirebaseHelper
import com.google.firebase.firestore.ListenerRegistration
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

// checkout: tecnico, ubicacion, usuarios, cnico, nombre, activo, prueba, actualizando, rapido, gratuito, estilo, limpio, filtro, moderno, desde
class TechnicianMapActivity : BaseActivity() {

    private lateinit var binding: ActivityTechnicianMapBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private var firestoreListener: ListenerRegistration? = null
    private val techMarkers = mutableMapOf<Int, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val ctx = applicationContext
        // Uso de getSharedPreferences para evitar la deprecación de PreferenceManager
        val prefs = ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
        Configuration.getInstance().load(ctx, prefs)
        Configuration.getInstance().userAgentValue = "ClimaTrack-Service-Admin-" + System.currentTimeMillis()
        
        val basePath = java.io.File(cacheDir.absolutePath, "osmdroid")
        if (basePath.exists()) basePath.deleteRecursively()
        
        Configuration.getInstance().osmdroidBasePath = basePath
        Configuration.getInstance().osmdroidTileCache = java.io.File(basePath, "tiles")

        binding = ActivityTechnicianMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.appBarLayout)

        usuarioRepository = UsuarioRepository(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupMap()

        binding.fabRefresh.setOnClickListener {
            Toast.makeText(this, "Actualizando técnicos...", Toast.LENGTH_SHORT).show()
            loadActiveTechnicians()
        }
    }

    private fun setupMap() {
        val osmFrSource = XYTileSource(
            "OSMFR", 0, 20, 256, ".png",
            arrayOf("https://a.tile.openstreetmap.fr/osmfr/", "https://b.tile.openstreetmap.fr/osmfr/", "https://c.tile.openstreetmap.fr/osmfr/"),
            "© OpenStreetMap France"
        )
        
        binding.mapView.setTileSource(osmFrSource)
        binding.mapView.setMultiTouchControls(true)
        
        val matrix = ColorMatrix(floatArrayOf(
            -0.8f, 0f, 0f, 0f, 255f,
            0f, -0.8f, 0f, 0f, 255f,
            0f, 0f, -0.8f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        binding.mapView.overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(matrix))

        val mapController = binding.mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(10.9639, -74.7964) 
        mapController.setCenter(startPoint)
        
        loadActiveTechnicians()
    }

    private fun loadActiveTechnicians() {
        firestoreListener?.remove()
        
        firestoreListener = FirebaseHelper.db.collection("usuarios")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("MAP_DEBUG", "Error en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                snapshots?.let { docs ->
                    var activeFound = false
                    var firstPos: GeoPoint? = null
                    
                    for (doc in docs) {
                        val rol = doc.getString("rol") ?: ""
                        if (!rol.contains("cnico", ignoreCase = true)) continue

                        val id = doc.getLong("id")?.toInt() ?: continue
                        val isActive = doc.getLong("isActive")?.toInt() ?: 0
                        val nombre = doc.getString("nombre") ?: "Técnico $id"
                        val lat = doc.getDouble("lastLat")
                        val lon = doc.getDouble("lastLon")

                        if (isActive == 1 && lat != null && lon != null && lat != 0.0) {
                            activeFound = true
                            val pos = GeoPoint(lat, lon)
                            if (firstPos == null) firstPos = pos
                            updateTechMarker(id, pos, nombre, doc.getString("workStartTime") ?: "--")
                        } else {
                            removeTechMarker(id)
                        }
                    }
                    
                    if (!activeFound) startDemoSimulation()

                    if (firstPos != null) {
                        binding.mapView.controller.animateTo(firstPos, 15.0, 1000L)
                    }
                    binding.mapView.invalidate()
                }
            }
    }

    private fun updateTechMarker(id: Int, pos: GeoPoint, nombre: String, info: String) {
        var marker = techMarkers[id]
        if (marker == null) {
            marker = Marker(binding.mapView)
            marker.title = nombre
            marker.subDescription = "Activo desde: $info"
            marker.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_directions)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            binding.mapView.overlays.add(marker)
            techMarkers[id] = marker
        }
        marker.position = pos
    }

    private fun removeTechMarker(id: Int) {
        techMarkers[id]?.let {
            binding.mapView.overlays.remove(it)
            techMarkers.remove(id)
        }
    }

    private fun startDemoSimulation() {
        val dest = GeoPoint(10.9639, -74.7964)
        val start = GeoPoint(10.9500, -74.8100)
        
        val demoMarker = Marker(binding.mapView)
        demoMarker.title = "Técnico de Prueba"
        demoMarker.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_directions)
        demoMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        binding.mapView.overlays.add(demoMarker)
        binding.mapView.controller.setCenter(start)

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 20000 
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener { animation ->
            val v = animation.animatedValue as Float
            val lat = start.latitude + (dest.latitude - start.latitude) * v
            val lon = start.longitude + (dest.longitude - start.longitude) * v
            demoMarker.position = GeoPoint(lat, lon)
            binding.mapView.invalidate()
        }
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
    }
}
