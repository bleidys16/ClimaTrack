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
    private lateinit var assignedOrdersAdapter: OrdersAdapter
    private var allUnassignedOrders: List<com.example.climatrack.models.OrdenInfo> = emptyList()
    private var allAssignedOrders: List<com.example.climatrack.models.OrdenInfo> = emptyList()
    private var ordersListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupEdgeToEdge(binding.root, binding.appBarLayout)

        ordenRepository = OrdenRepository(this)
        usuarioRepository = UsuarioRepository(this)
        sessionManager = SessionManager(this)

        setupRecyclerViews()

        binding.btnAutoAssign.setOnClickListener {
            performAutoAssignment()
        }

        binding.btnViewAllOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        binding.cardActiveTechs.setOnClickListener {
            startActivity(Intent(this, TechnicianMapActivity::class.java))
        }

        binding.cardPendingOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.putExtra("TAB_INDEX", 0)
            startActivity(intent)
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

        setupRefreshLayout()
        setupSearch()
        
        loadData()
        startRealtimeSync()
    }

    private fun startRealtimeSync() {
        ordersListener?.remove()
        ordersListener = ordenRepository.listenToOrders {
            runOnUiThread {
                refreshLocalUI()
            }
        }
    }

    private fun setupRefreshLayout() {
        binding.swipeRefresh.setColorSchemeResources(com.example.climatrack.R.color.ube, com.example.climatrack.R.color.status_finished)
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
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
        val filteredUnassigned = allUnassignedOrders.filter {
            it.numero.contains(query, true) || it.clienteNombre.contains(query, true)
        }
        ordersAdapter.updateList(filteredUnassigned)

        val filteredAssigned = allAssignedOrders.filter {
            it.numero.contains(query, true) || it.clienteNombre.contains(query, true)
        }
        assignedOrdersAdapter.updateList(filteredAssigned)
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

        assignedOrdersAdapter = OrdersAdapter(emptyList()) { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }
        binding.rvAssignedOrders.layoutManager = LinearLayoutManager(this)
        binding.rvAssignedOrders.adapter = assignedOrdersAdapter
    }

    private fun showAssignDialog(orderId: String) {
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
        updateFcmToken()
    }

    private fun updateFcmToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            usuarioRepository.updateFCMToken(sessionManager.getUserId(), token)
        }
    }

    private fun loadData() {
        // 1. Refresh UI from local data immediately
        refreshLocalUI()

        // 2. Sync with Cloud and refresh again when done
        usuarioRepository.fetchTechniciansFromCloud {
            ordenRepository.fetchOrdersFromCloud {
                runOnUiThread {
                    refreshLocalUI()
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun refreshLocalUI() {
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
        allAssignedOrders = ordenRepository.getAssignedActiveOrders()
        filterUnassignedOrders(binding.etSearchOrders.text.toString())
        val totalPending = allUnassignedOrders.size + allAssignedOrders.size
        binding.tvPendingOrdersCount.text = totalPending.toString()
    }

    private fun performAutoAssignment() {
        usuarioRepository.fetchTechniciansFromCloud {
            runOnUiThread {
                val unassigned = ordenRepository.getUnassignedOrders()
                if (unassigned.isEmpty()) {
                    Toast.makeText(this, "No hay órdenes pendientes de asignación", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                var assignedCount = 0
                for (order in unassigned) {
                    val techId = ordenRepository.getTechnicianWithLeastWork()
                    if (techId.isNotEmpty()) {
                        ordenRepository.assignTechnician(order.id, techId)
                        assignedCount++
                    } else {
                        android.util.Log.e("AUTO_ASSIGN", "No se encontró técnico disponible para la orden ${order.numero}")
                    }
                }

                if (assignedCount > 0) {
                    Toast.makeText(this, "Se asignaron $assignedCount órdenes automáticamente", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No se encontraron técnicos registrados para asignar", Toast.LENGTH_LONG).show()
                }
                loadData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ordersListener?.remove()
    }
}
