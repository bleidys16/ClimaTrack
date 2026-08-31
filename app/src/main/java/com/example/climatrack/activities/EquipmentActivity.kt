package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.R
import com.example.climatrack.adapters.EquipmentAdapter
import com.example.climatrack.databinding.ActivityEquipmentBinding
import com.example.climatrack.repositories.EquipoRepository

class EquipmentActivity : BaseActivity() {

    private lateinit var binding: ActivityEquipmentBinding
    private lateinit var equipoRepository: EquipoRepository
    private lateinit var adapter: EquipmentAdapter
    private var filterClientId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        equipoRepository = EquipoRepository(this)
        filterClientId = intent.getIntExtra("CLIENT_ID", -1)

        setupEdgeToEdge(binding.root, binding.toolbar, binding.navContainer)
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        
        if (filterClientId != -1) {
            binding.navContainer.visibility = android.view.View.GONE
            binding.toolbar.title = "Mis Equipos"
        } else {
            setupBottomNavigation()
        }
        
        setupFilterButton()

        binding.fabAddEquipment.setOnClickListener {
            val intent = Intent(this, EquipmentFormActivity::class.java)
            if (filterClientId != -1) intent.putExtra("CLIENT_ID", filterClientId)
            startActivity(intent)
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
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
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
            val intent = Intent(this, EquipmentFormActivity::class.java)
            intent.putExtra("EQUIPMENT_ID", equipment.id)
            startActivity(intent)
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
