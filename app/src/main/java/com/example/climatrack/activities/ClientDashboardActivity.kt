package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.databinding.ActivityClientDashboardBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.SessionManager

class ClientDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityClientDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var ordenRepository: OrdenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        sessionManager = SessionManager(this)
        ordenRepository = OrdenRepository(this)

        binding.btnRequestService.setOnClickListener {
            requestService()
        }

        loadMyServices()
    }

    private fun requestService() {
        startActivity(Intent(this, OrderRequestActivity::class.java))
    }

    private fun loadMyServices() {
        // Cargar servicios del cliente actual
    }
}
