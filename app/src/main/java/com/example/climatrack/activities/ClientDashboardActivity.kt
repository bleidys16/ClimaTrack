package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.OrdersAdapter
import com.example.climatrack.adapters.ReceiptsAdapter
import com.example.climatrack.databinding.ActivityClientDashboardBinding
import com.example.climatrack.models.OrdenInfo
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.PdfGenerator
import com.example.climatrack.utils.SessionManager
import com.google.android.material.tabs.TabLayout

class ClientDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityClientDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var usuarioRepository: com.example.climatrack.repositories.UsuarioRepository
    private lateinit var equipoRepository: com.example.climatrack.repositories.EquipoRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var receiptsAdapter: ReceiptsAdapter
    private var allOrders: List<OrdenInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.appBarLayout)

        sessionManager = SessionManager(this)
        ordenRepository = OrdenRepository(this)
        usuarioRepository = com.example.climatrack.repositories.UsuarioRepository(this)
        equipoRepository = com.example.climatrack.repositories.EquipoRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)

        setupRecyclerViews()
        setupTabLayout()

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

    private fun setupRecyclerViews() {
        ordersAdapter = OrdersAdapter(emptyList()) { order ->
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

        receiptsAdapter = ReceiptsAdapter(emptyList()) { order ->
            generateAndOpenReceipt(order)
        }

        binding.rvClientOrders.layoutManager = LinearLayoutManager(this)
        binding.rvClientOrders.adapter = ordersAdapter
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateUIForTab(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateUIForTab(position: Int) {
        if (position == 0) {
            binding.tvMyServices.text = "Mis Servicios Recientes"
            binding.rvClientOrders.adapter = ordersAdapter
            ordersAdapter.updateList(allOrders)
        } else {
            binding.tvMyServices.text = "Mis Comprobantes de Pago"
            binding.rvClientOrders.adapter = receiptsAdapter
            receiptsAdapter.updateList(allOrders.filter { it.estado == "FINALIZADA" })
        }
    }

    private fun generateAndOpenReceipt(order: OrdenInfo) {
        val mant = mantenimientoRepository.getByOrdenId(order.id)
        val pdfFile = PdfGenerator(this).generateClientReport(order, mant)
        if (pdfFile != null) {
            openPdf(pdfFile)
        } else {
            Toast.makeText(this, "Error al generar comprobante", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPdf(file: java.io.File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "No hay lector de PDF instalado", Toast.LENGTH_SHORT).show()
        }
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
        val clienteId = sessionManager.getUserId()
        
        // 1. First load from local DB for fast response
        allOrders = ordenRepository.getOrdenesByCliente(clienteId)
        updateUIForTab(binding.tabLayout.selectedTabPosition)

        // 2. Fetch from Cloud to get updates (like technician assignment)
        usuarioRepository.fetchTechniciansFromCloud {
            ordenRepository.fetchOrdersFromCloud {
                runOnUiThread {
                    allOrders = ordenRepository.getOrdenesByCliente(clienteId)
                    updateUIForTab(binding.tabLayout.selectedTabPosition)
                    
                    val user = usuarioRepository.getById(clienteId)
                    user?.imagenPerfil?.let { path ->
                        val file = java.io.File(path)
                        if (file.exists()) {
                            binding.ivClientAvatar.setImageURI(android.net.Uri.fromFile(file))
                        }
                    }
                }
            }
        }
    }
}
