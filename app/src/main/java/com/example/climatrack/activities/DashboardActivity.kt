package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityDashboardBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.SessionManager

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var ordenRepository: OrdenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        ordenRepository = OrdenRepository(this)

        setupEdgeToEdge(binding.root, binding.toolbar, binding.navContainer)
        setupUI()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun setupUI() {
        binding.tvWelcome.text = "Hola, ${sessionManager.getUserName()}"

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
