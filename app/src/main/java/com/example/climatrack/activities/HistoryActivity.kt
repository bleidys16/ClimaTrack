package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.R
import com.example.climatrack.adapters.HistoryAdapter
import com.example.climatrack.databinding.ActivityHistoryBinding
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.utils.SessionManager

class HistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mantenimientoRepository = MantenimientoRepository(this)
        sessionManager = SessionManager(this)

        setupEdgeToEdge(binding.root, binding.toolbar, binding.navContainer)
        setupToolbar()
        setupBottomNavigation()
        loadHistory()
    }

    private fun setupBottomNavigation() {
        setupCustomNavigation(binding.customNav.root, R.id.menu_history) { menuId ->
            when (menuId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                R.id.menu_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                    finish()
                }
                R.id.menu_equipment -> {
                    startActivity(Intent(this, EquipmentActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadHistory() {
        val tecnicoId = sessionManager.getUserId()
        val history = mantenimientoRepository.getHistorialInfo(tecnicoId)
        
        val adapter = HistoryAdapter(history)
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter
    }
}
