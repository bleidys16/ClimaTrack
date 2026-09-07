package com.example.climatrack.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.example.climatrack.databinding.ActivityTrackingMapBinding
import com.example.climatrack.repositories.OrdenRepository
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat

class TrackingMapActivity : BaseActivity() {

    private lateinit var binding: ActivityTrackingMapBinding
    private lateinit var ordenRepository: OrdenRepository
    private var clientMarker: Marker? = null
    private var orderId: Int = -1
    private var myLocationOverlay: MyLocationNewOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "ClimaTrack-Nav-" + System.currentTimeMillis()

        binding = ActivityTrackingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.appBarLayout)

        ordenRepository = OrdenRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        setupMap()
        loadOrderInfo()
    }

    private fun setupMap() {
        val osmFrSource = XYTileSource(
            "OSMFR", 0, 20, 256, ".png",
            arrayOf("https://a.tile.openstreetmap.fr/osmfr/", "https://b.tile.openstreetmap.fr/osmfr/", "https://c.tile.openstreetmap.fr/osmfr/"),
            "© OpenStreetMap France"
        )
        binding.mapView.setTileSource(osmFrSource)
        binding.mapView.setMultiTouchControls(true)
        
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.mapView)
        myLocationOverlay?.enableMyLocation()
        myLocationOverlay?.enableFollowLocation()
        binding.mapView.overlays.add(myLocationOverlay)
    }

    private fun loadOrderInfo() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvClientName.text = it.clienteNombre
            binding.tvClientAddress.text = it.direccion ?: "Sin dirección exacta"
            addClientMarker(it.latitudCliente, it.longitudCliente)
            
            // AGREGAR BOTÓN DE NAVEGACIÓN PROFESIONAL
            binding.toolbar.menu.clear()
            val navMenu = binding.toolbar.menu.add(0, 100, 0, "IR CON GPS")
            navMenu.setIcon(android.R.drawable.ic_menu_directions)
            navMenu.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
            
            binding.toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == 100) {
                    openExternalNavigation(it.latitudCliente, it.longitudCliente)
                }
                true
            }
        }
    }

    private fun openExternalNavigation(lat: Double?, lon: Double?) {
        if (lat == null || lon == null || lat == 0.0) {
            Toast.makeText(this, "Ubicación del cliente no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("google.navigation:q=$lat,$lon")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            val fallbackUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
            startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    }

    private fun addClientMarker(lat: Double?, lon: Double?) {
        if (lat == null || lon == null || lat == 0.0 || lon == 0.0) {
            Toast.makeText(this, "El cliente no registró ubicación GPS", Toast.LENGTH_SHORT).show()
            return
        }

        val pos = GeoPoint(lat, lon)
        clientMarker?.let { binding.mapView.overlays.remove(it) }
        
        clientMarker = Marker(binding.mapView)
        clientMarker?.position = pos
        clientMarker?.title = "CLIENTE"
        clientMarker?.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_myplaces)
        binding.mapView.overlays.add(clientMarker)
        
        binding.mapView.controller.setZoom(16.0)
        binding.mapView.controller.setCenter(pos)
        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        myLocationOverlay?.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        myLocationOverlay?.disableMyLocation()
    }
}
