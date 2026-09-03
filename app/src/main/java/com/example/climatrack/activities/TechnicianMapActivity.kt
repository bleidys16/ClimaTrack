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

import android.widget.Toast
import com.example.climatrack.R

class TechnicianMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTechnicianMapBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTechnicianMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.appBarLayout)

        usuarioRepository = UsuarioRepository(this)
        
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.fabRefresh.setOnClickListener {
            Toast.makeText(this, "Buscando técnicos activos...", Toast.LENGTH_SHORT).show()
            loadActiveTechnicians()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        loadActiveTechnicians()
    }

    private fun loadActiveTechnicians() {
        usuarioRepository.fetchTechniciansFromCloud {
            runOnUiThread {
                googleMap?.clear()
                val techs = usuarioRepository.getActiveTechnicians()
                
                if (techs.isEmpty()) {
                    Toast.makeText(this, "No hay técnicos activos en este momento", Toast.LENGTH_LONG).show()
                    val defaultPos = LatLng(4.6243, -74.0636)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 6f))
                    return@runOnUiThread
                }

                var firstPos: LatLng? = null
                var count = 0
                for (tech in techs) {
                    val lat = tech.lastLat
                    val lon = tech.lastLon
                    if (lat != null && lon != null) {
                        val pos = LatLng(lat, lon)
                        if (firstPos == null) firstPos = pos
                        count++
                        
                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title(tech.nombre)
                                .snippet("Inició: ${tech.workStartTime ?: "--"} | Finaliza: ${tech.workEndTime ?: "--"}")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    }
                }

                if (firstPos != null) {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPos, 12f))
                    Toast.makeText(this, "Se encontraron $count técnicos activos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Técnicos activos sin ubicación registrada", Toast.LENGTH_SHORT).show()
                }
            }
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
