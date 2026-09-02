package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.OrdersAdapter
import com.example.climatrack.adapters.TechnicianAdapter
import com.example.climatrack.databinding.ActivityAdminDashboardBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.UsuarioRepository
import com.example.climatrack.utils.SessionManager

class AdminDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var techAdapter: TechnicianAdapter
    private lateinit var ordersAdapter: OrdersAdapter
    private var allUnassignedOrders: List<com.example.climatrack.models.OrdenInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        ordenRepository = OrdenRepository(this)
        usuarioRepository = UsuarioRepository(this)
        sessionManager = SessionManager(this)

        setupRecyclerViews()

        binding.btnAutoAssign.setOnClickListener {
            performAutoAssignment()
        }

        binding.btnRegisterNewTech.setOnClickListener {
            startActivity(Intent(this, RegisterTechnicianActivity::class.java))
        }

        binding.btnViewTechMap.setOnClickListener {
            startActivity(Intent(this, TechnicianMapActivity::class.java))
        }

        binding.btnFailureIntel.setOnClickListener {
            startActivity(Intent(this, FailureIntelligenceActivity::class.java))
        }

        binding.btnManualOrder.setOnClickListener {
            startActivity(Intent(this, ManualOrderActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.ivAdminAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupSearch()
        
        loadData()
    }

    private fun setupSearch() {
        binding.etSearchOrders.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUnassignedOrders(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterUnassignedOrders(query: String) {
        val filtered = allUnassignedOrders.filter {
            it.numero.contains(query, true) || it.clienteNombre.contains(query, true)
        }
        ordersAdapter.updateList(filtered)
    }

    private fun setupRecyclerViews() {
        techAdapter = TechnicianAdapter(emptyList()) { tech ->
            val intent = Intent(this, TechnicianDetailActivity::class.java)
            intent.putExtra("TECH_ID", tech.id)
            startActivity(intent)
        }
        binding.rvTechnicians.layoutManager = LinearLayoutManager(this)
        binding.rvTechnicians.adapter = techAdapter

        ordersAdapter = OrdersAdapter(emptyList()) { order ->
            showAssignDialog(order.id)
        }
        binding.rvUnassignedOrders.layoutManager = LinearLayoutManager(this)
        binding.rvUnassignedOrders.adapter = ordersAdapter
    }

    private fun showAssignDialog(orderId: Int) {
        val technicians = usuarioRepository.getAllTecnicos()
        val techNames = technicians.map { it.nombre }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Asignar Técnico")
            .setItems(techNames) { _, which ->
                val selectedTech = technicians[which]
                ordenRepository.assignTechnician(orderId, selectedTech.id)
                Toast.makeText(this, "Orden asignada a ${selectedTech.nombre}", Toast.LENGTH_SHORT).show()
                loadData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val user = usuarioRepository.getById(sessionManager.getUserId())
        user?.imagenPerfil?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                binding.ivAdminAvatar.setImageURI(android.net.Uri.fromFile(file))
            }
        }

        val techs = usuarioRepository.getTechnicianStats()
        techAdapter.updateList(techs)
        
        val activeCount = techs.count { it.isActive == 1 }
        binding.tvActiveTechsCount.text = activeCount.toString()

        allUnassignedOrders = ordenRepository.getUnassignedOrders()
        filterUnassignedOrders(binding.etSearchOrders.text.toString())
        binding.tvPendingOrdersCount.text = allUnassignedOrders.size.toString()
    }

    private fun loadTechStats() {
        // Obsoleto, integrado en loadData
    }

    private fun loadUnassignedOrders() {
        // Obsoleto, integrado en loadData
    }

    private fun performAutoAssignment() {
        val unassigned = ordenRepository.getUnassignedOrders()
        if (unassigned.isEmpty()) {
            Toast.makeText(this, "No hay órdenes pendientes de asignación", Toast.LENGTH_SHORT).show()
            return
        }

        var assignedCount = 0
        for (order in unassigned) {
            val techId = ordenRepository.getTechnicianWithLeastWork()
            if (techId != -1) {
                ordenRepository.assignTechnician(order.id, techId)
                assignedCount++
            }
        }

        Toast.makeText(this, "Se asignaron $assignedCount órdenes automáticamente", Toast.LENGTH_LONG).show()
        loadUnassignedOrders()
    }
}
