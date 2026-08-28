package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.OrdersAdapter
import com.example.climatrack.databinding.ActivityClientDashboardBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.SessionManager

class ClientDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityClientDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var adapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        sessionManager = SessionManager(this)
        ordenRepository = OrdenRepository(this)

        setupRecyclerView()

        binding.btnRequestService.setOnClickListener {
            requestService()
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadMyServices()
    }

    private fun setupRecyclerView() {
        adapter = OrdersAdapter(emptyList()) { order ->
            if (order.estado == "PENDIENTE APROBACIÓN") {
                val intent = Intent(this, ApprovalActivity::class.java)
                intent.putExtra("ORDER_ID", order.id)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Estado: ${order.estado}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvClientOrders.layoutManager = LinearLayoutManager(this)
        binding.rvClientOrders.adapter = adapter
    }

    private fun requestService() {
        startActivity(Intent(this, OrderRequestActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        loadMyServices()
    }

    private fun loadMyServices() {
        val clienteId = sessionManager.getUserId()
        val orders = ordenRepository.getOrdenesByCliente(clienteId)
        adapter.updateList(orders)
        
        if (orders.isEmpty()) {
            Toast.makeText(this, "No tienes servicios registrados", Toast.LENGTH_SHORT).show()
        }
    }
}
