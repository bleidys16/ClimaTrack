package com.example.climatrack.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.climatrack.databinding.ActivityLocationBinding
import com.example.climatrack.models.Ubicacion
import com.example.climatrack.repositories.ServicioRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationActivity : BaseActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var orderId: Int = -1
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0
    private var currentAddress: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            checkGpsAndGetLocation()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado. Es necesario para registrar el servicio.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        servicioRepository = ServicioRepository(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            Toast.makeText(this, "Error: No se recibió el ID de la orden", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        loadOrderInfo()
        loadExistingLocation()

        binding.btnGetLocation.setOnClickListener { checkPermissions() }
        binding.btnSaveLocation.setOnClickListener { saveLocation() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderInfo() {
        val ordenRepository = com.example.climatrack.repositories.OrdenRepository(this)
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            binding.tvClientDisplay.text = "Cliente: ${it.clienteNombre}"
        }
    }

    private fun loadExistingLocation() {
        val ubicacion = servicioRepository.getUbicacionByOrden(orderId)
        ubicacion?.let {
            currentLat = it.latitud
            currentLon = it.longitud
            currentAddress = it.direccion
            
            binding.tvLat.text = "${it.latitud}"
            binding.tvLon.text = "${it.longitud}"
            binding.tvAddress.text = it.direccion ?: "Dirección no disponible"
            
            // Parse date
            try {
                val fullDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.fecha)
                fullDate?.let { date ->
                    binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    binding.tvTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                binding.tvDate.text = it.fecha
            }

            binding.cardLocationStatus.visibility = View.VISIBLE
            binding.tvStatusLabel.text = "Ubicación registrada anteriormente"
            binding.ivMarker.visibility = View.VISIBLE
            
            binding.btnSaveLocation.isEnabled = false
            binding.btnSaveLocation.text = "UBICACIÓN YA REGISTRADA"
        }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                checkGpsAndGetLocation()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    private fun checkGpsAndGetLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog()
        } else {
            getLastLocation()
        }
    }

    private fun showGpsDisabledDialog() {
        AlertDialog.Builder(this)
            .setTitle("GPS Desactivado")
            .setMessage("Para obtener la ubicación exacta, debe activar el GPS del dispositivo.")
            .setPositiveButton("Activar") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        binding.tvStatusLabel.text = "Obteniendo ubicación..."
        binding.cardLocationStatus.visibility = View.VISIBLE
        binding.btnGetLocation.isEnabled = false
        binding.ivMarker.visibility = View.INVISIBLE

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                binding.btnGetLocation.isEnabled = true
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    
                    binding.tvLat.text = "$currentLat"
                    binding.tvLon.text = "$currentLon"
                    
                    val now = Date()
                    binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                    binding.tvTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
                    
                    binding.tvStatusLabel.text = "Ubicación obtenida correctamente"
                    binding.ivMarker.visibility = View.VISIBLE
                    binding.btnSaveLocation.isEnabled = true
                    binding.btnSaveLocation.text = "GUARDAR COORDENADAS"
                    
                    getAddress(currentLat, currentLon)
                } else {
                    binding.tvStatusLabel.text = "No se pudo obtener la ubicación. Intente de nuevo."
                    Toast.makeText(this, "Error al obtener ubicación. Asegúrese de estar en un lugar abierto.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                binding.btnGetLocation.isEnabled = true
                binding.tvStatusLabel.text = "Error: ${e.message}"
                Toast.makeText(this, "Fallo en el sensor de ubicación", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getAddress(lat: Double, lon: Double) {
        binding.tvAddress.text = "Obteniendo dirección..."
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                currentAddress = address
                binding.tvAddress.text = address
            } else {
                currentAddress = "Sin dirección disponible"
                binding.tvAddress.text = currentAddress
            }
        } catch (e: IOException) {
            currentAddress = "Error al obtener dirección"
            binding.tvAddress.text = currentAddress
        }
    }

    private fun saveLocation() {
        if (currentLat == 0.0 && currentLon == 0.0) {
            Toast.makeText(this, "Error: Coordenadas inválidas", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val ubicacion = Ubicacion(
            ordenId = orderId,
            latitud = currentLat,
            longitud = currentLon,
            direccion = currentAddress,
            fecha = date
        )
        
        val result = servicioRepository.addUbicacion(ubicacion)
        if (result > 0) {
            Toast.makeText(this, "Ubicación registrada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show()
        }
    }
}
