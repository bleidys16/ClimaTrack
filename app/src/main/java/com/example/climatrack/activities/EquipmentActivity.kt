package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.EquipmentAdapter
import com.example.climatrack.databinding.ActivityEquipmentBinding
import com.example.climatrack.repositories.EquipoRepository

class EquipmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquipmentBinding
    private lateinit var equipoRepository: EquipoRepository
    private lateinit var adapter: EquipmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        equipoRepository = EquipoRepository(this)

        setupToolbar()
        setupRecyclerView()
        setupSearch()

        binding.fabAddEquipment.setOnClickListener {
            startActivity(Intent(this, EquipmentFormActivity::class.java))
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
        val equipment = equipoRepository.getAll()
        adapter.updateList(equipment)
    }

    private fun filterList(query: String) {
        val fullList = equipoRepository.getAll()
        val filtered = fullList.filter {
            it.codigo.contains(query, true) ||
            it.marca.contains(query, true) ||
            it.modelo.contains(query, true) ||
            it.serial?.contains(query, true) ?: false
        }
        adapter.updateList(filtered)
    }
}
