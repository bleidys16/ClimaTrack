package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.R
import com.example.climatrack.adapters.OrdersAdapter
import com.example.climatrack.databinding.ActivityOrdersBinding
import com.example.climatrack.models.OrdenInfo
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.SessionManager
import com.google.android.material.tabs.TabLayout

class OrdersActivity : BaseActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: OrdersAdapter
    private var allOrders: List<OrdenInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ordenRepository = OrdenRepository(this)
        sessionManager = SessionManager(this)

        setupEdgeToEdge(binding.root, binding.toolbar, binding.navContainer)
        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupBottomNavigation()
        loadOrders()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupTabs() {
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterOrders(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterOrders(position: Int) {
        val status = when (position) {
            0 -> "PENDIENTE"
            1 -> "EN PROCESO"
            2 -> "FINALIZADA"
            else -> "PENDIENTE"
        }
        val filtered = allOrders.filter { it.estado == status }
        updateUI(filtered)
    }

    private fun setupBottomNavigation() {
        setupCustomNavigation(binding.customNav.root, R.id.menu_orders) { menuId ->
            when (menuId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                R.id.menu_equipment -> {
                    startActivity(Intent(this, EquipmentActivity::class.java))
                    finish()
                }
                R.id.menu_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = OrdersAdapter(emptyList()) { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }

    private fun loadOrders() {
        val tecnicoId = sessionManager.getUserId()
        allOrders = ordenRepository.getAllInfoByTecnico(tecnicoId)
        filterOrders(binding.tabs.selectedTabPosition)
    }

    private fun updateUI(orders: List<OrdenInfo>) {
        if (orders.isEmpty()) {
            binding.llEmpty.visibility = View.VISIBLE
            binding.rvOrders.visibility = View.GONE
        } else {
            binding.llEmpty.visibility = View.GONE
            binding.rvOrders.visibility = View.VISIBLE
            adapter.updateList(orders)
        }
    }
}
