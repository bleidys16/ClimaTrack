package com.example.climatrack.activities

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityDashboardBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.SessionManager
import com.google.android.gms.location.LocationServices
import java.util.*

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        ordenRepository = OrdenRepository(this)
        usuarioRepository = UsuarioRepository(this)

        setupEdgeToEdge(binding.root, binding.toolbar, binding.navContainer)
        setupUI()
        setupStatusLogic()
        setupBottomNavigation()
    }

    private fun setupStatusLogic() {
        val userId = sessionManager.getUserId()
        val user = usuarioRepository.getById(userId)
        
        user?.let {
            binding.swActiveStatus.isChecked = it.isActive == 1
            binding.etWorkEndTime.setText(it.workEndTime ?: "")
        }

        val calendar = Calendar.getInstance()

        binding.etWorkEndTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                binding.etWorkEndTime.setText(time)
                updateTechnicianStatus()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        binding.swActiveStatus.setOnCheckedChangeListener { _, _ ->
            updateTechnicianStatus()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateTechnicianStatus() {
        val userId = sessionManager.getUserId()
        val isActive = if (binding.swActiveStatus.isChecked) 1 else 0
        val workEnd = binding.etWorkEndTime.text.toString()

        if (isActive == 1) {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
            fusedLocation.lastLocation.addOnSuccessListener { location ->
                usuarioRepository.updateStatus(userId, isActive, workEnd, location?.latitude, location?.longitude)
                if (location != null) Toast.makeText(this, "Estado actualizado con ubicación", Toast.LENGTH_SHORT).show()
            }
        } else {
            usuarioRepository.updateStatus(userId, isActive, workEnd, null, null)
            Toast.makeText(this, "Ahora estás desconectado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun setupUI() {
        binding.tvWelcome.text = "Hola Técnico"

        binding.btnOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        binding.btnEquipment.setOnClickListener {
            startActivity(Intent(this, EquipmentActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
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
