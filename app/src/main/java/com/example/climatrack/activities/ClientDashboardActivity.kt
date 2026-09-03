package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
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
    private lateinit var usuarioRepository: com.example.climatrack.repositories.UsuarioRepository
    private lateinit var equipoRepository: com.example.climatrack.repositories.EquipoRepository
    private lateinit var adapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        sessionManager = SessionManager(this)
        ordenRepository = OrdenRepository(this)
        usuarioRepository = com.example.climatrack.repositories.UsuarioRepository(this)
        equipoRepository = com.example.climatrack.repositories.EquipoRepository(this)

        setupRecyclerView()

        binding.btnRequestService.setOnClickListener {
            requestService()
        }

        binding.btnMyEquipment.setOnClickListener {
            val intent = Intent(this, EquipmentActivity::class.java)
            intent.putExtra("CLIENT_ID", sessionManager.getUserId())
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.ivClientAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        loadMyServices()
        checkMaintenanceReminders()
    }

    private fun checkMaintenanceReminders() {
        val overdueEquipments = equipoRepository.getEquiposVencidos(sessionManager.getUserId())

        if (overdueEquipments.isNotEmpty()) {
            binding.cardReminder.visibility = View.VISIBLE
            binding.tvReminderText.text = if (overdueEquipments.size == 1) 
                "Tu equipo ${overdueEquipments[0].marca} necesita mantenimiento preventivo." 
                else "Tienes ${overdueEquipments.size} equipos que requieren mantenimiento preventivo."
            
            binding.cardReminder.setOnClickListener {
                requestService()
            }
        } else {
            binding.cardReminder.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = OrdersAdapter(emptyList()) { order ->
            if (order.estado == "PENDIENTE APROBACIÓN") {
                val intent = Intent(this, ApprovalActivity::class.java)
                intent.putExtra("ORDER_ID", order.id)
                startActivity(intent)
            } else {
                val intent = Intent(this, ClientOrderDetailActivity::class.java)
                intent.putExtra("ORDER_ID", order.id)
                startActivity(intent)
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
        checkMaintenanceReminders()
        updateFcmToken()
    }

    private fun updateFcmToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            usuarioRepository.updateFCMToken(sessionManager.getUserId(), token)
        }
    }

    private fun loadMyServices() {
        val user = usuarioRepository.getById(sessionManager.getUserId())
        user?.imagenPerfil?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                binding.ivClientAvatar.setImageURI(android.net.Uri.fromFile(file))
            }
        }

        val clienteId = sessionManager.getUserId()
        val orders = ordenRepository.getOrdenesByCliente(clienteId)
        adapter.updateList(orders)
        
        if (orders.isEmpty()) {
            Toast.makeText(this, "No tienes servicios registrados", Toast.LENGTH_SHORT).show()
        }
    }
}
