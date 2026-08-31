package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.OrdersAdapter
import com.example.climatrack.databinding.ActivityEquipmentDetailBinding
import com.example.climatrack.repositories.EquipoRepository
import com.example.climatrack.repositories.OrdenRepository

class EquipmentDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityEquipmentDetailBinding
    private lateinit var equipoRepository: EquipoRepository
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var adapter: OrdersAdapter
    private var equipmentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        equipoRepository = EquipoRepository(this)
        ordenRepository = OrdenRepository(this)
        equipmentId = intent.getIntExtra("EQUIPMENT_ID", -1)

        if (equipmentId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadEquipmentData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = OrdersAdapter(emptyList()) { order ->
            val intent = Intent(this, ClientOrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }
        binding.rvEquipmentHistory.layoutManager = LinearLayoutManager(this)
        binding.rvEquipmentHistory.adapter = adapter
    }

    private fun loadEquipmentData() {
        val equip = equipoRepository.getById(equipmentId)
        equip?.let {
            binding.tvEquipTitle.text = "${it.marca} ${it.modelo}"
            binding.tvEquipCode.text = "Código: ${it.codigo}"
            binding.tvBrand.text = "Marca: ${it.marca}"
            binding.tvModel.text = "Modelo: ${it.modelo}"
            binding.tvType.text = "Tipo: ${it.tipo}"
            binding.tvLocation.text = "Ubicación: ${it.ubicacion ?: "No definida"}"
        }

        val history = ordenRepository.getOrdenesByEquipo(equipmentId)
        if (history.isEmpty()) {
            binding.llNoHistory.visibility = View.VISIBLE
            binding.rvEquipmentHistory.visibility = View.GONE
        } else {
            binding.llNoHistory.visibility = View.GONE
            binding.rvEquipmentHistory.visibility = View.VISIBLE
            adapter.updateList(history)
        }
    }
}
