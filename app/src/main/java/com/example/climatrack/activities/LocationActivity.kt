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
import com.example.climatrack.repositories.OrdenRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var orderId: Int = -1
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0
    private var currentAddress: String? = null
    private var clientLat: Double? = null
    private var clientLon: Double? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            checkGpsAndGetLocation()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        servicioRepository = ServicioRepository(this)
        ordenRepository = OrdenRepository(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            Toast.makeText(this, "Error: No se recibió el ID de la orden", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        setupToolbar()
        loadOrderInfo()
        loadExistingLocation()

        binding.btnGetLocation.setOnClickListener { checkPermissions() }
        binding.btnSaveLocation.setOnClickListener { saveLocation() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        updateMapWithClientLocation()
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderInfo() {
        val orden = ordenRepository.getById(orderId)
        orden?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            clientLat = it.latitudCliente
            clientLon = it.longitudCliente
            
            val info = ordenRepository.getAllInfoByTecnico(-1).find { item -> item.id == orderId }
            binding.tvClientDisplay.text = "Cliente: ${info?.clienteNombre ?: "Cargando..."}"
            
            updateMapWithClientLocation()
        }
    }

    private fun updateMapWithClientLocation() {
        val lat = clientLat ?: return
        val lon = clientLon ?: return
        val pos = LatLng(lat, lon)
        googleMap?.addMarker(MarkerOptions().position(pos).title("Ubicación del Cliente"))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
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
            
            binding.cardLocationStatus.visibility = View.VISIBLE
            binding.tvStatusLabel.text = "Ubicación registrada anteriormente"
            
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
            .setMessage("Debe activar el GPS del dispositivo.")
            .setPositiveButton("Activar") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    binding.tvLat.text = "$currentLat"
                    binding.tvLon.text = "$currentLon"
                    binding.btnSaveLocation.isEnabled = true
                    getAddress(currentLat, currentLon)
                    
                    val pos = LatLng(currentLat, currentLon)
                    googleMap?.addMarker(MarkerOptions().position(pos).title("Mi Ubicación"))
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLng(pos))
                }
            }
    }

    private fun getAddress(lat: Double, lon: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                currentAddress = addresses[0].getAddressLine(0)
                binding.tvAddress.text = currentAddress
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveLocation() {
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
            Toast.makeText(this, "Ubicación registrada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
