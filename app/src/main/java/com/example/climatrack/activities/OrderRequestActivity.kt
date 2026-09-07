package com.example.climatrack.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
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
    private lateinit var usuarioRepository: com.example.climatrack.repositories.UsuarioRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var lat: Double? = null
    private var lon: Double? = null
    private var selectedEquipmentId: Int = 1 // Default dummy

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
        setupEdgeToEdge(binding.root, binding.appBarLayout)

        ordenRepository = OrdenRepository(this)
        equipoRepository = EquipoRepository(this)
        usuarioRepository = com.example.climatrack.repositories.UsuarioRepository(this)
        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupToolbar()
        setupModelSpinner()
        binding.btnGetGps.setOnClickListener { checkPermissions() }
        binding.btnSubmitRequest.setOnClickListener { submitRequest() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupModelSpinner() {
        val myEquip = equipoRepository.getByCliente(sessionManager.getUserId())
        val baseModels = mutableListOf<String>()
        
        myEquip.forEach { 
            val name = it.nombre ?: "${it.marca} ${it.modelo}"
            baseModels.add("$name (Mío)") 
        }
        baseModels.addAll(listOf("LG Dual Inverter", "Samsung 360 Cassette", "Midea MS-18K", "York YXC-48", "Otro (Ingresar manualmente)"))
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, baseModels)
        binding.spinnerModel.setAdapter(adapter)

        binding.spinnerModel.setOnItemClickListener { _, _, position, _ ->
            if (position < myEquip.size) {
                selectedEquipmentId = myEquip[position].id
                binding.tilManualModel.visibility = View.GONE
            } else {
                selectedEquipmentId = 1 // Or handle better
                val text = baseModels[position]
                if (text.contains("Otro")) {
                    binding.tilManualModel.visibility = View.VISIBLE
                } else {
                    binding.tilManualModel.visibility = View.GONE
                }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses.isNotEmpty()) {
                        runOnUiThread {
                            val address = addresses[0].getAddressLine(0)
                            binding.etExactAddress.setText(address)
                            Toast.makeText(this@OrderRequestActivity, "Dirección autocompletada", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } else {
            try {
                @Suppress("DEPRECATION")
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
    }

    private fun submitRequest() {
        val desc = binding.etDescription.text.toString().trim()
        val addr = binding.etExactAddress.text.toString().trim()
        val selectedModelText = binding.spinnerModel.text.toString()
        val manualModel = binding.etManualModel.text.toString().trim()

        val finalModel = if (selectedModelText.contains("Otro")) manualModel else selectedModelText

        if (desc.isEmpty() || addr.isEmpty() || finalModel.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos, incluyendo el modelo", Toast.LENGTH_SHORT).show()
            return
        }

        // If the equipment is NOT one of user's own (based on selectedEquipmentId), create a placeholder equipment
        val myEquip = equipoRepository.getByCliente(sessionManager.getUserId())
        val isExisting = myEquip.any { it.id == selectedEquipmentId }
        
        var equipmentIdToUse = selectedEquipmentId
        
        if (!isExisting || selectedModelText.contains("Otro")) {
            // Create a new equipment record for this user
            val newEquip = com.example.climatrack.models.Equipo(
                codigo = "TEMP-" + System.currentTimeMillis().toString().takeLast(4),
                tipo = "Aire Acondicionado",
                marca = if (finalModel.contains(" ")) finalModel.split(" ")[0] else "Genérica",
                modelo = finalModel,
                clienteId = sessionManager.getUserId(),
                estado = "OPERATIVO",
                ubicacion = addr
            )
            equipmentIdToUse = equipoRepository.create(newEquip).toInt()
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
        val date = sdf.format(Date())
        val orderNum = "REQ-" + System.currentTimeMillis().toString().takeLast(6)

        val newOrder = Orden(
            numero = orderNum,
            fecha = date,
            clienteId = sessionManager.getUserId(),
            equipoId = equipmentIdToUse,
            tecnicoId = null,
            tipoServicio = "CORRECTIVO",
            descripcion = desc,
            estado = "SIN ASIGNAR",
            direccionExacta = addr,
            latitudCliente = lat,
            longitudCliente = lon,
            clienteEmail = usuarioRepository.getById(sessionManager.getUserId())?.email
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
