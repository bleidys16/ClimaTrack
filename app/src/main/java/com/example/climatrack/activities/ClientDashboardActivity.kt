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
        val myEquip = equipoRepository.getByCliente(sessionManager.getUserId())
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sixMonthsAgo = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, -6) }.time

        var equipmentsNeedingService = 0
        myEquip.forEach { equip ->
            val history = ordenRepository.getOrdenesByEquipo(equip.id)
            val lastMaintenance = history.find { it.estado == "FINALIZADA" }
            
            if (lastMaintenance != null) {
                try {
                    val date = sdf.parse(lastMaintenance.fecha)
                    if (date != null && date.before(sixMonthsAgo)) {
                        equipmentsNeedingService++
                    }
                } catch (e: Exception) { e.printStackTrace() }
            } else {
                // Never maintained -> Assume it needs one
                equipmentsNeedingService++
            }
        }

        if (equipmentsNeedingService > 0) {
            binding.cardReminder.visibility = View.VISIBLE
            binding.tvReminderText.text = if (equipmentsNeedingService == 1) 
                "Tienes 1 equipo que requiere mantenimiento preventivo." 
                else "Tienes $equipmentsNeedingService equipos que requieren mantenimiento preventivo."
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
