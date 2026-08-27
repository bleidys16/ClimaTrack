package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.climatrack.databinding.ActivityAdminDashboardBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.UsuarioRepository

class AdminDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        ordenRepository = OrdenRepository(this)
        usuarioRepository = UsuarioRepository(this)

        binding.btnAutoAssign.setOnClickListener {
            performAutoAssignment()
        }

        binding.btnRegisterNewTech.setOnClickListener {
            startActivity(Intent(this, RegisterTechnicianActivity::class.java))
        }

        binding.btnViewTechMap.setOnClickListener {
            startActivity(Intent(this, TechnicianMapActivity::class.java))
        }
        
        loadUnassignedOrders()
        loadTechStats()
    }

    override fun onResume() {
        super.onResume()
        loadUnassignedOrders()
        loadTechStats()
    }

    private fun loadTechStats() {
        val activeTechs = usuarioRepository.getActiveTechnicians()
        binding.tvActiveTechsCount.text = "${activeTechs.size} Técnicos Activos"
    }

    private fun loadUnassignedOrders() {
        // Implementar carga de órdenes sin técnico
        Toast.makeText(this, "Cargando órdenes sin asignar...", Toast.LENGTH_SHORT).show()
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
