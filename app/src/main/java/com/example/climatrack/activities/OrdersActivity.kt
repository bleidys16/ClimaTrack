package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
        
        val userRol = sessionManager.getUserRol()?.uppercase() ?: ""
        if ((userRol == "TÉCNICO") || (userRol == "TECNICO")) {
            setupBottomNavigation()
        } else {
            binding.navContainer.visibility = View.GONE
        }
        
        val initialTab = intent.getIntExtra("TAB_INDEX", 0)
        binding.tabs.getTabAt(initialTab)?.select()
        
        setupRefreshLayout()
        loadOrders()
    }

    private fun setupRefreshLayout() {
        binding.swipeRefresh.setColorSchemeResources(R.color.ube, R.color.status_finished)
        binding.swipeRefresh.setOnRefreshListener {
            ordenRepository.fetchOrdersFromCloud {
                runOnUiThread {
                    loadOrders()
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
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

    private fun setupTabs() {
        binding.tabs.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterOrders(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        },)
    }

    private fun filterOrders(position: Int) {
        val filtered = when (position) {
            0 -> allOrders.filter { it.estado == "SIN ASIGNAR" || it.estado == "PENDIENTE" || it.estado == "EN DIAGNÓSTICO" || it.estado == "PENDIENTE APROBACIÓN" || it.estado == "APROBADA" }
            1 -> allOrders.filter { it.estado == "EN PROCESO" }
            2 -> allOrders.filter { it.estado == "FINALIZADA" || it.estado == "CANCELADA" }
            else -> allOrders
        }
        updateUI(filtered)
    }

    private fun setupBottomNavigation() {
        setupCustomNavigation(binding.customNav.root, R.id.menu_orders) { menuId ->
            when (menuId) {
                R.id.menu_home -> {
                    navigateToHome()
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
            val userRol = sessionManager.getUserRol()?.uppercase() ?: ""
            val intent = if (userRol == "CLIENTE") {
                Intent(this, ClientOrderDetailActivity::class.java)
            } else {
                Intent(this, OrderDetailActivity::class.java)
            }
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }

    private fun loadOrders() {
        val userId = sessionManager.getUserId()
        val userRol = sessionManager.getUserRol()?.uppercase() ?: ""

        allOrders = if (userRol == "ADMINISTRADOR") {
            ordenRepository.getAllInfo()
        } else {
            ordenRepository.getAllInfoByTecnico(userId)
        }
        
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
