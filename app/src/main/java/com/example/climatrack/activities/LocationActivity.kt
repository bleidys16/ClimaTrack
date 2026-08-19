package com.example.climatrack.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.climatrack.databinding.ActivityLocationBinding
import com.example.climatrack.models.Ubicacion
import com.example.climatrack.repositories.ServicioRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var orderId: Int = -1
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getLastLocation()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        servicioRepository = ServicioRepository(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        binding.btnGetLocation.setOnClickListener { checkPermissions() }
        binding.btnSaveLocation.setOnClickListener { saveLocation() }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
                binding.tvLat.text = "Latitud: $currentLat"
                binding.tvLon.text = "Longitud: $currentLon"
                binding.tvTime.text = "Capturado: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
                binding.btnSaveLocation.isEnabled = true
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación. Active el GPS.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveLocation() {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val ubicacion = Ubicacion(
            ordenId = orderId,
            latitud = currentLat,
            longitud = currentLon,
            fecha = date
        )
        
        val result = servicioRepository.addUbicacion(ubicacion)
        if (result > 0) {
            Toast.makeText(this, "Ubicación registrada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
