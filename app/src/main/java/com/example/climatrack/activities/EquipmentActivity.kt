package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.R
import com.example.climatrack.adapters.EquipmentAdapter
import com.example.climatrack.databinding.ActivityEquipmentBinding
import com.example.climatrack.repositories.EquipoRepository
import com.example.climatrack.utils.SessionManager
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class EquipmentActivity : BaseActivity() {

    private lateinit var binding: ActivityEquipmentBinding
    private lateinit var equipoRepository: EquipoRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: EquipmentAdapter
    private var filterClientId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        equipoRepository = EquipoRepository(this)
        sessionManager = com.example.climatrack.utils.SessionManager(this)
        filterClientId = intent.getIntExtra("CLIENT_ID", -1)

        setupEdgeToEdge(binding.root, binding.toolbar, binding.navContainer)
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        
        val userRol = sessionManager.getUserRol()?.uppercase() ?: ""
        if (filterClientId != -1 || userRol == "ADMINISTRADOR") {
            binding.navContainer.visibility = android.view.View.GONE
            if (filterClientId != -1) binding.toolbar.title = "Mis Equipos"
        } else if (userRol == "TÉCNICO" || userRol == "TECNICO") {
            setupBottomNavigation()
        } else {
            binding.navContainer.visibility = android.view.View.GONE
        }
        
        setupFilterButton()
        setupQRScanner()

        binding.fabAddEquipment.setOnClickListener {
            val intent = Intent(this, EquipmentFormActivity::class.java)
            if (filterClientId != -1) intent.putExtra("CLIENT_ID", filterClientId)
            startActivity(intent)
        }
    }

    private fun setupQRScanner() {
        binding.ivScanQR.setOnClickListener {
            val scanner = GmsBarcodeScanning.getClient(this)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val code = barcode.rawValue ?: return@addOnSuccessListener
                    searchByQR(code)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al escanear: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun searchByQR(code: String) {
        val equipment = equipoRepository.getAll().find { it.codigo.equals(code, true) }
        if (equipment != null) {
            val intent = if (filterClientId != -1) {
                Intent(this, EquipmentDetailActivity::class.java)
            } else {
                Intent(this, EquipmentFormActivity::class.java)
            }
            intent.putExtra("EQUIPMENT_ID", equipment.id)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Equipo con código $code no encontrado", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupFilterButton() {
        binding.ivFilter.setOnClickListener { view ->
            val popup = android.widget.PopupMenu(this, view)
            popup.menu.add("Todos")
            popup.menu.add("OPERATIVO")
            popup.menu.add("EN MANTENIMIENTO")
            popup.menu.add("FUERA DE SERVICIO")

            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Todos") {
                    loadEquipment()
                } else {
                    filterByStatus(item.title.toString())
                }
                true
            }
            popup.show()
        }
    }

    private fun filterByStatus(status: String) {
        val fullList = if (filterClientId != -1) equipoRepository.getByCliente(filterClientId) else equipoRepository.getAll()
        val filtered = fullList.filter { it.estado == status }
        adapter.updateList(filtered)
    }

    private fun setupBottomNavigation() {
        setupCustomNavigation(binding.customNav.root, R.id.menu_equipment) { menuId ->
            when (menuId) {
                R.id.menu_home -> {
                    navigateToHome()
                }
                R.id.menu_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                    finish()
                }
                R.id.menu_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadEquipment()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = EquipmentAdapter(emptyList()) { equipment ->
            if (filterClientId != -1) {
                // Client view -> History
                val intent = Intent(this, EquipmentDetailActivity::class.java)
                intent.putExtra("EQUIPMENT_ID", equipment.id)
                startActivity(intent)
            } else {
                // Admin/Tech view -> Edit form
                val intent = Intent(this, EquipmentFormActivity::class.java)
                intent.putExtra("EQUIPMENT_ID", equipment.id)
                startActivity(intent)
            }
        }
        binding.rvEquipment.layoutManager = LinearLayoutManager(this)
        binding.rvEquipment.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadEquipment() {
        val equipment = if (filterClientId != -1) equipoRepository.getByCliente(filterClientId) else equipoRepository.getAll()
        adapter.updateList(equipment)
    }

    private fun filterList(query: String) {
        val fullList = if (filterClientId != -1) equipoRepository.getByCliente(filterClientId) else equipoRepository.getAll()
        val filtered = fullList.filter {
            it.codigo.contains(query, true) ||
            it.marca.contains(query, true) ||
            it.modelo.contains(query, true) ||
            it.serial?.contains(query, true) ?: false
        }
        adapter.updateList(filtered)
    }
}
