package com.example.climatrack.activities

import android.os.Bundle
import com.example.climatrack.databinding.ActivityTechnicianMapBinding
import com.example.climatrack.repositories.UsuarioRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class TechnicianMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTechnicianMapBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTechnicianMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        usuarioRepository = UsuarioRepository(this)
        
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.fabRefresh.setOnClickListener {
            loadActiveTechnicians()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        loadActiveTechnicians()
    }

    private fun loadActiveTechnicians() {
        googleMap?.clear()
        val techs = usuarioRepository.getActiveTechnicians()
        
        if (techs.isEmpty()) return

        var firstPos: LatLng? = null
        for (tech in techs) {
            val lat = tech.lastLat
            val lon = tech.lastLon
            if (lat != null && lon != null) {
                val pos = LatLng(lat, lon)
                if (firstPos == null) firstPos = pos
                
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(tech.nombre)
                        .snippet("Inició: ${tech.workStartTime ?: "--"} | Finaliza: ${tech.workEndTime ?: "--"}")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
            }
        }

        firstPos?.let {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
        }
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
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
