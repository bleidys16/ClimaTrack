package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.OrdersAdapter
import com.example.climatrack.databinding.ActivityOrdersBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.SessionManager

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ordenRepository = OrdenRepository(this)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        loadOrders()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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
        val orders = ordenRepository.getAllInfoByTecnico(tecnicoId)

        if (orders.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvOrders.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvOrders.visibility = View.VISIBLE
            adapter.updateList(orders)
        }
    }
}
