package com.example.climatrack.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.climatrack.databinding.ActivityOrderRequestBinding
import com.example.climatrack.models.Orden
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class OrderRequestActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderRequestBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var lat: Double? = null
    private var lon: Double? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getLastLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        ordenRepository = OrdenRepository(this)
        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnGetGps.setOnClickListener { checkPermissions() }
        binding.btnSubmitRequest.setOnClickListener { submitRequest() }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        binding.tvGpsStatus.text = "Obteniendo ubicación..."
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    binding.tvGpsStatus.text = "GPS Capturado: $lat, $lon"
                    
                    // Autocompletar dirección
                    getAddress(location.latitude, location.longitude)
                } else {
                    binding.tvGpsStatus.text = "No se pudo obtener el GPS"
                }
            }
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                binding.etExactAddress.setText(address)
                Toast.makeText(this, "Dirección autocompletada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun submitRequest() {
        val desc = binding.etDescription.text.toString().trim()
        val addr = binding.etExactAddress.text.toString().trim()

        if (desc.isEmpty() || addr.isEmpty()) {
            Toast.makeText(this, "Complete la descripción y la dirección", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val orderNum = "REQ-" + System.currentTimeMillis().toString().takeLast(6)

        val newOrder = Orden(
            numero = orderNum,
            fecha = date,
            clienteId = sessionManager.getUserId(), // Asumimos que el Usuario ID es el Cliente ID o están vinculados
            equipoId = 1, // Por ahora dummy, debería elegir equipo o ser 0
            tecnicoId = null,
            tipoServicio = "CORRECTIVO",
            descripcion = desc,
            estado = "SIN ASIGNAR",
            direccionExacta = addr,
            latitudCliente = lat,
            longitudCliente = lon
        )

        val result = ordenRepository.create(newOrder)
        if (result > 0) {
            Toast.makeText(this, "Solicitud enviada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
        }
    }
}
