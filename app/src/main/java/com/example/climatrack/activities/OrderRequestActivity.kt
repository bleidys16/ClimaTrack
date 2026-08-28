package com.example.climatrack.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.climatrack.databinding.ActivityOrderRequestBinding
import com.example.climatrack.models.Orden
import com.example.climatrack.repositories.EquipoRepository
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
    private lateinit var equipoRepository: EquipoRepository
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
        equipoRepository = EquipoRepository(this)
        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupModelSpinner()
        binding.btnGetGps.setOnClickListener { checkPermissions() }
        binding.btnSubmitRequest.setOnClickListener { submitRequest() }
    }

    private fun setupModelSpinner() {
        val baseModels = listOf("LG Dual Inverter", "Samsung 360 Cassette", "Midea MS-18K", "York YXC-48", "Otro (Ingresar manualmente)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, baseModels)
        binding.spinnerModel.setAdapter(adapter)

        binding.spinnerModel.setOnItemClickListener { _, _, position, _ ->
            if (baseModels[position].contains("Otro")) {
                binding.tilManualModel.visibility = View.VISIBLE
            } else {
                binding.tilManualModel.visibility = View.GONE
            }
        }
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
        val selectedModel = binding.spinnerModel.text.toString()
        val manualModel = binding.etManualModel.text.toString().trim()

        val finalModel = if (selectedModel.contains("Otro")) manualModel else selectedModel

        if (desc.isEmpty() || addr.isEmpty() || finalModel.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos, incluyendo el modelo", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
        val date = sdf.format(Date())
        val orderNum = "REQ-" + System.currentTimeMillis().toString().takeLast(6)

        val newOrder = Orden(
            numero = orderNum,
            fecha = date,
            clienteId = sessionManager.getUserId(),
            equipoId = 1, // En un flujo real esto debería buscar o crear un equipo con finalModel
            tecnicoId = null,
            tipoServicio = "CORRECTIVO",
            descripcion = "Modelo: $finalModel\n\n$desc",
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
