package com.example.climatrack.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityDashboardBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var usuarioRepository: UsuarioRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            updateTechnicianStatus(binding.swActiveStatus.isChecked)
        } else {
            Toast.makeText(this, "Permiso de ubicación necesario para activar jornada", Toast.LENGTH_LONG).show()
            binding.swActiveStatus.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        ordenRepository = OrdenRepository(this)
        usuarioRepository = UsuarioRepository(this)

        val appBar = binding.root.findViewById<android.view.View>(R.id.toolbar)?.parent as? android.view.View
        setupEdgeToEdge(binding.root, appBar ?: binding.toolbar, binding.navContainer)
        setupUI()
        setupStatusLogic()
        setupBottomNavigation()
    }

    private fun setupStatusLogic() {
        val userId = sessionManager.getUserId()
        val user = usuarioRepository.getById(userId)
        
        user?.let {
            val isActive = it.isActive == 1
            // Usamos un listener nulo temporalmente para evitar disparar el guardado al inicializar
            binding.swActiveStatus.setOnCheckedChangeListener(null)
            binding.swActiveStatus.isChecked = isActive
            binding.swActiveStatus.text = if (isActive) "Activo" else "Inactivo"
            binding.swActiveStatus.setTextColor(ContextCompat.getColor(this, 
                if (isActive) R.color.status_finished else R.color.status_canceled))
            
            binding.etWorkStartTime.setText(it.workStartTime ?: "")
            binding.etWorkEndTime.setText(it.workEndTime ?: "")
            
            if (isActive) {
                binding.layoutLocationInfo.visibility = View.VISIBLE
                it.lastLat?.let { lat ->
                    it.lastLon?.let { lon ->
                        binding.tvCurrentLocation.text = "Ubicación: $lat, $lon"
                    }
                }
            }

            // Reasignamos el listener real
            attachStatusListener()
        }
    }

    private fun attachStatusListener() {
        binding.swActiveStatus.setOnCheckedChangeListener { _, isChecked ->
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
            val currentTime = sdf.format(Date())
            
            binding.swActiveStatus.text = if (isChecked) "Activo" else "Inactivo"
            binding.swActiveStatus.setTextColor(ContextCompat.getColor(this, 
                if (isChecked) R.color.status_finished else R.color.status_canceled))
            
            if (isChecked) {
                binding.etWorkStartTime.setText(currentTime)
            } else {
                binding.etWorkEndTime.setText(currentTime)
            }
            updateTechnicianStatus(isChecked)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateTechnicianStatus(isChecked: Boolean) {
        val userId = sessionManager.getUserId()
        val isActive = if (isChecked) 1 else 0
        val workStart = binding.etWorkStartTime.text.toString()
        val workEnd = binding.etWorkEndTime.text.toString()

        if (isChecked) {
            if (checkLocationPermissions()) {
                binding.layoutLocationInfo.visibility = View.VISIBLE
                fetchCurrentLocation(userId, isActive, workStart, workEnd)
            } else {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        } else {
            binding.layoutLocationInfo.visibility = View.GONE
            usuarioRepository.updateStatus(userId, isActive, workStart, workEnd, null, null)
            Toast.makeText(this, "Jornada finalizada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation(userId: Int, isActive: Int, workStart: String, workEnd: String) {
        val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        
        // Guardado inicial sin ubicación por si falla el GPS
        usuarioRepository.updateStatus(userId, isActive, workStart, workEnd, null, null)
        
        fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    usuarioRepository.updateStatus(userId, isActive, workStart, workEnd, location.latitude, location.longitude)
                    binding.tvCurrentLocation.text = "Ubicación: ${location.latitude}, ${location.longitude}"
                    Toast.makeText(this, "Ubicación actualizada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación exacta", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al capturar GPS", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
        loadProfileImage()
        updateFcmToken()
    }

    private fun updateFcmToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            usuarioRepository.updateFCMToken(sessionManager.getUserId(), token)
        }
    }

    private fun loadProfileImage() {
        val user = usuarioRepository.getById(sessionManager.getUserId())
        user?.imagenPerfil?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                binding.ivUserAvatar.setImageURI(android.net.Uri.fromFile(file))
            }
        }
    }

    private fun setupUI() {
        binding.tvWelcome.text = "Hola Técnico"

        binding.ivUserAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.cardPending.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.putExtra("TAB_INDEX", 0)
            startActivity(intent)
        }

        binding.cardInProgress.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.putExtra("TAB_INDEX", 1)
            startActivity(intent)
        }

        binding.cardFinished.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.putExtra("TAB_INDEX", 2)
            startActivity(intent)
        }

        binding.btnOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        binding.btnEquipment.setOnClickListener {
            startActivity(Intent(this, EquipmentActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnRefreshLocation.setOnClickListener {
            if (binding.swActiveStatus.isChecked) {
                val userId = sessionManager.getUserId()
                val workStart = binding.etWorkStartTime.text.toString()
                val workEnd = binding.etWorkEndTime.text.toString()
                fetchCurrentLocation(userId, 1, workStart, workEnd)
            }
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupBottomNavigation() {
        setupCustomNavigation(binding.customNav.root, R.id.menu_home) { menuId ->
            when (menuId) {
                R.id.menu_orders -> startActivity(Intent(this, OrdersActivity::class.java))
                R.id.menu_equipment -> startActivity(Intent(this, EquipmentActivity::class.java))
                R.id.menu_history -> startActivity(Intent(this, HistoryActivity::class.java))
            }
        }
    }

    private fun loadStats() {
        val stats = ordenRepository.getDashboardStats(sessionManager.getUserId())
        
        binding.tvPendingCount.text = (stats["PENDIENTE"] ?: 0).toString()
        binding.tvInProgressCount.text = (stats["EN PROCESO"] ?: 0).toString()
        binding.tvFinishedCount.text = (stats["FINALIZADA"] ?: 0).toString()
    }
}
